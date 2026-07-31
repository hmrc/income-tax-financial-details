/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import constants.HipRepaymentHistoryDetailsIntegrationTestConstants
import helpers.ComponentSpecBase
import helpers.servicemocks.DesChargesStub.{stubRepaymentHistoryByIdHip, stubRepaymentHistoryHip}
import models.hip.repayments.*
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse

import java.time.LocalDate

class RepaymentHistoryControllerISpec extends ComponentSpecBase {

  val repaymentId = "12789971"
  val nino = "AA0000AA"
  val fromDate = "2021-07-23"
  val toDate = "2021-08-23"


  val validRepaymentHistoryJson: JsValue = Json.obj(
    "etmp_transaction_header" -> Json.obj(
      "status" -> "OK",
      "processingDate" -> "2021-09-01T12:00:00"
    ),
    "etmp_Response_Details" -> Json.obj(
      "repaymentsViewerDetails" -> Json.arr(
        Json.obj(
          "repaymentRequestNumber" -> "000000003135",
          "actor" -> "CUSTOMER",
          "channel" -> "ONLINE",
          "status" -> "A",
          "amountRequested" -> 200.0,
          "amountApprovedforRepayment" -> 100.0,
          "totalRepaymentAmount" -> 300.0,
          "repaymentMethod" -> "BACD",
          "creationDate" -> "2020-12-25",
          "estimatedRepaymentDate" -> "2021-01-21",
          "repaymentItems" -> Json.arr(
            Json.obj(
              "repaymentSupplementItem" -> Json.arr(
                Json.obj(
                  "parentCreditReference" -> "002420002231",
                  "amount" -> 400.0,
                  "fromDate" -> "2021-07-23",
                  "toDate" -> "2021-08-23",
                  "rate" -> 500.0
                )
              )
            )
          )
        )
      )
    )
  )


