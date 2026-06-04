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

import connectors.httpParsers.OutStandingChargesHttpParser.{OutStandingChargeResponse, UnexpectedOutStandingChargeResponse}
import connectors.OutStandingChargesConnector
import constants.OutStandingChargesConstant.{outStandingChargeModelOne, outStandingChargeModelTwo}
import models.outStandingCharges.OutstandingChargesSuccessResponse
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.matches
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.test.Helpers.*
import utils.TestSupport

import scala.concurrent.Future

class OutStandingChargesServiceSpec extends TestSupport {

  trait Setup {
    val OutStandingChargesConnector: OutStandingChargesConnector = mock(classOf[OutStandingChargesConnector])

    val service = new OutStandingChargesService(
      OutStandingChargesConnector
    )
  }

  val idType: String = "utr"
  val idNumber = "1234567890"
  val invalidUtr = "1234"
  val invalidUtr2 = "abcdefghijk"
  val taxYearEndDate: String = "2020-04-05"

  "listOutstandingCharges" when {
    "the call to des is successful" should {
      "return the success model" in new Setup {
        when(OutStandingChargesConnector.listOutStandingCharges(matches(idType), matches(idNumber), matches(taxYearEndDate))(any()))
          .thenReturn(Future.successful(Right(OutstandingChargesSuccessResponse(List(outStandingChargeModelOne, outStandingChargeModelTwo)))))

        val result: Future[OutStandingChargeResponse] = service.listOutStandingCharges(idType, idNumber, taxYearEndDate)(hc,ec)

        await(result) shouldBe Right(OutstandingChargesSuccessResponse(List(outStandingChargeModelOne, outStandingChargeModelTwo)))
      }
    }
    "the call to des fails" should {
      "return the error response" in new Setup {
        val errorJson = """{"code":"INTERNAL_SERVER_ERROR","reason":"There was an issue."}"""

        when(OutStandingChargesConnector.listOutStandingCharges(matches(idType), matches(idNumber), matches(taxYearEndDate))(any()))
          .thenReturn(Future.successful(Left(UnexpectedOutStandingChargeResponse(INTERNAL_SERVER_ERROR, errorJson))))

        val result: Future[OutStandingChargeResponse] = service.listOutStandingCharges(idType, idNumber, taxYearEndDate)(hc, ec)

        await(result) shouldBe Left(UnexpectedOutStandingChargeResponse(INTERNAL_SERVER_ERROR, errorJson))
      }
    }
  }
}
