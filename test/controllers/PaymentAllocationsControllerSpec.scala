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
import connectors.httpParsers.PaymentAllocationsHttpParser.{NotFoundResponse, UnexpectedResponse}
import controllers.predicates.AuthenticationPredicate
import mocks.MockMicroserviceAuthConnector
import models.hip.GetPaymentAllocationsHipApi
import models.hip.paymentAllocations.{AllocationDetail, PaymentAllocations, PaymentAllocationsError, PaymentAllocationsNotFound}
import models.paymentAllocations.{paymentAllocationsFull, paymentAllocationsWriteJsonFull}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.PaymentAllocationsService

import java.time.LocalDate
import scala.concurrent.Future

class PaymentAllocationsControllerSpec extends ControllerBaseSpec with MockMicroserviceAuthConnector {

  val controllerComponents: ControllerComponents = stubControllerComponents()
  val mockService: PaymentAllocationsService = mock[PaymentAllocationsService]
  val mockAppConfig: MicroserviceAppConfig = mock[MicroserviceAppConfig]
  val authPredicate = new AuthenticationPredicate(mockMicroserviceAuthConnector, controllerComponents, microserviceAppConfig)

  object PaymentAllocationsController extends PaymentAllocationsController(
    authentication = authPredicate,
    cc = controllerComponents,
    paymentAllocationsService = mockService,
    appConfig = mockAppConfig
  )

  val nino: String = "AA000000A"
  val paymentLot: String = "paymentLot"
  val paymentLotItem: String = "paymentLotItem"

  "getPaymentAllocations" when {
    "the GetPaymentAllocationsHipApi feature switch is disabled" should {
      s"return $OK with the retrieved payment allocations" when {
        "the connector returns the payment allocations" in {
          mockAuth()
          when(mockAppConfig.hipFeatureSwitchEnabled(GetPaymentAllocationsHipApi)).thenReturn(false)

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
          when(mockAppConfig.hipFeatureSwitchEnabled(GetPaymentAllocationsHipApi)).thenReturn(false)

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
          when(mockAppConfig.hipFeatureSwitchEnabled(GetPaymentAllocationsHipApi)).thenReturn(false)

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

  "the GetPaymentAllocationsHipApi feature switch is enabled" should {
    s"return $OK with the retrieved payment allocations" when {
      "the connector returns the payment allocations" in {

        val paymentAllocationsFullHip: PaymentAllocations = PaymentAllocations(
          amount = Some(500.00),
          method = Some("method"),
          reference = Some("reference"),
          transactionDate = Some(LocalDate.of(2022, 6, 23)),
          allocations = Seq(AllocationDetail(
            transactionId = Some("transactionId"),
            from = Some(LocalDate.of(2022, 6, 23)),
            to = Some(LocalDate.of(2022, 6, 23)),
            chargeType = Some("type"),
            mainType = Some("mainType"),
            amount = Some(1000.00),
            clearedAmount = Some(500.00),
            chargeReference = Some("chargeReference")
          ))
        )
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetPaymentAllocationsHipApi)).thenReturn(true)

        when(
          mockService.getPaymentAllocationsHip(
            ArgumentMatchers.eq(nino),
            ArgumentMatchers.eq(paymentLot),
            ArgumentMatchers.eq(paymentLotItem)
          )(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(Future.successful(Right(paymentAllocationsFullHip)))

        val result = PaymentAllocationsController.getPaymentAllocations(nino, paymentLot, paymentLotItem)(FakeRequest())

        status(result) shouldBe OK
        contentAsJson(result) shouldBe paymentAllocationsWriteJsonFull
      }
    }
    s"return a $NOT_FOUND response" when {
      "the service returns a NotFoundResponse" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetPaymentAllocationsHipApi)).thenReturn(true)

        when(
          mockService.getPaymentAllocationsHip(
            ArgumentMatchers.eq(nino),
            ArgumentMatchers.eq(paymentLot),
            ArgumentMatchers.eq(paymentLotItem)
          )(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(Future.successful(Left(PaymentAllocationsNotFound)))

        val result = PaymentAllocationsController.getPaymentAllocations(nino, paymentLot, paymentLotItem)(FakeRequest())

        status(result) shouldBe NOT_FOUND
        contentAsString(result) shouldBe "No payment allocations found"
      }
    }

    s"return $INTERNAL_SERVER_ERROR" when {
      "the service returns an error" in {
        mockAuth()
        when(mockAppConfig.hipFeatureSwitchEnabled(GetPaymentAllocationsHipApi)).thenReturn(true)

        when(
          mockService.getPaymentAllocationsHip(
            ArgumentMatchers.eq(nino),
            ArgumentMatchers.eq(paymentLot),
            ArgumentMatchers.eq(paymentLotItem)
          )(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(Future.successful(Left(PaymentAllocationsError)))

        val result = PaymentAllocationsController.getPaymentAllocations(nino, paymentLot, paymentLotItem)(FakeRequest())

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed to retrieve payment allocations"
      }
    }
  }
}
