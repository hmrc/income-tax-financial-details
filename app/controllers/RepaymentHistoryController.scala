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

package controllers


import services.RepaymentHistoryDetailsService
import controllers.predicates.AuthenticationPredicate
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RepaymentHistoryController @Inject()(authentication: AuthenticationPredicate,
                                           cc: ControllerComponents,
                                           repaymentHistoryDetailsService: RepaymentHistoryDetailsService
                                          )
                                          (implicit ec: ExecutionContext) extends BackendController(cc) {


  def getAllRepaymentHistory(nino: String): Action[AnyContent] = {
    authentication.async { implicit request =>
      {
        repaymentHistoryDetailsService.getRepaymentHistoryDetailsList(nino) map {
          case Right(repaymentHistory) => Ok(Json.toJson(repaymentHistory))
          case Left(error) =>
            if (error.status >= 400 && error.status < 500 ){
              Status(error.status)(Json.stringify(error.jsonError))
            }else {
              InternalServerError(Json.stringify(error.jsonError))
            }
        }
      }
    }
  }

  def getRepaymentHistoryById(nino: String, repaymentId: String): Action[AnyContent] =
    authentication.async { implicit request =>
      {
        repaymentHistoryDetailsService.getRepaymentHistoryDetails(nino, repaymentId).map {
            case Right(repaymentHistory) => Ok(Json.toJson(repaymentHistory))
            case Left(error) =>
              if (error.status >= 400 && error.status < 500 ){
                Status(error.status)(Json.stringify(error.jsonError))
              }else {
                InternalServerError(Json.stringify(error.jsonError))
              }
          }
      }
    }
}
