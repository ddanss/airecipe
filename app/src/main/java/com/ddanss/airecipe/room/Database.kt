package com.ddanss.airecipe.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Ingredient::class, Recipe::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
}
