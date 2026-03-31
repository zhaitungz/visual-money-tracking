package com.example.visualmoneytracker.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.visualmoneytracker.presentation.analytics.AnalyticsScreen
import com.example.visualmoneytracker.presentation.analytics.AnalyticsViewModel
import com.example.visualmoneytracker.presentation.gallery.GalleryScreen
import com.example.visualmoneytracker.presentation.gallery.GalleryViewModel
import com.example.visualmoneytracker.presentation.input.AmountEntryScreen
import com.example.visualmoneytracker.presentation.input.InputViewModel
import com.example.visualmoneytracker.presentation.settings.SettingsScreen
import com.example.visualmoneytracker.presentation.settings.category.CategoryManagementScreen
import com.example.visualmoneytracker.presentation.settings.category.CategoryViewModel
import com.example.visualmoneytracker.presentation.settings.wallet.WalletManagementScreen
import com.example.visualmoneytracker.presentation.settings.wallet.WalletViewModel

sealed class Screen(val route: String) {
    object Gallery : Screen("gallery")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")
    object AmountEntry : Screen("amount_entry?imagePath={imagePath}") {
        fun createRoute(imagePath: String) = "amount_entry?imagePath=${Uri.encode(imagePath)}"
    }
    object WalletManagement : Screen("wallet_management")
    object CategoryManagement : Screen("category_management")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    onFabClick: () -> Unit
) {
    NavHost(navController = navController, startDestination = Screen.Gallery.route) {
        composable(Screen.Gallery.route) {
            val viewModel: GalleryViewModel = hiltViewModel()
            GalleryScreen(
                viewModel = viewModel,
                onFabClick = onFabClick,
                onNavigateToWallets = { navController.navigate(Screen.WalletManagement.route) }
            )
        }

        composable(Screen.Analytics.route) {
            val viewModel: AnalyticsViewModel = hiltViewModel()
            AnalyticsScreen(viewModel = viewModel)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToCategories = { navController.navigate(Screen.CategoryManagement.route) },
                onNavigateToWallets = { navController.navigate(Screen.WalletManagement.route) }
            )
        }

        composable(
            route = Screen.AmountEntry.route,
            arguments = listOf(navArgument("imagePath") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("imagePath") ?: ""
            val imagePath = Uri.decode(encodedPath)
            val imageUri = if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
                Uri.parse(imagePath)
            } else {
                Uri.parse("file://$imagePath")
            }
            val viewModel: InputViewModel = hiltViewModel()
            AmountEntryScreen(
                viewModel = viewModel,
                imageUri = imageUri,
                onSaved = { navController.popBackStack(Screen.Gallery.route, false) }
            )
        }

        composable(Screen.WalletManagement.route) {
            val viewModel: WalletViewModel = hiltViewModel()
            WalletManagementScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CategoryManagement.route) {
            val viewModel: CategoryViewModel = hiltViewModel()
            CategoryManagementScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
