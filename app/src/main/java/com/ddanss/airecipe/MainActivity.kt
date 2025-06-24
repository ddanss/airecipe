package com.ddanss.airecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ddanss.airecipe.components.HistoryScreen
import com.ddanss.airecipe.components.HomeScreen
import com.ddanss.airecipe.ui.theme.AirecipeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AirecipeTheme {
                MainNavigationTab()
            }
        }
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val icon: Any,
    val contentDescription: String
) {
    INGREDIENTS("ingredients", "Ingredients", R.drawable.icon_ingredients, "Ingredients"),
    RECIPES("recipes", "Recipes", R.drawable.icon_recipes, "Recipes")
}

@Composable
fun MainNavigationTab(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destination.INGREDIENTS
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        modifier = Modifier.height(40.dp),
                        selected = selectedDestination == index,
                        onClick = {
                            selectedDestination = index
                            navController.navigate(destination.route)
                        },
                        icon = {
                            when (destination.icon) {
                                is ImageVector -> {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.contentDescription
                                    )
                                }
                                is Int -> {
                                    Icon(
                                        painter = painterResource(id = destination.icon),
                                        contentDescription = destination.contentDescription
                                    )
                                }
                            }
                        },
                        label = {
                            Text(destination.label)
                        }
                    )
                }
            }
        }
    ){ contentPadding ->
        NavHost(navController, startDestination = startDestination.route, modifier = Modifier.padding(contentPadding)) {
            Destination.entries.forEach { destination ->
                composable(destination.route) {
                    when(destination) {
                        Destination.INGREDIENTS -> HomeScreen()
                        Destination.RECIPES -> HistoryScreen()

                    }
                }
            }
        }
    }
}
