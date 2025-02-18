package com.judokit.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Configuration object to change some UI parameters on card entry and payment methods screens
 */
@Parcelize
class UiConfiguration internal constructor(
    val avsEnabled: Boolean,
    val shouldPaymentMethodsDisplayAmount: Boolean,
    val shouldPaymentButtonDisplayAmount: Boolean,
    val shouldPaymentMethodsVerifySecurityCode: Boolean
) : Parcelable {

    /**
     * Builder class for creating an instance of [UiConfiguration]
     */
    class Builder {
        private var avsEnabled: Boolean? = false
        private var shouldPaymentMethodsDisplayAmount: Boolean? = true
        private var shouldPaymentMethodsVerifySecurityCode: Boolean? = true
        private var shouldPaymentButtonDisplayAmount: Boolean? = false

        /**
         * Sets additional fields in card entry screen to provide address verification data,
         * such as country and postal code.
         */
        fun setAvsEnabled(enabled: Boolean?) = apply { this.avsEnabled = enabled }

        /**
         * Sets A boolean property that defines if the amount should be displayed on the payment
         * method screen.
         */
        fun setShouldPaymentMethodsDisplayAmount(shouldPaymentMethodsDisplayAmount: Boolean?) =
            apply { this.shouldPaymentMethodsDisplayAmount = shouldPaymentMethodsDisplayAmount }

        /**
         * Sets a boolean property that defines if the SDK should verify security code when paying
         * with a card in the payment methods screen.
         */
        fun setShouldPaymentMethodsVerifySecurityCode(shouldPaymentMethodsVerifySecurityCode: Boolean?) =
            apply {
                this.shouldPaymentMethodsVerifySecurityCode = shouldPaymentMethodsVerifySecurityCode
            }

        /**
         * Sets boolean property that defines if the amount should be displayed on the
         * transaction screen.
         */
        fun setShouldPaymentButtonDisplayAmount(shouldPaymentButtonDisplayAmount: Boolean?) =
            apply { this.shouldPaymentButtonDisplayAmount = shouldPaymentButtonDisplayAmount }

        /**
         * Creates an instance of [UiConfiguration] based on provided data in setters.
         * @throws IllegalArgumentException If avsEnabled, shouldPaymentMethodsDisplayAmount, shouldPaymentMethodsVerifySecurityCode or shouldPaymentButtonDisplayAmount is null.
         */
        fun build(): UiConfiguration {
            val avsEnabled = requireNotNull(this.avsEnabled)
            val shouldPaymentMethodsDisplayAmount =
                requireNotNull(this.shouldPaymentMethodsDisplayAmount)
            val shouldPaymentMethodsVerifySecurityCode =
                requireNotNull(this.shouldPaymentMethodsVerifySecurityCode)
            val shouldPaymentButtonDisplayAmount =
                requireNotNull(this.shouldPaymentButtonDisplayAmount)

            return UiConfiguration(
                avsEnabled,
                shouldPaymentMethodsDisplayAmount,
                shouldPaymentButtonDisplayAmount,
                shouldPaymentMethodsVerifySecurityCode
            )
        }
    }

    override fun toString(): String {
        return "UiConfiguration(avsEnabled=$avsEnabled, shouldDisplayAmount=$shouldPaymentMethodsDisplayAmount, shouldEnterSecurityCode=$shouldPaymentMethodsVerifySecurityCode)"
    }
}
