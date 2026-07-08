package com.kkm.timelink.ui.reservation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kkm.timelink.domain.model.ReservationPurpose
import com.kkm.timelink.domain.model.TimeSlot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostReservationScreen(
    uiState: HostReservationUiState,
    onDateSelected: (LocalDate) -> Unit,
    onSlotClick: (String) -> Unit,
    onPurposeSelected: (ReservationPurpose) -> Unit,
    onMessageChange: (String) -> Unit,
    onRequestClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("예약 신청") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HostProfile(
                    nickname = uiState.host?.nickname.orEmpty(),
                    bio = uiState.host?.bio.orEmpty()
                )
            }
            item {
                HorizontalDivider()
                Text(
                    text = "예약 가능한 시간",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            val availableDates = uiState.availableSlots
                .map { it.startAt.toLocalDate() }
                .distinct()
            val selectedDateSlots = uiState.availableSlots.filter {
                it.startAt.toLocalDate() == uiState.selectedDate
            }
            if (availableDates.isNotEmpty()) {
                item {
                    DateSelector(
                        dates = availableDates,
                        selectedDate = uiState.selectedDate,
                        onDateSelected = onDateSelected
                    )
                }
            }
            if (uiState.availableSlots.isEmpty()) {
                item {
                    Text(
                        text = "예약 가능한 시간 슬롯이 없습니다.",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else if (selectedDateSlots.isEmpty()) {
                item {
                    Text(
                        text = "선택한 날짜에 예약 가능한 시간이 없습니다.",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                item {
                    TimeSlotGrid(
                        slots = selectedDateSlots,
                        selectedSlotIds = uiState.selectedSlotIds,
                        onSlotClick = onSlotClick
                    )
                }
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                Text(
                    text = "예약 목적",
                    style = MaterialTheme.typography.titleMedium
                )
                PurposeSelector(
                    selectedPurpose = uiState.selectedPurpose,
                    onPurposeSelected = onPurposeSelected
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.message,
                    onValueChange = onMessageChange,
                    label = { Text("메시지") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Button(
                    onClick = onRequestClick,
                    enabled = !uiState.isSubmitting &&
                        uiState.selectedSlotIds.isNotEmpty() &&
                        uiState.message.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator()
                    } else {
                        Text("예약 신청")
                    }
                }
            }
            item {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(
    dates: List<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedDateText = selectedDate?.format(DATE_FORMATTER).orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedDateText,
            onValueChange = {},
            readOnly = true,
            label = { Text("날짜") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            dates.forEach { date ->
                DropdownMenuItem(
                    text = { Text(date.format(DATE_FORMATTER)) },
                    onClick = {
                        onDateSelected(date)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HostProfile(
    nickname: String,
    bio: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = nickname.ifBlank { "호스트" },
            style = MaterialTheme.typography.headlineSmall
        )
        if (bio.isNotBlank()) {
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TimeSlotGrid(
    slots: List<TimeSlot>,
    selectedSlotIds: List<String>,
    onSlotClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slots.chunked(2).forEach { rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSlots.forEach { slot ->
                    TimeSlotChip(
                        slot = slot,
                        selected = selectedSlotIds.contains(slot.id),
                        onClick = { onSlotClick(slot.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowSlots.size == 1) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

@Composable
private fun TimeSlotChip(
    slot: TimeSlot,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(formatSlotTimeOnly(slot)) },
        modifier = modifier.heightIn(min = 44.dp)
    )
}

@Composable
private fun PurposeSelector(
    selectedPurpose: ReservationPurpose,
    onPurposeSelected: (ReservationPurpose) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReservationPurpose.entries.forEach { purpose ->
            FilterChip(
                selected = selectedPurpose == purpose,
                onClick = { onPurposeSelected(purpose) },
                label = { Text(purposeLabel(purpose)) }
            )
        }
    }
}

private fun purposeLabel(purpose: ReservationPurpose): String = when (purpose) {
    ReservationPurpose.COFFEE_CHAT -> "커피챗"
    ReservationPurpose.MEAL -> "식사"
    ReservationPurpose.STUDY -> "스터디"
    ReservationPurpose.CONSULTING -> "상담"
    ReservationPurpose.ETC -> "기타"
}

private fun formatSlotTimeOnly(slot: TimeSlot): String {
    val zoneId = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(slot.startAt).atZone(zoneId)
    val end = Instant.ofEpochMilli(slot.endAt).atZone(zoneId)
    return "${start.format(TIME_FORMATTER)} - ${end.format(TIME_FORMATTER)}"
}

private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
