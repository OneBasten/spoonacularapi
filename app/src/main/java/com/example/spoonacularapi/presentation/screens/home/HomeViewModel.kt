package com.example.spoonacularapi.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.spoonacularapi.data.CategoriesData
import com.example.spoonacularapi.data.NetworkStatusManager
import com.example.spoonacularapi.domain.model.RecipeCategory
import com.example.spoonacularapi.data.NetworkUtils
import com.example.spoonacularapi.domain.model.Recipe
import com.example.spoonacularapi.domain.usecase.GetRecipesByCategoryPagedUseCase
import com.example.spoonacularapi.domain.usecase.GetRecipesPagedUseCase
import com.example.spoonacularapi.domain.usecase.SearchRecipesPagedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecipesPagedUseCase: GetRecipesPagedUseCase,
    private val searchRecipesPagedUseCase: SearchRecipesPagedUseCase,
    private val getRecipesByCategoryPagedUseCase: GetRecipesByCategoryPagedUseCase,
    private val networkUtils: NetworkUtils,
    private val networkStatusManager: NetworkStatusManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    private val _hasNetworkPermission = MutableStateFlow(false)
    val hasNetworkPermission: StateFlow<Boolean> = _hasNetworkPermission

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _apiLimitReached = MutableStateFlow(false)
    val apiLimitReached: StateFlow<Boolean> = _apiLimitReached

    private val _categories = MutableStateFlow<List<RecipeCategory>>(emptyList())
    val categories: StateFlow<List<RecipeCategory>> = _categories

    private val _selectedCategory = MutableStateFlow<RecipeCategory?>(null)
    val selectedCategory: StateFlow<RecipeCategory?> = _selectedCategory

    private val _loadType = MutableStateFlow<LoadType>(LoadType.All)

    val pagingData: Flow<PagingData<Recipe>> = _loadType.flatMapLatest { loadType ->
        when (loadType) {
            is LoadType.All -> getRecipesPagedUseCase().cachedIn(viewModelScope)
            is LoadType.Search -> searchRecipesPagedUseCase(loadType.query).cachedIn(viewModelScope)
            is LoadType.Category -> getRecipesByCategoryPagedUseCase(loadType.categoryTag).cachedIn(viewModelScope)
        }
    }

    init {
        _hasNetworkPermission.value = networkUtils.hasNetworkPermission()
        loadCategories()
        observeNetworkStatus()
        loadRecipesPaged()
    }

    private fun loadCategories() {
        val categoriesList = CategoriesData.categories.toMutableList()
        categoriesList[0] = categoriesList[0].copy(isSelected = true)
        _categories.value = categoriesList
        _selectedCategory.value = categoriesList[0]
    }

    private fun observeNetworkStatus() {
        networkStatusManager.isOnline
            .onEach { isOnline ->
                _isOffline.value = !isOnline
                if (isOnline && _isLoading.value.not() && _searchQuery.value.isEmpty()) {
                    loadRecipesPaged()
                }
            }
            .launchIn(viewModelScope)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _isSearching.value = query.isNotBlank()

        if (query.isBlank()) {
            loadRecipesPaged()
        } else {
            searchRecipesPaged(query)
        }
    }

    fun searchRecipesPaged(query: String) {
        _loadType.value = LoadType.Search(query)
        _isLoading.value = true
    }

    fun selectCategory(category: RecipeCategory) {
        _searchQuery.value = ""
        _isSearching.value = false

        val updatedCategories = _categories.value.map {
            it.copy(isSelected = it.id == category.id)
        }
        _categories.value = updatedCategories
        _selectedCategory.value = category

        if (category.id == "all") {
            loadRecipesPaged()
        } else {
            loadRecipesByCategoryPaged(category.apiTag)
        }
    }

    private fun loadRecipesByCategoryPaged(categoryTag: String) {
        _loadType.value = LoadType.Category(categoryTag)
        _isLoading.value = true
    }

    fun loadRecipesPaged() {
        _loadType.value = LoadType.All
        _isLoading.value = true
    }


    fun setError(message: String?) {
        _error.value = message
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        networkStatusManager.unregisterReceiver()
    }
}

sealed class LoadType {
    object All : LoadType()
    data class Search(val query: String) : LoadType()
    data class Category(val categoryTag: String) : LoadType()
}