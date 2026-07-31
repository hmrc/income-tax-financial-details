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

package models.hip.repayments

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SuccessfulRepaymentResponseSpec extends AnyWordSpec with Matchers {

  def generateTestEtmpTransactionHeader(testReturnParameters: Option[List[ReturnParameters]]) = EtmpFailureRepaymentResponse(TransactionHeader(
    status = "NOT_OK",
    processingDate = java.time.LocalDateTime.parse("2021-09-01T12:00:00"),
    returnParameters = testReturnParameters
  ))

  val noDataReturnParameter = ReturnParameters(paramName = "ERRORCODE", paramValue = "001")
  val nonMatchingParamName = ReturnParameters(paramName = "ERRORTEXT", paramValue = "001")
  val nonMatchingParamValue = ReturnParameters(paramName = "ERRORCODE", paramValue = "005")

  "The return parameters model" when {
    "calling the isNoDataCode method" should {
      "return a value of true" when {
        "the matching parameter name and value is provided" in {
          noDataReturnParameter.isNoDataCode shouldBe true
        }
      }

      "return a value of false" when {
        "a non-matching parameter name is provided" in {
          nonMatchingParamName.isNoDataCode shouldBe false
        }

        "a non-matching parameter value is provided" in {
          nonMatchingParamValue.isNoDataCode shouldBe false
        }
      }
    }
  }

  "The EtmpFailureRepaymentResponse model" when {
    "calling the containsNoDataResponse" should {
      "return a value of true" when {
        "a single ReturnParameter model has the No Data code" in {
          val testResponse = generateTestEtmpTransactionHeader(Some(List(noDataReturnParameter, nonMatchingParamValue, nonMatchingParamName)))

          testResponse.containsNoDataResponse shouldBe true
        }

        "multiple ReturnParameter models have the No Data code" in {
          val testResponse = generateTestEtmpTransactionHeader(Some(List(noDataReturnParameter, noDataReturnParameter)))

          testResponse.containsNoDataResponse shouldBe true
        }
      }

      "return a value of false" when {
        "no ReturnParameter models have the No Data code" in {
          val testResponse = generateTestEtmpTransactionHeader(Some(List(nonMatchingParamValue, nonMatchingParamName)))

          testResponse.containsNoDataResponse shouldBe false
        }

        "the return parameters are an empty list" in {
          val testResponse = generateTestEtmpTransactionHeader(Some(List.empty[ReturnParameters]))

          testResponse.containsNoDataResponse shouldBe false
        }

        "the return parameters are not present" in {
          val testResponse = generateTestEtmpTransactionHeader(None)

          testResponse.containsNoDataResponse shouldBe false
        }
      }
    }
  }
}
