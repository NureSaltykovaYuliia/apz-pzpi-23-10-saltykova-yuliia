package com.example.mydogspace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mydogspace.network.SessionManager
import com.example.mydogspace.ui.screens.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Events : Screen("events")
    object Partners : Screen("partners")
    object Profile : Screen("profile")
    object ChatDetail : Screen("chat_detail/{id}") {
        fun createRoute(id: Int) = "chat_detail/$id"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    val token by SessionManager.tokenState
    val startDest = if (!token.isNullOrBlank()) Screen.Home.route else Screen.Login.route
    
    // Активний редирект на логін, якщо токен зник або порожній
    androidx.compose.runtime.LaunchedEffect(token) {
        if (token.isNullOrBlank()) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != Screen.Login.route && currentRoute != Screen.Register.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Events.route) { EventsScreen(navController) }
        composable(Screen.Partners.route) { PartnersScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.ChatDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
            ChatDetailScreen(navController, id)
        }
    }
}
