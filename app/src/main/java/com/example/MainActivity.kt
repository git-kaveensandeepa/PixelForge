package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.navigation.Screen
import com.example.ui.prototype.PrototypePlayerScreen
import com.example.ui.theme.PixelForgeTheme
import com.example.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        enableEdgeToEdge()
        setContent {
            PixelForgeTheme {
                PixelForgeApp()
            }
        }
    }
}

@Composable
fun PixelForgeApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as Application

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = Modifier.fillMaxSize()
    ) {
        // Project Dashboard
        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = viewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onOpenProject = { projectId ->
                    navController.navigate(Screen.Editor.createRoute(projectId))
                },
                onOpenPrototype = { projectId ->
                    navController.navigate(Screen.PrototypePlayer.createRoute(projectId))
                }
            )
        }

        // Mobile UI Canvas Editor
        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val editorViewModel: EditorViewModel = viewModel(
                key = "editor_$projectId",
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return EditorViewModel(app, projectId) as T
                    }
                }
            )

            EditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRunPrototype = { id ->
                    navController.navigate(Screen.PrototypePlayer.createRoute(id))
                }
            )
        }

        // Interactive Prototype Player
        composable(
            route = Screen.PrototypePlayer.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            PrototypePlayerScreen(
                projectId = projectId,
                onClose = {
                    navController.popBackStack()
                }
            )
        }
    }
}
