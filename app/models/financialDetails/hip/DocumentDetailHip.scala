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

// TODO: enable disabled field after migration to Scala 3 => MISUV-7996
case class DocumentDetailHip(
                              /* Format: YYYY */
                              taxYear: Int,
                              /* SAP document number or Form Bundle Number for zero amount documents */
                              transactionId: String,
                              /* If the document was created using the Form Bundle, the FB Number is provided */
                              //formBundleNumber: Option[String] = None,
                              /* Gives the reason as to why there is a credit on the account.  */
                              //creditReason: Option[String] = None,
                              documentDate: LocalDate,
                              /* Document Text */
                              documentText: Option[String] = None,
                              documentDueDate: Option[LocalDate] = None,
                              /* Document descriptiom */
                              documentDescription: Option[String] = None,
                              /* Currency amount. 13-digits total with 2 decimal places */
                              originalAmount: BigDecimal, // renamed from totalAmount
                              /* Currency amount. 13-digits total with 2 decimal places */
                              outstandingAmount: BigDecimal, // renamed from documentOutstandingAmount
                              /* Currency amount. 13-digits total with 2 decimal places */
                              poaRelevantAmount: Option[BigDecimal] = None,
                              //lastClearingDate: Option[LocalDate] = None,
                              /* Last clearing reason */
                              //lastClearingReason: Option[String] = None,
                              /* Currency amount. 13-digits total with 2 decimal places */
                              lastClearedAmount: Option[BigDecimal] = None,
                              /* Y for Statistical. N for not */
                              //statisticalFlag: String,
                              /* Identifies a charge that has multiple items associated e.g. there are two BCD items due to coding occurring */
                              //  informationCode: Option[String] = None,
                              //  /* Payment Lot */
                              paymentLot: Option[String] = None,
                              /* Payment Lot Item */
                              paymentLotItem: Option[String] = None,
                              effectiveDateOfPayment: Option[LocalDate] = None,
                              /* Currency amount. 13-digits total with 2 decimal places */
                              accruingInterestAmount: Option[BigDecimal] = None,
                              /* Currency amount. 13-digits total with 2 decimal places */
                              interestRate: Option[BigDecimal] = None,
                              interestFromDate: Option[LocalDate] = None,
                              interestEndDate: Option[LocalDate] = None,
                              /* Late Payment Interets Id */
                              latePaymentInterestId: Option[String] = None,
                              /* Currency amount. 13-digits total with 2 decimal places */
                              latePaymentInterestAmount: Option[BigDecimal] = None,
                              /* Currency amount. 13-digits total with 2 decimal places */
                              lpiWithDunningLock: Option[BigDecimal] = None,
                              /* Currency amount. 13-digits total with 2 decimal places */
                              interestOutstandingAmount: Option[BigDecimal] = None,
                              /* Currency amount. 13-digits total with 2 decimal places */
                              amountCodedOut: Option[BigDecimal] = None,
                              chargeClassification: Option[String] = None
                              /* If Charge has been reduced, and credit arises, document number to be shown */
                              //documentNumberReducedCharge: Option[String] = None,
                              /* Document name of charge reduced */
                              //chargeTypeReducedCharge: Option[String] = None,
                              //amendmentDateReducedCharge: Option[LocalDate] = None,
                              /* Format: YYYY */
                              //taxYearReducedCharge: Option[String] = None
                            )



object DocumentDetailHip {
  private val normalise: Reads[JsObject] = Reads { json =>
    json.validate[JsObject].map { obj =>
      val lpiWithDunningLock =
        (obj \ "lpiWithDunningBlock").asOpt[BigDecimal]
          .orElse((obj \ "lpiWithDunningLock").asOpt[BigDecimal])

      val renamed: Seq[(String, JsValue)] =
        Seq(
          "taxYear"           -> Json.toJson((obj \ "taxYear").as[String].toInt), // <= RT conversion applied
          "transactionId"     -> Json.toJson((obj \ "documentID").as[String]),
          "originalAmount"    -> Json.toJson((obj \ "totalAmount").as[BigDecimal]),
          "outstandingAmount" -> Json.toJson((obj \ "documentOutstandingAmount").as[BigDecimal])
        ) ++
          (obj \ "latePaymentInterestID").asOpt[String]
            .map(v => "latePaymentInterestId" -> Json.toJson(v)).toSeq ++
          lpiWithDunningLock
            .map(v => "lpiWithDunningLock" -> Json.toJson(v)).toSeq

      obj ++ JsObject(renamed)
    }
  }

  private val macroReads: Reads[DocumentDetailHip] = Json.reads[DocumentDetailHip]

  implicit val reads: Reads[DocumentDetailHip] = normalise.andThen(macroReads)

  implicit val writes: Writes[DocumentDetailHip] = (d: DocumentDetailHip) => {
    val required = Json.obj(
      "taxYear"           -> d.taxYear,
      "transactionId"     -> d.transactionId,
      "documentDate"      -> d.documentDate,
      "originalAmount"    -> d.originalAmount,
      "outstandingAmount" -> d.outstandingAmount
    )

    val optionalFields: Seq[(String, JsValue)] = Seq(
      d.documentText.map("documentText" -> Json.toJson(_)),
      d.documentDueDate.map("documentDueDate" -> Json.toJson(_)),
      d.documentDescription.map("documentDescription" -> Json.toJson(_)),
      d.poaRelevantAmount.map("poaRelevantAmount" -> Json.toJson(_)),
      d.lastClearedAmount.map("lastClearedAmount" -> Json.toJson(_)),
      d.paymentLot.map("paymentLot" -> Json.toJson(_)),
      d.paymentLotItem.map("paymentLotItem" -> Json.toJson(_)),
      d.effectiveDateOfPayment.map("effectiveDateOfPayment" -> Json.toJson(_)),
      d.accruingInterestAmount.map("accruingInterestAmount" -> Json.toJson(_)),
      d.interestRate.map("interestRate" -> Json.toJson(_)),
      d.interestFromDate.map("interestFromDate" -> Json.toJson(_)),
      d.interestEndDate.map("interestEndDate" -> Json.toJson(_)),
      d.latePaymentInterestId.map("latePaymentInterestId" -> Json.toJson(_)),
      d.latePaymentInterestAmount.map("latePaymentInterestAmount" -> Json.toJson(_)),
      d.lpiWithDunningLock.map("lpiWithDunningLock" -> Json.toJson(_)),
      d.interestOutstandingAmount.map("interestOutstandingAmount" -> Json.toJson(_)),
      d.amountCodedOut.map("amountCodedOut" -> Json.toJson(_)),
      d.chargeClassification.map("chargeClassification" -> Json.toJson(_))
    ).flatten

    required ++ JsObject(optionalFields)
  }
}

