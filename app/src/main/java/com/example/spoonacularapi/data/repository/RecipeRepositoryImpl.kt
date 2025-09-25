package com.example.spoonacularapi.data.repository


import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.spoonacularapi.BuildConfig
import com.example.spoonacularapi.data.NetworkUtils
import com.example.spoonacularapi.data.local.RecipeDao
import com.example.spoonacularapi.data.local.RecipeEntity
import com.example.spoonacularapi.data.remote.RecipeApi
import com.example.spoonacularapi.data.remote.RecipeDto
import com.example.spoonacularapi.domain.model.Recipe
import com.example.spoonacularapi.domain.model.RecipesPagingSource
import com.example.spoonacularapi.domain.repository.RecipeRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val api: RecipeApi,
    private val dao: RecipeDao,
    private val networkUtils: NetworkUtils,
    private val gson: Gson
) : RecipeRepository {

    override fun getRecipeById(id: Int): Flow<Recipe?> {
        return dao.getRecipes().map { entities ->
            entities.find { it.id == id }?.toRecipe(gson)
        }
    }

    override suspend fun refreshRecipes(categoryTag: String?) {
        if (!networkUtils.isInternetAvailable()) {
            throw Exception("No internet connection")
        }

        try {
            val response = api.getRandomRecipes(
                apiKey = BuildConfig.API_KEY,
                tags = categoryTag
            )
            val recipes = response.recipes.map { it.toEntity(gson) }
            dao.insertRecipes(recipes)

            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            dao.deleteOldRecipes(oneDayAgo)

        } catch (e: HttpException) {
            when (e.code()) {
                402 -> throw Exception("API лимит исчерпан. Используются сохраненные данные.")
                401 -> throw Exception("Неверный API ключ")
                429 -> throw Exception("Слишком много запросов")
                else -> throw Exception("Ошибка сервера: ${e.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to refresh recipes: ${e.message}")
        }
    }

    override suspend fun searchRecipesOnline(query: String, categoryTag: String?): List<Recipe> {
        if (!networkUtils.isInternetAvailable()) {
            throw Exception("No internet connection")
        }

        try {
            val response = api.searchRecipes(
                query = query,
                apiKey = BuildConfig.API_KEY,
                type = categoryTag
            )
            val recipes = response.results.map { it.toEntity(gson) }
            dao.insertRecipes(recipes)
            return recipes.map { it.toRecipe(gson) }

        } catch (e: HttpException) {
            when (e.code()) {
                402 -> throw Exception("API лимит исчерпан. Используются сохраненные данные.")
                401 -> throw Exception("Неверный API ключ")
                429 -> throw Exception("Слишком много запросов")
                else -> throw Exception("Ошибка сервера: ${e.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Search failed: ${e.message}")
        }
    }


    override fun getRecipesPaged(): Flow<PagingData<Recipe>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = {
                RecipesPagingSource(api, dao, networkUtils, gson)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toRecipe(gson) }
        }
    }

    override fun searchRecipesPaged(query: String): Flow<PagingData<Recipe>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = {
                RecipesPagingSource(api, dao, networkUtils, gson, query = query)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toRecipe(gson) }
        }
    }

    override fun getRecipesByCategoryPaged(categoryTag: String): Flow<PagingData<Recipe>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = {
                RecipesPagingSource(api, dao, networkUtils, gson, categoryTag = categoryTag)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toRecipe(gson) }
        }
    }


    private fun RecipeEntity.toRecipe(gson: Gson): Recipe {
        return Recipe(
            id = id,
            title = title,
            image = image,
            summary = summary,
            readyInMinutes = readyInMinutes,
            servings = servings,
            sourceUrl = sourceUrl,
            dishTypes = if (dishTypes != null) {
                try {
                    gson.fromJson(dishTypes, Array<String>::class.java).toList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else emptyList(),
            isBookmarked = isBookmarked
        )
    }

    private fun RecipeDto.toEntity(gson: Gson): RecipeEntity {
        return RecipeEntity(
            id = id,
            title = title,
            image = image,
            summary = summary,
            readyInMinutes = readyInMinutes,
            servings = servings,
            sourceUrl = sourceUrl,
            dishTypes = if (dishTypes != null) gson.toJson(dishTypes) else null
        )
    }
}