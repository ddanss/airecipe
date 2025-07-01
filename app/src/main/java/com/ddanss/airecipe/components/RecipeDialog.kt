package com.ddanss.airecipe.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ddanss.airecipe.room.Recipe
import com.ddanss.airecipe.room.toReportString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray

@Composable
fun RecipeDialog(
    recipe: Recipe,
    onDismissRequest: () -> Unit,
    scope: CoroutineScope
) {
    val scrollState = rememberScrollState()
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    IconButton(onClick = { onDismissRequest() }) {
                        Icon(Icons.Filled.Close, "Close button")
                    }
                }
                HorizontalDivider(
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                Button(
                    onClick = {
                        showReportDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Report Content")
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Report Content Icon",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

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

    if (showReportDialog) {
        ReportRecipeDialog(
            recipe = recipe,
            onDismissRequest = { showReportDialog = false },
            onSubmitReport = { reason ->
                showReportDialog = false // Dismiss the report dialog
                // Optionally dismiss the main recipe dialog as well, or show a confirmation
                // onDismissRequest() 
                scope.launch {
                    submitReport(contentId = recipe.id.toString(), recipe = recipe.toReportString(), reason = reason)
                    // Show a toast or some confirmation to the user
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Report submitted. Thank you!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
