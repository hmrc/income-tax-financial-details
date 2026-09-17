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

package models.hip.chargeHistory

import play.api.libs.json.*

enum ChargeClassification(val hipValue: String) {
  case `Auto Correction` extends ChargeClassification("AC")
  case `Manual Correction` extends ChargeClassification("MC")
  case `Rejected Correction` extends ChargeClassification("RC")
  case `Revenue Amendments` extends ChargeClassification("RA")
  case `Annual Financial Adjustment` extends ChargeClassification("AF")
}

object ChargeClassification {
  private val hipToChargeClassification: Map[String, ChargeClassification] = ChargeClassification.values.map(cc => cc.hipValue -> cc).toMap

  given reads: Reads[ChargeClassification] = Reads {
    case JsString(value) =>
      hipToChargeClassification.get(value) match {
        case Some(chargeClassification) => JsSuccess(chargeClassification)
        case None => JsError(s"Unknown ChargeClassification value: $value")
      }
    case _ => JsError("Expected a string value for ChargeClassification")
  }
  
  given writes: Writes[ChargeClassification] = Writes { chargeClassification =>
    JsString(chargeClassification.hipValue)
  }
}

