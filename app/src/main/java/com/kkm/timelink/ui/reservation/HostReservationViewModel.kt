package com.kkm.timelink.ui.reservation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.kkm.timelink.domain.model.ReservationPurpose
import com.kkm.timelink.domain.model.TimeSlot
import com.kkm.timelink.domain.model.User
import com.kkm.timelink.domain.repository.AuthRepository
import com.kkm.timelink.domain.repository.ReservationRepository
import com.kkm.timelink.domain.repository.TimeSlotRepository
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HostReservationUiState(
    val reservationLinkId: String = "",
    val host: User? = null,
    val availableSlots: List<TimeSlot> = emptyList(),
    val selectedDate: LocalDate? = null,
    val selectedSlotIds: List<String> = emptyList(),
    val selectedPurpose: ReservationPurpose = ReservationPurpose.COFFEE_CHAT,
    val message: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false
)

sealed interface HostReservationEvent {
    data object Requested : HostReservationEvent
    data class Error(val message: String) : HostReservationEvent
}

@HiltViewModel
class HostReservationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val timeSlotRepository: TimeSlotRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val reservationLinkId: String = checkNotNull(savedStateHandle["reservationLinkId"])

    private val _uiState = MutableStateFlow(
        HostReservationUiState(reservationLinkId = reservationLinkId)
    )
    val uiState: StateFlow<HostReservationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HostReservationEvent>()
    val events: SharedFlow<HostReservationEvent> = _events.asSharedFlow()

    init {
        loadHostReservationPage()
    }

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedSlotIds = emptyList()
            )
        }
    }

    fun selectSlot(slotId: String) {
        val state = _uiState.value
        val slots = state.availableSlots.filter { slot ->
            slot.startAt.toLocalDate() == state.selectedDate
        }
        val clickedIndex = slots.indexOfFirst { it.id == slotId }
        if (clickedIndex == -1) return

        val selectedIds = state.selectedSlotIds
        val nextSelection = if (selectedIds.isEmpty()) {
            listOf(slotId)
        } else {
            val firstIndex = slots.indexOfFirst { it.id == selectedIds.first() }
            if (firstIndex == -1 || selectedIds.contains(slotId)) {
                listOf(slotId)
            } else {
                val range = if (firstIndex <= clickedIndex) {
                    firstIndex..clickedIndex
                } else {
                    clickedIndex..firstIndex
                }
                val candidate = range.map { slots[it] }
                if (candidate.zipWithNext().all { (current, next) -> current.endAt == next.startAt }) {
                    candidate.map { it.id }
                } else {
                    emitError("연속된 시간 슬롯만 선택할 수 있습니다.")
                    listOf(slotId)
                }
            }
        }
        _uiState.update { it.copy(selectedSlotIds = nextSelection) }
    }

    fun selectPurpose(purpose: ReservationPurpose) {
        _uiState.update { it.copy(selectedPurpose = purpose) }
    }

    fun updateMessage(value: String) {
        _uiState.update { it.copy(message = value) }
    }

    fun requestReservation() {
        val guestId = authRepository.getCurrentUserId()
        if (guestId == null) {
            emitError("로그인이 필요합니다.")
            return
        }

        val state = _uiState.value
        val host = state.host
        if (host == null) {
            emitError("Host 정보를 찾을 수 없습니다.")
            return
        }
        if (state.selectedSlotIds.isEmpty()) {
            emitError("예약할 시간 슬롯을 선택해 주세요.")
            return
        }
        if (state.message.isBlank()) {
            emitError("예약 메시지를 입력해 주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            runCatching {
                reservationRepository.requestReservation(
                    hostId = host.uid,
                    guestId = guestId,
                    slotIds = state.selectedSlotIds,
                    purpose = state.selectedPurpose,
                    message = state.message
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        selectedSlotIds = emptyList(),
                        message = "",
                        isSubmitting = false
                    )
                }
                _events.emit(HostReservationEvent.Requested)
                loadHostReservationPage()
            }.onFailure { throwable ->
                _uiState.update { it.copy(isSubmitting = false) }
                _events.emit(HostReservationEvent.Error(toMessage(throwable)))
            }
        }
    }

    private fun loadHostReservationPage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val host = userRepository.getUserByReservationLinkId(reservationLinkId)
                    ?: error("Host 정보를 찾을 수 없습니다.")
                val slots = timeSlotRepository.getAvailableTimeSlots(host.uid)
                host to slots
            }.onSuccess { (host, slots) ->
                val sortedSlots = slots.sortedBy { it.startAt }
                _uiState.update {
                    it.copy(
                        host = host,
                        availableSlots = sortedSlots,
                        selectedDate = sortedSlots.firstOrNull()?.startAt?.toLocalDate(),
                        selectedSlotIds = emptyList(),
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(HostReservationEvent.Error(toMessage(throwable)))
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(HostReservationEvent.Error(message))
        }
    }

    private fun toMessage(throwable: Throwable): String {
        if (
            throwable is FirebaseFirestoreException &&
            throwable.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION &&
            throwable.message.orEmpty().contains("requires an index", ignoreCase = true)
        ) {
            return "예약 조회에 필요한 Firestore 인덱스가 아직 준비되지 않았습니다."
        }
        return throwable.message?.takeIf { it.isNotBlank() }
            ?: "예약 신청 처리에 실패했습니다."
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
