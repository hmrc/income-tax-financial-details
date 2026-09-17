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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, Json}

class ChargeClassificationSpec extends AnyWordSpec with Matchers {

  "JSON writes" must {
    "serialize correctly" in {
      ChargeClassification.values.map(cc => (cc, cc.hipValue)).toSeq.foreach { case (obj, name) =>
        Json.toJson(obj) shouldBe JsString(name)
      }
    }
  }
  
  "Json reads" must {
    "deserialize correctly" in {
      ChargeClassification.values.map(cc => (cc.hipValue, cc)).toSeq.foreach { case (name, obj) =>
        JsString(name).as[ChargeClassification] shouldBe obj
      }
    }
  }
}