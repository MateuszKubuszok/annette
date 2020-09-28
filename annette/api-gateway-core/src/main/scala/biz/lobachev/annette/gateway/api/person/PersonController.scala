/*
 * Copyright 2013 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.lobachev.annette.gateway.api.person

import biz.lobachev.annette.core.model.{DescendantUnitPrincipal, DirectUnitPrincipal, PersonId, UnitChiefPrincipal}
import biz.lobachev.annette.gateway.api.person.Permissions._
import biz.lobachev.annette.gateway.core.authentication.{AuthenticatedAction, AuthenticatedRequest}
import biz.lobachev.annette.gateway.core.authorization.Authorizer
import biz.lobachev.annette.org_structure.api.OrgStructureService
import biz.lobachev.annette.persons.api.PersonService
import biz.lobachev.annette.persons.api.person.{
  CreatePersonPayload,
  DeletePersonPayload,
  PersonFindQuery,
  UpdatePersonPayload
}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PersonController @Inject() (
  authenticated: AuthenticatedAction,
  authorizer: Authorizer,
  personService: PersonService,
  orgStructureService: OrgStructureService,
  cc: ControllerComponents,
  implicit val ec: ExecutionContext
) extends AbstractController(cc) {

  // private val log = LoggerFactory.getLogger(this.getClass)

  def createPerson =
    authenticated.async(parse.json[CreatePersonPayload]) { implicit request =>
      val payload = request.body
      authorizer.performCheck(canMaintainPerson(payload.id)) {
        for {
          _      <- personService.createPerson(payload)
          person <- personService.getPersonById(payload.id, false)
        } yield Ok(Json.toJson(person))
      }
    }

  def updatePerson =
    authenticated.async(parse.json[UpdatePersonPayload]) { implicit request =>
      val payload = request.body
      authorizer.performCheck(canMaintainPerson(payload.id)) {
        for {
          _      <- personService.updatePerson(payload)
          person <- personService.getPersonById(payload.id, false)
        } yield Ok(Json.toJson(person))
      }
    }

  def deletePerson =
    authenticated.async(parse.json[DeletePersonPayload]) { implicit request =>
      val payload = request.body
      authorizer.performCheck(canMaintainPerson(payload.id)) {
        for {
          _ <- personService.deletePerson(payload)
        } yield Ok("")
      }
    }

  def getPersonById(id: PersonId, fromReadSide: Boolean) =
    authenticated.async { implicit request =>
      authorizer.performCheck(canViewOrMaintainPerson(id)) {
        for {
          person <- personService.getPersonById(id, fromReadSide)
        } yield Ok(Json.toJson(person))
      }
    }

  def getPersonsById(fromReadSide: Boolean) =
    authenticated.async(parse.json[Set[PersonId]]) { implicit request =>
      authorizer.performCheckAny(VIEW_ALL_PERSON, MAINTAIN_ALL_PERSON) {
        for {
          persons <- personService.getPersonsById(request.body, fromReadSide)
        } yield Ok(Json.toJson(persons))
      }
    }

  def findPersons =
    authenticated.async(parse.json[PersonFindQuery]) { implicit request =>
      authorizer.performCheckAny(VIEW_ALL_PERSON, MAINTAIN_ALL_PERSON) {
        for {
          result <- personService.findPersons(request.body)
        } yield Ok(Json.toJson(result))
      }

    }

  private def canMaintainPerson[A](personId: PersonId)(implicit request: AuthenticatedRequest[A]): Future[Boolean] =
    for {
      allowAll <- authorizer.checkAny(MAINTAIN_ALL_PERSON)
      result   <- if (!allowAll) canMaintainPersonForOrgUnits(personId)
                  else Future.successful(true)
    } yield result

  private def canMaintainPersonForOrgUnits[A](
    personId: PersonId
  )(implicit request: AuthenticatedRequest[A]): Future[Boolean] = {
    val personOrgUnitsFuture = for {
      personPrincipals <- orgStructureService.getPersonPrincipals(personId)
      personOrgUnits    = personPrincipals.map {
                            case DirectUnitPrincipal(orgUnitId)     => Some(orgUnitId)
                            case DescendantUnitPrincipal(orgUnitId) => Some(orgUnitId)
                            case _                                  => None
                          }.flatten
    } yield personOrgUnits

    for {
      maintainSubordinates       <- authorizer.checkAny(MAINTAIN_SUBORDINATE_PERSON)
      chiefOrgUnitIds             = if (maintainSubordinates)
                                      request.subject.principals
                                        .filter(_.principalType == UnitChiefPrincipal.PRINCIPAL_TYPE)
                                        .map(_.principalId)
                                        .toSet
                                    else Set.empty[String]
      maintainOrgUnitPermissions <- authorizer.findPermissions(MAINTAIN_ORG_UNIT_PERSON_PERMISSION_ID)
      maintainOrgUnitIds          = maintainOrgUnitPermissions.map(_.permission.arg1).toSet
      personOrgUnitIds           <- personOrgUnitsFuture

    } yield (chiefOrgUnitIds & maintainOrgUnitIds & personOrgUnitIds).size > 0
  }

  private def canViewOrMaintainPerson[A](
    personId: PersonId
  )(implicit request: AuthenticatedRequest[A]): Future[Boolean] =
    for {
      allowAll <- authorizer.checkAny(VIEW_ALL_PERSON, MAINTAIN_ALL_PERSON)
      result   <- if (!allowAll) canMaintainPersonForOrgUnits(personId)
                  else Future.successful(true)
    } yield result

}
