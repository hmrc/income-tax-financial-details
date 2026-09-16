package connectors.hip

import helpers.ComponentSpecBase
import models.hip.paymentAllocations.{AllocationDetail, PaymentAllocations, PaymentAllocationsResponseModel, PaymentAllocationsSuccess}
import play.api.libs.json.{JsObject, Json}

import java.time.LocalDate

class HipPaymentAllocationsConnectorISpec extends ComponentSpecBase {

  val connector: HipPaymentAllocationsConnector = app.injector.instanceOf[HipPaymentAllocationsConnector]

  val nino: String = "AA123456A"
  val paymentLot: String = "1234567890"
  val paymentLotItem: String = "0001"

  val url: String = s"/etmp/RESTAdapter/payment-allocation/NINO/$nino/ITSA?paymentLot=$paymentLot&paymentLotItem=$paymentLotItem"

  val paymentAllocationsResponseModel: PaymentAllocationsResponseModel = PaymentAllocationsResponseModel(
    success = PaymentAllocationsSuccess(
      paymentDetails = Seq(PaymentAllocations(
        amount = Some(12345.45),
        method = Some("B"),
        reference = Some("2222"),
        transactionDate = Some(LocalDate.of(2024, 5, 12)),
        allocations = Seq(AllocationDetail(
          transactionId = Some("1234567890"),
          from = Some(LocalDate.of(2024, 5, 10)),
          to = Some(LocalDate.of(2025, 5, 30)),
          chargeType = Some("1481"),
          mainType = Some("4915"),
          amount = Some(3892.90),
          clearedAmount = Some(1511.13),
          chargeReference = Some("1234567890")
        ))
      ))
    )
  )

  val paymentAllocationsResponseModelApi: JsObject = Json.obj(
    "success" -> Json.obj(
      "paymentDetails" -> Json.arr(
        "paymentAmount" -> 12345.45,
        "paymentMethod" -> "B",
        "paymentReference" -> "2222",
        "valueDate" -> "2024-05-12",
        "sapClearingDocsDetails" -> Json.arr(
          Json.obj(
            "sapDocNumber" -> "1234567890",
            "taxPeriodStartDate" -> "2024-05-10",
            "taxPeriodEndDate" -> "2025-05-30",
            "chargeType" -> "1481",
            "mainType" -> "4915",
            "amount" -> 3892.90,
            "clearedAmount" -> 1511.13,
            "chargeReference" -> "1234567890"
          )
        )
      )
    )
  )
}
