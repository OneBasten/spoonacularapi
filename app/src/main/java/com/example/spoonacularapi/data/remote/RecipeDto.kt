package com.example.spoonacularapi.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecipeDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "image") val image: String?,
    @Json(name = "summary") val summary: String?,
    @Json(name = "readyInMinutes") val readyInMinutes: Int? = 0,
    @Json(name = "servings") val servings: Int? = 0,
    @Json(name = "sourceUrl") val sourceUrl: String?,
    @Json(name = "dishTypes") val dishTypes: List<String>?
)

@JsonClass(generateAdapter = true)
data class RecipesResponse(
    @Json(name = "recipes") val recipes: List<RecipeDto>
)

@JsonClass(generateAdapter = true)
data class RecipesSearchResponse(
    @Json(name = "results") val results: List<RecipeDto>,
    @Json(name = "offset") val offset: Int,
    @Json(name = "number") val number: Int,
    @Json(name = "totalResults") val totalResults: Int
)