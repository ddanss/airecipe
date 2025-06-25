package com.ddanss.airecipe.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "ingredients") val ingredients: String,
    @ColumnInfo(name = "instruction") val instruction: String,
)

fun Recipe.toReportString(): String {
    return this.title+"\n"+this.ingredients+"\n"+this.instruction
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe ORDER BY id DESC")
    fun getAll(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipe WHERE id IN (:recipeIds)")
    fun loadAllByIds(recipeIds: IntArray): List<Recipe>

    @Query("SELECT * FROM recipe WHERE title LIKE :title LIMIT 1")
    fun findByTitle(title: String): Recipe

    @Insert
    fun insertAll(vararg recipes: Recipe)

    @Delete
    fun delete(recipe: Recipe)

    @Query("DELETE FROM recipe")
    fun deleteAll()
}
