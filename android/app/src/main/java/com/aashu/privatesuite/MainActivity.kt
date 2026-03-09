package com.aashu.privatesuite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aashu.privatesuite.presentation.theme.PrivateSuiteTheme
import androidx.navigation.compose.rememberNavController
import com.aashu.privatesuite.presentation.navigation.Screen
import com.aashu.privatesuite.presentation.navigation.NavGraph
import com.aashu.privatesuite.data.local.AuthManager
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrivateSuiteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = if (authManager.getToken() != null) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Login.route
                    }

                    NavGraph(navController = navController, startDestination = startDestination)
                }
            }
        }
    }
}

