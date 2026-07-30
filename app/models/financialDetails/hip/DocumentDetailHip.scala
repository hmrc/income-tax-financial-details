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
                              originalAmount: BigDecimal, // renamed from totalAmount
                              outstandingAmount: BigDecimal, // renamed from documentOutstandingAmount
                              poaRelevantAmount: Option[BigDecimal] = None,
                              lastClearingDate: Option[LocalDate] = None,
                              lastClearingReason: Option[String] = None,
                              lastClearedAmount: Option[BigDecimal] = None,
                              statisticalFlag: String,
                              informationCode: Option[String] = None, // not needed or in spec
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
                              documentNumberReducedCharge: Option[String] = None,
                              chargeTypeReducedCharge: Option[String] = None,
                              amendmentDateReducedCharge: Option[LocalDate] = None,
                              taxYearReducedCharge: Option[String] = None,
                              chargeClassification: Option[String] = None
                            )


object DocumentDetailHip {


  implicit val writes: OWrites[DocumentDetailHip] = OWrites { model =>
    Json
      .obj(
        "taxYear" -> model.taxYear.toString,
        "documentID" -> model.transactionId,
        "formBundleNumber" -> model.formBundleNumber,
        "creditReason" -> model.creditReason,
        "documentDate" -> model.documentDate,
        "documentText" -> model.documentText,
        "documentDueDate" -> model.documentDueDate,
        "documentDescription" -> model.documentDescription,
        "totalAmount" -> model.originalAmount,
        "documentOutstandingAmount" -> model.outstandingAmount,
        "poaRelevantAmount" -> model.poaRelevantAmount,
        "lastClearingDate" -> model.lastClearingDate,
        "lastClearingReason" -> model.lastClearingReason,
        "lastClearedAmount" -> model.lastClearedAmount,
        "statisticalFlag" -> model.statisticalFlag,
        "informationCode" -> model.informationCode,
        "paymentLot" -> model.paymentLot,
        "paymentLotItem" -> model.paymentLotItem,
        "effectiveDateOfPayment" -> model.effectiveDateOfPayment,
        "accruingInterestAmount" -> model.accruingInterestAmount,
        "interestRate" -> model.interestRate,
        "interestFromDate" -> model.interestFromDate,
        "interestEndDate" -> model.interestEndDate,
        "latePaymentInterestID" -> model.latePaymentInterestId,
        "latePaymentInterestAmount" -> model.latePaymentInterestAmount,
        "lpiWithDunningLock" -> model.lpiWithDunningLock,
        "interestOutstandingAmount" -> model.interestOutstandingAmount,
        "amountCodedOut" -> model.amountCodedOut,
        "documentNumberReducedCharge" -> model.documentNumberReducedCharge,
        "chargeTypeReducedCharge" -> model.chargeTypeReducedCharge,
        "amendmentDateReducedCharge" -> model.amendmentDateReducedCharge,
        "taxYearReducedCharge" -> model.taxYearReducedCharge,
        "chargeClassification" -> model.chargeClassification
      )
      .fields
      .collect {
        case (key, value) if value != JsNull =>
          key -> value
      }
      .foldLeft(Json.obj()) {
        case (json, (key, value)) =>
          json + (key -> value)
      }
  }

