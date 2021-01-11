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

package biz.lobachev.annette.authorization.impl.role.dao

import akka.Done
import biz.lobachev.annette.authorization.api.role._
import biz.lobachev.annette.authorization.impl.role.RoleEntity
import biz.lobachev.annette.core.model.elastic.FindResult

import scala.concurrent.Future

trait RoleIndexDao {

  def createEntityIndex(): Future[Done]

  def createRole(event: RoleEntity.RoleCreated): Future[Unit]

  def updateRole(event: RoleEntity.RoleUpdated): Future[Unit]

  def deleteRole(event: RoleEntity.RoleDeleted): Future[Unit]

  def findRoles(query: AuthRoleFindQuery): Future[FindResult]

}
