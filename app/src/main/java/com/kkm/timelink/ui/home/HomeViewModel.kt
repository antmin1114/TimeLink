package com.kkm.timelink.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkm.timelink.domain.repository.AuthRepository
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

data class HomeUiState(
    val nickname: String = "",
    val reservationLinkId: String = "",
    val reservationLinkInput: String = "",
    val isOpeningReservationLink: Boolean = false,
    val isLoadingProfile: Boolean = false
)

sealed interface HomeEvent {
    data class Error(val message: String) : HomeEvent
    data class NavigateToReservationLink(val reservationLinkId: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        loadReservationLink(showLoading = true)
    }

    fun loadReservationLink(showLoading: Boolean = _uiState.value.reservationLinkId.isBlank()) {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            emitError("로그인이 필요합니다.")
            return
        }

        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoadingProfile = true) }
            }
            runCatching {
                userRepository.createUserIfMissing(uid)
                userRepository.getUser(uid)
            }.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        nickname = user?.nickname.orEmpty(),
                        reservationLinkId = user?.reservationLinkId.orEmpty(),
                        isLoadingProfile = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoadingProfile = false) }
                _events.emit(
                    HomeEvent.Error(
                        throwable.message?.takeIf { it.isNotBlank() }
                            ?: "예약 링크 정보를 불러오지 못했습니다."
                    )
                )
            }
        }
    }

    fun updateReservationLinkInput(value: String) {
        _uiState.update { it.copy(reservationLinkInput = value) }
    }

    fun openReservationLinkInput() {
        val reservationLinkId = _uiState.value.reservationLinkInput.trim()
        if (reservationLinkId.isBlank()) {
            emitError("예약 링크 ID를 입력해 주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isOpeningReservationLink = true) }
            runCatching {
                userRepository.getUserByReservationLinkId(reservationLinkId)
                    ?: error("존재하지 않는 예약 링크 ID입니다.")
            }.onSuccess {
                _uiState.update { it.copy(isOpeningReservationLink = false) }
                _events.emit(HomeEvent.NavigateToReservationLink(reservationLinkId))
            }.onFailure { throwable ->
                _uiState.update { it.copy(isOpeningReservationLink = false) }
                _events.emit(
                    HomeEvent.Error(
                        throwable.message?.takeIf { it.isNotBlank() }
                            ?: "예약 링크 ID를 확인하지 못했습니다."
                    )
                )
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(HomeEvent.Error(message))
        }
    }
}
