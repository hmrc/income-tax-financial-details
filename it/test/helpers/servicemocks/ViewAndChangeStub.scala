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

package helpers.servicemocks

import helpers.WiremockHelper
import play.api.http.Status
import play.api.libs.json.JsValue

object ViewAndChangeStub {

  private def base(nino: String): String =
    s"/income-tax-view-change/$nino/financial-details"

  def stubGetCredits(nino: String, from: String, to: String)(status: Int, body: String): Unit =
    WiremockHelper.stubGet(s"${base(nino)}/credits/from/$from/to/$to", status, body)

  def stubGetCharges(nino: String, from: String, to: String)(status: Int, body: String): Unit =
    WiremockHelper.stubGet(s"${base(nino)}/charges/from/$from/to/$to", status, body)

  def stubGetPayments(nino: String, from: String, to: String)(status: Int, body: String): Unit =
    WiremockHelper.stubGet(s"${base(nino)}/payments/from/$from/to/$to", status, body)

  def stubGetOnlyOpenItems(nino: String)(status: Int, body: String): Unit =
    WiremockHelper.stubGet(s"${base(nino)}/only-open-items", status, body)

  def stubGetChargeByDocumentId(nino: String, documentId: String)(status: Int, body: String): Unit =
    WiremockHelper.stubGet(s"${base(nino)}/charges/documentId/$documentId", status, body)
}
