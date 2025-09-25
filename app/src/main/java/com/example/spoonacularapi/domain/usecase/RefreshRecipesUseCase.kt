package com.example.spoonacularapi.domain.usecase

import com.example.spoonacularapi.domain.repository.RecipeRepository
import javax.inject.Inject

class RefreshRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(categoryTag: String? = null) {
        repository.refreshRecipes(categoryTag)
    }
}