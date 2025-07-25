package com.location.distancefinder.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.location.distancefinder.presentation.screens.home.HomeScreen
import com.location.distancefinder.presentation.screens.map.MapScreen
import com.location.distancefinder.presentation.screens.search.SearchScreen
import com.location.distancefinder.presentation.screens.favorites.FavoritesScreen

@Composable
fun LocationFinderNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToMap = {
                    navController.navigate("map")
                },
                onNavigateToSearch = {
                    navController.navigate("search")
                },
                onNavigateToFavorites = {
                    navController.navigate("favorites")
                }
            )
        }
        
        composable("map") {
            MapScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("search") {
            SearchScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPlaceSelected = { place ->
                    // Navigate to map with selected place
                    navController.navigate("map")
                }
            )
        }
        
        composable("favorites") {
            FavoritesScreen(
                onBack = {
                    navController.popBackStack()
                },
                onFavoriteSelected = { favorite ->
                    // Navigate to map with favorite location
                    navController.navigate("map")
                }
            )
        }
    }
}