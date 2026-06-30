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
    val isLoading: Boolean = false
)

sealed interface ReservationDetailEvent {
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
            emitError("Login is required.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val reservation = reservationRepository.getReservation(reservationId)
                    ?: error("Reservation not found.")
                check(reservation.hostId == uid || reservation.guestId == uid) {
                    "You do not have permission to view this reservation."
                }
                reservation
            }.onSuccess { reservation ->
                _uiState.update {
                    it.copy(
                        reservation = reservation,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(
                    ReservationDetailEvent.Error(
                        throwable.message?.takeIf { it.isNotBlank() }
                            ?: "Failed to load reservation detail."
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
