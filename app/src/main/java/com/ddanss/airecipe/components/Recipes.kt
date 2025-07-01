package com.ddanss.airecipe.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
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
import com.ddanss.airecipe.MainApplication
import com.ddanss.airecipe.room.Ingredient
import com.ddanss.airecipe.room.Recipe
import com.ddanss.airecipe.room.toReportString
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val functions: FirebaseFunctions = Firebase.functions // Or Firebase.functions("your-region") if not us-central1

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
            },
            scope = scope
        )
    }

    if (isLoading) {
        LoadingDialog()
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

suspend fun submitReport(contentId: String, recipe: String, reason: String?) {
    val data = hashMapOf(
        "contentId" to contentId,
        "recipe" to recipe,
        "reason" to reason
    )

    try {
        val result = functions
            .getHttpsCallable("reportContent") // Must match the exported function name
            .call(data)
            .await() // Use await() for suspend function

        // result.data will be a Map<String, Any>
        val success = (result.data as? Map<String, Any>)?.get("success") as? Boolean
        if (success == true) {
            // Show success message to the user (e.g., Toast)
            // Toast.makeText(context, "Report submitted successfully!", Toast.LENGTH_SHORT).show()
            println("Report submitted successfully: ${(result.data as Map<String, Any>)["reportId"]}")
        } else {
            // Handle error or success == false
            // Toast.makeText(context, "Failed to submit report.", Toast.LENGTH_SHORT).show()
            println("Report submission failed or returned success:false. Data: ${result.data}")
        }
    } catch (e: Exception) {
        // Handle exceptions (e.g., network error, function error)
        // Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        println("Error calling reportContent function: ${e.message}")
        e.printStackTrace()
    }
}
