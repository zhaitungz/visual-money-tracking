package com.example.visualmoneytracker.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToWallets: () -> Unit
) {
    val syncViewModel: SyncViewModel = hiltViewModel()
    val reminderViewModel: ReminderViewModel = hiltViewModel()
    val syncState by syncViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var reminderEnabled by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cài đặt") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                ListItem(
                    headlineContent = { Text("Danh mục") },
                    supportingContent = { Text("Quản lý danh mục giao dịch") },
                    leadingContent = { Icon(Icons.Default.List, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToCategories() }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Ví") },
                    supportingContent = { Text("Quản lý ví và số dư") },
                    leadingContent = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToWallets() }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Nhắc nhở") },
                    supportingContent = { Text("Bật/tắt nhắc nhở định kỳ") },
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                reminderEnabled = enabled
                                reminderViewModel.setReminder(enabled)
                            }
                        )
                    }
                )
            }
            item {
                if (!syncState.isAuthenticated) {
                    ListItem(
                        headlineContent = { Text("Kết nối Box") },
                        supportingContent = { Text("Đăng nhập để đồng bộ dữ liệu lên Box") },
                        leadingContent = { Icon(Icons.Default.Cloud, contentDescription = null) },
                        modifier = Modifier.clickable { syncViewModel.openAuthInBrowser(context) }
                    )
                } else {
                    ListItem(
                        headlineContent = { Text("Đồng bộ lên Box") },
                        supportingContent = {
                            Text(
                                when {
                                    syncState.isSyncing -> "Đang đồng bộ..."
                                    syncState.lastSyncResult != null -> syncState.lastSyncResult!!
                                    syncState.error != null -> "Lỗi: ${syncState.error}"
                                    else -> "Đã kết nối Box"
                                }
                            )
                        },
                        leadingContent = { Icon(Icons.Default.Cloud, contentDescription = null) },
                        modifier = Modifier.clickable {
                            if (!syncState.isSyncing) syncViewModel.syncNow()
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Đăng xuất Box") },
                        supportingContent = { Text("Ngắt kết nối tài khoản Box") },
                        leadingContent = { Icon(Icons.Default.Cloud, contentDescription = null) },
                        modifier = Modifier.clickable { showLogoutDialog = true }
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất Box") },
            text = { Text("Bạn có chắc muốn ngắt kết nối tài khoản Box không?") },
            confirmButton = {
                TextButton(onClick = {
                    syncViewModel.logout()
                    showLogoutDialog = false
                }) { Text("Đăng xuất") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") }
            }
        )
    }
}
