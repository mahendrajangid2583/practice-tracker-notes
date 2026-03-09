package com.aashu.privatesuite.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object CollectionDetail : Screen("collection/{collectionId}?taskId={taskId}") {
        fun createRoute(collectionId: String, taskId: String? = null) = 
            if (taskId != null) "collection/$collectionId?taskId=$taskId" else "collection/$collectionId"
    }
    object Editor : Screen("editor/{taskId}") {
        fun createRoute(taskId: String) = "editor/$taskId"
    }
    object Streak : Screen("streak")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object DailyTargetSettings : Screen("daily_target_settings")
    object ReadNote : Screen("read_note/{taskId}") {
        fun createRoute(taskId: String) = "read_note/$taskId"
    }
}
