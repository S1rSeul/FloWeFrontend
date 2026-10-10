package com.example.abobaproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.abobaproject.ui.theme.AbobaProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AbobaProjectTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel()
                val taskViewModel: TaskViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Auth.route
                ) {
                    // 1. Экран входа/регистрации (AND-01 / AND-02)
                    composable(Screen.Auth.route) {
                        AuthScreen(
                            onAuthSuccess = {
                                if (mainViewModel.hasSpace) {
                                    navController.navigate(Screen.TaskList.route) {
                                        popUpTo(Screen.Auth.route) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(Screen.NoSpace.route) {
                                        popUpTo(Screen.Auth.route) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    // 2. Экран "Нет пространства" (AND-11)
                    composable(Screen.NoSpace.route) {
                        NoSpaceScreen(
                            onCreateSpaceClick = {
                                val code = mainViewModel.createNewSpace()
                                navController.navigate(Screen.WaitingPartner.createRoute(code))
                            },
                            onJoinSpaceClick = { code ->
                                if (mainViewModel.joinSpaceByCode(code)) {
                                    navController.navigate(Screen.SpaceConnected.route) {
                                        popUpTo(Screen.NoSpace.route) { inclusive = true }
                                    }
                                }
                            },
                            onLogoutClick = {
                                mainViewModel.logout()
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0)
                                }
                            }
                        )
                    }

                    // 3. Экран "Ожидание партнёра" (AND-12 / AND-13)
                    composable(
                        route = Screen.WaitingPartner.route,
                        arguments = listOf(navArgument("inviteCode") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val inviteCode = backStackEntry.arguments?.getString("inviteCode") ?: ""
                        WaitingPartnerScreen(
                            inviteCode = inviteCode,
                            onPartnerConnected = {
                                mainViewModel.onPartnerJoined()
                                navController.navigate(Screen.SpaceConnected.route) {
                                    popUpTo(Screen.NoSpace.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 4. Экран после подключения к пространству (AND-21)
                    composable(Screen.SpaceConnected.route) {
                        SpaceConnectedScreen(
                            isPartnerOnline = taskViewModel.isPartnerOnline,
                            onNavigateToTasks = {
                                navController.navigate(Screen.TaskList.route) {
                                    popUpTo(Screen.SpaceConnected.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 5. Экран списка задач TaskTracker (AND-22, AND-19, AND-30, AND-32)
                    composable(Screen.TaskList.route) {
                        TaskListScreen(
                            taskViewModel = taskViewModel,
                            onCreateTaskClick = {
                                navController.navigate(Screen.CreateTask.route)
                            },
                            onTaskClick = { taskId ->
                                navController.navigate(Screen.TaskDetail.createRoute(taskId))
                            }
                        )
                    }

                    // 6. Экран создания задачи (AND-23)
                    composable(Screen.CreateTask.route) {
                        CreateTaskScreen(
                            onTaskCreated = { title, description, deadlineMillis, assignee ->
                                taskViewModel.addTask(title, description, deadlineMillis, assignee)
                                navController.popBackStack()
                            },
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // 7. Экран деталей задачи (AND-24)
                    composable(
                        route = Screen.TaskDetail.route,
                        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                        TaskDetailScreen(
                            taskId = taskId,
                            taskViewModel = taskViewModel,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // 8. Главный экран
                    composable(Screen.Main.route) {
                        MainScreen(
                            onLogoutClick = {
                                mainViewModel.logout()
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
