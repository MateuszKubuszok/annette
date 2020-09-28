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

package biz.lobachev.annette.attributes.api.attribute_def

import biz.lobachev.annette.core.elastic.SortBy
import play.api.libs.json.Json

case class FindAttributeDefQuery(
  offset: Int = 0,
  size: Int,
  filter: Option[String] = None,
  attributeTypes: Option[Set[AttributeType.AttributeType]] = None,
  subType: Option[String] = None,
  sortBy: Option[SortBy] = None
)

object FindAttributeDefQuery {
  implicit val format = Json.format[FindAttributeDefQuery]
}
