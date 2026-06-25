package com.kkm.timelink.ui.timeslot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.kkm.timelink.domain.model.TimeSlot
import com.kkm.timelink.domain.repository.AuthRepository
import com.kkm.timelink.domain.repository.TimeSlotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

private val defaultStartDateTime: LocalDateTime = LocalDateTime.now()
    .plusHours(1)
    .withMinute(0)
    .withSecond(0)
    .withNano(0)
private val defaultEndDateTime: LocalDateTime = defaultStartDateTime.plusHours(1)

data class TimeSlotUiState(
    val selectedDate: LocalDate = defaultStartDateTime.toLocalDate(),
    val startHour: Int = defaultStartDateTime.hour,
    val startMinute: Int = 0,
    val endHour: Int = if (
        defaultEndDateTime.toLocalDate().isAfter(defaultStartDateTime.toLocalDate())
    ) {
        24
    } else {
        defaultEndDateTime.hour
    },
    val endMinute: Int = 0,
    val durationMinutes: Int = 30,
    val timeSlots: List<TimeSlot> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val updatingSlotId: String? = null
)

sealed interface TimeSlotEvent {
    data object Created : TimeSlotEvent
    data object Disabled : TimeSlotEvent
    data object Enabled : TimeSlotEvent
    data class Error(val message: String) : TimeSlotEvent
}

@HiltViewModel
class TimeSlotViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val timeSlotRepository: TimeSlotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimeSlotUiState())
    val uiState: StateFlow<TimeSlotUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TimeSlotEvent>()
    val events: SharedFlow<TimeSlotEvent> = _events.asSharedFlow()

    init {
        loadTimeSlots()
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun selectStartTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(startHour = hour, startMinute = minute) }
    }

    fun selectEndTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(endHour = hour, endMinute = minute) }
    }

    fun selectEndOfDay() {
        _uiState.update { it.copy(endHour = 24, endMinute = 0) }
    }

    fun selectDuration(durationMinutes: Int) {
        if (durationMinutes != 30 && durationMinutes != 60) return
        _uiState.update { it.copy(durationMinutes = durationMinutes) }
    }

    fun createTimeSlot() {
        val hostId = authRepository.getCurrentUserId()
        if (hostId == null) {
            emitError("로그인이 필요합니다.")
            return
        }

        val state = _uiState.value
        val startAt = state.selectedDate
            .atTime(state.startHour, state.startMinute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val endAt = if (state.endHour == 24) {
            state.selectedDate.plusDays(1).atStartOfDay()
        } else {
            state.selectedDate.atTime(state.endHour, state.endMinute)
        }.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (endAt <= startAt) {
            emitError("종료 시간은 시작 시간보다 늦어야 합니다.")
            return
        }
        val slotDurationMillis = state.durationMinutes * MILLIS_PER_MINUTE
        if ((endAt - startAt) % slotDurationMillis != 0L) {
            emitError("선택한 시간 범위는 슬롯 단위로 정확히 나누어져야 합니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            runCatching {
                timeSlotRepository.createTimeSlot(
                    hostId = hostId,
                    startAt = startAt,
                    endAt = endAt,
                    durationMinutes = state.durationMinutes
                )
            }.onSuccess {
                _uiState.update { it.copy(isCreating = false) }
                _events.emit(TimeSlotEvent.Created)
                loadTimeSlots()
            }.onFailure { throwable ->
                _uiState.update { it.copy(isCreating = false) }
                _events.emit(TimeSlotEvent.Error(toMessage(throwable)))
            }
        }
    }

    fun disableTimeSlot(slotId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(updatingSlotId = slotId) }
            runCatching {
                timeSlotRepository.disableTimeSlot(slotId)
            }.onSuccess {
                _uiState.update { it.copy(updatingSlotId = null) }
                _events.emit(TimeSlotEvent.Disabled)
                loadTimeSlots()
            }.onFailure { throwable ->
                _uiState.update { it.copy(updatingSlotId = null) }
                _events.emit(TimeSlotEvent.Error(toMessage(throwable)))
            }
        }
    }

    fun enableTimeSlot(slotId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(updatingSlotId = slotId) }
            runCatching {
                timeSlotRepository.enableTimeSlot(slotId)
            }.onSuccess {
                _uiState.update { it.copy(updatingSlotId = null) }
                _events.emit(TimeSlotEvent.Enabled)
                loadTimeSlots()
            }.onFailure { throwable ->
                _uiState.update { it.copy(updatingSlotId = null) }
                _events.emit(TimeSlotEvent.Error(toMessage(throwable)))
            }
        }
    }

    fun loadTimeSlots() {
        val hostId = authRepository.getCurrentUserId()
        if (hostId == null) {
            emitError("로그인이 필요합니다.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                timeSlotRepository.getHostTimeSlots(hostId)
            }.onSuccess { slots ->
                _uiState.update { it.copy(timeSlots = slots, isLoading = false) }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(TimeSlotEvent.Error(toMessage(throwable)))
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _events.emit(TimeSlotEvent.Error(message))
        }
    }

    private fun toMessage(throwable: Throwable): String {
        if (
            throwable is FirebaseFirestoreException &&
            throwable.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION &&
            throwable.message.orEmpty().contains("requires an index", ignoreCase = true)
        ) {
            return "시간 슬롯 조회에 필요한 Firestore 인덱스가 아직 준비되지 않았습니다."
        }
        return throwable.message?.takeIf { it.isNotBlank() }
            ?: "시간 슬롯 처리에 실패했습니다."
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
