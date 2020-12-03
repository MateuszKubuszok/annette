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

package biz.lobachev.annette.persons.api.category

import biz.lobachev.annette.core.exception.AnnetteTransportExceptionWithIdCompanion
import com.lightbend.lagom.scaladsl.api.transport.TransportErrorCode

object PersonCategoryNotFound extends AnnetteTransportExceptionWithIdCompanion {
  val ErrorCode   = TransportErrorCode.NotFound
  val MessageCode = "annette.personService.category.notFound"
}

object PersonCategoryAlreadyExist extends AnnetteTransportExceptionWithIdCompanion {
  val ErrorCode   = TransportErrorCode.BadRequest
  val MessageCode = "annette.personService.category.alreadyExist"
}