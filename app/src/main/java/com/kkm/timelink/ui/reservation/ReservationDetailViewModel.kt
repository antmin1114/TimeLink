package com.kkm.timelink.ui.reservation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkm.timelink.domain.model.Reservation
import com.kkm.timelink.domain.repository.AuthRepository
import com.kkm.timelink.domain.repository.ReservationRepository
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

data class ReservationDetailUiState(
    val reservation: Reservation? = null,
    val currentUserId: String? = null,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false
)

sealed interface ReservationDetailEvent {
    data object Approved : ReservationDetailEvent
    data object Rejected : ReservationDetailEvent
    data object Cancelled : ReservationDetailEvent
    data class Error(val message: String) : ReservationDetailEvent
}

@HiltViewModel
class ReservationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val reservationId: String = checkNotNull(savedStateHandle["reservationId"])

    private val _uiState = MutableStateFlow(ReservationDetailUiState())
    val uiState: StateFlow<ReservationDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReservationDetailEvent>()
    val events: SharedFlow<ReservationDetailEvent> = _events.asSharedFlow()

    init {
        loadReservation()
    }

    fun loadReservation() {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            emitError("로그인이 필요합니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val reservation = reservationRepository.getReservation(reservationId)
                    ?: error("예약 정보를 찾을 수 없습니다.")
                check(reservation.hostId == uid || reservation.guestId == uid) {
                    "이 예약을 조회할 권한이 없습니다."
                }
                reservation
            }.onSuccess { reservation ->
                _uiState.update {
                    it.copy(
                        reservation = reservation,
                        currentUserId = uid,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(
                    ReservationDetailEvent.Error(
                        throwable.message?.takeIf { it.isNotBlank() }
                            ?: "예약 상세 정보를 불러오지 못했습니다."
                    )
                )
            }
        }
    }

    fun approveReservation() {
        updateReservationStatus(
            action = { reservationRepository.approveReservation(reservationId) },
            successEvent = ReservationDetailEvent.Approved
        )
    }

    fun rejectReservation(reason: String) {
        updateReservationStatus(
            action = { reservationRepository.rejectReservation(reservationId, reason) },
            successEvent = ReservationDetailEvent.Rejected
        )
    }

    fun cancelReservation() {
        updateReservationStatus(
            action = { reservationRepository.cancelReservation(reservationId) },
            successEvent = ReservationDetailEvent.Cancelled
        )
    }

    private fun updateReservationStatus(
        action: suspend () -> Unit,
        successEvent: ReservationDetailEvent
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            runCatching {
                action()
                reservationRepository.getReservation(reservationId)
                    ?: error("예약 정보를 찾을 수 없습니다.")
            }.onSuccess { reservation ->
                _uiState.update {
                    it.copy(
                        reservation = reservation,
                        isActionLoading = false
                    )
                }
                _events.emit(successEvent)
            }.onFailure { throwable ->
                _uiState.update { it.copy(isActionLoading = false) }
                _events.emit(
                    ReservationDetailEvent.Error(
                        throwable.message?.takeIf { it.isNotBlank() }
                            ?: "예약 상태 변경에 실패했습니다."
                    )
                )
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(ReservationDetailEvent.Error(message))
        }
    }
}
