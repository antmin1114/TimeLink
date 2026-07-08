package com.kkm.timelink.ui.reservation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
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

enum class ReservationListMode {
    RECEIVED,
    MINE
}

data class ReservationListUiState(
    val mode: ReservationListMode = ReservationListMode.RECEIVED,
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface ReservationListEvent {
    data class Error(val message: String) : ReservationListEvent
}

@HiltViewModel
class ReservationListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val mode = when (savedStateHandle.get<String>("mode")) {
        ReservationListMode.MINE.name -> ReservationListMode.MINE
        else -> ReservationListMode.RECEIVED
    }

    private val _uiState = MutableStateFlow(ReservationListUiState(mode = mode))
    val uiState: StateFlow<ReservationListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReservationListEvent>()
    val events: SharedFlow<ReservationListEvent> = _events.asSharedFlow()

    init {
        loadReservations()
    }

    fun loadReservations() {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            emitError("로그인이 필요합니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                when (mode) {
                    ReservationListMode.RECEIVED ->
                        reservationRepository.getReceivedReservations(uid)

                    ReservationListMode.MINE ->
                        reservationRepository.getMyReservations(uid)
                }
            }.onSuccess { reservations ->
                _uiState.update {
                    it.copy(
                        reservations = reservations,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(ReservationListEvent.Error(toMessage(throwable)))
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(ReservationListEvent.Error(message))
        }
    }

    private fun toMessage(throwable: Throwable): String {
        if (
            throwable is FirebaseFirestoreException &&
            throwable.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION &&
            throwable.message.orEmpty().contains("requires an index", ignoreCase = true)
        ) {
            return "예약 목록 조회에 필요한 Firestore 인덱스가 아직 준비되지 않았습니다."
        }
        return throwable.message?.takeIf { it.isNotBlank() }
            ?: "예약 목록을 불러오지 못했습니다."
    }
}
