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

import config.MicroserviceAppConfig
import connectors.RawResponseReads
import models.hip.{GetPaymentAllocationsHipApi, HipResponseErrorsObject}
import models.hip.paymentAllocations.{HipPaymentAllocationsResponse, PaymentAllocationsError, PaymentAllocationsNotFound, PaymentAllocationsResponseError, PaymentAllocationsResponseModel}
import play.api.http.Status.*
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.libs.json.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HipPaymentAllocationsConnector @Inject()(val httpClient: HttpClientV2, val appConfig: MicroserviceAppConfig) extends RawResponseReads with HipConnectorDataHelper {

  def getUrl(nino: String): String = {
    s"${appConfig.hipUrl}/etmp/RESTAdapter/payment-allocation/NINO/$nino/ITSA"
  }

  def queryParameters(paymentLot: String, paymentLotItem: String): Seq[(String, String)] = {
    Seq(
      "paymentLot" -> paymentLot,
      "paymentLotItem" -> paymentLotItem
    )
  }

  def getHeaders: Seq[(String, String)] = appConfig.getHIPHeaders(GetPaymentAllocationsHipApi)

  def getPaymentAllocations(nino: String, paymentLot: String, paymentLotItem: String)
                           (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[HipPaymentAllocationsResponse] = {
    val url = getUrl(nino)
    val params = queryParameters(paymentLot, paymentLotItem)
    httpClient
      .get(url"$url")
      .setHeader(getHeaders: _*)
      .transform(_.addQueryStringParameters(params: _*))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK =>
            response.json.validate[PaymentAllocationsResponseModel] match {
              case JsSuccess(result, _) => result.paymentDetails.headOption match {
                case Some(paymentAllocations) => Right(paymentAllocations)
                case None =>
                  logger.error("Unable to parse payment allocations response")
                  Left(PaymentAllocationsError)
              }
              case JsError(errors) =>
                logger.error(s"Json validation error. Reasons: $errors")
                Left(PaymentAllocationsError)
            }
          case NOT_FOUND =>
            logger.info("No allocations found for payment")
            Left(PaymentAllocationsNotFound)
          case UNPROCESSABLE_ENTITY => Left(handleUnprocessableStatusResponse(response))
          case _ =>
            logger.error(s"RESPONSE from HIP status: -  ${response.status}, body -  ${response.body}")
            Left(PaymentAllocationsError)
        }
      } recover {
      case ex =>
        logger.error(s"Unexpected failed future, ${ex.getMessage}")
        Left(PaymentAllocationsError)
    }
  }

  private def handleUnprocessableStatusResponse(unprocessableResponse: HttpResponse): PaymentAllocationsResponseError = {
    val notFoundCodes = Set("003", "005", "015")
    unprocessableResponse.json.validate[HipResponseErrorsObject] match {
      case JsError(errors) =>
        logger.error(s"${unprocessableResponse.status} returned from HIP with body: ${unprocessableResponse.body}")
        PaymentAllocationsError
      case JsSuccess(success, _) =>
        success match {
          case error: HipResponseErrorsObject if notFoundCodes.contains(error.errors.code) =>
            logger.info("Data not found, converting to 404 response")
            PaymentAllocationsNotFound
          case _ =>
            logger.error(s"${unprocessableResponse.status} returned from HIP with body: ${unprocessableResponse.body}")
            PaymentAllocationsError
        }
    }
  }


}