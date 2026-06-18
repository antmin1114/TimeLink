package com.kkm.timelink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kkm.timelink.ui.theme.TimeLinkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeLinkTheme {
                TimeLinkApp()
            }
        }
    }
}

@Composable
fun TimeLinkApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TimeLinkRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TimeLinkRoute.Home.route) {
                HomeScreen()
            }
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier = Modifier) {
    Text(
        text = "TimeLink",
        modifier = modifier
    )
}

private enum class TimeLinkRoute(val route: String) {
    Home("home")
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TimeLinkTheme {
        HomeScreen()
    }
}
