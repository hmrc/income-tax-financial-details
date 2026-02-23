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
import models.hip.chargeHistory.{ChargeHistoryDetails, ChargeHistorySuccess, ChargeHistorySuccessWrapper}
import java.time.LocalDateTime
import models.paymentAllocations.{AllocationDetail, PaymentAllocations}
import play.api.http.Status.CREATED
import play.api.libs.json.{JsObject, Json}

import java.time.LocalDate

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


  val paymentLot = "1234567890"
  val paymentLotItem = "1098765432"

  val paymentAllocations: PaymentAllocations = PaymentAllocations(
    amount = Some(1000.00),
    method = Some("Payment by Card"),
    reference = Some("reference"),
    transactionDate = Some(LocalDate.parse("2023-12-25")),
    allocations = Seq(
      AllocationDetail(
        transactionId = Some("1"),
        from = Some(LocalDate.parse("2023-04-06")),
        to = Some(LocalDate.parse("2024-04-05")),
        chargeType = Some("ITSA-POA 1"),
        mainType = Some("SA Payment on Account 1"),
        amount = Some(500.00),
        clearedAmount = Some(500.00),
        chargeReference = Some("charge-ref-1")
      )
    )
  )
  val paymentAllocationsJson: JsObject = Json.obj(
    "paymentDetails" -> Json.arr(
      Json.obj(
        "paymentAmount" -> 1000.00,
        "paymentMethod" -> "method",
        "valueDate" -> "2022-06-23",
        "paymentReference" -> "reference",
        "sapClearingDocsDetails" -> Json.arr(
          Json.obj(
            "sapDocNumber" -> "transactionId",
            "taxPeriodStartDate" -> "2022-06-23",
            "taxPeriodEndDate" -> "2022-06-23",
            "chargeType" -> "type",
            "mainType" -> "mainType",
            "amount" -> 1500.00,
            "clearedAmount" -> 500.00,
            "chargeReference" -> "chargeReference"
          )
        )
      )
    )
  )

  val paymentAllocationsResponseBody: JsObject = Json.obj(
    "paymentDetails" -> Json.arr(
      Json.obj(
        "paymentAmount" -> 1000.00,
        "paymentMethod" -> "Payment by Card",
        "paymentReference" -> "reference",
        "valueDate" -> "2023-12-25",
        "sapClearingDocsDetails" -> Json.arr(
          Json.obj(
            "sapDocNumber" -> "1",
            "taxPeriodStartDate" -> "2023-04-06",
            "taxPeriodEndDate" -> "2024-04-05",
            "chargeType" -> "ITSA-POA 1",
            "mainType" -> "SA Payment on Account 1",
            "amount" -> 500.00,
            "clearedAmount" -> 500.00,
            "chargeReference" -> "charge-ref-1"
          )
        )
      )
    )
  )
}
