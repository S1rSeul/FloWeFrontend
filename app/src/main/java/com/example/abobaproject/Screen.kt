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

    object QrScanner : Screen("qr_scanner_screen")

    object Calendar : Screen("calendar_screen")
    object CreateEvent : Screen("create_event_screen")

    object EditEvent : Screen("edit_event_screen/{eventId}") {
        fun createRoute(eventId: String) = "edit_event_screen/$eventId"
    }

    object SavingsList : Screen("savings_list_screen")
    object CreateSavings : Screen("create_savings_screen")

    object CreateExpense : Screen("create_expense_screen")
    object ExpenseHistory : Screen("expense_history_screen")
    object Budgets : Screen("budgets_screen")
}
