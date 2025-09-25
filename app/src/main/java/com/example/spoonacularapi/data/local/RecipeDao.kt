package com.example.spoonacularapi.data.local


import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Query("SELECT * FROM recipes ORDER BY timestamp DESC")
    fun getRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): RecipeEntity?

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE dishTypes LIKE '%' || :categoryTag || '%' ORDER BY timestamp DESC")
    fun getRecipesByCategory(categoryTag: String): Flow<List<RecipeEntity>>

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE timestamp < :timestamp")
    suspend fun deleteOldRecipes(timestamp: Long)

    // методы для пагинации

    @Query("DELETE FROM recipes WHERE searchQuery = :query")
    suspend fun deleteSearchResults(query: String)

    @Query("DELETE FROM recipes WHERE categoryTag = :categoryTag")
    suspend fun deleteCategoryResults(categoryTag: String)
}