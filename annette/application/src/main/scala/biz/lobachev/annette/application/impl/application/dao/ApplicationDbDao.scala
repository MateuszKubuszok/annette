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

package biz.lobachev.annette.application.impl.application.dao

import akka.Done
import biz.lobachev.annette.application.api.application._
import biz.lobachev.annette.application.impl.application.ApplicationEntity
import com.datastax.driver.core.BoundStatement

import scala.collection.immutable.Seq
import scala.concurrent.Future

trait ApplicationDbDao {

  def createTables(): Future[Done]

  def prepareStatements(): Future[Done]

  def createApplication(event: ApplicationEntity.ApplicationCreated): Seq[BoundStatement]

  def updateApplicationName(event: ApplicationEntity.ApplicationNameUpdated): Seq[BoundStatement]

  def updateApplicationCaption(event: ApplicationEntity.ApplicationCaptionUpdated): Seq[BoundStatement]

  def updateApplicationTranslations(event: ApplicationEntity.ApplicationTranslationsUpdated): Seq[BoundStatement]

  def updateApplicationServerUrl(event: ApplicationEntity.ApplicationServerUrlUpdated): Seq[BoundStatement]

  def deleteApplication(event: ApplicationEntity.ApplicationDeleted): Seq[BoundStatement]

  def getApplicationById(id: ApplicationId): Future[Option[Application]]

  def getApplicationsById(ids: Set[ApplicationId]): Future[Seq[Application]]
}
