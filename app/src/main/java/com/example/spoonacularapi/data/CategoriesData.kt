package com.example.spoonacularapi.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SoupKitchen
import com.example.spoonacularapi.domain.model.RecipeCategory

object CategoriesData {
    val categories = listOf(
        RecipeCategory(
            id = "all",
            name = "Все",
            apiTag = "",
            icon = Icons.Default.RestaurantMenu
        ),
        RecipeCategory(
            id = "main_course",
            name = "Основное",
            apiTag = "main course",
            icon = Icons.Default.Restaurant
        ),
        RecipeCategory(
            id = "side_dish",
            name = "Гарниры",
            apiTag = "side dish",
            icon = Icons.Default.Grass
        ),
        RecipeCategory(
            id = "dessert",
            name = "Десерты",
            apiTag = "dessert",
            icon = Icons.Default.Cake
        ),
        RecipeCategory(
            id = "appetizer",
            name = "Закуски",
            apiTag = "appetizer",
            icon = Icons.Default.Fastfood
        ),
        RecipeCategory(
            id = "salad",
            name = "Салаты",
            apiTag = "salad",
            icon = Icons.Default.LocalDining
        ),
        RecipeCategory(
            id = "bread",
            name = "Хлеб",
            apiTag = "bread",
            icon = Icons.Default.BakeryDining
        ),
        RecipeCategory(
            id = "breakfast",
            name = "Завтрак",
            apiTag = "breakfast",
            icon = Icons.Default.BreakfastDining
        ),
        RecipeCategory(
            id = "soup",
            name = "Супы",
            apiTag = "soup",
            icon = Icons.Default.SoupKitchen
        ),
        RecipeCategory(
            id = "beverage",
            name = "Напитки",
            apiTag = "beverage",
            icon = Icons.Default.LocalCafe
        ),
        RecipeCategory(
            id = "sauce",
            name = "Соусы",
            apiTag = "sauce",
            icon = Icons.Default.LocalBar
        ),
        RecipeCategory(
            id = "snack",
            name = "Перекус",
            apiTag = "snack",
            icon = Icons.Default.LocalPizza
        )
    )
}