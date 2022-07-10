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

package biz.lobachev.annette.service_catalog.impl.service.dao

import biz.lobachev.annette.core.model.auth.AnnettePrincipal
import biz.lobachev.annette.core.model.translation.{TextCaption, TranslationCaption}
import biz.lobachev.annette.service_catalog.api.service.{Service, ServiceId}
import io.scalaland.chimney.dsl._

import java.time.OffsetDateTime

case class ServiceRecord(
  id: ServiceId,
  name: String,
  description: String,
  icon: String,
  captionText: String,
  captionTranslation: String,
  captionDescriptionText: String,
  captionDescriptionTranslation: String,
  link: String,
  active: Boolean,
  updatedAt: OffsetDateTime = OffsetDateTime.now(),
  updatedBy: AnnettePrincipal
) {
  def toService: Service = {
    val caption            =
      if (captionText.nonEmpty) TextCaption(captionText)
      else TranslationCaption(captionTranslation)
    val captionDescription =
      if (captionDescriptionText.nonEmpty) TextCaption(captionDescriptionText)
      else TranslationCaption(captionDescriptionTranslation)
    this
      .into[Service]
      .withFieldConst(_.caption, caption)
      .withFieldConst(_.captionDescription, captionDescription)
      .transform
  }
}
