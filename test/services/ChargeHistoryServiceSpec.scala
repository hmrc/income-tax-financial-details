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
import connectors.hip.GetChargeHistoryConnector
import constants.hip.ChargeHistoryTestConstants.{serviceErrorResponse, serviceSuccessResponse}
import models.hip.chargeHistory.{ChargeHistoryResponseError, ChargeHistorySuccessWrapper}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.TestSupport

import scala.concurrent.Future

class ChargeHistoryServiceSpec extends TestSupport {

  trait Setup {
    val getChargeHistoryConnector: GetChargeHistoryConnector = mock(classOf[GetChargeHistoryConnector])
    val viewAndChangeConnector: ViewAndChangeConnector = mock(classOf[ViewAndChangeConnector])

    val service = new ChargeHistoryService(
      getChargeHistoryConnector,
      viewAndChangeConnector
    )
  }

  "getChargeHistory" when {
    s"the call is successful" should {
      s"return the success model" in new Setup {
        when(getChargeHistoryConnector.getChargeHistory(any(), any())(any(), any()))
          .thenReturn(Future.successful(serviceSuccessResponse))

        val result:  Future[Either[ChargeHistoryResponseError, ChargeHistorySuccessWrapper]]= service.getChargeHistory("123", "456")(hc, ec)

        await(result) shouldBe serviceSuccessResponse
      }
    }
  }

  "getChargeHistory" when {
    s"the call is unsuccessful" should {
      s"call the view and change connector and return its response" in new Setup {
        when(
          getChargeHistoryConnector.getChargeHistory(any(), any())(any(), any())
        ).thenReturn(Future.successful(serviceErrorResponse))

        when(
          viewAndChangeConnector.getChargeHistory(any(), any())(any(), any())
        ).thenReturn(Future.successful(serviceSuccessResponse))

        val result: Future[Either[ChargeHistoryResponseError, ChargeHistorySuccessWrapper]] =
          service.getChargeHistory("123", "456")(hc, ec)

        await(result) shouldBe serviceSuccessResponse
      }
    }
  }
}
