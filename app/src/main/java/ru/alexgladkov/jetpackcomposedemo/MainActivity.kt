package ru.alexgladkov.jetpackcomposedemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import ru.alexgladkov.jetpackcomposedemo.screens.daily.DailyScreen
import ru.alexgladkov.jetpackcomposedemo.screens.daily.DailyViewModel
import ru.alexgladkov.jetpackcomposedemo.screens.main.MainBottomScreen
import ru.alexgladkov.jetpackcomposedemo.screens.settings.SettingsScreen
import ru.alexgladkov.jetpackcomposedemo.screens.tabs.dailyFlow
import ru.alexgladkov.jetpackcomposedemo.screens.tabs.settingsFlow
import ru.alexgladkov.jetpackcomposedemo.ui.themes.JetHabbitCorners
import ru.alexgladkov.jetpackcomposedemo.ui.themes.JetHabbitSize
import ru.alexgladkov.jetpackcomposedemo.ui.themes.JetHabbitStyle
import ru.alexgladkov.jetpackcomposedemo.ui.themes.JetHabbitTheme
import ru.alexgladkov.jetpackcomposedemo.ui.themes.MainTheme
import ru.alexgladkov.jetpackcomposedemo.ui.themes.baseDarkPalette
import ru.alexgladkov.jetpackcomposedemo.ui.themes.baseLightPalette

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkModeValue = true // isSystemInDarkTheme()

            val currentStyle = remember { mutableStateOf(JetHabbitStyle.Purple) }
            val currentFontSize = remember { mutableStateOf(JetHabbitSize.Medium) }
            val currentPaddingSize = remember { mutableStateOf(JetHabbitSize.Medium) }
            val currentCornersStyle = remember { mutableStateOf(JetHabbitCorners.Rounded) }
            val isDarkMode = remember { mutableStateOf(isDarkModeValue) }

            MainTheme(
                style = currentStyle.value,
                darkTheme = isDarkMode.value,
                textSize = currentFontSize.value,
                corners = currentCornersStyle.value,
                paddingSize = currentPaddingSize.value
            ) {
                val navController = rememberNavController()
                val systemUiController = rememberSystemUiController()

                // Set status bar color
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = if (isDarkMode.value) baseDarkPalette.primaryBackground else baseLightPalette.primaryBackground,
                        darkIcons = !isDarkMode.value
                    )
                }

                // Navigation Items
                val items = listOf(
                    MainBottomScreen.Daily,
                    MainBottomScreen.Settings,
                )

                Scaffold(
                    bottomBar = {
                        BottomNavigation {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            items.forEach { screen ->
                                val isSelected = currentDestination?.hierarchy
                                    ?.any { it.route == screen.route } == true

                                BottomNavigationItem(
                                    modifier = Modifier.background(JetHabbitTheme.colors.primaryBackground),
                                    icon = {
                                        Icon(
                                            imageVector = when (screen) {
                                                MainBottomScreen.Daily -> Icons.Filled.Favorite
                                                MainBottomScreen.Settings -> Icons.Filled.Settings
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) JetHabbitTheme.colors.tintColor else JetHabbitTheme.colors.controlColor
                                        )
                                    },
                                    label = { stringResource(id = screen.resourceId) },
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }

                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    })
                            }
                        }
                    }
                ) { paddingValues ->
                    NavHost(navController = navController, startDestination = MainBottomScreen.Daily.route) {
                        dailyFlow(navController, paddingValues)

                        composable(MainBottomScreen.Settings.route) {
                            SettingsScreen(
                                modifier = Modifier.padding(paddingValues),
                                isDarkMode = isDarkMode.value,
                                currentTextSize = currentFontSize.value,
                                currentPaddingSize = currentPaddingSize.value,
                                currentCornersStyle = currentCornersStyle.value,
                                onDarkModeChanged = {
                                    isDarkMode.value = it
                                },
                                onNewStyle = {
                                    currentStyle.value = it
                                },
                                onTextSizeChanged = {
                                    currentFontSize.value = it
                                },
                                onCornersStyleChanged = {
                                    currentCornersStyle.value = it
                                },
                                onPaddingSizeChanged = {
                                    currentPaddingSize.value = it
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}