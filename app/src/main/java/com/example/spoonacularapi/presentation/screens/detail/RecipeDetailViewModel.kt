package com.example.spoonacularapi.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spoonacularapi.domain.usecase.GetRecipeDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val getRecipeDetailUseCase: GetRecipeDetailUseCase,
) : ViewModel() {

    private val _recipe = MutableStateFlow<com.example.spoonacularapi.domain.model.Recipe?>(null)
    val recipe: StateFlow<com.example.spoonacularapi.domain.model.Recipe?> = _recipe

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadRecipe(recipeId: Int) {
        viewModelScope.launch (Dispatchers.IO){
            _isLoading.value = true
            _error.value = null
            try {
                getRecipeDetailUseCase(recipeId).collect { recipeDetail ->
                    _recipe.value = recipeDetail
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                _isLoading.value = false
            }
        }
    }

}