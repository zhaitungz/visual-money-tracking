package com.example.visualmoneytracker.presentation.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToWallets: () -> Unit
) {
    var reminderEnabled by remember { mutableStateOf(false) }

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
                    leadingContent = { Icon(Icons.Default.Category, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToCategories() }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Ví") },
                    supportingContent = { Text("Quản lý ví và số dư") },
                    leadingContent = { Icon(Icons.Default.Wallet, contentDescription = null) },
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
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }
                )
            }
        }
    }
}
