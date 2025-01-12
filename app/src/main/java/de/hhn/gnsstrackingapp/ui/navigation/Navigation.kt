package de.hhn.gnsstrackingapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.hhn.gnsstrackingapp.ui.screens.map.LocationViewModel
import de.hhn.gnsstrackingapp.ui.screens.map.MapScreen
import de.hhn.gnsstrackingapp.ui.screens.map.MapViewModel
import de.hhn.gnsstrackingapp.ui.screens.settings.SettingsScreen
import de.hhn.gnsstrackingapp.ui.screens.settings.SettingsViewModel
import de.hhn.gnsstrackingapp.ui.screens.statistics.StatisticsViewModel

@Composable
fun MainNavigation(
    navHostController: NavHostController = rememberNavController(),
    mapViewModel: MapViewModel,
    locationViewModel: LocationViewModel,
    statisticsViewModel: StatisticsViewModel,
    settingsViewModel: SettingsViewModel,
    //webServicesProvider: WebServicesProvider,
    navigationViewModel: NavigationViewModel,
    isFullscreen: MutableState<Boolean>
) {
    //val isFullscreen = remember { mutableStateOf(false) }

    NavHost(navController = navHostController, startDestination = Screen.MapScreen.route) {
        composable(Screen.MapScreen.route) {
            MapScreen(
                mapViewModel,
                locationViewModel,
                navigationViewModel,
                isFullscreen
            )
        }
        composable(Screen.StatisticsScreen.route) {

        }
        composable(Screen.SettingsScreen.route) { SettingsScreen(settingsViewModel) }
    }
}
