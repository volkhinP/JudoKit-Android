package com.judokit.android.api.model.request

import com.judokit.android.model.PrimaryAccountDetails
import com.judokit.android.requireNotNull
import com.judokit.android.requireNotNullOrEmpty

class TokenRequest private constructor(
    private var uniqueRequest: Boolean?,
    private var yourPaymentReference: String?,
    private var amount: String?,
    private var currency: String?,
    private var judoId: String?,
    private var yourConsumerReference: String?,
    private var yourPaymentMetaData: Map<String, String>?,
    private var endDate: String?,
    private var cardLastFour: String?,
    private var cardToken: String,
    private var cardType: Int,
    private var cv2: String?,
    private var cardAddress: Address?,
    private var emailAddress: String?,
    private var mobileNumber: String?,
    private var primaryAccountDetails: PrimaryAccountDetails?,
    private var initialRecurringPayment: Boolean?
) {
    class Builder {
        private var uniqueRequest: Boolean? = false
        private var yourPaymentReference: String? = null
        private var amount: String? = null
        private var currency: String? = null
        private var judoId: String? = null
        private var yourConsumerReference: String? = null
        private var yourPaymentMetaData: Map<String, String>? = null
        private var endDate: String? = null
        private var cardLastFour: String? = null
        private var cardToken: String? = null
        private var cardType: Int = 0
        private var cv2: String? = null
        private var address: Address? = null
        private var emailAddress: String? = null
        private var mobileNumber: String? = null
        private var primaryAccountDetails: PrimaryAccountDetails? = null
        private var initialRecurringPayment: Boolean? = null

        fun setUniqueRequest(uniqueRequest: Boolean?) = apply { this.uniqueRequest = uniqueRequest }

        fun setYourPaymentReference(yourPaymentReference: String?) =
            apply { this.yourPaymentReference = yourPaymentReference }

        fun setAmount(amount: String?) = apply { this.amount = amount }

        fun setCurrency(currency: String?) = apply { this.currency = currency }

        fun setJudoId(judoId: String?) = apply { this.judoId = judoId }

        fun setYourConsumerReference(yourConsumerReference: String?) =
            apply { this.yourConsumerReference = yourConsumerReference }

        fun setYourPaymentMetaData(yourPaymentMetaData: Map<String, String>?) =
            apply { this.yourPaymentMetaData = yourPaymentMetaData }

        fun setEndDate(endDate: String?) = apply { this.endDate = endDate }

        fun setCardLastFour(cardLastFour: String?) = apply { this.cardLastFour = cardLastFour }

        fun setCardToken(cardToken: String?) = apply { this.cardToken = cardToken }

        fun setCardType(cardType: Int) = apply { this.cardType = cardType }

        fun setCv2(cv2: String?) = apply { this.cv2 = cv2 }

        fun setAddress(address: Address?) = apply { this.address = address }

        fun setEmailAddress(emailAddress: String?) = apply { this.emailAddress = emailAddress }

        fun setMobileNumber(mobileNumber: String?) = apply { this.mobileNumber = mobileNumber }

        fun setPrimaryAccountDetails(primaryAccountDetails: PrimaryAccountDetails?) =
            apply { this.primaryAccountDetails = primaryAccountDetails }

        fun setInitialRecurringPayment(initialRecurringPayment: Boolean?) =
            apply { this.initialRecurringPayment = initialRecurringPayment }

        fun build(): TokenRequest {
            val id = requireNotNullOrEmpty(judoId, "judoId")
            val myAmount = requireNotNullOrEmpty(amount, "amount")
            val myCurrency = requireNotNullOrEmpty(currency, "currency")
            val consumerReference =
                requireNotNullOrEmpty(yourConsumerReference, "yourConsumerReference")
            val paymentReference =
                requireNotNullOrEmpty(yourPaymentReference, "yourPaymentReference")
            val myCardToken = requireNotNullOrEmpty(cardToken, "cardToken")
            val myAddress = requireNotNull(address, "address")

            return TokenRequest(
                uniqueRequest,
                paymentReference,
                myAmount,
                myCurrency,
                id,
                consumerReference,
                yourPaymentMetaData,
                endDate,
                cardLastFour,
                myCardToken,
                cardType,
                cv2,
                myAddress,
                emailAddress,
                mobileNumber,
                primaryAccountDetails,
                initialRecurringPayment
            )
        }
    }
}
