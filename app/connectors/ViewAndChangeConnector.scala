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
import connectors.hip.HipConnectorDataHelper
import connectors.hip.httpParsers.ChargeHipHttpParser.HttpGetResult
import connectors.httpParsers.ClaimToAdjustPoaHttpParser.*
import connectors.httpParsers.PaymentAllocationsHttpParser.{PaymentAllocationsReads, PaymentAllocationsResponse}
import connectors.httpParsers.ViewAndChangeHttpParser.{ViewAndChangeJsonResponse, given}
import connectors.httpParsers.OutStandingChargesHttpParser.{OutStandingChargeResponse, OutStandingChargesReads}
import models.claimToAdjustPoa.ClaimToAdjustPoaRequest
import models.claimToAdjustPoa.ClaimToAdjustPoaResponse.{ClaimToAdjustPoaResponse, ErrorResponse}
import models.hip.chargeHistory.{ChargeHistoryError, ChargeHistoryNotFound, ChargeHistoryResponseError, ChargeHistorySuccessWrapper}
import models.hip.repayments.SuccessfulRepaymentResponse
import models.hip.{GetChargeHistoryHipApi, HipResponseErrorsObject}
import play.api.{Logger, Logging}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.ws.writeableOf_JsValue
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewAndChangeConnector @Inject()( val appConfig: MicroserviceAppConfig,
                                       val http: HttpClientV2)
                                      ( implicit ec: ExecutionContext )extends Logging with HipConnectorDataHelper {
  def listOutStandingChargesUrl(idType: String, idNumber: String, taxYearEndDate: String): String =
    s"${appConfig.viewAndChangeBaseUrl}/income-tax/charges/outstanding/$idType/$idNumber/$taxYearEndDate"

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

  def listOutStandingCharges(idType: String, idNumber: String, taxYearEndDate: String)
                            (implicit headerCarrier: HeaderCarrier): Future[OutStandingChargeResponse] = {
    val url = listOutStandingChargesUrl(idType, idNumber, taxYearEndDate)

    http
      .get(url"$url")
      .execute[OutStandingChargeResponse](OutStandingChargesReads, ec)
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

  def getChargeHistoryDetailsUrl(idType: String, idValue: String, chargeReference: String): String = {
    s"${appConfig.viewAndChangeBaseUrl}/etmp/RESTAdapter/ITSA/TaxPayer/GetChargeHistory?idType=$idType&idValue=$idValue&chargeReference=$chargeReference"
  }

  def getHeaders: Seq[(String, String)] = appConfig.getHIPHeaders(GetChargeHistoryHipApi, Some(xMessageTypeFor5705))


  def getChargeHistory(idValue: String, chargeReference: String)
                      (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Either[ChargeHistoryResponseError, ChargeHistorySuccessWrapper]] = {

    val url = getChargeHistoryDetailsUrl("NINO", idValue, chargeReference)

    http
      .get(url"$url")
      .setHeader(getHeaders: _*)
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case OK =>
              logger.debug(s"RESPONSE status:${response.status}, body:${response.body}")
              response.json.validate[ChargeHistorySuccessWrapper].fold(
                invalid => {
                  logger.error(s"Validation Errors: $invalid")
                  Left(ChargeHistoryError(INTERNAL_SERVER_ERROR, "Json validation error parsing ChargeHistorySuccess model"))
                }, {
                  valid =>
                    logger.info("Successfully parsed response to ChargeHistorySuccess model")
                    Right(valid)
                }
              )
            case NOT_FOUND =>
              logger.warn(s" RESPONSE status: ${response.status}, body: ${response.body}")
              Left(ChargeHistoryNotFound(response.status, response.body))
            case UNPROCESSABLE_ENTITY => Left(handleUnprocessableStatusResponse(response))
            case _ =>
              logger.error(s"RESPONSE status: ${response.status}, body: ${response.body}")
              Left(ChargeHistoryError(response.status, response.body))
          }
      } recover {
      case ex =>
        logger.error(s"Unexpected failed future, ${ex.getMessage}")
        Left(ChargeHistoryError(INTERNAL_SERVER_ERROR, s"Unexpected failed future, ${ex.getMessage}"))
    }
  }

  private[connectors] def paymentAllocationsUrl(nino: String,paymentLot: String, paymentLotItem: String): String = {
    s"${appConfig.viewAndChangeBaseUrl}/$nino/payment-allocations/$paymentLot/$paymentLotItem"
  }

  def getPaymentAllocations(nino: String, paymentLot: String, paymentLotItem: String)
                           (implicit hc: HeaderCarrier): Future[PaymentAllocationsResponse] =
    http
      .get(url"${paymentAllocationsUrl(nino,paymentLot,paymentLotItem)}")
      .execute[PaymentAllocationsResponse](PaymentAllocationsReads, ec)

   def handleUnprocessableStatusResponse(unprocessableResponse: HttpResponse): ChargeHistoryResponseError = {
    val notFoundCodes = Set("005", "014")
    unprocessableResponse.json.validate[HipResponseErrorsObject] match {
      case JsError(errors) =>
        logger.error("Unable to parse response as Business Validation Error - " + errors)
        logger.error(s"${unprocessableResponse.status} returned from HiP with body: ${unprocessableResponse.body}")
        ChargeHistoryError(unprocessableResponse.status, unprocessableResponse.body)
      case JsSuccess(success, _) =>
        success match {
          case error: HipResponseErrorsObject if notFoundCodes.contains(error.errors.code) =>
            logger.info(s"Resource not found code identified, code:${error.errors.code}, converting to 404 response")
            ChargeHistoryNotFound(NOT_FOUND, unprocessableResponse.body)
          case _ =>
            logger.error(s"${unprocessableResponse.status} returned from HiP with body: ${unprocessableResponse.body}")
            ChargeHistoryError(unprocessableResponse.status, unprocessableResponse.body)
        }
    }
  }
  //RepaymentHistoryDetails
  //private def getRepaymentHeaders: Seq[(String, String)] = appConfig.getHIPHeaders(GetRepaymentHistoryDetails)
  
  private def getRepaymentUrl(idValue: String, repaymentRequestNumber: Option[String]): String = {
    repaymentRequestNumber match {
      case Some(value) => s"${appConfig.viewAndChangeBaseUrl}/repayments/$idValue/repaymentId/$value"
      case None => s"${appConfig.viewAndChangeBaseUrl}/repayments/$idValue"
    }
  }
  
  def getRepaymentHistoryDetailsList(idValue: String)
                                    (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[SuccessfulRepaymentResponse]] = {
    val url = getRepaymentUrl(idValue, None)
    //logger.debug(s"Calling GET $url \nHeaders: $getRepaymentHeaders")
    http
      .get(url"$url")
      //.setHeader(getHeaders: _*)
      .execute[HttpGetResult[SuccessfulRepaymentResponse]]
  }

  def getRepaymentHistoryDetails(idValue: String, repaymentRequestNumber: String)
                                (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[SuccessfulRepaymentResponse]] = { 
    val url = getRepaymentUrl(idValue, Some(repaymentRequestNumber))
    //logger.debug(s"Calling GET $url \nHeaders: $getRepaymentHeaders")
    http
      .get(url"$url")
      //.setHeader(getHeaders: _*)
      .execute[HttpGetResult[SuccessfulRepaymentResponse]]
  }
}
