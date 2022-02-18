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

package biz.lobachev.annette.bpm_repository.api.bp

import biz.lobachev.annette.bpm_repository.api.domain.{BpmModelId, BusinessProcessId, DataSchemaId, ProcessDefinitionId}
import biz.lobachev.annette.core.model.auth.AnnettePrincipal
import play.api.libs.json.Json

case class CreateBusinessProcessPayload(
  id: BusinessProcessId,
  name: String,
  description: String,
  processDefinitionId: ProcessDefinitionId,
  bpmModelId: Option[BpmModelId] = None,
  dataSchemaId: Option[DataSchemaId] = None,
  variables: Map[String, BusinessProcessVariable],
  updatedBy: AnnettePrincipal
)

object CreateBusinessProcessPayload {
  implicit val format = Json.format[CreateBusinessProcessPayload]
}
