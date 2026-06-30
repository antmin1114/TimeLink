package com.kkm.timelink

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kkm.timelink.ui.auth.AuthEvent
import com.kkm.timelink.ui.auth.AuthViewModel
import com.kkm.timelink.ui.auth.LoginScreen
import com.kkm.timelink.ui.home.HomeEvent
import com.kkm.timelink.ui.home.HomeScreen
import com.kkm.timelink.ui.home.HomeViewModel
import com.kkm.timelink.ui.profile.ProfileEvent
import com.kkm.timelink.ui.profile.ProfileScreen
import com.kkm.timelink.ui.profile.ProfileViewModel
import com.kkm.timelink.ui.reservation.HostReservationEvent
import com.kkm.timelink.ui.reservation.HostReservationScreen
import com.kkm.timelink.ui.reservation.HostReservationViewModel
import com.kkm.timelink.ui.reservation.ReservationDetailEvent
import com.kkm.timelink.ui.reservation.ReservationDetailScreen
import com.kkm.timelink.ui.reservation.ReservationDetailViewModel
import com.kkm.timelink.ui.reservation.ReservationListEvent
import com.kkm.timelink.ui.reservation.ReservationListMode
import com.kkm.timelink.ui.reservation.ReservationListScreen
import com.kkm.timelink.ui.reservation.ReservationListViewModel
import com.kkm.timelink.ui.theme.TimeLinkTheme
import com.kkm.timelink.ui.timeslot.TimeSlotEvent
import com.kkm.timelink.ui.timeslot.TimeSlotManagementScreen
import com.kkm.timelink.ui.timeslot.TimeSlotViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
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
    val startDestination = remember {
        if (uiState.isSignedIn) {
            TimeLinkRoute.Home.route
        } else {
            TimeLinkRoute.Login.route
        }
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

    LaunchedEffect(authViewModel, navController) {
        authViewModel.uiState
            .map { it.isSignedIn }
            .distinctUntilChanged()
            .drop(1)
            .collect { isSignedIn ->
                val route = if (isSignedIn) {
                    TimeLinkRoute.Home.route
                } else {
                    TimeLinkRoute.Login.route
                }
                navController.navigate(route) {
                    popUpTo(0)
                    launchSingleTop = true
                }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
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
                val homeViewModel: HomeViewModel = hiltViewModel()
                val homeUiState by homeViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    homeViewModel.events.collect { event ->
                        when (event) {
                            is HomeEvent.Error -> Toast.makeText(
                                context,
                                event.message,
                                Toast.LENGTH_SHORT
                            ).show()

                            is HomeEvent.NavigateToReservationLink -> {
                                navController.navigate("host/${event.reservationLinkId}")
                            }
                        }
                    }
                }

                HomeScreen(
                    currentUserId = uiState.currentUserId.orEmpty(),
                    uiState = homeUiState,
                    isSigningOut = uiState.isSigningOut,
                    onProfileClick = {
                        uiState.currentUserId?.let { uid ->
                            navController.navigate("profile/$uid")
                        }
                    },
                    onTimeSlotsClick = {
                        navController.navigate(TimeLinkRoute.TimeSlots.route)
                    },
                    onOpenReservationLinkClick = { reservationLinkId ->
                        navController.navigate("host/$reservationLinkId")
                    },
                    onReservationLinkInputChange = homeViewModel::updateReservationLinkInput,
                    onOpenReservationLinkInputClick = homeViewModel::openReservationLinkInput,
                    onReceivedReservationsClick = {
                        navController.navigate(
                            "reservations/${ReservationListMode.RECEIVED.name}"
                        )
                    },
                    onMyReservationsClick = {
                        navController.navigate(
                            "reservations/${ReservationListMode.MINE.name}"
                        )
                    },
                    onSignOutClick = {
                        coroutineScope.launch {
                            authViewModel.beginSignOut()
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
            composable(
                route = TimeLinkRoute.Profile.route,
                arguments = listOf(navArgument("uid") { type = NavType.StringType })
            ) { backStackEntry ->
                val profileViewModel: ProfileViewModel = hiltViewModel(backStackEntry)
                val profileUiState by profileViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    profileViewModel.events.collect { event ->
                        when (event) {
                            ProfileEvent.Saved -> Toast.makeText(
                                context,
                                "프로필을 저장했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()

                            is ProfileEvent.Error -> Toast.makeText(
                                context,
                                event.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                ProfileScreen(
                    uiState = profileUiState,
                    onNicknameChange = profileViewModel::updateNickname,
                    onBioChange = profileViewModel::updateBio,
                    onProfileImageUrlChange = profileViewModel::updateProfileImageUrl,
                    onSaveClick = profileViewModel::saveProfile,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = TimeLinkRoute.ReservationList.route,
                arguments = listOf(navArgument("mode") { type = NavType.StringType })
            ) { backStackEntry ->
                val reservationListViewModel: ReservationListViewModel =
                    hiltViewModel(backStackEntry)
                val reservationListUiState by reservationListViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    reservationListViewModel.events.collect { event ->
                        when (event) {
                            is ReservationListEvent.Error -> Toast.makeText(
                                context,
                                event.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                ReservationListScreen(
                    uiState = reservationListUiState,
                    onReservationClick = { reservationId ->
                        navController.navigate("reservation/$reservationId")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = TimeLinkRoute.ReservationDetail.route,
                arguments = listOf(navArgument("reservationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val reservationDetailViewModel: ReservationDetailViewModel =
                    hiltViewModel(backStackEntry)
                val reservationDetailUiState by reservationDetailViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    reservationDetailViewModel.events.collect { event ->
                        when (event) {
                            is ReservationDetailEvent.Error -> Toast.makeText(
                                context,
                                event.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                ReservationDetailScreen(
                    uiState = reservationDetailUiState,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(TimeLinkRoute.TimeSlots.route) { backStackEntry ->
                val timeSlotViewModel: TimeSlotViewModel = hiltViewModel(backStackEntry)
                val timeSlotUiState by timeSlotViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    timeSlotViewModel.events.collect { event ->
                        val message = when (event) {
                            TimeSlotEvent.Created -> "예약 가능 시간을 등록했습니다."
                            TimeSlotEvent.Disabled -> "시간 슬롯을 비활성화했습니다."
                            TimeSlotEvent.Enabled -> "시간 슬롯을 활성화했습니다."
                            is TimeSlotEvent.Error -> event.message
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

                TimeSlotManagementScreen(
                    uiState = timeSlotUiState,
                    onDateSelected = timeSlotViewModel::selectDate,
                    onStartTimeSelected = timeSlotViewModel::selectStartTime,
                    onEndTimeSelected = timeSlotViewModel::selectEndTime,
                    onEndOfDaySelected = timeSlotViewModel::selectEndOfDay,
                    onDurationSelected = timeSlotViewModel::selectDuration,
                    onCreateClick = timeSlotViewModel::createTimeSlot,
                    onDisableClick = timeSlotViewModel::disableTimeSlot,
                    onEnableClick = timeSlotViewModel::enableTimeSlot,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = TimeLinkRoute.HostReservation.route,
                arguments = listOf(
                    navArgument("reservationLinkId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val hostReservationViewModel: HostReservationViewModel =
                    hiltViewModel(backStackEntry)
                val hostReservationUiState by hostReservationViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    hostReservationViewModel.events.collect { event ->
                        val message = when (event) {
                            HostReservationEvent.Requested -> "예약 신청을 보냈습니다."
                            is HostReservationEvent.Error -> event.message
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

                HostReservationScreen(
                    uiState = hostReservationUiState,
                    onDateSelected = hostReservationViewModel::selectDate,
                    onSlotClick = hostReservationViewModel::selectSlot,
                    onPurposeSelected = hostReservationViewModel::selectPurpose,
                    onMessageChange = hostReservationViewModel::updateMessage,
                    onRequestClick = hostReservationViewModel::requestReservation,
                    onBackClick = { navController.popBackStack() }
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
    Home("home"),
    Profile("profile/{uid}"),
    TimeSlots("time-slots"),
    HostReservation("host/{reservationLinkId}"),
    ReservationList("reservations/{mode}"),
    ReservationDetail("reservation/{reservationId}")
}
