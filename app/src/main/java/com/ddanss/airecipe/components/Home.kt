package com.ddanss.airecipe.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.ddanss.airecipe.MainApplication
import com.ddanss.airecipe.room.Ingredient
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val db = (context.applicationContext as MainApplication).database
    val scope = rememberCoroutineScope()

    val ingredients = db.ingredientDao().getAll().collectAsState(initial = emptyList())

    Scaffold (
        floatingActionButton = {
            AddButton {
                scope.launch {
                    val ingredient = Ingredient(name = "test", exists = true)
                    db.ingredientDao().insertAll(ingredient)
                }
            }
        }
    ) { contentPadding ->
        LazyColumn (contentPadding = contentPadding) {
            items(
                items = ingredients.value,
                key = { ingredient -> ingredient.id }
            ) { ingredient ->
                Text(text = ingredient.name)
            }
        }
    }
}

@Composable
fun AddButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = { onClick() },
        icon = { Icon(Icons.Filled.Add, "Add button") },
        text = { Text(text = "Add Ingredient") }
    )
}
