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

package connectors

import config.MicroserviceAppConfig
import connectors.httpParsers.ClaimToAdjustPoaHttpParser._
import connectors.httpParsers.ViewAndChangeHttpParser.ViewAndChangeJsonResponse
import connectors.httpParsers.ViewAndChangeHttpParser.given
import models.claimToAdjustPoa.ClaimToAdjustPoaRequest
import models.claimToAdjustPoa.ClaimToAdjustPoaResponse.{ClaimToAdjustPoaResponse, ErrorResponse}
import play.api.Logger
import play.api.libs.ws.writeableOf_JsValue
import play.api.Logging
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewAndChangeConnector @Inject()(
                                        val appConfig: MicroserviceAppConfig,
                                        val http: HttpClientV2
                                      )(implicit ec: ExecutionContext) extends Logging {

  private def base(nino: String): String =
    s"${appConfig.viewAndChangeBaseUrl}/income-tax-view-change/$nino/financial-details"

  private val claimToAdjustEndpoint: String =
    s"${appConfig.viewAndChangeBaseUrl}/income-tax/calculations/POA/ClaimToAdjust"

  def getCredits(nino: String, from: String, to: String)
                (implicit hc: HeaderCarrier): Future[ViewAndChangeJsonResponse] = {
    val url = s"${base(nino)}/credits/from/$from/to/$to"
    http.get(url"$url").execute[ViewAndChangeJsonResponse]
  }

  def getChargeDetails(nino: String, from: String, to: String)
                      (implicit hc: HeaderCarrier): Future[ViewAndChangeJsonResponse] = {
    val url = s"${base(nino)}/charges/from/$from/to/$to"
    http.get(url"$url").execute[ViewAndChangeJsonResponse]
  }

  def getChargeDetailsByDocumentId(nino: String, docId: String)
                                  (implicit hc: HeaderCarrier): Future[ViewAndChangeJsonResponse] = {
    val url = s"${base(nino)}/charges/documentId/$docId"
    http.get(url"$url").execute[ViewAndChangeJsonResponse]
  }

  def getPayments(nino: String, from: String, to: String)
                 (implicit hc: HeaderCarrier): Future[ViewAndChangeJsonResponse] = {
    val url = s"${base(nino)}/payments/from/$from/to/$to"
    http.get(url"$url").execute[ViewAndChangeJsonResponse]
  }

  def getOnlyOpenItems(nino: String)
                      (implicit hc: HeaderCarrier): Future[ViewAndChangeJsonResponse] = {
    val url = s"${base(nino)}/only-open-items"
    http.get(url"$url").execute[ViewAndChangeJsonResponse]
  }

  def postClaimToAdjustPoa(request: ClaimToAdjustPoaRequest)
                          (implicit hc: HeaderCarrier): Future[ClaimToAdjustPoaResponse] = {
    http
      .post(url"$claimToAdjustEndpoint")
      .withBody(Json.toJson(request))
      .transform(_.withRequestTimeout(Duration(appConfig.claimToAdjustTimeout, SECONDS)))
      .execute[ClaimToAdjustPoaResponse]
      .recover { case e =>
        Logger("application").error(e.getMessage, e)
        ClaimToAdjustPoaResponse(
          INTERNAL_SERVER_ERROR,
          Left(ErrorResponse(e.getMessage))
        )
      }
  }
}  