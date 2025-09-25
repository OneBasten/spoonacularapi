package com.example.spoonacularapi.domain.usecase

import androidx.paging.PagingData
import com.example.spoonacularapi.domain.model.Recipe
import com.example.spoonacularapi.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipesPagedUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<PagingData<Recipe>> {
        return repository.getRecipesPaged()
    }
}