package com.example.abobaproject

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Auth : Screen("auth_screen")
    object Routing : Screen("routing_screen")
    object NoSpace : Screen("no_space_screen")
    object CreateSpace : Screen("create_space_screen")
    object WaitingPartner : Screen("waiting_partner_screen/{inviteCode}") {
        fun createRoute(inviteCode: String) = "waiting_partner_screen/$inviteCode"
    }
    object SpaceConnected : Screen("space_connected_screen")
    object TaskList : Screen("task_list_screen")
    object CreateTask : Screen("create_task_screen")
    object TaskDetail : Screen("task_detail_screen/{taskId}") {
        fun createRoute(taskId: String) = "task_detail_screen/$taskId"
    }
    object Main : Screen("main_screen")
}
