package com.ddanss.airecipe.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ddanss.airecipe.MainApplication
import com.ddanss.airecipe.room.Ingredient
import com.ddanss.airecipe.room.Recipe
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    var textState by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val db = (context.applicationContext as MainApplication).database
    val ingredientsOnHand = db.ingredientDao().getAllChecked().collectAsState(initial = emptyList())
    val recipes = db.recipeDao().getAll().collectAsState(initial = emptyList())

    var recipeToDisplay by remember { mutableStateOf<Recipe?>(recipes.value.firstOrNull()) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextField(
                value = textState,
                onValueChange = { textState = it },
                label = { Text("I'm feeling like cooking something...") },
                placeholder = { Text("EG: cold and healthy") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            FloatingActionButton (
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val newRecipe = searchForIngredient(
                                context = context,
                                style = textState,
                                ingredientsOnHand = ingredientsOnHand
                            )
                            recipeToDisplay = newRecipe
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text("OK")
            }
        }
        LazyColumn() {
            items(
                items = recipes.value,
                key = { recipe -> recipe.id }
            ) { recipe ->
                RecipeRow(recipe, onClick = {
                    recipeToDisplay = recipe
                })
            }
        }
    }

    recipeToDisplay?.let {
        RecipeDialog(
            recipe = it,
            onDismissRequest = {
                recipeToDisplay = null
            }
        )
    }

    if (isLoading) {
        LoadingDialog()
    }
}

@Composable
fun RecipeRow(recipe: Recipe, onClick: () -> Unit) {
    Text(
        text = recipe.title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() },
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun RecipeDialog(
    recipe: Recipe,
    onDismissRequest: () -> Unit
) {
    val scrollState = rememberScrollState()
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .heightIn(max = 600.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    IconButton(onClick = { onDismissRequest() }) {
                        Icon(Icons.Filled.Close, "Close button")
                    }
                }
                Divider(modifier = Modifier.padding(bottom = 16.dp))

                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val ingredientsJson = Json.parseToJsonElement(recipe.ingredients)
                    ingredientsJson.jsonArray.forEach { ingredientEle ->
                        val ingredient = Json.decodeFromJsonElement<Map<String, String>>(ingredientEle)
                        Text(
                            text = "• " + ingredient["name"] + " (" + ingredient["quantity"] + " " + ingredient["unit"] + ")",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = recipe.instruction,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingDialog() {
    Dialog(onDismissRequest = { /* Dialog cannot be dismissed by clicking outside */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                Text("Searching for recipes...")
            }
        }
    }
}

suspend fun searchForIngredient(context: Context, style: String, ingredientsOnHand: State<List<Ingredient>>): Recipe? {
    val aiModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = Schema.obj(
                    mapOf(
                        "title" to Schema.string(),
                        "ingredients" to Schema.array(
                            Schema.obj(
                                mapOf(
                                    "name" to Schema.string(),
                                    "quantity" to Schema.string(),
                                    "unit" to Schema.string()
                                )
                            )
                        ),
                        "instructions" to Schema.string()
                    )
                )
            })

    var prompt = "Give me one cooking recipe."
    if (style.isNotBlank()) {
        prompt = prompt.plus(" I want something $style.")
    }
    if (ingredientsOnHand.value.isNotEmpty()) {
        prompt = prompt.plus(" The only ingredients I have are ${ingredientsOnHand.value.joinToString(", ") { it.name }}.")
    }
    
    return try {
        val response = aiModel.generateContent(prompt)
        val jsonElement = response.text?.let { Json.parseToJsonElement(it) }
        
        if (jsonElement != null) {
            val title = jsonElement.jsonObject["title"]?.jsonPrimitive?.content
            val ingredients = jsonElement.jsonObject["ingredients"]?.jsonArray ?: buildJsonArray {  }
            val instruction = jsonElement.jsonObject["instructions"]?.jsonPrimitive?.content

            if (title != null && instruction != null) {
                val recipe = Recipe(title = title, ingredients = ingredients.toString(), instruction = instruction)
                val db = (context.applicationContext as MainApplication).database
                withContext(Dispatchers.IO) {
                    db.recipeDao().insertAll(recipe)
                }
                recipe
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Oops, an error occurred while searching for recipes. Please try again.", Toast.LENGTH_LONG).show()
        }
        null
    }
}
