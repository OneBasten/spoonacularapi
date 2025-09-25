package com.example.spoonacularapi.domain.model

data class Recipe(
    val id: Int,
    val title: String,
    val image: String?,
    val summary: String?,
    val readyInMinutes: Int?,
    val servings: Int?,
    val sourceUrl: String?,
    val dishTypes: List<String>? = emptyList(),
    val isBookmarked: Boolean = false
)