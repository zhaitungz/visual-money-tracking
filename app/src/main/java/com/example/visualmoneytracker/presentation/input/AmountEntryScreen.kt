package com.example.visualmoneytracker.presentation.input

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.visualmoneytracker.domain.model.TransactionType
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountEntryScreen(
    viewModel: InputViewModel,
    imageUri: Uri,
    onSaved: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) onSaved()
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Image preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Ảnh giao dịch",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Amount display
        val displayAmount = if (state.amount.isEmpty()) "0"
        else {
            val num = state.amount.toLongOrNull() ?: 0L
            NumberFormat.getNumberInstance(Locale("vi", "VN")).format(num)
        }
        Text(
            text = displayAmount,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )

        // Income/Expense toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.onTransactionTypeToggled(TransactionType.EXPENSE) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.transactionType == TransactionType.EXPENSE)
                        Color(0xFFF44336) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (state.transactionType == TransactionType.EXPENSE)
                        Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) { Text("Chi tiêu") }

            Button(
                onClick = { viewModel.onTransactionTypeToggled(TransactionType.INCOME) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.transactionType == TransactionType.INCOME)
                        Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (state.transactionType == TransactionType.INCOME)
                        Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) { Text("Thu nhập") }
        }

        // Category picker
        var categoryExpanded by remember { mutableStateOf(false) }
        val selectedCategory = state.availableCategories.find { it.id == state.selectedCategoryId }
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "Chọn danh mục",
                onValueChange = {},
                readOnly = true,
                label = { Text("Danh mục") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                state.availableCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            viewModel.onCategorySelected(category.id)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Wallet picker
        var walletExpanded by remember { mutableStateOf(false) }
        val selectedWallet = state.availableWallets.find { it.id == state.selectedWalletId }
        ExposedDropdownMenuBox(
            expanded = walletExpanded,
            onExpandedChange = { walletExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedWallet?.name ?: "Chọn ví",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ví") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = walletExpanded,
                onDismissRequest = { walletExpanded = false }
            ) {
                state.availableWallets.forEach { wallet ->
                    DropdownMenuItem(
                        text = { Text(wallet.name) },
                        onClick = {
                            viewModel.onWalletSelected(wallet.id)
                            walletExpanded = false
                        }
                    )
                }
            }
        }

        // Numpad
        Numpad(
            onDigit = viewModel::onDigitPressed,
            onDelete = viewModel::onDeletePressed
        )

        // Save button
        Button(
            onClick = { viewModel.onSave(imageUri) },
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "Đang lưu..." else "Lưu")
        }
    }
}

@Composable
fun Numpad(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("000", "0", "⌫")
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { key ->
                    if (key == "⌫") {
                        TextButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(2f)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Xóa",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        TextButton(
                            onClick = { onDigit(key) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(2f)
                        ) {
                            Text(key, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
