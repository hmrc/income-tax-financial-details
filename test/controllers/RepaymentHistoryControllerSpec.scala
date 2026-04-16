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

package controllers

import config.MicroserviceAppConfig
import connectors.httpParsers.RepaymentHistoryHttpParser.UnexpectedRepaymentHistoryResponse
import constants.BaseTestConstants.*
import constants.RepaymentHistoryTestConstants.*
import controllers.predicates.AuthenticationPredicate
import mocks.{MockMicroserviceAuthConnector, MockRepaymentHistoryDetailsConnector}
import models.hip.{ErrorResponse, GetRepaymentHistoryDetails}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.ControllerComponents
import play.api.test.Helpers.*
import services.RepaymentHistoryDetailsService

import scala.concurrent.Future

class RepaymentHistoryControllerSpec extends ControllerBaseSpec with MockMicroserviceAuthConnector with MockRepaymentHistoryDetailsConnector{

  val mockCC: ControllerComponents = stubControllerComponents()
  val mockAppConfig: MicroserviceAppConfig = mock[MicroserviceAppConfig]
  val authPredicate = new AuthenticationPredicate(mockMicroserviceAuthConnector, mockCC, microserviceAppConfig)
  val mockRepaymentHistoryDetailsService: RepaymentHistoryDetailsService = mock[RepaymentHistoryDetailsService]

  object TestRepaymentHistoryController extends RepaymentHistoryController(
    authPredicate, mockCC,mockRepaymentHistoryDetailsService, mockAppConfig
  )

  "getAllRepaymentHistory from HIP" should {
    "return 200 OK" when {
      "user is authenticated and HipRepaymentHistoryDetailsConnector returns success" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetRepaymentHistoryDetails)).thenReturn(true)
        when(mockRepaymentHistoryDetailsService.getRepaymentHistoryDetailsList(ArgumentMatchers.eq(testNino))(any(), any()))
          .thenReturn(Future.successful(Right(hipRepaymentHistoryList)))

