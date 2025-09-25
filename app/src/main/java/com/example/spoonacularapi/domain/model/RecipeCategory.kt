package com.example.spoonacularapi.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class RecipeCategory(
    val id: String,
    val name: String,
    val apiTag: String,
    val icon: ImageVector,
    val isSelected: Boolean = false
)