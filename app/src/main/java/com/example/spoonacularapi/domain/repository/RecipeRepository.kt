package com.example.spoonacularapi.domain.repository


import androidx.paging.PagingData
import com.example.spoonacularapi.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipeById(id: Int): Flow<Recipe?>
    suspend fun refreshRecipes(categoryTag: String? = null)
    suspend fun searchRecipesOnline(query: String, categoryTag: String? = null): List<Recipe>

    fun getRecipesPaged(): Flow<PagingData<Recipe>>
    fun searchRecipesPaged(query: String): Flow<PagingData<Recipe>>
    fun getRecipesByCategoryPaged(categoryTag: String): Flow<PagingData<Recipe>>
}

