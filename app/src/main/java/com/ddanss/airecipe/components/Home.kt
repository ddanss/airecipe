package com.ddanss.airecipe.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
        LazyColumn (contentPadding = contentPadding) {
            items(
                items = ingredients.value,
                key = { ingredient -> ingredient.id }
            ) { ingredient ->
                IngredientRow(ingredient)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientRow(ingredient: Ingredient) {
    val context = LocalContext.current
    val db = (context.applicationContext as MainApplication).database
    val scope = rememberCoroutineScope()
    val ingredientsDB = db.ingredientDao()

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                scope.launch {
                    ingredientsDB.delete(ingredient)
                }
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val color by animateColorAsState(
                when (state.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                    SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.8f)
                    else -> Color.LightGray.copy(alpha = 0.5f)
                }, label = "background color"
            )
            val alignment = when (state.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart // Swiping right
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd   // Swiping left
                else -> Alignment.Center
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Icon",
                    tint = Color.White
                )
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface) // Add background to prevent transparency during swipe
                .padding(vertical = 8.dp), // Add padding that was removed from Text
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp) // Adjusted padding
                    .weight(1f)
                    .padding(end = 8.dp),
                text = ingredient.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Checkbox(checked = ingredient.checked, onCheckedChange = {
                scope.launch {
                    ingredientsDB.updateChecked(ingredient.id, it)
                }
            })
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

@Composable
fun AddDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (text: String) -> Unit
) {
    var textState by remember { mutableStateOf("") }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextField(
                value = textState,
                onValueChange = { textState = it },
                label = { Text("Add an ingredient") },
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onDismissRequest() }) { Text(text = "Cancel") }
                TextButton(onClick = { onConfirm(textState) }) { Text(text = "Add") }
            }
        }
    }
}
