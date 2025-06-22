package com.ddanss.airecipe.components

import android.content.Context
import android.util.Log
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
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch


@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    var textState by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val db = (context.applicationContext as MainApplication).database
    val ingredients = db.ingredientDao().getAll().collectAsState(initial = emptyList())
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
                            ingredients = ingredients
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
            items(texts) { text ->
                Text(text)
            }
        }
    }

}

suspend fun searchForIngredient(context: Context, style: String, ingredients: State<List<Ingredient>>) {
    val aiModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    val prompt = "Give me one cooking recipe. I want something that is $style."
    val response = aiModel.generateContent(prompt)
    Log.d("AI", "Response: $response")

}


// test array of texts.
val texts = listOf(
    "This is a sample text",
    "This is another sample text",
    "This is a third sample text",
    "This is a fourth sample text",
    "This is a fifth sample text",
)