package com.example.visualmoneytracker.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoneytracker.data.remote.cloud.BoxSyncManager
import com.example.visualmoneytracker.domain.model.CloudProvider
import com.example.visualmoneytracker.domain.usecase.SyncToCloudUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncUiState(
    val isAuthenticated: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncResult: String? = null,
    val error: String? = null
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val boxSyncManager: BoxSyncManager,
    private val syncToCloud: SyncToCloudUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isAuthenticated = boxSyncManager.isAuthenticated) }
    }

    fun getAuthorizationUrl(): String {
        return "https://account.box.com/api/oauth2/authorize" +
            "?client_id=${boxSyncManager.clientId}" +
            "&redirect_uri=${Uri.encode(boxSyncManager.redirectUri)}" +
            "&response_type=code"
    }

    fun openAuthInBrowser(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getAuthorizationUrl()))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun handleAuthCode(code: String) {
        viewModelScope.launch {
            val result = boxSyncManager.exchangeCode(code)
            _uiState.update {
                it.copy(
                    isAuthenticated = result.isSuccess,
                    error = if (result.isFailure) result.exceptionOrNull()?.message else null
                )
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null) }
            val result = syncToCloud(CloudProvider.Box)
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncResult = if (result.isSuccess) "Đồng bộ thành công" else null,
                    error = if (result.isFailure) result.exceptionOrNull()?.message else null
                )
            }
        }
    }

    fun logout() {
        boxSyncManager.logout()
        _uiState.update { it.copy(isAuthenticated = false, lastSyncResult = null) }
    }
}
