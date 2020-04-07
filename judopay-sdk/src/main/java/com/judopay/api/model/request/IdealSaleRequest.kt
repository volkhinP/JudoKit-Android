package com.judopay.api.model.request

import com.judopay.model.Currency
import com.judopay.requireNotNull
import com.judopay.requireNotNullOrEmpty
import java.math.BigDecimal

data class IdealSaleRequest(
    val amount: BigDecimal,
    val merchantPaymentReference: String,
    val paymentMetadata: Map<String, String>?,
    val merchantConsumerReference: String,
    val siteId: String,
    val bic: String,
    val currency: String = Currency.EUR.name,
    val country: String = "NL",
    val paymentMethod: String = "IDEAL",
    val accountHolderName: String = "iDEAL User"
) {
    class Builder {
        private var amount: BigDecimal? = null
        private var merchantPaymentReference: String? = null
        private var paymentMetadata: Map<String, String>? = null
        private var merchantConsumerReference: String? = null
        private var siteId: String? = null
        private var bic: String? = null

        fun setAmount(amount: BigDecimal?) = apply { this.amount = amount }

        fun setMerchantPaymentReference(merchantPaymentReference: String?) =
            apply { this.merchantPaymentReference = merchantPaymentReference }

        fun setPaymentMetadata(paymentMetadata: Map<String, String>?) =
            apply { this.paymentMetadata = paymentMetadata }

        fun setMerchantConsumerReference(merchantConsumerReference: String?) =
            apply { this.merchantConsumerReference = merchantConsumerReference }

        fun setSiteId(siteId: String?) = apply { this.siteId = siteId }

        fun setBic(bic: String?) = apply { this.bic = bic }

        fun build(): IdealSaleRequest {
            val myAmount = requireNotNull(amount, "amount")
            val myMerchantPaymentReference =
                requireNotNullOrEmpty(merchantPaymentReference, "merchantPaymentReference")
            val myMerchantConsumerReference =
                requireNotNullOrEmpty(merchantConsumerReference, "merchantConsumerReference")
            val mySiteId = requireNotNullOrEmpty(siteId, "siteId")
            val myBic = requireNotNullOrEmpty(bic, "bic")

            return IdealSaleRequest(
                myAmount,
                myMerchantPaymentReference,
                paymentMetadata,
                myMerchantConsumerReference,
                mySiteId,
                myBic
            )
        }
    }
}
