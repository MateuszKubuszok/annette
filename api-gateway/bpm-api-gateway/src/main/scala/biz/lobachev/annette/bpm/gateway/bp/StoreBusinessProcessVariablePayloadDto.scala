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

package biz.lobachev.annette.bpm.gateway.bp

import biz.lobachev.annette.bpm_repository.api.domain.{BusinessProcessId, Datatype, VariableName}
import play.api.libs.json.Json

case class StoreBusinessProcessVariablePayloadDto(
  businessProcessId: BusinessProcessId,
  variableName: VariableName,
  oldVariableName: Option[VariableName] = None,
  name: String,
  caption: String,
  datatype: Datatype.Datatype,
  defaultValue: String
)

object StoreBusinessProcessVariablePayloadDto {
  implicit val format = Json.format[StoreBusinessProcessVariablePayloadDto]
}