  s"GET ${controllers.routes.RepaymentHistoryController.getRepaymentHistoryById(nino, repaymentId)}" should {
    s"return $OK" when {
      "repayment history are successfully retrieved by Id" in {

        isAuthorised(true)

        stubRepaymentHistoryByIdHip(nino, repaymentId)(
          status = OK,
          response = validRepaymentHistoryJson)

        val res: WSResponse = IncomeTaxFinancialDetails.getRepaymentHistoryById(nino, repaymentId)

        val expectedResponseBody: JsValue = Json.toJson(SuccessfulRepaymentResponse(
          transactionHeader = TransactionHeader(
            status = "OK",
            processingDate = java.time.LocalDateTime.parse("2021-09-01T12:00:00"),
            returnParameters = None
          ),
          responseDetails = ResponseDetails(
            repaymentsViewerDetails = Seq(
              RepaymentViewerDetail(
                repaymentRequestNumber = "000000003135",
                actor = "CUSTOMER",
                channel = "ONLINE",
                status = "A",
                amountRequested = 200.0,
                amountApprovedforRepayment = Some(100.0),
                totalAmountforRepaymentSupplement = None,
                totalRepaymentAmount = Some(300.0),
                repaymentMethod = Some("BACD"),
                creationDate = Some(LocalDate.parse("2020-12-25")),
                estimatedRepaymentDate = Some(LocalDate.parse("2021-01-21")),
                repaymentItems = Some(Seq(
                  RepaymentItem(
                    //creditItems = None,
                    //paymentItems = None,
                    creditReasons = None,
                    repaymentSupplementItem = Some(Seq(
                      RepaymentSupplementItem(
                        creditReference = None,
                        parentCreditReference = Some("002420002231"),
                        amount = Some(400.0),
                        fromDate = Some(LocalDate.parse("2021-07-23")),
                        toDate = Some(LocalDate.parse("2021-08-23")),
                        rate = Some(500.0)
                      )
                    ))
                  )
                ))
              )
            )
          )
        ))


        res should have(
          httpStatus(OK),
          jsonBodyMatching(expectedResponseBody)
        )
      }
    }


    s"return $NOT_FOUND" when {
      "an unexpected status with NOT_FOUND was returned when retrieving repayment history by ID" in {

        isAuthorised(true)

        val errorJson = Json.arr(
          Json.obj("code" -> "NO_DATA_FOUND", "reason" -> "The remote endpoint has indicated that no data can be found.")
        )
        stubRepaymentHistoryByIdHip(nino, repaymentId)(
          status = NOT_FOUND, response = errorJson
        )
        val res: WSResponse = IncomeTaxFinancialDetails.getRepaymentHistoryById(nino, repaymentId)
        res should have(
          httpStatus(NOT_FOUND)
        )
      }

      "an unexpected status with UNPROCESSABLE_ENTITY was returned with No Data error codes when retrieving repayment history by ID" in {

        isAuthorised(true)

        val errorJson = HipRepaymentHistoryDetailsIntegrationTestConstants.repaymentsHistoryFailureWithError

        stubRepaymentHistoryByIdHip(nino, repaymentId)(
          status = UNPROCESSABLE_ENTITY, response = errorJson
        )
        val res: WSResponse = IncomeTaxFinancialDetails.getRepaymentHistoryById(nino, repaymentId)
        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }

    s"return $INTERNAL_SERVER_ERROR" when {
      "an unexpected status was returned when retrieving repayment history by ID" in {

        isAuthorised(true)

        stubRepaymentHistoryByIdHip(nino, repaymentId)(
          status = SERVICE_UNAVAILABLE
        )

        val res: WSResponse = IncomeTaxFinancialDetails.getRepaymentHistoryById(nino, repaymentId)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }

  }

  s"GET ${controllers.routes.RepaymentHistoryController.getAllRepaymentHistory(nino)}" should {
    s"return $OK" when {
      "repayment history is successfully retrieved by date range" in {

        isAuthorised(true)

        stubRepaymentHistoryHip(nino)(
          status = OK,
          response = validRepaymentHistoryJson)

        val res: WSResponse = IncomeTaxFinancialDetails.getAllRepaymentHistory(nino)

        val expectedResponseBody: JsValue = Json.toJson(SuccessfulRepaymentResponse(
          transactionHeader = TransactionHeader(
            status = "OK",
            processingDate = java.time.LocalDateTime.parse("2021-09-01T12:00:00"),
            returnParameters = None
          ),
          responseDetails = ResponseDetails(
            repaymentsViewerDetails = Seq(
              RepaymentViewerDetail(
                repaymentRequestNumber = "000000003135",
                actor = "CUSTOMER",
                channel = "ONLINE",
                status = "A",
                amountRequested = 200.0,
                amountApprovedforRepayment = Some(100.0),
                totalAmountforRepaymentSupplement = None,
                totalRepaymentAmount = Some(300.0),
                repaymentMethod = Some("BACD"),
                creationDate = Some(LocalDate.parse("2020-12-25")),
                estimatedRepaymentDate = Some(LocalDate.parse("2021-01-21")),
                repaymentItems = Some(Seq(
                  RepaymentItem(
                    //creditItems = None,
                    //paymentItems = None,
                    creditReasons = None,
                    repaymentSupplementItem = Some(Seq(
                      RepaymentSupplementItem(
                        creditReference = None,
                        parentCreditReference = Some("002420002231"),
                        amount = Some(400.0),
                        fromDate = Some(LocalDate.parse("2021-07-23")),
                        toDate = Some(LocalDate.parse("2021-08-23")),
                        rate = Some(500.0)
                      )
                    ))
                  )
                ))
              )
            )
          )
        ))


        res should have(
          httpStatus(OK),
          jsonBodyMatching(expectedResponseBody)
        )
      }
    }

    s"return $INTERNAL_SERVER_ERROR" when {
      "an unexpected status was returned when retrieving repayment history by date range" in {

        isAuthorised(true)

        stubRepaymentHistoryHip(nino)(
          status = SERVICE_UNAVAILABLE
        )

        val res: WSResponse = IncomeTaxFinancialDetails.getAllRepaymentHistory(nino)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }

    s"return $NOT_FOUND" when {
      "an unexpected status with NOT_FOUND was returned when retrieving repayment history by date range" in {

        isAuthorised(true)

        val errorJson = Json.arr(
          Json.obj("code" -> "NO_DATA_FOUND", "reason" -> "The remote endpoint has indicated that no data can be found.")
        )
        stubRepaymentHistoryHip(nino)(
          status = NOT_FOUND, response = errorJson
        )
        val res: WSResponse = IncomeTaxFinancialDetails.getAllRepaymentHistory(nino)

        res should have(
          httpStatus(NOT_FOUND)
        )
      }

      "an unexpected status with UNPROCESSABLE_ENTITY was returned with No Data error codes when retrieving repayment history by date range" in {
        isAuthorised(true)

        val errorJson = HipRepaymentHistoryDetailsIntegrationTestConstants.repaymentsHistoryFailureWithError

        stubRepaymentHistoryHip(nino)(
          status = UNPROCESSABLE_ENTITY, response = errorJson
        )
        val res: WSResponse = IncomeTaxFinancialDetails.getAllRepaymentHistory(nino)

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }
  }
}
