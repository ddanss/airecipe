package com.ddanss.airecipe.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ddanss.airecipe.MainApplication
import com.ddanss.airecipe.room.Ingredient
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val db = (context.applicationContext as MainApplication).database
    val scope = rememberCoroutineScope()

    val ingredients = db.ingredientDao().getAll().collectAsState(initial = emptyList())

    val openAddDialog = remember { mutableStateOf(false) }

    Scaffold (
        floatingActionButton = {
            AddButton {
                openAddDialog.value = true
            }
        }
    ) { contentPadding ->
        if (ingredients.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Start by adding your ingredients!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = Color.LightGray
                )
            }
        } else {
            LazyColumn (
                modifier = Modifier.padding(contentPadding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(
                    items = ingredients.value,
                    key = { ingredient -> ingredient.id }
                ) { ingredient ->
                    IngredientRow(ingredient)
                }
            }
        }
    }
    when { openAddDialog.value ->
        AddDialog(
            onDismissRequest = { openAddDialog.value = false },
            onConfirm = {
                scope.launch {
                    val ingredient = Ingredient(name = it, checked = true)
                    db.ingredientDao().insertAll(ingredient)
                }
                openAddDialog.value = false
            }
        )
    }
}
