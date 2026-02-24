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

package connectors.httpParsers

import connectors.httpParsers.ChargeHttpParser.{ChargeResponseError, UnexpectedChargeErrorResponse, UnexpectedChargeResponse}
import play.api.Logging
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object ViewAndChangeHttpParser extends Logging {

  type ViewAndChangeJsonResponse = Either[ChargeResponseError, JsValue]

  implicit object ViewAndChangeReads extends HttpReads[ViewAndChangeJsonResponse] {
    override def read(method: String, url: String, response: HttpResponse): ViewAndChangeJsonResponse = {
      response.status match {
        case OK =>
          response.json.validate[JsValue] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(errors) =>
              logger.error(s"ViewAndChange returned invalid JSON: $errors")
              Left(UnexpectedChargeErrorResponse)
          }

        case status if status >= BAD_REQUEST && status < INTERNAL_SERVER_ERROR =>
          logger.error(s"ViewAndChange returned $status with body: ${response.body}")
          Left(UnexpectedChargeResponse(status, response.body))

        case status =>
          logger.error(s"Unexpected response from ViewAndChange with status: $status and body: ${response.body}")
          Left(UnexpectedChargeErrorResponse)
      }
    }
  }
}
