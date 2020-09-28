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

package biz.lobachev.annette.application.api.application

import biz.lobachev.annette.application.api.translation.{Caption, TranslationId}
import biz.lobachev.annette.core.model.AnnettePrincipal
import play.api.libs.json.{Format, Json}

case class UpdateApplicationPayload(
  id: ApplicationId,
  name: String,
  caption: Caption,
  translations: Set[TranslationId] = Set.empty,
  serverUrl: String,
  updatedBy: AnnettePrincipal
)

object UpdateApplicationPayload {
  implicit val format: Format[UpdateApplicationPayload] = Json.format
}