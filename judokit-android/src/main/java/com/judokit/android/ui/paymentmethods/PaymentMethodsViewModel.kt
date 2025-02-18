package com.judokit.android.ui.paymentmethods

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import androidx.lifecycle.viewModelScope
import com.judokit.android.Judo
import com.judokit.android.R
import com.judokit.android.api.JudoApiService
import com.judokit.android.api.model.request.Address
import com.judokit.android.api.model.request.TokenRequest
import com.judokit.android.api.model.response.CardDate
import com.judokit.android.api.model.response.CardToken
import com.judokit.android.api.model.response.Consumer
import com.judokit.android.api.model.response.JudoApiCallResult
import com.judokit.android.api.model.response.Receipt
import com.judokit.android.db.entity.TokenizedCardEntity
import com.judokit.android.db.repository.TokenizedCardRepository
import com.judokit.android.model.CardNetwork
import com.judokit.android.model.Currency
import com.judokit.android.model.PaymentMethod
import com.judokit.android.model.PaymentWidgetType
import com.judokit.android.model.displayName
import com.judokit.android.model.formatted
import com.judokit.android.model.paymentButtonType
import com.judokit.android.model.typeId
import com.judokit.android.toMap
import com.judokit.android.ui.common.ButtonState
import com.judokit.android.ui.paymentmethods.adapter.model.IdealBank
import com.judokit.android.ui.paymentmethods.adapter.model.IdealBankItem
import com.judokit.android.ui.paymentmethods.adapter.model.PaymentMethodGenericItem
import com.judokit.android.ui.paymentmethods.adapter.model.PaymentMethodItem
import com.judokit.android.ui.paymentmethods.adapter.model.PaymentMethodItemType
import com.judokit.android.ui.paymentmethods.adapter.model.PaymentMethodSavedCardItem
import com.judokit.android.ui.paymentmethods.adapter.model.PaymentMethodSelectorItem
import com.judokit.android.ui.paymentmethods.components.GooglePayCardViewModel
import com.judokit.android.ui.paymentmethods.components.NoPaymentMethodSelectedViewModel
import com.judokit.android.ui.paymentmethods.components.PayByBankCardViewModel
import com.judokit.android.ui.paymentmethods.components.PaymentCallToActionViewModel
import com.judokit.android.ui.paymentmethods.components.PaymentMethodsHeaderViewModel
import com.judokit.android.ui.paymentmethods.model.CardPaymentMethodModel
import com.judokit.android.ui.paymentmethods.model.CardViewModel
import com.judokit.android.ui.paymentmethods.model.Event
import com.judokit.android.ui.paymentmethods.model.GooglePayPaymentMethodModel
import com.judokit.android.ui.paymentmethods.model.IdealPaymentCardViewModel
import com.judokit.android.ui.paymentmethods.model.IdealPaymentMethodModel
import com.judokit.android.ui.paymentmethods.model.PayByBankPaymentMethodModel
import com.judokit.android.ui.paymentmethods.model.PaymentCardViewModel
import com.judokit.android.ui.paymentmethods.model.PaymentMethodModel
import com.zapp.library.merchant.util.PBBAAppUtils
import kotlinx.coroutines.launch
import retrofit2.await
import java.util.Date

// view-model actions
sealed class PaymentMethodsAction {
    data class DeleteCard(val cardId: Int) : PaymentMethodsAction()
    data class SelectPaymentMethod(val method: PaymentMethod) : PaymentMethodsAction()
    data class SelectStoredCard(val id: Int) : PaymentMethodsAction()
    data class UpdateButtonState(val buttonEnabled: Boolean) :
        PaymentMethodsAction()

    data class EditMode(val isInEditMode: Boolean) : PaymentMethodsAction()
    data class SelectIdealBank(val idealBank: IdealBank) : PaymentMethodsAction()

    data class PayWithSelectedStoredCard(val securityCode: String? = null) : PaymentMethodsAction()
    object PayWithSelectedIdealBank : PaymentMethodsAction()
    object PayWithPayByBank : PaymentMethodsAction()
    object Update : PaymentMethodsAction() // TODO: temporary
}

