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

package biz.lobachev.annette.subscription.impl.subscription_type.model

import biz.lobachev.annette.core.model.auth.AnnettePrincipal

import java.time.OffsetDateTime
import biz.lobachev.annette.subscription.api.subscription_type.{SubscriptionType, SubscriptionTypeId}
import io.scalaland.chimney.dsl._
import play.api.libs.json.Json

case class SubscriptionTypeState(
  id: SubscriptionTypeId,
  name: String,
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedBy: AnnettePrincipal
) {

  def toSubscriptionType: SubscriptionType =
    this.into[SubscriptionType].transform

}

object SubscriptionTypeState {
  implicit val format = Json.format[SubscriptionTypeState]
}
