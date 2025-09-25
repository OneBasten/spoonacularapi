package com.example.spoonacularapi.di

import android.content.Context
import androidx.room.Room
import com.example.spoonacularapi.data.NetworkStatusManager
import com.example.spoonacularapi.data.NetworkUtils
import com.example.spoonacularapi.data.local.RecipeDao
import com.example.spoonacularapi.data.local.RecipeDatabase
import com.example.spoonacularapi.data.remote.RecipeApi
import com.example.spoonacularapi.data.repository.RecipeRepositoryImpl
import com.example.spoonacularapi.domain.repository.RecipeRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRecipeDatabase(@ApplicationContext context: Context): RecipeDatabase {
        return Room.databaseBuilder(
            context,
            RecipeDatabase::class.java,
            "recipes.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideRecipeDao(database: RecipeDatabase) = database.recipeDao()

    @Provides
    @Singleton
    fun provideRecipeApi(): RecipeApi {
        return Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(RecipeApi::class.java)
    }
    @Provides
    @Singleton
    fun provideNetworkStatusManager(@ApplicationContext context: Context): NetworkStatusManager {
        return NetworkStatusManager(context)
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(
        @ApplicationContext context: Context,
        networkStatusManager: NetworkStatusManager
    ): NetworkUtils {
        return NetworkUtils(context, networkStatusManager)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        api: RecipeApi,
        dao: RecipeDao,
        networkUtils: NetworkUtils,
        gson: Gson
    ): RecipeRepository {
        return RecipeRepositoryImpl(api, dao, networkUtils, gson)
    }

}