package com.example.visualmoneytracker.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoneytracker.domain.model.Transaction
import com.example.visualmoneytracker.domain.model.Wallet
import com.example.visualmoneytracker.domain.usecase.DeleteTransactionUseCase
import com.example.visualmoneytracker.domain.usecase.GetTransactionsByMonthUseCase
import com.example.visualmoneytracker.domain.usecase.GetWalletBalanceUseCase
import com.example.visualmoneytracker.domain.usecase.GetWalletsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class GalleryUiState(
    val groupedTransactions: Map<YearMonth, List<Transaction>> = emptyMap(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: Long? = null,
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getTransactionsByMonth: GetTransactionsByMonthUseCase,
    private val getWallets: GetWalletsUseCase,
    private val getWalletBalance: GetWalletBalanceUseCase,
    private val deleteTransaction: DeleteTransactionUseCase
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val selectedWalletId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val transactions = combine(selectedMonth, selectedWalletId) { month, walletId ->
        Pair(month, walletId)
    }.flatMapLatest { (month, walletId) ->
        getTransactionsByMonth(month.year, month.monthValue, walletId)
    }

    val uiState: StateFlow<GalleryUiState> = combine(
        transactions,
        getWallets(),
        selectedMonth,
        selectedWalletId
    ) { txList, wallets, month, walletId ->
        val grouped = txList.groupBy { tx ->
            YearMonth.of(tx.timestamp.year, tx.timestamp.monthValue)
        }.toSortedMap(compareByDescending { it })

        val totalBalance = if (walletId != null) {
            getWalletBalance(walletId).getOrDefault(0.0)
        } else {
            wallets.sumOf { w -> getWalletBalance(w.id).getOrDefault(0.0) }
        }

        GalleryUiState(
            groupedTransactions = grouped,
            selectedMonth = month,
            wallets = wallets,
            selectedWalletId = walletId,
            totalBalance = totalBalance,
            isLoading = false,
            error = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GalleryUiState()
    )

    fun onMonthSelected(month: YearMonth) {
        selectedMonth.value = month
    }

    fun onWalletFilterSelected(walletId: Long?) {
        selectedWalletId.value = walletId
    }

    fun onDeleteTransaction(id: Long) {
        viewModelScope.launch {
            deleteTransaction(id)
        }
    }
}
