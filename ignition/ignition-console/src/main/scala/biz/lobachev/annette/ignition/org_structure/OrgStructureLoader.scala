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

package biz.lobachev.annette.ignition.org_structure

import biz.lobachev.annette.core.model.auth.AnnettePrincipal
import biz.lobachev.annette.ignition.core.{EntityLoader, IgnitionLagomClient, ServiceLoader}
import biz.lobachev.annette.ignition.org_structure.loaders.{
  CategoryEntityLoader,
  HierarchyEntityLoader,
  OrgRoleEntityLoader
}
import biz.lobachev.annette.org_structure.api.{OrgStructureServiceApi, OrgStructureServiceImpl}
import com.softwaremill.macwire.wire
import com.typesafe.config.Config

class OrgStructureLoader(val client: IgnitionLagomClient, val config: Config, val principal: AnnettePrincipal)
    extends ServiceLoader {

  lazy val serviceApi = client.serviceClient.implement[OrgStructureServiceApi]
  lazy val service    = wire[OrgStructureServiceImpl]

  override def createEntityLoader(entity: String, entityConfig: Config, principal: AnnettePrincipal): EntityLoader[_] =
    entity match {
      case "category"  => new CategoryEntityLoader(service, entityConfig, principal)
      case "org-role"  => new OrgRoleEntityLoader(service, entityConfig, principal)
      case "hierarchy" => new HierarchyEntityLoader(service, entityConfig, principal)
    }
}
