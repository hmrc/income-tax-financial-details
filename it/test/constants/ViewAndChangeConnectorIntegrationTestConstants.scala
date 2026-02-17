/*
 * Copyright 2017 HM Revenue & Customs
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

package constants

import models.claimToAdjustPoa.ClaimToAdjustPoaApiResponse.SuccessResponse
import models.claimToAdjustPoa.{ClaimToAdjustPoaRequest, MainIncomeLower}
import models.claimToAdjustPoa.ClaimToAdjustPoaResponse.ClaimToAdjustPoaResponse
import play.api.http.Status.CREATED
import play.api.libs.json.{JsObject, Json}

object ViewAndChangeConnectorIntegrationTestConstants {

  val request = ClaimToAdjustPoaRequest(
    nino = "AA1111111A",
    taxYear = "2025",
    amount = 12.00,
    poaAdjustmentReason = MainIncomeLower
  )

  val responseBody: JsObject = Json.obj(
    "successResponse" -> Json.obj(
      "processingDate" -> "2024-01-31T09:27:17Z"
    )
  )

  val invalidResponseBody: JsObject = Json.obj(
    "unexpectedField" -> "error"
  )

  val validResponseBody =
    ClaimToAdjustPoaResponse(
      CREATED,
      Right(SuccessResponse("2024-01-31T09:27:17Z"))
    )

}
