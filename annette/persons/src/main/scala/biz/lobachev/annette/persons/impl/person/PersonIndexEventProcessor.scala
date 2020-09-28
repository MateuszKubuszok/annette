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

package biz.lobachev.annette.persons.impl.person

import akka.Done
import biz.lobachev.annette.persons.impl.person.PersonEntity._
import biz.lobachev.annette.persons.impl.person.dao.{PersonIndexDao}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraReadSide

import scala.concurrent.{ExecutionContext, Future}

private[impl] class PersonIndexEventProcessor(
  readSide: CassandraReadSide,
  indexDao: PersonIndexDao
)(implicit
  ec: ExecutionContext
) extends ReadSideProcessor[PersonEntity.Event] {

  def buildHandler() =
    readSide
      .builder[PersonEntity.Event]("Person_Person_ElasticEventOffset")
      .setGlobalPrepare(globalPrepare)
      .setEventHandler[PersonEntity.PersonCreated](e => createPerson(e.event))
      .setEventHandler[PersonEntity.PersonUpdated](e => updatePerson(e.event))
      .setEventHandler[PersonEntity.PersonDeleted](e => deletePerson(e.event))
      .build()

  def aggregateTags = PersonEntity.Event.Tag.allTags

  def globalPrepare(): Future[Done] =
    indexDao.createEntityIndex()

  private def createPerson(event: PersonCreated) =
    for {
      _ <- indexDao.createPerson(event)
    } yield Nil

  private def updatePerson(event: PersonUpdated) =
    for {
      _ <- indexDao.updatePerson(event)
    } yield Nil

  private def deletePerson(event: PersonDeleted) =
    for {
      _ <- indexDao.deletePerson(event)
    } yield Nil

}
