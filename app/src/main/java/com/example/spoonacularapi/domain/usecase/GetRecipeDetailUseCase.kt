package com.example.spoonacularapi.domain.usecase

import com.example.spoonacularapi.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipeDetailUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(id: Int): Flow<com.example.spoonacularapi.domain.model.Recipe?> {
        return repository.getRecipeById(id)
    }
}