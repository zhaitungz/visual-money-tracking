package com.example.visualmoneytracker.presentation.settings.wallet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.visualmoneytracker.domain.model.Wallet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletManagementScreen(
    viewModel: WalletViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<WalletWithBalance?>(null) }
    var deleteTarget by remember { mutableStateOf<WalletWithBalance?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ví") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Quay lại") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm ví")
            }
        }
    ) { padding ->
        if (state.wallets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Chưa có ví nào",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Nhấn + để tạo ví đầu tiên",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.wallets) { walletWithBalance ->
                    ListItem(
                        headlineContent = { Text(walletWithBalance.wallet.name) },
                        supportingContent = {
                            Text("Số dư: ${formatAmount(walletWithBalance.currentBalance)}")
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { editTarget = walletWithBalance }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Sửa")
                                }
                                IconButton(onClick = { deleteTarget = walletWithBalance }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa")
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddWalletDialog(
            onConfirm = { name, balance ->
                viewModel.onAddWallet(name, balance)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editTarget?.let { wb ->
        EditWalletDialog(
            wallet = wb.wallet,
            onConfirm = { updatedWallet ->
                viewModel.onUpdateWallet(updatedWallet)
                editTarget = null
            },
            onDismiss = { editTarget = null }
        )
    }

    deleteTarget?.let { wb ->
        DeleteWalletDialog(
            wallet = wb.wallet,
            otherWallets = state.wallets.filter { it.wallet.id != wb.wallet.id }.map { it.wallet },
            onConfirm = { reassignTo ->
                viewModel.onDeleteWallet(wb.wallet.id, reassignTo)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun AddWalletDialog(onConfirm: (String, Double) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("0") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm ví") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên ví") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Số dư ban đầu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val b = balance.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) onConfirm(name, b)
                },
                enabled = name.isNotBlank()
            ) { Text("Thêm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun EditWalletDialog(
    wallet: Wallet,
    onConfirm: (Wallet) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(wallet.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sửa ví") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên ví") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(wallet.copy(name = name)) },
                enabled = name.isNotBlank()
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun DeleteWalletDialog(
    wallet: Wallet,
    otherWallets: List<Wallet>,
    onConfirm: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var deleteTransactions by remember { mutableStateOf(true) }
    var selectedReassignWallet by remember { mutableStateOf<Wallet?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xóa ví \"${wallet.name}\"") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chọn cách xử lý giao dịch trong ví này:")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = deleteTransactions,
                        onClick = {
                            deleteTransactions = true
                            selectedReassignWallet = null
                        }
                    )
                    Text("Xóa tất cả giao dịch")
                }
                if (otherWallets.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !deleteTransactions,
                            onClick = {
                                deleteTransactions = false
                                selectedReassignWallet = otherWallets.first()
                            }
                        )
                        Text("Chuyển sang ví khác")
                    }
                    if (!deleteTransactions) {
                        otherWallets.forEach { w ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 32.dp)
                            ) {
                                RadioButton(
                                    selected = selectedReassignWallet?.id == w.id,
                                    onClick = { selectedReassignWallet = w }
                                )
                                Text(w.name)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val reassignId = if (deleteTransactions) null else selectedReassignWallet?.id
                onConfirm(reassignId)
            }) { Text("Xóa") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 1_000_000) "${(amount / 1_000_000).toLong()}M"
    else if (amount >= 1_000) "${(amount / 1_000).toLong()}K"
    else amount.toLong().toString()
}
