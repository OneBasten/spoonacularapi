package com.example.spoonacularapi.domain.model

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.spoonacularapi.BuildConfig
import com.example.spoonacularapi.data.NetworkUtils
import com.example.spoonacularapi.data.local.RecipeDao
import com.example.spoonacularapi.data.local.RecipeEntity
import com.example.spoonacularapi.data.remote.RecipeApi
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class RecipesPagingSource(
    private val api: RecipeApi,
    private val dao: RecipeDao,
    private val networkUtils: NetworkUtils,
    private val gson: Gson,
    private val query: String? = null,
    private val categoryTag: String? = null
) : PagingSource<Int, RecipeEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecipeEntity> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            Log.d("RecipesPagingSource", "Loading page: $page, size: $pageSize, query: '$query', category: '$categoryTag'")

            if (networkUtils.isInternetAvailable()) {
                Log.d("RecipesPagingSource", "Loading from API...")

                val response = if (!query.isNullOrEmpty()) {
                    Log.d("RecipesPagingSource", "Making search API call for query: '$query'")
                    api.searchRecipes(
                        query = query,
                        number = pageSize,
                        offset = page * pageSize,
                        apiKey = BuildConfig.API_KEY
                    )
                } else if (!categoryTag.isNullOrEmpty()) {
                    Log.d("RecipesPagingSource", "Making category API call for category: '$categoryTag'")
                    api.getRecipesByCategory(
                        number = pageSize,
                        offset = page * pageSize,
                        apiKey = BuildConfig.API_KEY,
                        type = categoryTag
                    )
                } else {
                    Log.d("RecipesPagingSource", "Making general recipes API call")
                    api.searchRecipes(
                        query = "",
                        number = pageSize,
                        offset = page * pageSize,
                        apiKey = BuildConfig.API_KEY
                    )
                }

                Log.d("RecipesPagingSource", "API response received: ${response.results.size} recipes")

                val recipes = response.results.mapNotNull { recipeDto ->
                    try {
                        RecipeEntity(
                            id = recipeDto.id,
                            title = recipeDto.title ?: "No Title",
                            image = recipeDto.image,
                            summary = recipeDto.summary,
                            readyInMinutes = recipeDto.readyInMinutes ?: 0,
                            servings = recipeDto.servings ?: 0,
                            sourceUrl = recipeDto.sourceUrl,
                            dishTypes = if (recipeDto.dishTypes != null) gson.toJson(recipeDto.dishTypes) else null,
                            isBookmarked = false,
                            timestamp = System.currentTimeMillis(),
                            page = page,
                            searchQuery = query,
                            categoryTag = categoryTag
                        )
                    } catch (e: Exception) {
                        Log.e("RecipesPagingSource", "Error converting recipe DTO to Entity: ${e.message}")
                        Log.e("RecipesPagingSource", "Problematic recipe: id=${recipeDto.id}, title=${recipeDto.title}")
                        null
                    }
                }

                Log.d("RecipesPagingSource", "Successfully converted ${recipes.size} recipes to entities")

                if (recipes.isNotEmpty()) {
                    dao.insertRecipes(recipes)
                    Log.d("RecipesPagingSource", "Inserted ${recipes.size} recipes into database")
                } else {
                    Log.w("RecipesPagingSource", "No valid recipes to insert into database")
                }

                val nextKey = if (response.results.size < pageSize) {
                    Log.d("RecipesPagingSource", "No more pages available")
                    null
                } else {
                    Log.d("RecipesPagingSource", "Next page available: ${page + 1}")
                    page + 1
                }

                LoadResult.Page(
                    data = recipes,
                    prevKey = if (page == 0) {
                        Log.d("RecipesPagingSource", "First page, no previous key")
                        null
                    } else {
                        Log.d("RecipesPagingSource", "Previous page: ${page - 1}")
                        page - 1
                    },
                    nextKey = nextKey
                )

            } else {
                Log.d("RecipesPagingSource", "No internet connection, loading from database")

                val offlineRecipes = if (!query.isNullOrEmpty()) {
                    Log.d("RecipesPagingSource", "Loading search results from DB for query: '$query'")
                    dao.searchRecipes(query).first()
                } else if (!categoryTag.isNullOrEmpty()) {
                    Log.d("RecipesPagingSource", "Loading category results from DB for category: '$categoryTag'")
                    dao.getRecipesByCategory(categoryTag).first()
                } else {
                    Log.d("RecipesPagingSource", "Loading all recipes from DB")
                    dao.getRecipes().first()
                }

                Log.d("RecipesPagingSource", "Found ${offlineRecipes.size} recipes in database")

                val pagedOfflineRecipes = offlineRecipes
                    .sortedByDescending { it.timestamp }
                    .drop(page * pageSize)
                    .take(pageSize)

                Log.d("RecipesPagingSource", "Paged offline recipes: ${pagedOfflineRecipes.size} items for page $page")

                val nextKey = if (pagedOfflineRecipes.size < pageSize) {
                    Log.d("RecipesPagingSource", "No more offline pages available")
                    null
                } else {
                    Log.d("RecipesPagingSource", "Next offline page available: ${page + 1}")
                    page + 1
                }

                LoadResult.Page(
                    data = pagedOfflineRecipes,
                    prevKey = if (page == 0) {
                        Log.d("RecipesPagingSource", "First offline page, no previous key")
                        null
                    } else {
                        Log.d("RecipesPagingSource", "Previous offline page: ${page - 1}")
                        page - 1
                    },
                    nextKey = nextKey
                )
            }

        } catch (e: HttpException) {
            Log.e("RecipesPagingSource", "HTTP error in PagingSource: ${e.code()} - ${e.message}")
            when (e.code()) {
                402 -> {
                    Log.w("RecipesPagingSource", "API limit reached")
                    LoadResult.Error(Exception("API лимит исчерпан"))
                }
                401 -> {
                    Log.e("RecipesPagingSource", "Invalid API key")
                    LoadResult.Error(Exception("Неверный API ключ"))
                }
                429 -> {
                    Log.w("RecipesPagingSource", "Too many requests")
                    LoadResult.Error(Exception("Слишком много запросов"))
                }
                else -> {
                    Log.e("RecipesPagingSource", "Server error: ${e.code()}")
                    LoadResult.Error(Exception("Ошибка сервера: ${e.code()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("RecipesPagingSource", "Error in PagingSource: ${e.message}")
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RecipeEntity>): Int? {
        Log.d("RecipesPagingSource", "Getting refresh key for paging state")

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }?.also { key ->
            Log.d("RecipesPagingSource", "Refresh key calculated: $key")
        }
    }
}