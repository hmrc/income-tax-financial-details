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
import connectors.{OutStandingChargesConnector, ViewAndChangeConnector}
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
    val ViewAndChangeConnector: ViewAndChangeConnector = mock(classOf[ViewAndChangeConnector])

    val service = new OutStandingChargesService(
      OutStandingChargesConnector,
      ViewAndChangeConnector
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
      "call the viewAndChangeConnector" in new Setup {
        val errorJson = """{"code":"NO_DATA_FOUND","reason":"The remote endpoint has indicated that no data can be found."}"""
        
        when(OutStandingChargesConnector.listOutStandingCharges(matches(idType), matches(idNumber), matches(taxYearEndDate))(any()))
          .thenReturn(Future.successful(Left(UnexpectedOutStandingChargeResponse(NOT_FOUND, errorJson))))
        when(ViewAndChangeConnector.listOutStandingCharges(matches(idType), matches(idNumber), matches(taxYearEndDate))(any()))
          .thenReturn(Future.successful(Right(OutstandingChargesSuccessResponse(List(outStandingChargeModelOne, outStandingChargeModelTwo)))))

        val result: Future[OutStandingChargeResponse] = service.listOutStandingCharges(idType, idNumber, taxYearEndDate)(hc, ec)

        await(result) shouldBe Right(OutstandingChargesSuccessResponse(List(outStandingChargeModelOne, outStandingChargeModelTwo)))
      }
    }
  }
}
