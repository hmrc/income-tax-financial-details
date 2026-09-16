/*
 * Copyright 2026 HM Revenue & Customs
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

package models.hip.paymentAllocations

import play.api.libs.json.*

sealed trait PaymentAllocationsResponseError

case class PaymentAllocationsError(code: String, text: String) extends PaymentAllocationsResponseError

case class PaymentAllocationsNotFound(code: String, text: String) extends PaymentAllocationsResponseError

type HipPaymentAllocationsResponse = Either[PaymentAllocationsResponseError, PaymentAllocationsResponseModel]

object PaymentAllocationsError {
  implicit val format: OFormat[PaymentAllocationsError] = Json.format[PaymentAllocationsError]
}

object PaymentAllocationsNotFound {
  implicit val format: OFormat[PaymentAllocationsNotFound] = Json.format[PaymentAllocationsNotFound]
}

case class PaymentAllocationsResponseModel(success: PaymentAllocationsSuccess)

object PaymentAllocationsResponseModel {
  implicit val format: OFormat[PaymentAllocationsResponseModel] = Json.format[PaymentAllocationsResponseModel]
}

case class PaymentAllocationsSuccess(paymentDetails: Seq[PaymentAllocations])

object PaymentAllocationsSuccess {
  implicit val format: OFormat[PaymentAllocationsSuccess] = Json.format[PaymentAllocationsSuccess]
}
