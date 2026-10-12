package com.example.abobaproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.abobaproject.data.local.TokenManager
import com.example.abobaproject.ui.theme.AbobaProjectTheme
import kotlinx.coroutines.flow.firstOrNull

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
                    startDestination = Screen.Splash.route
                ) {
                    // 0. Splash — проверка авто-логина (AND-08)
                    composable(Screen.Splash.route) {
                        val context = LocalContext.current
                        val tokenManager = remember { TokenManager(context) }

                        LaunchedEffect(Unit) {
                            val token = tokenManager.accessTokenFlow.firstOrNull()
                            val nextRoute = if (token != null) Screen.Routing.route else Screen.Auth.route
                            navController.navigate(nextRoute) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }

                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    // 1. Экран входа/регистрации
                    composable(Screen.Auth.route) {
                        AuthScreen(
                            onAuthSuccess = {
                                navController.navigate(Screen.Routing.route) {
                                    popUpTo(Screen.Auth.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. Роутинг по состоянию пары (AND-14)
                    composable(Screen.Routing.route) {
                        val spaceViewModel: SpaceViewModel = viewModel()

                        LaunchedEffect(Unit) {
                            spaceViewModel.checkSpaceStatus()
                        }

                        when (val state = spaceViewModel.spaceState) {
                            is SpaceState.Loading -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            is SpaceState.NoSpace -> {
                                LaunchedEffect(state) {
                                    navController.navigate(Screen.NoSpace.route) {
                                        popUpTo(Screen.Routing.route) { inclusive = true }
                                    }
                                }
                            }
                            is SpaceState.WaitingPartner -> {
                                LaunchedEffect(state) {
                                    val code = state.couple.inviteCode ?: ""
                                    navController.navigate(Screen.WaitingPartner.createRoute(code)) {
                                        popUpTo(Screen.Routing.route) { inclusive = true }
                                    }
                                }
                            }
                            is SpaceState.Active -> {
                                LaunchedEffect(state) {
                                    navController.navigate(Screen.TaskList.route) {
                                        popUpTo(Screen.Routing.route) { inclusive = true }
                                    }
                                }
                            }
                            is SpaceState.Error -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Ошибка: ${state.message}")
                                }
                            }
                        }
                    }

                    // 3. Экран "Нет пространства" (AND-11)
                    composable(Screen.NoSpace.route) {
                        val spaceViewModel: SpaceViewModel = viewModel()
                        NoSpaceScreen(
                            spaceViewModel = spaceViewModel,
                            onSpaceReady = {
                                when (val st = spaceViewModel.spaceState) {
                                    is SpaceState.WaitingPartner -> {
                                        val code = st.couple.inviteCode ?: ""
                                        navController.navigate(Screen.WaitingPartner.createRoute(code)) {
                                            popUpTo(Screen.NoSpace.route) { inclusive = true }
                                        }
                                    }
                                    is SpaceState.Active -> {
                                        navController.navigate(Screen.TaskList.route) {
                                            popUpTo(Screen.NoSpace.route) { inclusive = true }
                                        }
                                    }
                                    else -> Unit
                                }
                            },
                            onLogoutClick = {
                                spaceViewModel.logout {
                                    navController.navigate(Screen.Auth.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    // 4. Экран ожидания партнёра (AND-13)
                    composable(
                        route = Screen.WaitingPartner.route,
                        arguments = listOf(navArgument("inviteCode") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val inviteCode = backStackEntry.arguments?.getString("inviteCode") ?: ""
                        val spaceViewModel: SpaceViewModel = viewModel()
                        WaitingPartnerScreen(
                            inviteCode = inviteCode,
                            spaceViewModel = spaceViewModel,
                            onPartnerConnected = {
                                navController.navigate(Screen.TaskList.route) {
                                    popUpTo(Screen.WaitingPartner.route) { inclusive = true }
                                }
                            },
                            onLogoutClick = {
                                spaceViewModel.logout {
                                    navController.navigate(Screen.Auth.route) { popUpTo(0) { inclusive = true } }
                                }
                            }
                        )
                    }

                    // 5. Экран после подключения (AND-21)
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

                    // 6. Список задач (AND-22)
                    composable(Screen.TaskList.route) {
                        val spaceViewModel: SpaceViewModel = viewModel()
                        TaskListScreen(
                            taskViewModel = taskViewModel,
                            onCreateTaskClick = { navController.navigate(Screen.CreateTask.route) },
                            onTaskClick = { taskId -> navController.navigate(Screen.TaskDetail.createRoute(taskId)) },
                            onLogoutClick = {
                                spaceViewModel.logout {
                                    navController.navigate(Screen.Auth.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    // 7. Создание задачи (AND-23)
                    composable(Screen.CreateTask.route) {
                        CreateTaskScreen(
                            onTaskCreated = { title, description, deadlineMillis, assignee ->
                                taskViewModel.addTask(title, description, deadlineMillis, assignee)
                                navController.popBackStack()
                            },
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // 8. Детали задачи (AND-24)
                    composable(
                        route = Screen.TaskDetail.route,
                        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                        TaskDetailScreen(
                            taskId = taskId,
                            taskViewModel = taskViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // 9. Главный экран
                    composable(Screen.Main.route) {
                        val spaceViewModel: SpaceViewModel = viewModel()  // 👈 добавили
                        MainScreen(
                            onLogoutClick = {
                                spaceViewModel.logout {                    // 👈 заменили mainViewModel на spaceViewModel
                                    navController.navigate(Screen.Auth.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}