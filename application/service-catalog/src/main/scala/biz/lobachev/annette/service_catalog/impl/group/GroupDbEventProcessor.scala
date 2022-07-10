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

package biz.lobachev.annette.service_catalog.impl.group

import biz.lobachev.annette.microservice_core.event_processing.SimpleEventHandling
import biz.lobachev.annette.service_catalog.impl.group.dao.GroupDbDao
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraReadSide

import scala.concurrent.ExecutionContext

private[impl] class GroupDbEventProcessor(
  readSide: CassandraReadSide,
  dbDao: GroupDbDao
)(implicit ec: ExecutionContext)
    extends ReadSideProcessor[GroupEntity.Event]
    with SimpleEventHandling {

  def buildHandler() =
    readSide
      .builder[GroupEntity.Event]("group-cassandra")
      .setGlobalPrepare(dbDao.createTables)
      .setEventHandler[GroupEntity.GroupCreated](handle(dbDao.createGroup))
      .setEventHandler[GroupEntity.GroupUpdated](handle(dbDao.updateGroup))
      .setEventHandler[GroupEntity.GroupActivated](handle(dbDao.activateGroup))
      .setEventHandler[GroupEntity.GroupDeactivated](handle(dbDao.deactivateGroup))
      .setEventHandler[GroupEntity.GroupDeleted](handle(dbDao.deleteGroup))
      .build()

  def aggregateTags = GroupEntity.Event.Tag.allTags

}
