package com.example.spoonacularapi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.spoonacularapi.data.local.RecipeDao
import com.example.spoonacularapi.data.local.RecipeDatabase
import com.example.spoonacularapi.data.local.RecipeEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeDaoTest {
    private lateinit var database: RecipeDatabase
    private lateinit var dao: RecipeDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RecipeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.recipeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertRecipes_and_getRecipes_shouldReturnAllRecipes() = runTest {
        // ARRANGE (ПОДГОТОВКА) - создаем тестовые данные
        val testRecipes = listOf(
            createTestRecipeEntity(1, "Pasta"),
            createTestRecipeEntity(2, "Pizza")
        )

        dao.insertRecipes(testRecipes)
        val result = dao.getRecipes().first()

        assertThat(result).hasSize(2)
        assertThat(result).containsExactlyElementsIn(testRecipes)
    }

    @Test
    fun searchRecipes_shouldReturnMatchingResults() = runTest {

        val pastaRecipe = createTestRecipeEntity(1, "Spaghetti Carbonara")
        val pizzaRecipe = createTestRecipeEntity(2, "Margherita Pizza")
        dao.insertRecipes(listOf(pastaRecipe, pizzaRecipe))

        val result = dao.searchRecipes("Pizza").first()

        assertThat(result).hasSize(1)
        assertThat(result[0].title).contains("Pizza")
        assertThat(result[0].title).doesNotContain("Pasta")
    }


    @Test
    fun getRecipesByCategory_shouldFilterByDishTypes() = runTest {

        val dessert = createTestRecipeEntity(1, "Cake", dishTypes = "[\"dessert\"]")
        val mainCourse = createTestRecipeEntity(2, "Steak", dishTypes = "[\"main course\"]")
        dao.insertRecipes(listOf(dessert, mainCourse))

        val result = dao.getRecipesByCategory("dessert").first()

        // ASSERT
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Cake")
        assertThat(result[0].title).isNotEqualTo("Steak")
    }

    @Test
    fun toggleBookmark_shouldUpdateIsBookmarked() = runTest {
        val recipe = createTestRecipeEntity(1, "Test Recipe", isBookmarked = false)
        dao.insertRecipe(recipe)

        val retrieved = dao.getRecipeById(1)!!
        dao.updateRecipe(retrieved.copy(isBookmarked = true))
        val updated = dao.getRecipeById(1)!!

        assertThat(updated.isBookmarked).isTrue()
        assertThat(updated.isBookmarked).isNotEqualTo(recipe.isBookmarked)
    }

    private fun createTestRecipeEntity(
        id: Int,
        title: String,
        dishTypes: String? = null,
        isBookmarked: Boolean = false
    ): RecipeEntity {
        return RecipeEntity(
            id = id,
            title = title,
            image = null,
            summary = null,
            readyInMinutes = 0,
            servings = 0,
            sourceUrl = null,
            dishTypes = dishTypes,
            isBookmarked = isBookmarked,
            timestamp = System.currentTimeMillis()
        )
    }
}