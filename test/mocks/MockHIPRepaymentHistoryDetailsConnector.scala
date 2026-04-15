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

package mocks

import connectors.hip.HipRepaymentHistoryDetailsConnector
import connectors.hip.httpParsers.ChargeHipHttpParser.HttpGetResult
import models.hip.repayments.SuccessfulRepaymentResponse
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}

import scala.concurrent.Future

trait MockHIPRepaymentHistoryDetailsConnector extends AnyWordSpecLike with Matchers with OptionValues with BeforeAndAfterEach {

  val mockHipRepaymentHistoryDetailsConnector: HipRepaymentHistoryDetailsConnector = mock(classOf[HipRepaymentHistoryDetailsConnector])

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHipRepaymentHistoryDetailsConnector)
  }

  def getRepaymentHistoryDetailsList(nino: String)
                                    (response: HttpGetResult[SuccessfulRepaymentResponse]): Unit = {
    when(mockHipRepaymentHistoryDetailsConnector.getRepaymentHistoryDetailsList(
      idValue = ArgumentMatchers.eq(nino)
    )(ArgumentMatchers.any(), ArgumentMatchers.any())) thenReturn Future.successful(response)
  }

  def getRepaymentHistoryDetails(nino: String, repaymentId: String)
                                (response: HttpGetResult[SuccessfulRepaymentResponse]): Unit = {
    when(mockHipRepaymentHistoryDetailsConnector.getRepaymentHistoryDetails(
      idValue = ArgumentMatchers.eq(nino),
      repaymentRequestNumber = ArgumentMatchers.eq(repaymentId)
    )(ArgumentMatchers.any(), ArgumentMatchers.any())) thenReturn Future.successful(response)
  }
}

