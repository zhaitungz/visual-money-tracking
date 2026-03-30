package com.example.visualmoneytracker

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.visualmoneytracker.presentation.input.InputBottomSheet
import com.example.visualmoneytracker.presentation.navigation.NavGraph
import com.example.visualmoneytracker.presentation.navigation.Screen
import com.example.visualmoneytracker.presentation.settings.wallet.WalletViewModel
import com.example.visualmoneytracker.ui.theme.VisualMoneyTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisualMoneyTrackerTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showInputBottomSheet by remember { mutableStateOf(false) }

    val walletViewModel: WalletViewModel = hiltViewModel()
    val walletState by walletViewModel.uiState.collectAsState()

    val bottomNavItems = listOf(
        Triple(Screen.Gallery.route, Icons.Default.GridView, "Gallery"),
        Triple(Screen.Analytics.route, Icons.Default.BarChart, "Analytics"),
        Triple(Screen.Settings.route, Icons.Default.Settings, "Cài đặt")
    )

    val isGalleryTab = currentRoute == Screen.Gallery.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            onFabClick = {
                if (walletState.wallets.isEmpty()) {
                    navController.navigate(Screen.WalletManagement.route)
                } else {
                    showInputBottomSheet = true
                }
            }
        )
    }

    if (showInputBottomSheet) {
        InputBottomSheet(
            onDismiss = { showInputBottomSheet = false },
            onImageSelected = { uri ->
                showInputBottomSheet = false
                navController.navigate(Screen.AmountEntry.createRoute(uri.toString()))
            }
        )
    }
}
