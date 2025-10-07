package com.example.spoonacularapi.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeApi {
    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("number") number: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("apiKey") apiKey: String,
        @Query("type") type: String? = null,
        @Query("addRecipeInformation") addRecipeInformation: Boolean = true
    ): RecipesSearchResponse

    @GET("recipes/complexSearch")
    suspend fun getRecipesByCategory(
        @Query("number") number: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("apiKey") apiKey: String,
        @Query("type") type: String? = null,
        @Query("addRecipeInformation") addRecipeInformation: Boolean = true
    ): RecipesSearchResponse
}