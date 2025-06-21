package com.ddanss.airecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("home", "Home", Icons.Default.Home, "Home"),
    HISTORY("history", "History", Icons.Default.Favorite, "History")
}

@Composable
fun MainNavigationTab(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = Destination.HOME
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            selectedDestination = index
                            navController.navigate(destination.route)
                        },
                        icon = {
                            Icon(destination.icon, contentDescription = destination.contentDescription)
                        },
                        label = {
                            Text(destination.label)
                        }
                    )
                }
            }
        }
    ){ contentPadding ->
        NavHost(navController, startDestination = startDestination.route) {
            Destination.entries.forEach { destination ->
                composable(destination.route) {
                    when(destination) {
                        Destination.HOME -> HomeScreen(contentPadding)
                        Destination.HISTORY -> HistoryScreen(contentPadding)

                    }
                }
            }
        }

    }

}

@Composable
fun HistoryScreen(contentPadding: PaddingValues) {
    TODO("Not yet implemented")
}

@Composable
fun HomeScreen(contentPadding: PaddingValues) {

}
