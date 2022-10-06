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

package biz.lobachev.annette.persons.impl.category.dao.pg

import biz.lobachev.annette.persons.impl.PgEventHandler
import biz.lobachev.annette.persons.impl.category.{dao, CategoryEntity}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide

class CategoryPgIndexEventProcessor(
  readSide: SlickReadSide,
  indexDao: dao.CategoryIndexDao,
  readSideId: String
) extends ReadSideProcessor[CategoryEntity.Event]
    with PgEventHandler {

  def buildHandler() =
    readSide
      .builder[CategoryEntity.Event](readSideId)
      .setGlobalPrepare(globalPrepare(indexDao.createEntityIndex))
      .setEventHandler[CategoryEntity.CategoryCreated](handle(indexDao.createCategory))
      .setEventHandler[CategoryEntity.CategoryUpdated](handle(indexDao.updateCategory))
      .setEventHandler[CategoryEntity.CategoryDeleted](handle(indexDao.deleteCategory))
      .build()

  def aggregateTags = CategoryEntity.Event.Tag.allTags
}
