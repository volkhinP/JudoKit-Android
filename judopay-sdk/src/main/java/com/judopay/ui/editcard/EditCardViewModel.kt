package com.judopay.ui.editcard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.judopay.db.JudoRoomDatabase
import com.judopay.db.entity.TokenizedCardEntity
import com.judopay.db.repository.TokenizedCardRepository
import com.judopay.ui.editcard.adapter.ColorPickerItem
import kotlinx.coroutines.launch

sealed class EditCardAction {
    data class ChangePattern(val pattern: CardPattern) : EditCardAction()
    data class ChangeTitle(val newTitle: String) : EditCardAction()

    object ToggleDefaultCardState : EditCardAction()
    object Save : EditCardAction()
}

internal class EditCardViewModelFactory(
    private val application: Application,
    private val cardId: Int
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass == EditCardViewModel::class.java) {
            EditCardViewModel(application, cardId) as T
        } else super.create(modelClass)
    }
}

class EditCardViewModel(
    application: Application,
    private val cardId: Int
) : AndroidViewModel(application) {

    val model = MutableLiveData<EditCardModel>()

    private val context = application
    private val tokenizedCardDao = JudoRoomDatabase.getDatabase(application).tokenizedCardDao()
    private val cardRepository = TokenizedCardRepository(tokenizedCardDao)

    private val patterns = CardPattern.values()
    private val cachedPatternItems = patterns.map { ColorPickerItem(it) }

    // mutable data
    private lateinit var cardEntity: TokenizedCardEntity

    private var selectedPattern: CardPattern = patterns.first()
    private var isSelectedAsDefault: Boolean = false
    private var currentCardTitle: String = ""

    private val isSaveButtonEnabled: Boolean
        get() {
            return (selectedPattern != cardEntity.pattern ||
                    isSelectedAsDefault != cardEntity.isDefault ||
                    currentCardTitle != cardEntity.title) && currentCardTitle.length <= 28
        }

    init {
        loadCardEntity()
    }

    fun send(action: EditCardAction) {
        when (action) {
            is EditCardAction.ChangePattern -> buildModel(action.pattern)
            is EditCardAction.Save -> persistChanges()
            is EditCardAction.ToggleDefaultCardState -> {
                isSelectedAsDefault = !isSelectedAsDefault
                buildModel()
            }
            is EditCardAction.ChangeTitle -> buildModel(newTitle = action.newTitle)
        }
    }

    private fun buildModel(
        newPattern: CardPattern = this.selectedPattern,
        newTitle: String = currentCardTitle
    ) {
        selectedPattern = newPattern
        currentCardTitle = newTitle

        cachedPatternItems.forEach { it.isSelected = it.pattern == selectedPattern }

        model.postValue(
            EditCardModel(
                cachedPatternItems,
                isSaveButtonEnabled,
                cardEntity.toPaymentCardViewModel(currentCardTitle, selectedPattern),
                title = newTitle,
                isDefault = isSelectedAsDefault
            )
        )
    }

    private fun loadCardEntity() = viewModelScope.launch {
        cardEntity = cardRepository.findWithId(cardId)
        selectedPattern = cardEntity.pattern
        isSelectedAsDefault = cardEntity.isDefault
        currentCardTitle = cardEntity.title
        buildModel()
    }

    private fun persistChanges() = viewModelScope.launch {
        cardRepository.updateDefault(cardEntity.apply {
            title = currentCardTitle
            pattern = selectedPattern
            isDefault = isSelectedAsDefault
        })
    }
}
