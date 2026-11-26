package com.eduardo.alrededor.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eduardo.alrededor.ui.detail.DetailRoute
import com.eduardo.alrededor.ui.explore.ExploreRoute
import com.eduardo.alrededor.ui.permission.PermissionRoute
import com.eduardo.alrededor.ui.settings.SettingsRoute

object Routes {
    const val PERMISOS = "permisos"
    const val EXPLORAR = "explorar"
    const val DETALLE = "detalle/{idPagina}"
    const val AJUSTES = "ajustes"

    fun detalle(idPagina: Long) = "detalle/$idPagina"
}

@Composable
fun GuiaNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.PERMISOS) {

        composable(Routes.PERMISOS) {
            PermissionRoute(
                onContinue = {
                    navController.navigate(Routes.EXPLORAR) {
                        popUpTo(Routes.PERMISOS) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EXPLORAR) {
            ExploreRoute(
                onPlaceClick = { id -> navController.navigate(Routes.detalle(id)) },
                onSettingsClick = { navController.navigate(Routes.AJUSTES) }
            )
        }

        composable(
            route = Routes.DETALLE,
            arguments = listOf(navArgument("idPagina") { type = NavType.LongType })
        ) {
            DetailRoute(onBack = { navController.popBackStack() })
        }

        composable(Routes.AJUSTES) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text) }
}
