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

package biz.lobachev.annette.ignition.core.org_structure

import biz.lobachev.annette.org_structure.api.role.OrgRoleId

protected case class OrgStructureIgnitionData(
  orgRoles: Seq[OrgRoleIgnitionData] = Seq.empty,
  categories: Seq[CategoryIgnitionData] = Seq.empty,
  disposedCategory: String,
  removeDisposed: Boolean = false,
  orgStructure: Seq[String] = Seq.empty
)

protected case class CategoryIgnitionData(
  id: OrgRoleId,
  name: String,
  forOrganization: Boolean = false,
  forUnit: Boolean = false,
  forPosition: Boolean = false
)

protected case class OrgRoleIgnitionData(
  id: OrgRoleId,
  name: String,
  description: String = ""
)
