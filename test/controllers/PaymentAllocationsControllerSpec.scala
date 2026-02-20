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

import connectors.httpParsers.PaymentAllocationsHttpParser.{NotFoundResponse, UnexpectedResponse}
import controllers.predicates.AuthenticationPredicate
import mocks.MockMicroserviceAuthConnector
import models.paymentAllocations.{paymentAllocationsFull, paymentAllocationsWriteJsonFull}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.PaymentAllocationsService

import scala.concurrent.Future

class PaymentAllocationsControllerSpec extends ControllerBaseSpec with MockMicroserviceAuthConnector {

  val controllerComponents: ControllerComponents = stubControllerComponents()
  val mockService = mock[PaymentAllocationsService]
  object PaymentAllocationsController extends PaymentAllocationsController(
    authentication = new AuthenticationPredicate(mockMicroserviceAuthConnector, controllerComponents, microserviceAppConfig),
    cc = controllerComponents,
    paymentAllocationsService = mockService
  )

  val nino: String = "AA000000A"
  val paymentLot: String = "paymentLot"
  val paymentLotItem: String = "paymentLotItem"

  "getPaymentAllocations" should {
    s"return $OK with the retrieved payment allocations" when {
      "the connector returns the payment allocations" in {
        mockAuth()
        when(
          mockService.getPaymentAllocations(
            ArgumentMatchers.eq(nino),
            ArgumentMatchers.eq(paymentLot),
            ArgumentMatchers.eq(paymentLotItem)
          )(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(Future.successful(Right(paymentAllocationsFull)))

        val result = PaymentAllocationsController.getPaymentAllocations(nino, paymentLot, paymentLotItem)(FakeRequest())

        status(result) shouldBe OK
        contentAsJson(result) shouldBe paymentAllocationsWriteJsonFull
      }
    }
    s"return a $NOT_FOUND response" when {
      "the service returns a NotFoundResponse" in {
        mockAuth()
        when(
          mockService.getPaymentAllocations(
            ArgumentMatchers.eq(nino),
            ArgumentMatchers.eq(paymentLot),
            ArgumentMatchers.eq(paymentLotItem)
          )(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(Future.successful(Left(NotFoundResponse)))

        val result = PaymentAllocationsController.getPaymentAllocations(nino, paymentLot, paymentLotItem)(FakeRequest())

        status(result) shouldBe NOT_FOUND
        contentAsString(result) shouldBe "No payment allocations found"
      }
    }

    s"return $INTERNAL_SERVER_ERROR" when {
      "the service returns an error" in {
        mockAuth()
        when(
          mockService.getPaymentAllocations(
            ArgumentMatchers.eq(nino),
            ArgumentMatchers.eq(paymentLot),
            ArgumentMatchers.eq(paymentLotItem)
          )(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(Future.successful(Left(UnexpectedResponse)))

        val result = PaymentAllocationsController.getPaymentAllocations(nino, paymentLot, paymentLotItem)(FakeRequest())

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed to retrieve payment allocations"
      }
    }
  }

}
