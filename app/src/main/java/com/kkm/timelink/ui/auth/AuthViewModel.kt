package com.kkm.timelink.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkm.timelink.domain.repository.AuthRepository
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
    val currentUserId: String? = null
) {
    val isSignedIn: Boolean = currentUserId != null
}

sealed interface AuthEvent {
    data class Error(val message: String) : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(currentUserId = authRepository.getCurrentUserId())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun signInWithGoogle(idToken: String?) {
        if (idToken.isNullOrBlank()) {
            emitError("Google 로그인 토큰을 가져오지 못했습니다.")
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
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(
                    AuthEvent.Error(
                        throwable.message ?: "Google 로그인에 실패했습니다."
                    )
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                authRepository.signOut()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUserId = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(
                    AuthEvent.Error(
                        throwable.message ?: "로그아웃에 실패했습니다."
                    )
                )
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(AuthEvent.Error(message))
        }
    }
}
