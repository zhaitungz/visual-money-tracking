package com.example.visualmoneytracker.presentation.input

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoneytracker.domain.model.Category
import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.model.Wallet
import com.example.visualmoneytracker.domain.usecase.GetCategoriesUseCase
import com.example.visualmoneytracker.domain.usecase.GetWalletsUseCase
import com.example.visualmoneytracker.domain.usecase.SaveTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AmountEntryUiState(
    val imagePath: String = "",
    val amount: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val selectedCategoryId: Long = 0L,
    val availableCategories: List<Category> = emptyList(),
    val selectedWalletId: Long? = null,
    val availableWallets: List<Wallet> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccessfully: Boolean = false
) {
    val canSave: Boolean
        get() = amount.isNotEmpty() && (amount.toLongOrNull() ?: 0L) > 0L && selectedWalletId != null && !isSaving
}

@HiltViewModel
class InputViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getWallets: GetWalletsUseCase,
    private val saveTransaction: SaveTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AmountEntryUiState())
    val uiState: StateFlow<AmountEntryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val categories = getCategories().first()
            val wallets = getWallets().first()
            val defaultCategory = categories.find { it.name == "Khác" } ?: categories.firstOrNull()
            val autoWallet = if (wallets.size == 1) wallets.first().id else null
            _uiState.update {
                it.copy(
                    availableCategories = categories,
                    selectedCategoryId = defaultCategory?.id ?: 0L,
                    availableWallets = wallets,
                    selectedWalletId = autoWallet
                )
            }
        }
    }

    fun setImagePath(path: String) {
        _uiState.update { it.copy(imagePath = path) }
    }

    fun onDigitPressed(digit: String) {
        _uiState.update { state ->
            val current = state.amount
            val newAmount = if (current == "0") digit else current + digit
            state.copy(amount = newAmount)
        }
    }

    fun onDeletePressed() {
        _uiState.update { state ->
            val newAmount = if (state.amount.length <= 1) "" else state.amount.dropLast(1)
            state.copy(amount = newAmount)
        }
    }

    fun onTransactionTypeToggled(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type) }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onWalletSelected(walletId: Long) {
        _uiState.update { it.copy(selectedWalletId = walletId) }
    }

    fun onSave(imageUri: Uri) {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return
        val walletId = state.selectedWalletId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = saveTransaction(
                rawImageUri = imageUri,
                amount = amount,
                type = state.transactionType,
                categoryId = state.selectedCategoryId,
                walletId = walletId
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) } },
                onFailure = { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
