package com.kkm.timelink.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkm.timelink.domain.repository.AuthRepository
import com.kkm.timelink.domain.repository.NotificationRepository
import com.kkm.timelink.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSigningOut: Boolean = false,
    val currentUserId: String? = null
) {
    val isSignedIn: Boolean = currentUserId != null
}

sealed interface AuthEvent {
    data class Error(val message: String) : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(currentUserId = authRepository.getCurrentUserId())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        _uiState.value.currentUserId?.let(::syncNotificationToken)
    }

    fun signInWithGoogle(idToken: String?) {
        if (idToken.isNullOrBlank()) {
            emitError("사용 가능한 Google 계정을 찾을 수 없습니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                authRepository.signInWithGoogle(idToken)
            }.onSuccess { uid ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUserId = uid
                    )
                }
                createProfileIfMissing(uid)
                syncNotificationToken(uid)
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(AuthEvent.Error("Google 로그인에 실패했습니다."))
            }
        }
    }

    fun beginSignOut() {
        _uiState.update { it.copy(isSigningOut = true) }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true) }
            runCatching {
                authRepository.getCurrentUserId()?.let { uid ->
                    runCatching { notificationRepository.clearToken(uid) }
                }
                authRepository.signOut()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSigningOut = false,
                        currentUserId = null
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isSigningOut = false) }
                _events.emit(AuthEvent.Error("로그아웃에 실패했습니다."))
            }
        }
    }

    private fun createProfileIfMissing(uid: String) {
        viewModelScope.launch {
            runCatching {
                userRepository.createUserIfMissing(uid)
            }.onFailure {
                _events.emit(AuthEvent.Error("로그인은 완료됐지만 프로필 생성에 실패했습니다."))
            }
        }
    }

    private fun syncNotificationToken(uid: String) {
        viewModelScope.launch {
            runCatching { notificationRepository.syncToken(uid) }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(AuthEvent.Error(message))
        }
    }
}
