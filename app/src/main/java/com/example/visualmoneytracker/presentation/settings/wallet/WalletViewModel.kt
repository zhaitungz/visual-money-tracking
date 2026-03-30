package com.example.visualmoneytracker.presentation.settings.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoneytracker.domain.model.Wallet
import com.example.visualmoneytracker.domain.usecase.DeleteWalletUseCase
import com.example.visualmoneytracker.domain.usecase.GetWalletBalanceUseCase
import com.example.visualmoneytracker.domain.usecase.GetWalletsUseCase
import com.example.visualmoneytracker.domain.usecase.SaveWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class WalletWithBalance(
    val wallet: Wallet,
    val currentBalance: Double
)

data class WalletUiState(
    val wallets: List<WalletWithBalance> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val getWallets: GetWalletsUseCase,
    private val saveWallet: SaveWalletUseCase,
    private val deleteWallet: DeleteWalletUseCase,
    private val getWalletBalance: GetWalletBalanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState(isLoading = true))
    val uiState: StateFlow<WalletUiState> = _uiState.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), WalletUiState()
    )

    init {
        viewModelScope.launch {
            getWallets().collect { wallets ->
                val walletsWithBalance = wallets.map { wallet ->
                    val balance = getWalletBalance(wallet.id).getOrDefault(wallet.openingBalance)
                    WalletWithBalance(wallet, balance)
                }
                _uiState.value = WalletUiState(wallets = walletsWithBalance, isLoading = false)
            }
        }
    }

    fun onAddWallet(name: String, openingBalance: Double) {
        viewModelScope.launch {
            saveWallet(
                Wallet(
                    id = 0,
                    name = name,
                    openingBalance = openingBalance,
                    createdAt = LocalDateTime.now()
                )
            )
        }
    }

    fun onUpdateWallet(wallet: Wallet) {
        viewModelScope.launch {
            saveWallet(wallet)
        }
    }

    fun onDeleteWallet(walletId: Long, reassignToWalletId: Long?) {
        viewModelScope.launch {
            deleteWallet(walletId, reassignToWalletId)
        }
    }
}
