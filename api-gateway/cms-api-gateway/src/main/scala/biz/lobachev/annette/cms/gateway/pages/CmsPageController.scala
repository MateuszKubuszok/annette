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

package biz.lobachev.annette.cms.gateway.pages

import akka.stream.Materializer
import biz.lobachev.annette.api_gateway_core.authentication.{AuthenticatedAction, CookieAuthenticatedAction}
import biz.lobachev.annette.api_gateway_core.authorization.Authorizer
import biz.lobachev.annette.cms.api.pages.page._
import biz.lobachev.annette.cms.api.files.{FileTypes, RemoveFilePayload, StoreFilePayload}
import biz.lobachev.annette.cms.api.{CmsService, CmsStorage}
import biz.lobachev.annette.cms.gateway.Permissions
import biz.lobachev.annette.cms.gateway.pages.page._
import biz.lobachev.annette.core.model.indexing.SortBy
import io.scalaland.chimney.dsl._
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, ControllerComponents}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CmsPageController @Inject() (
  authenticated: AuthenticatedAction,
  cookieAuthenticated: CookieAuthenticatedAction,
  authorizer: Authorizer,
  cc: ControllerComponents,
  cmsService: CmsService,
  cmsStorage: CmsStorage,
  implicit val ec: ExecutionContext,
  implicit val materializer: Materializer
) extends AbstractController(cc) {

  val spaceSubscriptionType = "space"

  // ****************************** Page ******************************

  def createPage =
    authenticated.async(parse.json[CreatePagePayloadDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.body
          .into[CreatePagePayload]
          .withFieldConst(_.createdBy, request.subject.principals.head)
          .transform
        for {
          page <- cmsService.createPage(payload)
        } yield Ok(Json.toJson(page))
      }
    }

  def updatePageTitle =
    authenticated.async(parse.json[UpdatePageTitlePayloadDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.body
          .into[UpdatePageTitlePayload]
          .withFieldConst(_.updatedBy, request.subject.principals.head)
          .transform
        for {
          updated <- cmsService.updatePageTitle(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def updatePageAuthor =
    authenticated.async(parse.json[UpdatePageAuthorPayloadDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.body
          .into[UpdatePageAuthorPayload]
          .withFieldConst(_.updatedBy, request.subject.principals.head)
          .transform
        for {
          updated <- cmsService.updatePageAuthor(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def updatePageWidgetContent =
    authenticated.async(parse.json[UpdatePageWidgetContentPayloadDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.body
          .into[UpdatePageWidgetContentPayload]
          .withFieldConst(_.updatedBy, request.subject.principals.head)
          .transform
        for {
          updated <- cmsService.updatePageWidgetContent(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def changePageWidgetContentOrder =
    authenticated.async(parse.json[ChangePageWidgetContentOrderPayloadDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.body
          .into[ChangePageWidgetContentOrderPayload]
          .withFieldConst(_.updatedBy, request.subject.principals.head)
          .transform
        for {
          updated <- cmsService.changePageWidgetContentOrder(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def deletePageWidgetContent =
    authenticated.async(parse.json[DeletePageWidgetContentPayloadDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.body
          .into[DeletePageWidgetContentPayload]
          .withFieldConst(_.updatedBy, request.subject.principals.head)
          .transform
        for {
          updated <- cmsService.deletePageWidgetContent(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def updatePagePublicationTimestamp =
    authenticated.async(parse.json[UpdatePagePublicationTimestampPayloadDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.body
          .into[UpdatePagePublicationTimestampPayload]
          .withFieldConst(_.updatedBy, request.subject.principals.head)
          .transform
        for {
          updated <- cmsService.updatePagePublicationTimestamp(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def publishPage(id: String) =
    authenticated.async { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = PublishPagePayload(id, request.subject.principals.head)
        for {
          updated <- cmsService.publishPage(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def unpublishPage(id: String) =
    authenticated.async { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = UnpublishPagePayload(id, request.subject.principals.head)
        for {
          updated <- cmsService.unpublishPage(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def assignPageTargetPrincipal =
    authenticated.async(parse.json[AssignPageTargetPrincipalPayloadDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.body
          .into[AssignPageTargetPrincipalPayload]
          .withFieldConst(_.updatedBy, request.subject.principals.head)
          .transform
        for {
          updated <- cmsService.assignPageTargetPrincipal(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def unassignPageTargetPrincipal =
    authenticated.async(parse.json[UnassignPageTargetPrincipalPayloadDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.body
          .into[UnassignPageTargetPrincipalPayload]
          .withFieldConst(_.updatedBy, request.subject.principals.head)
          .transform
        for {
          updated <- cmsService.unassignPageTargetPrincipal(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def deletePage =
    authenticated.async(parse.json[DeletePagePayloadDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.body
          .into[DeletePagePayload]
          .withFieldConst(_.deletedBy, request.subject.principals.head)
          .transform
        for {
          updated <- cmsService.deletePage(payload)
        } yield Ok(Json.toJson(updated))
      }
    }

  def findPages: Action[PageFindQueryDto] =
    authenticated.async(parse.json[PageFindQueryDto]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val payload = request.request.body
        for {
          result <- {
            val sortBy =
              if (payload.filter.map(_.isEmpty).getOrElse(true) && payload.sortBy.isEmpty)
                Some(
                  Seq(
                    SortBy("publicationTimestamp", Some(false))
                  )
                )
              else payload.sortBy
            val query  = payload
              .into[PageFindQuery]
              .withFieldConst(_.sortBy, sortBy)
              .transform
            cmsService.findPages(query)
          }

        } yield Ok(Json.toJson(result))
      }
    }

  def getPagesById(
    fromReadSide: Boolean,
    withContent: Option[Boolean] = None,
    withTargets: Option[Boolean] = None
  ) =
    authenticated.async(parse.json[Set[PageId]]) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val ids = request.request.body
        for {
          result <- cmsService.getPagesById(ids, fromReadSide, withContent, withTargets)
        } yield Ok(Json.toJson(result))
      }
    }

  def getPageById(
    id: PageId,
    fromReadSide: Boolean,
    withContent: Option[Boolean] = None,
    withTargets: Option[Boolean] = None
  ) =
    authenticated.async { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        for {
          result <- cmsService.getPageById(id, fromReadSide, withContent, withTargets)
        } yield Ok(Json.toJson(result))
      }
    }

  def uploadPageFile(pageId: String, fileType: String) =
    cookieAuthenticated.async(parse.multipartFormData) { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        val file    = request.body.files.head
        val fileId  = UUID.randomUUID().toString
        val payload = StoreFilePayload(
          objectId = s"page-$pageId",
          fileType = FileTypes.withName(fileType),
          fileId = fileId,
          filename = file.filename,
          contentType = fileMimeTypes.forFileName(file.filename).getOrElse(play.api.http.ContentTypes.BINARY),
          updatedBy = request.subject.principals.head
        )
        for {
          _ <- cmsService.storeFile(payload)
          _ <- cmsStorage.uploadFile(file.ref.path, payload)
        } yield Ok(Json.toJson(payload))
      }
    }

  def removePageFile(pageId: String, fileType: String, fileId: String) =
    authenticated.async { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        for {
          _ <- cmsService.removeFile(
                 RemoveFilePayload(
                   objectId = s"page-$pageId",
                   fileType = FileTypes.withName(fileType),
                   fileId = fileId,
                   updatedBy = request.subject.principals.head
                 )
               )
        } yield Ok("")
      }
    }

  def getPageFiles(pageId: String) =
    authenticated.async { implicit request =>
      authorizer.performCheckAny(Permissions.MAINTAIN_ALL_PAGES) {
        for {
          result <- cmsService.getFiles(s"page-$pageId")
        } yield Ok(Json.toJson(result))
      }
    }
}
