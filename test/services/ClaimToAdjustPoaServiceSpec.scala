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

import connectors.{ClaimToAdjustPoaConnector, ViewAndChangeConnector}
import constants.ClaimToAdjustPoaTestConstants.*
import models.claimToAdjustPoa.ClaimToAdjustPoaResponse.ClaimToAdjustPoaResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.test.Helpers.*
import utils.TestSupport

import scala.concurrent.Future

class ClaimToAdjustPoaServiceSpec extends TestSupport {

  trait Setup {
    val claimToAdjustPoaConnector: ClaimToAdjustPoaConnector = mock(classOf[ClaimToAdjustPoaConnector])
    val viewAndChangeConnector: ViewAndChangeConnector = mock(classOf[ViewAndChangeConnector])

    val service = new ClaimToAdjustPoaService(
      claimToAdjustPoaConnector,
      viewAndChangeConnector
    )
  }

  "postClaimToAdjustPoa" when {
    s"the call to DES is successful" should {
      s"return the success model" in new Setup {
        when(claimToAdjustPoaConnector.postClaimToAdjustPoa(any())(any()))
          .thenReturn(Future.successful(claimToAdjustPoaSuccessResponse))

        val result: Future[ClaimToAdjustPoaResponse] = service.postClaimToAdjustPoa(claimToAdjustPoaRequest)(hc, ec)

        await(result) shouldBe claimToAdjustPoaResult
      }
    }
  }

  "the call to DES is unsuccessful" should {
    "call the view and change connector and return its response" in new Setup {
      when(claimToAdjustPoaConnector.postClaimToAdjustPoa(any())(any()))
        .thenReturn(Future.successful(claimToAdjustPoaErrorResponse))
      when(viewAndChangeConnector.postClaimToAdjustPoa(any())(any()))
        .thenReturn(Future.successful(claimToAdjustPoaViewAndChangeResult))

      val result: Future[ClaimToAdjustPoaResponse] = service.postClaimToAdjustPoa(claimToAdjustPoaRequest)(hc, ec)

      await(result) shouldBe claimToAdjustPoaViewAndChangeResult
    }
  }

}
