package com.ddanss.airecipe.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ddanss.airecipe.room.Recipe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray

@Composable
fun ReportRecipeDialog(
    recipe: Recipe,
    onDismissRequest: () -> Unit,
    onSubmitReport: (reason: String) -> Unit
) {
    var reportReason by remember { mutableStateOf("") }
    val recipeScrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .heightIn(max = 700.dp) // Allow slightly more height for this dialog
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Report Recipe",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = reportReason,
                    onValueChange = { reportReason = it },
                    label = { Text( style = MaterialTheme.typography.titleSmall, text = "Why is this content inappropriate?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = false,
                    maxLines = 3,
                    textStyle = TextStyle(fontSize = 16.sp)
                )

                Text(
                    text = "You are reporting the following content:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card( // Inner card to visually group the reported content
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp) // Max height for scrollable content
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(recipeScrollState)
                    ) {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                        Text(
                            text = "Ingredients:",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        val ingredientsJson = Json.parseToJsonElement(recipe.ingredients)
                        ingredientsJson.jsonArray.forEach { ingredientEle ->
                            val ingredient = Json.decodeFromJsonElement<Map<String, String>>(ingredientEle)
                            Text(
                                text = "• " + ingredient["name"] + " (" + ingredient["quantity"] + " " + ingredient["unit"] + ")",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Instructions:",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = recipe.instruction,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmitReport(reportReason) },
                    ) {
                        Text("Submit Report")
                    }
                }
            }
        }
    }
}
