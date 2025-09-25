package com.example.spoonacularapi.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.spoonacularapi.presentation.screens.detail.RecipeDetailScreen
import com.example.spoonacularapi.presentation.screens.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{recipeId}") {
        fun createRoute(recipeId: Int) = "detail/$recipeId"
    }
}

@Composable
fun RecipeNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: 0
            RecipeDetailScreen(
                recipeId = recipeId,
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
    }
}