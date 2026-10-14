package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Editor : Screen("editor/{projectId}") {
        fun createRoute(projectId: String) = "editor/$projectId"
    }
    object PrototypePlayer : Screen("prototype/{projectId}") {
        fun createRoute(projectId: String) = "prototype/$projectId"
    }
}
