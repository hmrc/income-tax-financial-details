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
import models.claimToAdjustPoa.ClaimToAdjustPoaResponse.ClaimToAdjustPoaResponse
import models.claimToAdjustPoa.{ClaimToAdjustPoaRequest, MainIncomeLower}
import models.hip.chargeHistory.{ChargeHistoryDetails, ChargeHistorySuccess, ChargeHistorySuccessWrapper}
import play.api.http.Status.CREATED
import play.api.libs.json.{JsNull, JsObject, Json}

import java.time.LocalDateTime

object ViewAndChangeConnectorIntegrationTestConstants {

  val getChargeHistoryResponseBody: JsObject =
    Json.obj(
      "success" -> Json.obj(
        "processingDate" -> "2024-01-31T09:27:17Z",
        "chargeHistoryDetails" -> Json.obj(
          "idType" -> "NINO",
          "idNumber" -> "AB123456C",
          "regimeType" -> "ITSA",
          "chargeHistoryDetails" -> Json.arr(
            Json.obj(
              "taxYear" -> "2023",
              "documentId" -> "DOC123",
              "documentDate" -> "2023-12-01",
              "documentDescription" -> "Balancing Charge",
              "totalAmount" -> 1234.56,
              "reversalDate" -> "2024-01-31T09:27:17Z",
              "reversalReason" -> "Customer Request",
              "poaAdjustmentReason" -> JsNull
            )
          )
        )
      )
    )

  val getChargeHistoryExpectedModel =
    ChargeHistorySuccessWrapper(
      ChargeHistorySuccess(
        processingDate = LocalDateTime.parse("2024-01-31T09:27:17"),
        chargeHistoryDetails =
          ChargeHistoryDetails(
            idType = "NINO",
            idValue = "AB123456C",
            regimeType = "ITSA",
            chargeHistoryDetails = None
          )
      )
    )

  val postClaimToAdjustPoaRequest = ClaimToAdjustPoaRequest(
    nino = "AA1111111A",
    taxYear = "2025",
    amount = 12.00,
    poaAdjustmentReason = MainIncomeLower
  )

  val postClaimToAdjustPoaResponseBody: JsObject = Json.obj(
    "successResponse" -> Json.obj(
      "processingDate" -> "2024-01-31T09:27:17Z"
    )
  )

  val postClaimToAdjustPoaInvalidResponseBody: JsObject = Json.obj(
    "unexpectedField" -> "error"
  )

  val postClaimToAdjustPoaValidResponseBody =
    ClaimToAdjustPoaResponse(
      CREATED,
      Right(SuccessResponse("2024-01-31T09:27:17Z"))
    )

  val chargeHistoryNotFoundResponseBody: JsObject = Json.obj(
    "message" -> "Charge history not found"
  )

  val chargeHistoryErrorResponseBody: JsObject = Json.obj(
    "errors" -> Json.obj(
      "code" -> "005",
      "message" -> "Some not found error"
    )
  )

  val chargeHistoryValidationError = Json.obj(
    "errors" -> Json.obj(
      "code" -> "999",
      "message" -> "Other validation error"
    )
  )
  
}
