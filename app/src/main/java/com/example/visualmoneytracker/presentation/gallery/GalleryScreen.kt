package com.example.visualmoneytracker.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.visualmoneytracker.domain.model.Transaction
import com.example.visualmoneytracker.domain.model.TransactionType
import com.example.visualmoneytracker.domain.model.Wallet
import java.io.File
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onFabClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item(span = { GridItemSpan(3) }) {
                WalletBalanceHeader(
                    totalBalance = state.totalBalance,
                    wallets = state.wallets,
                    selectedWalletId = state.selectedWalletId,
                    onWalletSelected = viewModel::onWalletFilterSelected
                )
            }

            item(span = { GridItemSpan(3) }) {
                MonthSwitcher(
                    selectedMonth = state.selectedMonth,
                    onPrevious = { viewModel.onMonthSelected(state.selectedMonth.minusMonths(1)) },
                    onNext = { viewModel.onMonthSelected(state.selectedMonth.plusMonths(1)) }
                )
            }

            state.groupedTransactions.forEach { (month, transactions) ->
                item(span = { GridItemSpan(3) }) {
                    MonthHeader(month = month, transactions = transactions)
                }
                items(transactions) { tx ->
                    TransactionCard(
                        transaction = tx,
                        onDelete = { viewModel.onDeleteTransaction(tx.id) }
                    )
                }
            }
        }

        CameraFab(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun WalletBalanceHeader(
    totalBalance: Double,
    wallets: List<Wallet>,
    selectedWalletId: Long?,
    onWalletSelected: (Long?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Tổng số dư",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatAmount(totalBalance),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WalletFilterChip(
                label = "Tất cả",
                selected = selectedWalletId == null,
                onClick = { onWalletSelected(null) }
            )
            wallets.forEach { wallet ->
                WalletFilterChip(
                    label = wallet.name,
                    selected = selectedWalletId == wallet.id,
                    onClick = { onWalletSelected(wallet.id) }
                )
            }
        }
    }
}

@Composable
fun WalletFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun MonthSwitcher(
    selectedMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Tháng trước")
        }
        Text(
            text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale("vi"))} ${selectedMonth.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Tháng sau")
        }
    }
}

@Composable
fun MonthHeader(month: YearMonth, transactions: List<Transaction>) {
    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${month.month.getDisplayName(TextStyle.SHORT, Locale("vi"))} ${month.year}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "+${formatAmount(income)}", color = Color(0xFF4CAF50), fontSize = 12.sp)
            Text(text = "-${formatAmount(expense)}", color = Color(0xFFF44336), fontSize = 12.sp)
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
    ) {
        AsyncImage(
            model = File(transaction.imagePath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        AmountOverlay(
            amount = transaction.amount,
            type = transaction.type,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
fun AmountOverlay(amount: Double, type: TransactionType, modifier: Modifier = Modifier) {
    val isIncome = type == TransactionType.INCOME
    val color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isIncome) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = formatAmount(amount),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CameraFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(Icons.Default.Add, contentDescription = "Thêm giao dịch")
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 1_000_000) {
        "${(amount / 1_000_000).toLong()}M"
    } else if (amount >= 1_000) {
        "${(amount / 1_000).toLong()}K"
    } else {
        amount.toLong().toString()
    }
}
