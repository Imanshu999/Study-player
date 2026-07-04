package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.Video
import com.example.ui.StudyPlayerViewModel
import com.example.ui.admin.AdminPanelScreen
import com.example.ui.home.HomeScreen
import com.example.ui.player.VideoPlayerScreen
import com.example.ui.registration.RegistrationScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StudyPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val profile by viewModel.profileState.collectAsState()
                val isRegistered = profile.isRegistered

                val navController = rememberNavController()
                
                // Track selected video in parent state for safe transition
                var selectedVideoForPlayback by remember { mutableStateOf<Video?>(null) }

                // Auto-route based on registration state
                LaunchedEffect(isRegistered) {
                    if (isRegistered) {
                        navController.navigate("home") {
                            popUpTo("registration") { inclusive = true }
                        }
                    } else {
                        navController.navigate("registration") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (isRegistered) "home" else "registration",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("registration") {
                            RegistrationScreen(
                                viewModel = viewModel,
                                onAuthSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("registration") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToPlayer = { video ->
                                    selectedVideoForPlayback = video
                                    navController.navigate("player")
                                },
                                onNavigateToAdmin = {
                                    navController.navigate("admin")
                                }
                            )
                        }

                        composable("player") {
                            selectedVideoForPlayback?.let { video ->
                                VideoPlayerScreen(
                                    video = video,
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }

                        composable("admin") {
                            val isAdminAuth by viewModel.isAdminAuthenticated.collectAsState()
                            
                            if (isAdminAuth) {
                                AdminPanelScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        navController.navigate("home") {
                                            popUpTo("admin") { inclusive = true }
                                        }
                                    }
                                )
                            } else {
                                // Safeguard: if somehow navigated to admin page without auth, go home
                                LaunchedEffect(Unit) {
                                    navController.navigate("home") {
                                        popUpTo("admin") { inclusive = true }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
