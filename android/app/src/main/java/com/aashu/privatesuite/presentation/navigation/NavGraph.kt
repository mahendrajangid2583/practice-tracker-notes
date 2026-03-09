package com.aashu.privatesuite.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.aashu.privatesuite.presentation.dashboard.DashboardScreen
import com.aashu.privatesuite.presentation.collection.CollectionDetailScreen
import com.aashu.privatesuite.presentation.editor.EditorScreen
import com.aashu.privatesuite.presentation.streak.StreakScreen
import com.aashu.privatesuite.presentation.login.LoginScreen
import com.aashu.privatesuite.presentation.search.SearchScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(
            route = Screen.CollectionDetail.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.StringType },
                navArgument("taskId") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
             val taskId = backStackEntry.arguments?.getString("taskId")
             CollectionDetailScreen(navController = navController, highlightTaskId = taskId)
        }
        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            EditorScreen(
                taskId = taskId, 
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Streak.route) {
            StreakScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            com.aashu.privatesuite.presentation.settings.SettingsScreen(navController = navController)
        }
        composable(Screen.DailyTargetSettings.route) {
            com.aashu.privatesuite.presentation.settings.targets.DailyTargetSettingsScreen(navController = navController)
        }
        composable(
            route = Screen.ReadNote.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            com.aashu.privatesuite.presentation.note.ReadNoteScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onNavigateToTask = { collectionId, taskId ->
                    navController.navigate(Screen.CollectionDetail.createRoute(collectionId, taskId))
                }
            )
        }
    }
}
