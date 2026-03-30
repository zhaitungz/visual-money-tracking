package com.example.visualmoneytracker.presentation.analytics

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoneytracker.domain.model.CategoryBreakdown
import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.model.Wallet
import com.example.visualmoneytracker.domain.usecase.GetAnalyticsUseCase
import com.example.visualmoneytracker.domain.usecase.GetTransactionsByMonthUseCase
import com.example.visualmoneytracker.domain.usecase.GetWalletsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class AnalyticsUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val viewMode: TransactionType = TransactionType.EXPENSE,
    val breakdowns: List<CategoryBreakdown> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

private val CHART_COLORS = listOf(
    Color(0xFFE53935), Color(0xFF8E24AA), Color(0xFF1E88E5),
    Color(0xFF00ACC1), Color(0xFF43A047), Color(0xFFFB8C00),
    Color(0xFF6D4C41), Color(0xFF546E7A)
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalytics: GetAnalyticsUseCase,
    private val getTransactionsByMonth: GetTransactionsByMonthUseCase,
    private val getWallets: GetWalletsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState()
    )

    init {
        loadWallets()
        loadAnalytics()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            getWallets().collect { wallets ->
                _uiState.update { it.copy(wallets = wallets) }
            }
        }
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoading = true) }
            try {
                val transactions = getTransactionsByMonth(
                    state.selectedMonth.year,
                    state.selectedMonth.monthValue,
                    state.selectedWalletId
                ).first()

                val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                val breakdowns = getAnalytics(
                    state.selectedMonth.year,
                    state.selectedMonth.monthValue,
                    state.viewMode,
                    state.selectedWalletId
                )

                _uiState.update {
                    it.copy(
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        breakdowns = breakdowns,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onMonthSelected(month: YearMonth) {
        _uiState.update { it.copy(selectedMonth = month) }
        loadAnalytics()
    }

    fun onToggleViewMode(mode: TransactionType) {
        _uiState.update { it.copy(viewMode = mode) }
        loadAnalytics()
    }

    fun onWalletFilterSelected(walletId: Long?) {
        _uiState.update { it.copy(selectedWalletId = walletId) }
        loadAnalytics()
    }

    fun chartColorFor(index: Int): Color = CHART_COLORS[index % CHART_COLORS.size]
}
