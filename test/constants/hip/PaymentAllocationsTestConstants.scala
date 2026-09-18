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

package constants.hip

import models.hip.paymentAllocations.*
import play.api.http.Status.*
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate

object PaymentAllocationsTestConstants {

  val paymentAllocationsResponseModelFull: PaymentAllocationsResponseModel = {
    PaymentAllocationsResponseModel(
          paymentDetails = Seq(PaymentAllocations(
            amount = Some(1220.34),
            method = Some("A"),
            reference = Some("1594"),
            transactionDate = Some(LocalDate.of(2026, 5, 12)),
            allocations = Seq(AllocationDetail(
              transactionId = Some("1234567890"),
              from = Some(LocalDate.of(2026, 1, 12)),
              to = Some(LocalDate.of(2026, 5, 30)),
              chargeType = Some("ITSA POA"),
              mainType = Some("SA Payment on Account 1"),
              amount = Some(1220.34),
              clearedAmount = Some(1220.34),
              chargeReference = Some("1594")
            ))
          ))
        )
  }

  val paymentAllocationsResponseModelFullJson: JsValue = Json.parse(
    """
      |{
      |    "paymentDetails" : [ {
      |      "amount" : 1220.34,
      |      "method" : "A",
      |      "reference" : "1594",
      |      "transactionDate" : "2026-05-12",
      |      "allocations" : [ {
      |        "transactionId" : "1234567890",
      |        "from" : "2026-01-12",
      |        "to" : "2026-05-30",
      |        "chargeType" : "ITSA POA",
      |        "mainType" : "SA Payment on Account 1",
      |        "amount" : 1220.34,
      |        "clearedAmount" : 1220.34,
      |        "chargeReference" : "1594"
      |      } ]
      |    } ]
      |}""".stripMargin
  )

  val paymentAllocationsResponseFromApi: JsObject = Json.obj(
    "success" -> Json.obj(
      "paymentDetails" -> Json.arr(
        Json.obj(
          "paymentAmount" -> 1220.34,
          "paymentMethod" -> "A",
          "paymentReference" -> "1594",
          "valueDate" -> "2026-05-12",
          "sapClearingDocsDetails" -> Json.arr(
            Json.obj(
              "sapDocNumber" -> "1234567890",
              "taxPeriodStartDate" -> "2026-01-12",
              "taxPeriodEndDate" -> "2026-05-30",
              "chargeType" -> "ITSA POA",
              "mainType" -> "SA Payment on Account 1",
              "amount" -> 1220.34,
              "clearedAmount" -> 1220.34,
              "chargeReference" -> "1594"
            )
          )
        )
      )
    )
  )

  val allocationDetail: AllocationDetail = AllocationDetail(
    transactionId = Some("1234567890"),
    from = Some(LocalDate.of(2026, 1, 12)),
    to = Some(LocalDate.of(2026, 5, 30)),
    chargeType = Some("ITSA POA"),
    mainType = Some("SA Payment on Account 1"),
    amount = Some(1220.34),
    clearedAmount = Some(1220.34),
    chargeReference = Some("1594")
  )

  val paymentAllocationSingle = PaymentAllocations(
    amount = Some(1220.34),
    method = Some("A"),
    reference = Some("1594"),
    transactionDate = Some(LocalDate.of(2026, 5, 12)),
    allocations = Seq(allocationDetail)
  )

  val successResponseFromApi = HttpResponse(OK, paymentAllocationsResponseFromApi.toString, Map.empty)
  val badSuccessFromApi = HttpResponse(OK, Json.obj("badSuccess" -> Json.obj()).toString, Map.empty)
  val notFoundFromApi = HttpResponse(NOT_FOUND, "{}", Map.empty)
  val unprocessableEntityFromApi = HttpResponse(UNPROCESSABLE_ENTITY, "{}", Map.empty)
  val serverErrorFromApi = HttpResponse(INTERNAL_SERVER_ERROR, "{}", Map.empty)
}
