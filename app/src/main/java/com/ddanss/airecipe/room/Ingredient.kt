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
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "checked") val checked: Boolean,
)

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredient")
    fun getAll(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredient WHERE id IN (:ingredientIds)")
    suspend fun loadAllByIds(ingredientIds: IntArray): List<Ingredient>

    @Query("SELECT * FROM ingredient WHERE name LIKE :name LIMIT 1")
    suspend fun findByName(name: String): Ingredient

    @Insert
    suspend fun insertAll(vararg ingredients: Ingredient)

    @Delete
    suspend fun delete(ingredient: Ingredient)

    @Query("DELETE FROM ingredient")
    suspend fun deleteAll()

    @Query("UPDATE ingredient SET checked = :checked WHERE id = :id")
    suspend fun updateChecked(id: Int, checked: Boolean)

    @Query("SELECT * FROM ingredient WHERE checked = 1")
    fun getAllChecked(): Flow<List<Ingredient>>
}