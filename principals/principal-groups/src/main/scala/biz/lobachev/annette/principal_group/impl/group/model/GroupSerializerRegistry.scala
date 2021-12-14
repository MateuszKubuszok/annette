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

package biz.lobachev.annette.principal_group.impl.group.model
import biz.lobachev.annette.principal_group.impl.group.PrincipalGroupEntity.{
  PrincipalAssigned,
  PrincipalGroupCategoryUpdated,
  PrincipalGroupCreated,
  PrincipalGroupDeleted,
  PrincipalGroupDescriptionUpdated,
  PrincipalGroupNameUpdated,
  PrincipalUnassigned
}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object GroupSerializerRegistry extends JsonSerializerRegistry {
  override def serializers =
    List(
      // state
      JsonSerializer[PrincipalGroupState],
      // events
      JsonSerializer[PrincipalGroupCreated],
      JsonSerializer[PrincipalGroupNameUpdated],
      JsonSerializer[PrincipalGroupDescriptionUpdated],
      JsonSerializer[PrincipalGroupCategoryUpdated],
      JsonSerializer[PrincipalAssigned],
      JsonSerializer[PrincipalUnassigned],
      JsonSerializer[PrincipalGroupDeleted]
    )
}