  // scala 3 may not have the 22 parameter limit that was in scala 2, however not all libs support the new limit yet, so we will keep this as a Reads implementation for now.
  implicit val reads: Reads[DocumentDetailHip] = Reads { json =>
    for {
      taxYear <- (json \ "taxYear")
        .validate[String]
        .flatMap { value =>
          value.toIntOption match {
            case Some(year) => JsSuccess(year)
            case None => JsError("error.expected.validTaxYear")
          }
        }
      transactionId <- (json \ "documentID").validate[String]
      formBundleNumber <- (json \ "formBundleNumber").validateOpt[String]
      creditReason <- (json \ "creditReason").validateOpt[String]
      documentDate <- (json \ "documentDate").validate[LocalDate]
      documentText <- (json \ "documentText").validateOpt[String]
      documentDueDate <- (json \ "documentDueDate").validateOpt[LocalDate]
      documentDescription <- (json \ "documentDescription").validateOpt[String]
      originalAmount <- (json \ "totalAmount").validate[BigDecimal]
      outstandingAmount <- (json \ "documentOutstandingAmount").validate[BigDecimal]
      poaRelevantAmount <- (json \ "poaRelevantAmount").validateOpt[BigDecimal]
      lastClearingDate <- (json \ "lastClearingDate").validateOpt[LocalDate]
      lastClearingReason <- (json \ "lastClearingReason").validateOpt[String]
      lastClearedAmount <- (json \ "lastClearedAmount").validateOpt[BigDecimal]
      statisticalFlag <- (json \ "statisticalFlag").validate[String]
      informationCode <- (json \ "informationCode").validateOpt[String]
      paymentLot <- (json \ "paymentLot").validateOpt[String]
      paymentLotItem <- (json \ "paymentLotItem").validateOpt[String]
      effectiveDateOfPayment <- (json \ "effectiveDateOfPayment").validateOpt[LocalDate]
      accruingInterestAmount <- (json \ "accruingInterestAmount").validateOpt[BigDecimal]
      interestRate <- (json \ "interestRate").validateOpt[BigDecimal]
      interestFromDate <- (json \ "interestFromDate").validateOpt[LocalDate]
      interestEndDate <- (json \ "interestEndDate").validateOpt[LocalDate]
      latePaymentInterestId <- (json \ "latePaymentInterestID").validateOpt[String]
      latePaymentInterestAmount <- (json \ "latePaymentInterestAmount").validateOpt[BigDecimal]
      lpiWithDunningLock <- {
        val oldField = (json \ "lpiWithDunningBlock").validateOpt[BigDecimal]
        val newField = (json \ "lpiWithDunningLock").validateOpt[BigDecimal]
        oldField.flatMap {
          case value@Some(_) => JsSuccess(value)
          case None => newField
        }
      }

      interestOutstandingAmount <- (json \ "interestOutstandingAmount").validateOpt[BigDecimal]
      amountCodedOut <- (json \ "amountCodedOut").validateOpt[BigDecimal]
      documentNumberReducedCharge <- (json \ "documentNumberReducedCharge").validateOpt[String]
      chargeTypeReducedCharge <- (json \ "chargeTypeReducedCharge").validateOpt[String]
      amendmentDateReducedCharge <- (json \ "amendmentDateReducedCharge").validateOpt[LocalDate]
      taxYearReducedCharge <- (json \ "taxYearReducedCharge").validateOpt[String]
      chargeClassification <- (json \ "chargeClassification").validateOpt[String]
    } yield DocumentDetailHip(
      taxYear = taxYear,
      transactionId = transactionId,
      formBundleNumber = formBundleNumber,
      creditReason = creditReason,
      documentDate = documentDate,
      documentText = documentText,
      documentDueDate = documentDueDate,
      documentDescription = documentDescription,
      originalAmount = originalAmount,
      outstandingAmount = outstandingAmount,
      poaRelevantAmount = poaRelevantAmount,
      lastClearingDate = lastClearingDate,
      lastClearingReason = lastClearingReason,
      lastClearedAmount = lastClearedAmount,
      statisticalFlag = statisticalFlag,
      informationCode = informationCode,
      paymentLot = paymentLot,
      paymentLotItem = paymentLotItem,
      effectiveDateOfPayment = effectiveDateOfPayment,
      accruingInterestAmount = accruingInterestAmount,
      interestRate = interestRate,
      interestFromDate = interestFromDate,
      interestEndDate = interestEndDate,
      latePaymentInterestId = latePaymentInterestId,
      latePaymentInterestAmount = latePaymentInterestAmount,
      lpiWithDunningLock = lpiWithDunningLock,
      interestOutstandingAmount = interestOutstandingAmount,
      amountCodedOut = amountCodedOut,
      documentNumberReducedCharge = documentNumberReducedCharge,
      chargeTypeReducedCharge = chargeTypeReducedCharge,
      amendmentDateReducedCharge = amendmentDateReducedCharge,
      taxYearReducedCharge = taxYearReducedCharge,
      chargeClassification = chargeClassification
    )
  }
}

