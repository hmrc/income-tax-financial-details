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

package services

import connectors.ViewAndChangeConnector
import connectors.httpParsers.RepaymentHistoryHttpParser.UnexpectedRepaymentHistoryResponse
import mocks.{MockHIPRepaymentHistoryDetailsConnector, MockRepaymentHistoryDetailsConnector}
import models.hip.ErrorResponse.UnexpectedResponse
import models.hip.repayments.*
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{mock, when}
import utils.TestSupport

import java.time.LocalDate
import scala.concurrent.Future

class RepaymentHistoryDetailServiceSpec extends TestSupport
  with MockRepaymentHistoryDetailsConnector
  with MockHIPRepaymentHistoryDetailsConnector {

  val mockViewAndChangeConnector = mock(classOf[ViewAndChangeConnector])

  val service = new RepaymentHistoryDetailsService(
    mockHipRepaymentHistoryDetailsConnector,
    mockRepaymentHistoryDetailsConnector,
    mockViewAndChangeConnector
  )

  lazy val defaultHIPRepaymentHistoryResp = SuccessfulRepaymentResponse(
    transactionHeader = TransactionHeader(
      status = "OK",
      processingDate = java.time.LocalDateTime.parse("2021-09-01T12:00:00")
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
  )

  "getRepaymentHistoryDetailsList" should {
    "return success response with Json" when {
      "provided with the correct params" in {
        getRepaymentHistoryDetailsList("testNino")(Right(defaultHIPRepaymentHistoryResp))
        val expected = service.getRepaymentHistoryDetailsList("testNino").futureValue
        expected shouldBe Right(defaultHIPRepaymentHistoryResp)
      }
    }
    "fallback to VC when HiP fails" in {
      getRepaymentHistoryDetailsList("testNino")(Left(UnexpectedResponse))
      when(
        mockViewAndChangeConnector.getRepaymentHistoryDetailsList(
          ArgumentMatchers.eq("testNino")
        )(ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(Future.successful(Right(defaultHIPRepaymentHistoryResp)))
      val expected = service.getRepaymentHistoryDetailsList("testNino").futureValue
      expected shouldBe Right(defaultHIPRepaymentHistoryResp)
    }
  }

  "getRepaymentHistoryDetails" should {
    "return success response with Json" when {
      "provided with the correct params" in {
        getRepaymentHistoryDetails("testNino", "testDocumentId")(Right(defaultHIPRepaymentHistoryResp))
        val expected = service.getRepaymentHistoryDetails("testNino", "testDocumentId").futureValue
        expected shouldBe Right(defaultHIPRepaymentHistoryResp)
      }
    }
    "fallback to VC when HiP fails" in {
      getRepaymentHistoryDetails("testNino", "testDocumentId")(Left(UnexpectedResponse))

      when(
        mockViewAndChangeConnector.getRepaymentHistoryDetails(
          ArgumentMatchers.eq("testNino"), ArgumentMatchers.eq("testDocumentId")
        )(ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(Future.successful(Right(defaultHIPRepaymentHistoryResp)))
      val expected = service.getRepaymentHistoryDetails("testNino", "testDocumentId").futureValue
      expected shouldBe Right(defaultHIPRepaymentHistoryResp)
    }
  }


  "getIFRepaymentHistoryDetailsList" should {
    "return success response with Json" when {
      "correct params provided" in {
        getAllRepaymentHistoryDetails("testNino")(Right(defaultRepaymentHistoryResp))
        val expected = service.getIFRepaymentHistoryDetailsList("testNino").futureValue
        expected shouldBe Right(defaultRepaymentHistoryResp)
      }
    }
    "fallback to VC when IF fails" in {
      getAllRepaymentHistoryDetails("testNino")(Left(UnexpectedRepaymentHistoryResponse(500, "error")))
      when(
        mockViewAndChangeConnector.getIFRepaymentHistoryDetailsList(
          ArgumentMatchers.eq("testNino")
        )(ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(Future.successful(Right(defaultRepaymentHistoryResp)))
      val expected = service.getIFRepaymentHistoryDetailsList("testNino").futureValue
      expected shouldBe Right(defaultRepaymentHistoryResp)
    }
  }

  "getIFRepaymentHistoryDetails" should {
    "return HiP success mapped to Json" in {
      getRepaymentHistoryDetailsById("testNino", "testDocumentId")(Right(defaultRepaymentHistoryResp))
      val expected = service.getIFRepaymentHistoryDetails("testNino", "testDocumentId").futureValue
      expected shouldBe Right(defaultRepaymentHistoryResp)
    }

    "fallback to VC when IF fails" in {
      getRepaymentHistoryDetailsById("testNino", "testDocumentId")(Left(UnexpectedRepaymentHistoryResponse(500, "error")))
      when(
        mockViewAndChangeConnector.getIFRepaymentHistoryDetails(
          ArgumentMatchers.eq("testNino"),
          ArgumentMatchers.eq("testDocumentId")
        )(ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(Future.successful(Right(defaultRepaymentHistoryResp)))
      val expected = service.getIFRepaymentHistoryDetails("testNino", "testDocumentId").futureValue
      expected shouldBe Right(defaultRepaymentHistoryResp)
    }
  }

}
