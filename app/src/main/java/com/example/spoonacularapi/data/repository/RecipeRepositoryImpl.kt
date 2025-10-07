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