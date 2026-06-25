package com.kkm.timelink.ui.timeslot

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kkm.timelink.domain.model.TimeSlot
import com.kkm.timelink.domain.model.TimeSlotStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotManagementScreen(
    uiState: TimeSlotUiState,
    onDateSelected: (LocalDate) -> Unit,
    onStartTimeSelected: (Int, Int) -> Unit,
    onEndTimeSelected: (Int, Int) -> Unit,
    onEndOfDaySelected: () -> Unit,
    onDurationSelected: (Int) -> Unit,
    onCreateClick: () -> Unit,
    onDisableClick: (String) -> Unit,
    onEnableClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("시간 슬롯 관리") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "예약 가능 시간 등록",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("날짜", style = MaterialTheme.typography.labelLarge)
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    onDateSelected(LocalDate.of(year, month + 1, day))
                                },
                                uiState.selectedDate.year,
                                uiState.selectedDate.monthValue - 1,
                                uiState.selectedDate.dayOfMonth
                            ).apply {
                                datePicker.minDate = System.currentTimeMillis()
                            }.show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(uiState.selectedDate.format(DATE_FORMATTER))
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeInput(
                        label = "시작 시간",
                        timeText = formatTime(uiState.startHour, uiState.startMinute),
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> onStartTimeSelected(hour, minute) },
                                uiState.startHour,
                                uiState.startMinute,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TimeInput(
                        label = "종료 시간",
                        timeText = formatTime(uiState.endHour, uiState.endMinute),
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> onEndTimeSelected(hour, minute) },
                                uiState.endHour.coerceAtMost(23),
                                uiState.endMinute,
                                true
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                TextButton(onClick = onEndOfDaySelected) {
                    Text("종료 시간을 24:00으로 설정")
                }
            }
            item {
                Text("슬롯 분할 단위", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60).forEach { duration ->
                        FilterChip(
                            selected = uiState.durationMinutes == duration,
                            onClick = { onDurationSelected(duration) },
                            label = { Text("${duration}분 단위") }
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = onCreateClick,
                    enabled = !uiState.isCreating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator()
                    } else {
                        Text("예약 가능 시간 등록")
                    }
                }
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "내 시간 슬롯",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.timeSlots.isEmpty()) {
                item {
                    Text(
                        text = "오늘 이후에 생성된 시간 슬롯이 없습니다.",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(uiState.timeSlots, key = { it.id }) { slot ->
                    TimeSlotItem(
                        slot = slot,
                        isUpdating = uiState.updatingSlotId == slot.id,
                        onDisableClick = { onDisableClick(slot.id) },
                        onEnableClick = { onEnableClick(slot.id) }
                    )
                    HorizontalDivider()
                }
            }
            item {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {}
            }
        }
    }
}

@Composable
private fun TimeInput(
    label: String,
    timeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(timeText)
        }
    }
}

@Composable
private fun TimeSlotItem(
    slot: TimeSlot,
    isUpdating: Boolean,
    onDisableClick: () -> Unit,
    onEnableClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = formatSlotTime(slot),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${slot.durationMinutes}분 · ${statusLabel(slot.status)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (slot.status == TimeSlotStatus.AVAILABLE.name) {
            TextButton(
                onClick = onDisableClick,
                enabled = !isUpdating
            ) {
                Text(if (isUpdating) "처리 중" else "비활성화")
            }
        } else if (slot.status == TimeSlotStatus.DISABLED.name) {
            TextButton(
                onClick = onEnableClick,
                enabled = !isUpdating && slot.startAt > System.currentTimeMillis()
            ) {
                Text(if (isUpdating) "처리 중" else "활성화")
            }
        }
    }
}

private fun formatSlotTime(slot: TimeSlot): String {
    val zoneId = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(slot.startAt).atZone(zoneId)
    val end = Instant.ofEpochMilli(slot.endAt).atZone(zoneId)
    return "${start.format(SLOT_DATE_TIME_FORMATTER)} - ${end.format(TIME_FORMATTER)}"
}

private fun statusLabel(status: String): String = when (status) {
    TimeSlotStatus.AVAILABLE.name -> "예약 가능"
    TimeSlotStatus.RESERVED.name -> "예약됨"
    TimeSlotStatus.DISABLED.name -> "비활성화"
    else -> status
}

private fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val SLOT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
