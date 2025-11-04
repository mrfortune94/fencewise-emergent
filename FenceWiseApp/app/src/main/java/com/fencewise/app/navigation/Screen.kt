package com.fencewise.app.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Jobs : Screen("jobs")
    object Timesheets : Screen("timesheets")
    object Chat : Screen("chat")
}