        val result = TestRepaymentHistoryController.getAllRepaymentHistory(testNino)(fakeRequest)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(hipRepaymentHistoryList)
      }
    }

    "return 422 UNPROCESSABLE_ENTITY" when {
      "user is authenticated and HipRepaymentHistoryDetailsConnector returns UNPROCESSABLE_ENTITY" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetRepaymentHistoryDetails)).thenReturn(true)
        when(mockRepaymentHistoryDetailsService.getRepaymentHistoryDetailsList(ArgumentMatchers.eq(testNino))(any(), any()))
          .thenReturn(Future.successful(Left(ErrorResponse.UnprocessableData("Error"))))

        val result = TestRepaymentHistoryController.getAllRepaymentHistory(testNino)(fakeRequest)
        status(result) shouldBe UNPROCESSABLE_ENTITY
        contentAsJson(result) shouldBe Json.toJson("Error")
      }
    }
  }

  "getRepaymentHistoryById from HIP" should {
    "return 200 OK" when {
      "user is authenticated and HipRepaymentHistoryDetailsConnector returns success" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetRepaymentHistoryDetails)).thenReturn(true)
        when(mockRepaymentHistoryDetailsService.getRepaymentHistoryDetails(ArgumentMatchers.eq(testNino),
          ArgumentMatchers.eq("testRepaymentId"))(any(), any()))
          .thenReturn(Future.successful(Right(hipRepaymentHistorySingleItem)))

        val result = TestRepaymentHistoryController.getRepaymentHistoryById(testNino, "testRepaymentId")(fakeRequest)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(hipRepaymentHistorySingleItem)
      }
    }

    "return 422 UNPROCESSABLE_ENTITY" when {
      "user is authenticated and HipRepaymentHistoryDetailsConnector returns UNPROCESSABLE_ENTITY" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetRepaymentHistoryDetails)).thenReturn(true)
        when(mockRepaymentHistoryDetailsService.getRepaymentHistoryDetails(ArgumentMatchers.eq(testNino),
          ArgumentMatchers.eq("testRepaymentId"))(any(), any()))
          .thenReturn(Future.successful(Left(ErrorResponse.UnprocessableData("Error"))))

        val result = TestRepaymentHistoryController.getRepaymentHistoryById(testNino, "testRepaymentId")(fakeRequest)
        status(result) shouldBe UNPROCESSABLE_ENTITY
        contentAsJson(result) shouldBe Json.toJson("Error")
      }
    }
  }

  "getAllRepaymentHistory from IF" should {
    "return 200 OK" when {
      "user is authenticated and HipRepaymentHistoryDetailsConnector returns success" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetRepaymentHistoryDetails)).thenReturn(false)
        when(mockRepaymentHistoryDetailsService.getIFRepaymentHistoryDetailsList(ArgumentMatchers.eq(testNino))(any(), any()))
          .thenReturn(Future.successful(Right(defaultRepaymentHistoryResp)))

        val result = TestRepaymentHistoryController.getAllRepaymentHistory(testNino)(fakeRequest)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(defaultRepaymentHistoryResp)
      }
    }

    "return 422 UNPROCESSABLE_ENTITY" when {
      "user is authenticated and HipRepaymentHistoryDetailsConnector returns UNPROCESSABLE_ENTITY" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetRepaymentHistoryDetails)).thenReturn(false)
        when(mockRepaymentHistoryDetailsService.getIFRepaymentHistoryDetailsList(ArgumentMatchers.eq(testNino))(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedRepaymentHistoryResponse(422, JsString("error").toString))))

        val result = TestRepaymentHistoryController.getAllRepaymentHistory(testNino)(fakeRequest)
        status(result) shouldBe UNPROCESSABLE_ENTITY
        contentAsJson(result) shouldBe Json.toJson("error")
      }
    }

    "return 500" when {
      "user is authenticated and IF returns 502" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetRepaymentHistoryDetails)).thenReturn(false)
        when(mockRepaymentHistoryDetailsService.getIFRepaymentHistoryDetailsList(ArgumentMatchers.eq(testNino))(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedRepaymentHistoryResponse(502, JsString("error").toString))))

        val result = TestRepaymentHistoryController.getAllRepaymentHistory(testNino)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed to retrieve repayment history by date range"
      }
    }
  }

  "getRepaymentHistoryById from IF" should {
    "return 200 OK" when {
      "user is authenticated and IF returns success" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetRepaymentHistoryDetails)).thenReturn(false)
        when(mockRepaymentHistoryDetailsService.getIFRepaymentHistoryDetails(ArgumentMatchers.eq(testNino),
          ArgumentMatchers.eq("testRepaymentId"))(any(), any()))
          .thenReturn(Future.successful(Right(defaultRepaymentHistoryResp)))

        val result = TestRepaymentHistoryController.getRepaymentHistoryById(testNino, "testRepaymentId")(fakeRequest)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(defaultRepaymentHistoryResp)
      }
    }

    "return 422 UNPROCESSABLE_ENTITY" when {
      "user is authenticated and IF returns UNPROCESSABLE_ENTITY" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetRepaymentHistoryDetails)).thenReturn(false)
        when(mockRepaymentHistoryDetailsService.getIFRepaymentHistoryDetails(ArgumentMatchers.eq(testNino),
          ArgumentMatchers.eq("testRepaymentId"))(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedRepaymentHistoryResponse(422, JsString("error").toString))))

        val result = TestRepaymentHistoryController.getRepaymentHistoryById(testNino, "testRepaymentId")(fakeRequest)
        status(result) shouldBe UNPROCESSABLE_ENTITY
        contentAsJson(result) shouldBe Json.toJson("error")
      }
    }

    "return 500" when {
      "user is authenticated and IF returns 502" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetRepaymentHistoryDetails)).thenReturn(false)
        when(mockRepaymentHistoryDetailsService.getIFRepaymentHistoryDetails(ArgumentMatchers.eq(testNino),
          ArgumentMatchers.eq("testRepaymentId"))(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedRepaymentHistoryResponse(502, JsString("error").toString))))

        val result = TestRepaymentHistoryController.getRepaymentHistoryById(testNino, "testRepaymentId")(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed to retrieve repayment history by ID"
      }
    }
  }
}
