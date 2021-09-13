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

package biz.lobachev.annette.cms.gateway.post

import biz.lobachev.annette.cms.api.post.PostId
import play.api.libs.json.{Format, Json}

case class DeletePostPayloadDto(
  id: PostId
)

object DeletePostPayloadDto {
  implicit val format: Format[DeletePostPayloadDto] = Json.format
}
