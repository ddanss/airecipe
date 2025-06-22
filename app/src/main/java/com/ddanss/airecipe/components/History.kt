package com.ddanss.airecipe.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.max
import androidx.compose.ui.window.Dialog
import com.ddanss.airecipe.MainApplication
import com.ddanss.airecipe.room.Ingredient
import com.ddanss.airecipe.room.Recipe
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    val db = (context.applicationContext as MainApplication).database
    val ingredientsOnHand = db.ingredientDao().getAllChecked().collectAsState(initial = emptyList())
    val recipes = db.recipeDao().getAll().collectAsState(initial = emptyList())

    var recipeToDisplay by remember { mutableStateOf(recipes.value.firstOrNull()) }

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
                placeholder = { Text("hot, spicy, healthy") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            FloatingActionButton (
                onClick = {
                    scope.launch {
                        searchForIngredient(
                            context = context,
                            style = textState,
                            ingredientsOnHand = ingredientsOnHand
                        )
                    } },
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
}

@Composable
fun RecipeRow(recipe: Recipe, onClick: () -> Unit) {
    Text(
        text = recipe.title,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text( modifier = Modifier.weight(1f).padding(end = 8.dp), text = "Title: " + recipe.title)
                IconButton( onClick = { onDismissRequest() }) {
                    Icon(Icons.Filled.Close, "Close button")
                }
            }
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                val ingredientsJson = Json.parseToJsonElement(recipe.ingredients)
                ingredientsJson.jsonArray.forEach { ingredientEle ->
                    val ingredient = Json.decodeFromJsonElement<Map<String, String>>(ingredientEle)
                    Text(text = " " + ingredient["name"] + " (" + ingredient["quantity"] + " " + ingredient["unit"] + ")")
                }
                Text(text = recipe.instruction, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun searchForIngredient(context: Context, style: String, ingredientsOnHand: State<List<Ingredient>>) {
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
        prompt = prompt.plus(" I have only ${ingredientsOnHand.value.joinToString(", ")} on hand.")
    }
    val response = aiModel.generateContent(prompt)

    val jsonElement = response.text?.let { Json.parseToJsonElement(it) }
    if (jsonElement != null) {
        val title = jsonElement.jsonObject["title"]?.jsonPrimitive?.content
        val ingredients = jsonElement.jsonObject["ingredients"]?.jsonArray ?: buildJsonArray {  }
        val instruction = jsonElement.jsonObject["instructions"]?.jsonPrimitive?.content

        val db = (context.applicationContext as MainApplication).database
        GlobalScope.launch(Dispatchers.IO) {
            val recipe = Recipe(title = title ?: "", ingredients = ingredients.toString(), instruction = instruction ?: "")
            db.recipeDao().insertAll(recipe)
        }
    }
}
