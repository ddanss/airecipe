package com.ddanss.airecipe.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ddanss.airecipe.room.Recipe

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
