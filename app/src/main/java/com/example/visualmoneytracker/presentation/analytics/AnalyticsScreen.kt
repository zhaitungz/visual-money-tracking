package com.example.visualmoneytracker.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.visualmoneytracker.domain.model.CategoryBreakdown
import com.example.visualmoneytracker.domain.model.TransactionType
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
    ) {
        item {
            // Month switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.onMonthSelected(state.selectedMonth.minusMonths(1)) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Tháng trước")
                }
                Text(
                    text = "${state.selectedMonth.month.getDisplayName(TextStyle.FULL, Locale("vi"))} ${state.selectedMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { viewModel.onMonthSelected(state.selectedMonth.plusMonths(1)) }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Tháng sau")
                }
            }
        }

        item {
            // Monthly summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Thu nhập", style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatAmount(state.totalIncome),
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Chi tiêu", style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatAmount(state.totalExpense),
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        item {
            // Expense/Income toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.onToggleViewMode(TransactionType.EXPENSE) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.viewMode == TransactionType.EXPENSE)
                            Color(0xFFF44336) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (state.viewMode == TransactionType.EXPENSE)
                            Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) { Text("Chi tiêu") }

                Button(
                    onClick = { viewModel.onToggleViewMode(TransactionType.INCOME) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.viewMode == TransactionType.INCOME)
                            Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (state.viewMode == TransactionType.INCOME)
                            Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) { Text("Thu nhập") }
            }
        }

        item {
            // Wallet filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { viewModel.onWalletFilterSelected(null) },
                    label = { Text("Tất cả") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (state.selectedWalletId == null)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                )
                state.wallets.forEach { wallet ->
                    AssistChip(
                        onClick = { viewModel.onWalletFilterSelected(wallet.id) },
                        label = { Text(wallet.name) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (state.selectedWalletId == wallet.id)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        item {
            // Pie chart
            if (state.breakdowns.isNotEmpty()) {
                PieChart(
                    breakdowns = state.breakdowns,
                    colorFor = viewModel::chartColorFor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(16.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không có dữ liệu", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item { Divider(modifier = Modifier.padding(horizontal = 16.dp)) }

        itemsIndexed(state.breakdowns) { index, breakdown ->
            CategoryBreakdownItem(
                breakdown = breakdown,
                color = viewModel.chartColorFor(index)
            )
        }
    }
}

@Composable
fun PieChart(
    breakdowns: List<CategoryBreakdown>,
    colorFor: (Int) -> Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height)
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f

        breakdowns.forEachIndexed { index, breakdown ->
            val sweep = breakdown.percentage / 100f * 360f
            drawArc(
                color = colorFor(index),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize
            )
            startAngle += sweep
        }

        // Inner circle for donut effect
        drawCircle(
            color = androidx.compose.ui.graphics.Color.White,
            radius = diameter / 4f,
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}

@Composable
fun CategoryBreakdownItem(breakdown: CategoryBreakdown, color: Color) {
    ListItem(
        headlineContent = { Text(breakdown.category.name) },
        supportingContent = { Text("${breakdown.percentage.toInt()}%") },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, CircleShape)
            )
        },
        trailingContent = {
            Text(
                formatAmount(breakdown.amount),
                fontWeight = FontWeight.Medium
            )
        }
    )
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
