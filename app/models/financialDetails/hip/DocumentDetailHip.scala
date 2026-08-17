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

package models.financialDetails.hip

import play.api.libs.json.*

import java.time.LocalDate

case class DocumentDetailHip(
                              taxYear: Int,
                              transactionId: String,
                              formBundleNumber: Option[String] = None,
                              creditReason: Option[String] = None,
                              documentDate: LocalDate,
                              documentText: Option[String] = None,
                              documentDueDate: Option[LocalDate] = None,
                              documentDescription: Option[String] = None,
                              originalAmount: BigDecimal,
                              outstandingAmount: BigDecimal,
                              poaRelevantAmount: Option[BigDecimal] = None,
                              lastClearedAmount: Option[BigDecimal] = None,
                              statisticalFlag: String,
                              informationCode: Option[String] = None,
                              paymentLot: Option[String] = None,
                              paymentLotItem: Option[String] = None,
                              effectiveDateOfPayment: Option[LocalDate] = None,
                              accruingInterestAmount: Option[BigDecimal] = None,
                              interestRate: Option[BigDecimal] = None,
                              interestFromDate: Option[LocalDate] = None,
                              interestEndDate: Option[LocalDate] = None,
                              latePaymentInterestId: Option[String] = None,
                              latePaymentInterestAmount: Option[BigDecimal] = None,
                              lpiWithDunningLock: Option[BigDecimal] = None,
                              interestOutstandingAmount: Option[BigDecimal] = None,
                              amountCodedOut: Option[BigDecimal] = None,
                              chargeClassification: Option[String] = None
                            )



object DocumentDetailHip {

  private val taxYearReads: Reads[Int] = Reads {
    case JsString(s) => JsSuccess(s.toInt)
    case n: JsNumber => JsSuccess(n.value.toInt)
    case _ => JsError("error.expected.jsnumberorjsstring")
  }

  private def firstOf[A](primary: JsResult[A], secondary: => JsResult[A]): JsResult[A] =
    primary.fold(_ => secondary, value => JsSuccess(value))

  private val normalise: Reads[JsObject] = Reads { json =>
    json.validate[JsObject].flatMap { obj =>
      val lpiWithDunningLock =
        (obj \ "lpiWithDunningBlock").asOpt[BigDecimal]
          .orElse((obj \ "lpiWithDunningLock").asOpt[BigDecimal])

      for {
        taxYear           <- (obj \ "taxYear").validate[Int](taxYearReads)
        transactionId     <- firstOf((obj \ "transactionId").validate[String], (obj \ "documentID").validate[String])
        originalAmount    <- firstOf((obj \ "originalAmount").validate[BigDecimal], (obj \ "totalAmount").validate[BigDecimal])
        outstandingAmount <- firstOf((obj \ "outstandingAmount").validate[BigDecimal], (obj \ "documentOutstandingAmount").validate[BigDecimal])
      } yield {
        val renamed: Seq[(String, JsValue)] =
          Seq(
            "taxYear"           -> Json.toJson(taxYear),
            "transactionId"     -> Json.toJson(transactionId),
            "originalAmount"    -> Json.toJson(originalAmount),
            "outstandingAmount" -> Json.toJson(outstandingAmount)
          ) ++
            (obj \ "latePaymentInterestID").asOpt[String]
              .map(v => "latePaymentInterestId" -> Json.toJson(v)).toSeq ++
            lpiWithDunningLock
              .map(v => "lpiWithDunningLock" -> Json.toJson(v)).toSeq

        obj ++ JsObject(renamed)
      }
    }
  }

  private val macroReads: Reads[DocumentDetailHip] = Json.reads[DocumentDetailHip]

  implicit val reads: Reads[DocumentDetailHip] = normalise.andThen(macroReads)

  private val macroWrites: OWrites[DocumentDetailHip] = Json.writes[DocumentDetailHip]

  implicit val writes: OWrites[DocumentDetailHip] = OWrites { d =>
    JsObject(macroWrites.writes(d).fields.filterNot { case (_, value) => value == JsNull })
  }

}

