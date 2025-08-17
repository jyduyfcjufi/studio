package com.example.aistudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aistudio.ui.screens.InferenceScreen
import com.example.aistudio.ui.screens.ModelListScreen
import com.example.aistudio.ui.theme.AIStudioTheme
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIStudioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "model_list") {
        composable("model_list") {
            ModelListScreen(
                onNavigateToModel = { modelPath ->
                    val encodedPath = URLEncoder.encode(modelPath, "UTF-8")
                    navController.navigate("inference/$encodedPath")
                }
            )
        }
        composable(
            "inference/{modelPath}",
            arguments = listOf(navArgument("modelPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("modelPath") ?: ""
            val modelPath = URLDecoder.decode(encodedPath, "UTF-8")
            InferenceScreen(modelPath = modelPath)
        }
    }
}
