package de.hhn.gnsstrackingapp.ui.navigation

sealed class Screen(
    val route: String
) {
    data object MapScreen : Screen("map_screen")
    data object StatisticsScreen : Screen("stat_screen")
    data object SettingsScreen : Screen("settings_screen")
}
