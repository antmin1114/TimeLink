package com.kkm.timelink

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kkm.timelink.ui.auth.AuthEvent
import com.kkm.timelink.ui.auth.AuthViewModel
import com.kkm.timelink.ui.auth.LoginScreen
import com.kkm.timelink.ui.home.HomeScreen
import com.kkm.timelink.ui.theme.TimeLinkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
fun TimeLinkApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by authViewModel.uiState.collectAsState()
    val webClientId = context.getString(R.string.google_web_client_id)
    val credentialManager = remember {
        CredentialManager.create(context)
    }

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.Error -> Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    LaunchedEffect(uiState.isSignedIn) {
        val route = if (uiState.isSignedIn) {
            TimeLinkRoute.Home.route
        } else {
            TimeLinkRoute.Login.route
        }
        navController.navigate(route) {
            popUpTo(0)
            launchSingleTop = true
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (uiState.isSignedIn) {
                TimeLinkRoute.Home.route
            } else {
                TimeLinkRoute.Login.route
            },
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TimeLinkRoute.Login.route) {
                LoginScreen(
                    isLoading = uiState.isLoading,
                    onGoogleSignInClick = {
                        coroutineScope.launch {
                            val idToken = getGoogleIdToken(
                                credentialManager = credentialManager,
                                context = context,
                                webClientId = webClientId
                            )
                            authViewModel.signInWithGoogle(idToken)
                        }
                    }
                )
            }
            composable(TimeLinkRoute.Home.route) {
                HomeScreen(
                    currentUserId = uiState.currentUserId.orEmpty(),
                    isLoading = uiState.isLoading,
                    onSignOutClick = {
                        coroutineScope.launch {
                            runCatching {
                                credentialManager.clearCredentialState(
                                    ClearCredentialStateRequest()
                                )
                            }
                            authViewModel.signOut()
                        }
                    }
                )
            }
        }
    }
}

private suspend fun getGoogleIdToken(
    credentialManager: CredentialManager,
    context: android.content.Context,
    webClientId: String
): String? {
    val credential = getGoogleCredential(
        credentialManager = credentialManager,
        context = context,
        webClientId = webClientId,
        filterByAuthorizedAccounts = true
    ) ?: getGoogleCredential(
        credentialManager = credentialManager,
        context = context,
        webClientId = webClientId,
        filterByAuthorizedAccounts = false
    ) ?: return null

    if (
        credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }

    return null
}

private suspend fun getGoogleCredential(
    credentialManager: CredentialManager,
    context: android.content.Context,
    webClientId: String,
    filterByAuthorizedAccounts: Boolean
) = try {
    credentialManager.getCredential(
        context = context,
        request = buildGoogleCredentialRequest(
            webClientId = webClientId,
            filterByAuthorizedAccounts = filterByAuthorizedAccounts
        )
    ).credential
} catch (_: NoCredentialException) {
    null
} catch (_: GetCredentialCustomException) {
    null
} catch (_: GetCredentialException) {
    null
}

private fun buildGoogleCredentialRequest(
    webClientId: String,
    filterByAuthorizedAccounts: Boolean
): GetCredentialRequest {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(webClientId)
        .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
        .build()

    return GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
}

private enum class TimeLinkRoute(val route: String) {
    Login("login"),
    Home("home")
}
