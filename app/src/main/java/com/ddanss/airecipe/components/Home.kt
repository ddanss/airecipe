package com.ddanss.airecipe.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = ingredient.name,
                    fontSize = 20.sp
                )
            }
        }
    }
    when { openAddDialog.value ->
        AddDialog(
            onDismissRequest = { openAddDialog.value = false },
            onConfirm = {
                scope.launch {
                    val ingredient = Ingredient(name = "carrot", exists = true)
                    db.ingredientDao().insertAll(ingredient)
                    val ingredient2 = Ingredient(name = "onion", exists = true)
                    db.ingredientDao().insertAll(ingredient2)
                    val ingredient3 = Ingredient(name = "potato", exists = true)
                    db.ingredientDao().insertAll(ingredient3)
                    val ingredient4 = Ingredient(name = "tofu", exists = true)
                    db.ingredientDao().insertAll(ingredient4)
                }
                openAddDialog.value = false
            }
        )
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
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        ) {
            Text(text = "Add Ingredient")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onDismissRequest() }) { Text(text = "Cancel") }
                TextButton(onClick = { onConfirm() }) { Text(text = "Add") }
            }
        }
    }
}