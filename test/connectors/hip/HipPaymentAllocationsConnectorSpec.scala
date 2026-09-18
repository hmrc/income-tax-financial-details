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

package connectors.hip

import constants.hip.PaymentAllocationsTestConstants.*
import mocks.MockHttpV2
import models.hip.GetPaymentAllocationsHipApi
import models.hip.paymentAllocations.{PaymentAllocationsError, PaymentAllocationsNotFound}
import org.mockito.stubbing.OngoingStubbing
import uk.gov.hmrc.http.HttpResponse
import utils.TestSupport

import scala.concurrent.Future

class HipPaymentAllocationsConnectorSpec extends TestSupport with MockHttpV2 {
  
  object TestPaymentAllocationsConnector extends HipPaymentAllocationsConnector(mockHttpClientV2, microserviceAppConfig)

  import TestPaymentAllocationsConnector.*

  val hipPlatformUrl: String = microserviceAppConfig.hipUrl
  val testNino: String = "AA123456A"
  val testPaymentLot: String = "081203010024"
  val testPaymentLotItem: String = "000001"
  val paymentAllocationsUrl: String = s"$hipPlatformUrl/etmp/RESTAdapter/payment-allocation/NINO/$testNino/ITSA"

  lazy val mockPaymentAllocations: HttpResponse => OngoingStubbing[Future[HttpResponse]] =
    setupMockHttpGetWithHeaderCarrier(paymentAllocationsUrl, microserviceAppConfig.getHIPHeaders(GetPaymentAllocationsHipApi))(_)

  "HipPaymentAllocationsConnector" should {
    "format the API url correctly" when {
      "the paymentAllocationsUrl is called" in {
        getUrl(testNino) shouldBe paymentAllocationsUrl
      }
    }

    "return the correct query parameters" when {
      "queryParameters is called" in {
        val expectedQueryParameters: Seq[(String, String)] = Seq(
          "paymentLot" -> testPaymentLot,
          "paymentLotItem" -> testPaymentLotItem
        )
        queryParameters(testPaymentLot, testPaymentLotItem) shouldBe expectedQueryParameters
      }
    }

    "pass the correct headers" when {
      "the paymentAllocations url is called" in {
        mockPaymentAllocations(successResponseFromApi)

        getHeaders.exists(_._1 == "Authorization") shouldBe true
        getHeaders.exists(_._1 == "correlationId") shouldBe true
        getHeaders.exists(_._1 == "X-Originating-System") shouldBe true
        getHeaders.exists(_._1 == "X-Receipt-Date") shouldBe true
        getHeaders.exists(_._1 == "X-Regime-Type") shouldBe true
        getHeaders.exists(_._1 == "X-Transmitting-System") shouldBe true

        getPaymentAllocations(testNino, testPaymentLot, testPaymentLotItem).futureValue shouldBe Right(paymentAllocationSingle)
      }
    }

    "return a paymentAllocationsResponseModel when the response has been successfully retrieved and parsed" in {
      mockPaymentAllocations(successResponseFromApi)
      getPaymentAllocations(testNino, testPaymentLot, testPaymentLotItem).futureValue shouldBe Right(paymentAllocationSingle)
    }

    "return a paymentAllocationsError when the response has been successfully retrieved but unable to be parsed" in {
      mockPaymentAllocations(badSuccessFromApi)
      getPaymentAllocations(testNino, testPaymentLot, testPaymentLotItem).futureValue shouldBe
        Left(PaymentAllocationsError)
    }

    "return a paymentAllocationNotFound when the response has returned was a NotFound" in {
      mockPaymentAllocations(notFoundFromApi)
      getPaymentAllocations(testNino, testPaymentLot, testPaymentLotItem).futureValue shouldBe
        Left(PaymentAllocationsNotFound)
    }

    "return a paymentAllocationError when the response returned was an InternalServerError" in {
      mockPaymentAllocations(serverErrorFromApi)
      getPaymentAllocations(testNino, testPaymentLot, testPaymentLotItem).futureValue shouldBe
        Left(PaymentAllocationsError)
    }
  }

}
