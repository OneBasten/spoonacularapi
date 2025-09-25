package com.example.spoonacularapi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val image: String?,
    val summary: String?,
    val readyInMinutes: Int? = 0,
    val servings: Int? = 0,
    val sourceUrl: String?,
    val dishTypes: String?,
    val isBookmarked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val page: Int = 0,
    val searchQuery: String? = null,
    val categoryTag: String? = null
)