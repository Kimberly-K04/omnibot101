package com.kwamboka.omnibot101.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kwamboka.omnibot101.ui.theme.screens.Chatbot.ChatbotScreen
import com.kwamboka.omnibot101.ui.theme.screens.Login.LoginScreen
import com.kwamboka.omnibot101.ui.theme.screens.Register.RegisterScreen
import com.kwamboka.omnibot101.ui.theme.screens.Dashboard.DashboardScreen
import com.kwamboka.omnibot101.ui.theme.screens.ForgotPassword.ForgotPasswordScreen
import com.kwamboka.omnibot101.ui.theme.screens.Mood.MoodScreen
import com.kwamboka.omnibot101.ui.theme.screens.StudyPlanner.StudyPlannerScreen
import com.kwamboka.omnibot101.ui.theme.screens.EcoAccess.EcoAccessScreen
import com.kwamboka.omnibot101.ui.theme.screens.Socialize.SocializeScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASH
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination
    ) {
        composable(ROUTE_SPLASH) {
            SplashScreen(navController)
        }

        composable(ROUTE_LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(ROUTE_DASHBOARD) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onForgotPassword = {
                    navController.navigate(ROUTE_FORGOT_PASSWORD)
                },
                onNavigateToRegister = {
                    navController.navigate(ROUTE_REGISTER)
                }
            )
        }

        composable(ROUTE_REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(ROUTE_DASHBOARD) {
                        popUpTo(ROUTE_REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackToLogin = {
                    navController.navigate(ROUTE_LOGIN)
                }
            )
        }

        composable(ROUTE_DASHBOARD) {
            DashboardScreen(
                onChatClick = {
                    navController.navigate(ROUTE_CHAT)
                },
                onMoodCheckClick = {
                    navController.navigate(ROUTE_MOOD)
                },
                onEcoAccessClick = {
                    navController.navigate(ROUTE_ECO)
                },
                onStudyPlannerClick = {
                    navController.navigate(ROUTE_STUDY_PLANNER)
                }
            )
        }

        composable(ROUTE_FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackToLogin = {
                    navController.popBackStack(ROUTE_LOGIN, false)
                }
            )
        }

        composable(ROUTE_CHAT) {
            ChatbotScreen()
        }
        composable(ROUTE_MOOD) {
            MoodScreen(
                onChatbotClick = {
                    navController.navigate(ROUTE_CHAT)
                },
                onSocializeClick = {
                    navController.navigate(ROUTE_SOCIAL)
                },
                onMoodSubmit = { mood ->
                    // Handle mood logic
                },
                neonPurple = Color(0xFF9D00FF)
            )
        }

        composable(ROUTE_STUDY_PLANNER) {
            StudyPlannerScreen()
        }

        composable(ROUTE_ECO) {
            EcoAccessScreen()
        }
        composable(ROUTE_SOCIAL) {
            SocializeScreen()
        }

    }
}