// view-model custom factory to inject the `judo` configuration object
internal class PaymentMethodsViewModelFactory(
    private val cardDate: CardDate,
    private val cardRepository: TokenizedCardRepository,
    private val service: JudoApiService,
    private val application: Application,
    private val judo: Judo
) : NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass == PaymentMethodsViewModel::class.java) {
            PaymentMethodsViewModel(
                cardDate,
                cardRepository,
                service,
                application,
                judo
            ) as T
        } else super.create(modelClass)
    }
}

class PaymentMethodsViewModel(
    private val cardDate: CardDate,
    private val cardRepository: TokenizedCardRepository,
    private val service: JudoApiService,
    application: Application,
    private val judo: Judo
) : AndroidViewModel(application) {

    val model = MutableLiveData<PaymentMethodsModel>()
    val judoApiCallResult = MutableLiveData<JudoApiCallResult<Receipt>>()
    val payWithIdealObserver = MutableLiveData<Event<String>>()
    val payWithPayByBankObserver = MutableLiveData<Event<Nothing>>()
    val selectedCardNetworkObserver = MutableLiveData<Event<CardNetwork>>()

    private val context = application

    val allCardsSync = cardRepository.allCardsSync

    private val selectedPaymentMethod: PaymentMethod
        get() = model.value?.currentPaymentMethod?.type ?: judo.paymentMethods.first()

    private val selectedCardIdentifier: Int
        get() {
            model.value?.let { myModel ->
                val method = myModel.currentPaymentMethod
                if (method is CardPaymentMethodModel) {
                    return method.selectedCard?.id ?: -1
                }
            }
            return -1
        }

    private val selectedBank: IdealBank
        get() {
            model.value?.let { myModel ->
                val method = myModel.currentPaymentMethod
                if (method is IdealPaymentMethodModel) {
                    return method.selectedBank
                }
            }
            return IdealBank.ING_BANK
        }

    init {
        buildModel()
    }

    fun send(action: PaymentMethodsAction) {
        when (action) {
            is PaymentMethodsAction.DeleteCard -> {
                deleteCardWithId(action.cardId)
                buildModel()
            }
            is PaymentMethodsAction.PayWithSelectedStoredCard -> {
                buildModel(isLoading = true)
                val paymentMethod = model.value?.currentPaymentMethod
                payWithSelectedCard(paymentMethod, action.securityCode)
            }
            is PaymentMethodsAction.PayWithSelectedIdealBank -> {
                buildModel(isLoading = true)
                payWithSelectedIdealBank()
            }
            is PaymentMethodsAction.PayWithPayByBank -> {
                buildModel(isLoading = true)
                payWithPayByBankObserver.postValue(Event())
            }
            is PaymentMethodsAction.SelectStoredCard -> {
                buildModel(isLoading = false, selectedCardId = action.id)
            }
            is PaymentMethodsAction.SelectIdealBank -> {
                buildModel(isLoading = false, selectedBank = action.idealBank)
            }
            is PaymentMethodsAction.Update -> buildModel()
            is PaymentMethodsAction.SelectPaymentMethod -> {
                if (selectedPaymentMethod != action.method) buildModel(action.method, false)
            }
            is PaymentMethodsAction.UpdateButtonState -> buildModel(
                isLoading = !action.buttonEnabled
            )
            is PaymentMethodsAction.EditMode -> buildModel(isInEditMode = action.isInEditMode)
        }
    }

    private fun payWithSelectedCard(paymentMethod: PaymentMethodModel?, securityCode: String?) {
        if (paymentMethod is CardPaymentMethodModel) {
            val isSecurityCodeRequired =
                judo.uiConfiguration.shouldPaymentMethodsVerifySecurityCode && securityCode == null
            if (isSecurityCodeRequired) {
                showSecurityCodeDialog(paymentMethod)
            } else {
                sendCardPaymentRequest(paymentMethod, securityCode)
            }
        }
    }

    private fun showSecurityCodeDialog(paymentMethod: CardPaymentMethodModel) {
        val card = paymentMethod.selectedCard
        card?.let {
            selectedCardNetworkObserver.postValue(Event(it.network))
            buildModel(isLoading = false)
        }
    }

    @Throws(IllegalStateException::class)
    private fun sendCardPaymentRequest(
        paymentMethod: CardPaymentMethodModel,
        securityCode: String?
    ) = viewModelScope.launch {
        val card = paymentMethod.selectedCard
        card?.let {
            val entity = cardRepository.findWithId(it.id)
            if (judo.paymentWidgetType == PaymentWidgetType.SERVER_TO_SERVER_PAYMENT_METHODS) {
                buildReceipt(entity)
            } else {
                val request = TokenRequest.Builder()
                    .setAmount(judo.amount.amount)
                    .setCurrency(judo.amount.currency.name)
                    .setJudoId(judo.judoId)
                    .setYourPaymentReference(judo.reference.paymentReference)
                    .setYourConsumerReference(judo.reference.consumerReference)
                    .setYourPaymentMetaData(judo.reference.metaData?.toMap())
                    .setCardLastFour(entity.ending)
                    .setCardToken(entity.token)
                    .setCardType(entity.network.typeId)
                    .setCv2(securityCode)
                    .setAddress(Address.Builder().build())
                    .setInitialRecurringPayment(judo.initialRecurringPayment)
                    .build()

                val response = when (judo.paymentWidgetType) {
                    PaymentWidgetType.PAYMENT_METHODS -> service.tokenPayment(request).await()
                    PaymentWidgetType.PRE_AUTH_PAYMENT_METHODS -> service.preAuthTokenPayment(
                        request
                    ).await()
                    else -> throw IllegalStateException("Unexpected payment widget type: ${judo.paymentWidgetType}")
                }
                cardRepository.updateAllLastUsedToFalse()
                cardRepository.insert(entity.apply { isLastUsed = true })

                buildModel()
                judoApiCallResult.postValue(response)
            }
        }
    }

    private fun payWithSelectedIdealBank() = viewModelScope.launch {
        model.value?.let { methodModel ->
            if (methodModel.currentPaymentMethod is IdealPaymentMethodModel) {
                payWithIdealObserver.postValue(
                    Event(
                        methodModel.currentPaymentMethod.selectedBank.bic
                    )
                )
            }
        }
    }

    private fun deleteCardWithId(id: Int) = viewModelScope.launch {
        cardRepository.deleteCardWithId(id)
    }

    // TODO: needs to be refactored
    private fun buildModel(
        selectedMethod: PaymentMethod = selectedPaymentMethod,
        isLoading: Boolean = false,
        selectedCardId: Int = selectedCardIdentifier,
        isInEditMode: Boolean = false,
        selectedBank: IdealBank = this.selectedBank
    ) = viewModelScope.launch {
        val cardModel: CardViewModel

        val recyclerViewData = mutableListOf<PaymentMethodItem>()
        var allMethods = judo.paymentMethods.toList()
        val cards = allCardsSync.value

        allMethods = filterPaymentMethods(allMethods)

        if (allMethods.size > 1) {
            recyclerViewData.add(
                PaymentMethodSelectorItem(
                    PaymentMethodItemType.SELECTOR,
                    allMethods,
                    selectedMethod
                )
            )
        }

        val method: PaymentMethodModel = when (selectedMethod) {
            PaymentMethod.CARD -> {
                var selectedCard: PaymentMethodSavedCardItem? = null
                if (cards.isNullOrEmpty()) {
                    // placeholder
                    recyclerViewData.add(
                        PaymentMethodGenericItem(
                            PaymentMethodItemType.NO_SAVED_CARDS_PLACEHOLDER,
                            isInEditMode
                        )
                    )
                    cardModel = NoPaymentMethodSelectedViewModel()
                } else {
                    recyclerViewData.add(
                        PaymentMethodGenericItem(
                            PaymentMethodItemType.SAVED_CARDS_HEADER,
                            isInEditMode
                        )
                    )

                    // cards
                    val defaultSelected = cards.map { it.isDefault }.contains(true)
                    val cardItems = cards.map { entity ->
                        entity.toPaymentMethodSavedCardItem().apply {
                            isSelected = when {
                                selectedCardId > -1 -> id == selectedCardId
                                defaultSelected -> entity.isDefault
                                else -> entity.isLastUsed
                            }
                            this.isInEditMode = isInEditMode
                        }
                    }
                    recyclerViewData.addAll(cardItems)

                    // footer
                    recyclerViewData.add(
                        PaymentMethodGenericItem(
                            PaymentMethodItemType.SAVED_CARDS_FOOTER,
                            isInEditMode
                        )
                    )

                    selectedCard = cardItems.firstOrNull { it.isSelected } ?: cardItems.first()
                    cardModel = selectedCard.toPaymentCardViewModel()
                }
                CardPaymentMethodModel(selectedCard = selectedCard, items = recyclerViewData)
            }

            PaymentMethod.GOOGLE_PAY -> {
                cardModel = GooglePayCardViewModel()
                GooglePayPaymentMethodModel(items = recyclerViewData)
            }

            PaymentMethod.PAY_BY_BANK -> {
                cardModel = PayByBankCardViewModel()
                PayByBankPaymentMethodModel(items = recyclerViewData)
            }

            PaymentMethod.IDEAL -> {
                val bankItems = IdealBank.values().map {
                    IdealBankItem(idealBank = it).apply { isSelected = it == selectedBank }
                }

                recyclerViewData.addAll(bankItems)

                cardModel = IdealPaymentCardViewModel(idealBank = selectedBank)

                IdealPaymentMethodModel(selectedBank = selectedBank, items = recyclerViewData)
            }
        }

        val callToActionModel = PaymentCallToActionViewModel(
            amount = judo.amount.formatted,
            buttonType = method.type.paymentButtonType,
            paymentButtonState = buildPaymentButtonState(method.type, isLoading, cardModel),
            shouldDisplayAmount = judo.uiConfiguration.shouldPaymentMethodsDisplayAmount
        )

        val headerViewModel = PaymentMethodsHeaderViewModel(cardModel, callToActionModel)
        model.postValue(PaymentMethodsModel(headerViewModel, method))
    }

    private fun buildPaymentButtonState(
        method: PaymentMethod,
        isLoading: Boolean,
        cardModel: CardViewModel
    ): ButtonState = when (method) {
        PaymentMethod.CARD -> payWithCardButtonState(isLoading, cardModel)
        PaymentMethod.PAY_BY_BANK,
        PaymentMethod.GOOGLE_PAY -> if (isLoading) ButtonState.Disabled(R.string.empty) else ButtonState.Enabled(
            R.string.empty
        )
        PaymentMethod.IDEAL ->
            if (isLoading) ButtonState.Loading else ButtonState.Enabled(R.string.pay_now)
    }

    private fun payWithCardButtonState(
        isLoading: Boolean,
        cardModel: CardViewModel
    ): ButtonState = when {
        isLoading -> ButtonState.Loading
        cardModel is PaymentCardViewModel && cardDate.apply {
            cardDate = cardModel.expireDate
        }.isAfterToday ->
            ButtonState.Enabled(R.string.pay_now)
        else -> ButtonState.Disabled(R.string.pay_now)
    }

    private fun buildReceipt(card: TokenizedCardEntity) = with(card) {
        val receipt = Receipt(
            judoId = judo.judoId.toLong(),
            yourPaymentReference = judo.reference.paymentReference,
            createdAt = Date(),
            amount = judo.amount.amount.toBigDecimal(),
            currency = judo.amount.currency.name,
            consumer = Consumer(yourConsumerReference = judo.reference.consumerReference),
            cardDetails = CardToken(
                lastFour = ending,
                token = token,
                type = network.typeId,
                scheme = network.displayName
            )
        )
        judoApiCallResult.postValue(JudoApiCallResult.Success(receipt))
    }

    private fun filterPaymentMethods(allMethods: List<PaymentMethod>): List<PaymentMethod> {
        var paymentMethods = allMethods
        if (judo.amount.currency != Currency.EUR) {
            paymentMethods = judo.paymentMethods.filter { it != PaymentMethod.IDEAL }
        }
        if (judo.amount.currency != Currency.GBP || !PBBAAppUtils.isCFIAppAvailable(context)) {
            paymentMethods = paymentMethods.filter { it != PaymentMethod.PAY_BY_BANK }
        }
        return paymentMethods
    }
}
