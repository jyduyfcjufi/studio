package com.example.aistudio.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.aistudio.viewmodels.AppViewModel
import com.example.aistudio.viewmodels.SettingsViewModel

private sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Chat : BottomNavItem("chat", Icons.Default.Chat, "Chat")
    object Models : BottomNavItem("models", Icons.Default.DataObject, "Models")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}

private val bottomNavItems = listOf(
    BottomNavItem.Chat,
    BottomNavItem.Models,
    BottomNavItem.Settings,
)

@OptIn(ExperimentalMaterial3.class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // 在顶层创建共享的 ViewModel 实例
    val appViewModel: AppViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Chat.route,
            Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Chat.route) {
                InferenceScreen(
                    appViewModel = appViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
            composable(BottomNavItem.Models.route) {
                ModelListScreen(
                    onModelSelected = { model ->
                        // 当模型被选择时，更新 AppViewModel 并导航到聊天页
                        appViewModel.selectModel(model)
                        navController.navigate(BottomNavItem.Chat.route) {
                            // 清除返回栈，使用户不能从聊天页返回到模型列表
                            popUpTo(navController.graph.findStartDestination().id)
                        }
                    }
                )
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}