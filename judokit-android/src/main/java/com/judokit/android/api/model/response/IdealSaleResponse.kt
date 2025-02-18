package com.judokit.android.api.model.response

import java.math.BigDecimal

data class IdealSaleResponse(
    val orderId: String,
    val paymentMethod: String,
    val status: String,
    val merchantPaymentReference: String,
    val currency: String,
    val amount: BigDecimal,
    val consumer: BankConsumer,
    val judoId: String,
    val merchantSiteName: String,
    val redirectUrl: String?,
    val merchantRedirectUrl: String?
)
