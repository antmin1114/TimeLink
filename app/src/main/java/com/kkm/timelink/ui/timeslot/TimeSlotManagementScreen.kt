package com.kkm.timelink.ui.timeslot

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkm.timelink.domain.model.TimeSlot
import com.kkm.timelink.domain.model.TimeSlotStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val SlotNavy = Color(0xFF151D3B)
private val SlotPurple = Color(0xFF6264F4)
private val SlotMuted = Color(0xFF858C9E)
private val SlotBorder = Color(0xFFE1E4ED)

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
    onHomeClick: () -> Unit,
    onReservationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(modifier.fillMaxSize().background(Color(0xFFF7F8FE))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreenHeader(onBackClick) }
            item { IntroCard() }
            item {
                RegistrationCard(
                    uiState = uiState,
                    onDateClick = {
                        DatePickerDialog(context, { _, y, m, d -> onDateSelected(LocalDate.of(y, m + 1, d)) }, uiState.selectedDate.year, uiState.selectedDate.monthValue - 1, uiState.selectedDate.dayOfMonth).apply {
                            datePicker.minDate = System.currentTimeMillis()
                        }.show()
                    },
                    onStartTimeSelected = onStartTimeSelected,
                    onEndTimeSelected = onEndTimeSelected,
                    onEndOfDaySelected = onEndOfDaySelected,
                    onDurationSelected = onDurationSelected,
                    onCreateClick = onCreateClick
                )
            }
            item { SlotListHeader() }
            when {
                uiState.isLoading && uiState.timeSlots.isEmpty() -> item { LoadingCard() }
                uiState.timeSlots.isEmpty() -> item { EmptySlotCard() }
                else -> items(uiState.timeSlots, key = { it.id }) { slot ->
                    TimeSlotItem(slot, uiState.updatingSlotId == slot.id, { onDisableClick(slot.id) }, { onEnableClick(slot.id) })
                }
            }
        }
    }
}

@Composable
private fun ScreenHeader(onBackClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(54.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(24.dp)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
        }
        Text("시간 슬롯 관리", color = SlotNavy, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun IntroCard() {
    Surface(Modifier.fillMaxWidth().height(128.dp), shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("예약 가능한 시간 등록", color = SlotNavy, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("예약을 받을 날짜와 시간을 설정해보세요.", color = SlotMuted, fontSize = 14.sp)
            }
            CalendarArtwork(Modifier.size(105.dp))
        }
    }
}

@Composable
private fun RegistrationCard(
    uiState: TimeSlotUiState,
    onDateClick: () -> Unit,
    onStartTimeSelected: (Int, Int) -> Unit,
    onEndTimeSelected: (Int, Int) -> Unit,
    onEndOfDaySelected: () -> Unit,
    onDurationSelected: (Int) -> Unit,
    onCreateClick: () -> Unit
) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 3.dp) {
        Column(Modifier.padding(16.dp)) {
            Text("날짜", color = SlotNavy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            PickerField(uiState.selectedDate.format(DATE_FORMATTER), PickerIcon.Calendar, onDateClick)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("시작 시간", color = SlotNavy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    TimeSelectBox(
                        selectedHour = uiState.startHour,
                        selectedMinute = uiState.startMinute,
                        includeEndOfDay = false,
                        onTimeSelected = onStartTimeSelected
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("종료 시간", color = SlotNavy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    TimeSelectBox(
                        selectedHour = uiState.endHour,
                        selectedMinute = uiState.endMinute,
                        includeEndOfDay = true,
                        onTimeSelected = onEndTimeSelected
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().background(Color(0xFFF4F3FF), RoundedCornerShape(11.dp)).clickable(onClick = onEndOfDaySelected).padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(15.dp).background(Color(0xFF7774F5), CircleShape), contentAlignment = Alignment.Center) { Text("i", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                Text("종료 시간을 24:00으로 설정할 수 있습니다.", color = SlotPurple, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text("슬롯 분할 단위", color = SlotNavy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                DurationButton(30, uiState.durationMinutes == 30, onDurationSelected, Modifier.weight(1f))
                DurationButton(60, uiState.durationMinutes == 60, onDurationSelected, Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.fillMaxWidth().height(52.dp).background(Brush.horizontalGradient(listOf(Color(0xFF5B62F5), Color(0xFF8061EC))), RoundedCornerShape(13.dp)).clickable(enabled = !uiState.isCreating, onClick = onCreateClick),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isCreating) CircularProgressIndicator(Modifier.size(23.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("예약 가능 시간 등록", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TimeSelectBox(
    selectedHour: Int,
    selectedMinute: Int,
    includeEndOfDay: Boolean,
    onTimeSelected: (Int, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = remember(includeEndOfDay) {
        buildList {
            for (hour in 0..23) {
                add(hour to 0)
                add(hour to 30)
            }
            if (includeEndOfDay) add(24 to 0)
        }
    }

    Box {
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp).clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlotBorder)
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PickerGlyph(PickerIcon.Clock, Modifier.size(20.dp))
                Text(
                    formatTime(selectedHour, selectedMinute),
                    color = SlotNavy,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(start = 12.dp).weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "시간 선택 열기",
                    tint = Color(0xFF9BA2B3),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            options.forEach { (hour, minute) ->
                DropdownMenuItem(
                    text = { Text(formatTime(hour, minute)) },
                    onClick = {
                        expanded = false
                        onTimeSelected(hour, minute)
                    }
                )
            }
        }
    }
}

@Composable
private fun PickerField(text: String, icon: PickerIcon, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().height(48.dp).clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, SlotBorder)) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            PickerGlyph(icon, Modifier.size(20.dp))
            Text(text, color = SlotNavy, fontSize = 17.sp, modifier = Modifier.padding(start = 12.dp).weight(1f))
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF9BA2B3),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun DurationButton(value: Int, selected: Boolean, onClick: (Int) -> Unit, modifier: Modifier) {
    val background = if (selected) Brush.horizontalGradient(listOf(Color(0xFF5D64F5), Color(0xFF8064EE))) else Brush.linearGradient(listOf(Color.White, Color.White))
    Surface(modifier.height(48.dp).clickable { onClick(value) }, shape = RoundedCornerShape(11.dp), color = Color.Transparent, border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, SlotBorder)) {
        Box(Modifier.fillMaxSize().background(background), contentAlignment = Alignment.Center) { Text("${value}분 단위", color = if (selected) Color.White else SlotNavy, fontSize = 16.sp) }
    }
}

@Composable
private fun SlotListHeader() {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, start = 2.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "내 시간 슬롯",
            color = SlotNavy,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text("전체 보기", color = SlotPurple, fontSize = 14.sp)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = SlotPurple,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmptySlotCard() {
    Surface(Modifier.fillMaxWidth().height(178.dp), shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 3.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            EmptyCalendar(Modifier.size(65.dp)); Spacer(Modifier.height(12.dp)); Text("오늘 이후 생성된 시간 슬롯이 없습니다.", color = SlotMuted, fontSize = 15.sp)
        }
    }
}

@Composable private fun LoadingCard() { Surface(Modifier.fillMaxWidth().height(130.dp), shape = RoundedCornerShape(18.dp), color = Color.White) { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(color = SlotPurple) } } }

@Composable
private fun TimeSlotItem(slot: TimeSlot, isUpdating: Boolean, onDisableClick: () -> Unit, onEnableClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(formatSlotTime(slot), color = SlotNavy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(5.dp)); Text("${slot.durationMinutes}분 · ${statusLabel(slot.status)}", color = SlotMuted, fontSize = 13.sp) }
            when (slot.status) {
                TimeSlotStatus.AVAILABLE.name -> TextButton(onClick = onDisableClick, enabled = !isUpdating) { Text(if (isUpdating) "처리 중" else "비활성화") }
                TimeSlotStatus.DISABLED.name -> TextButton(onClick = onEnableClick, enabled = !isUpdating && slot.startAt > System.currentTimeMillis()) { Text(if (isUpdating) "처리 중" else "활성화") }
            }
        }
    }
}

private enum class PickerIcon { Calendar, Clock }
@Composable private fun PickerGlyph(icon: PickerIcon, modifier: Modifier) { Canvas(modifier) { val c = Color(0xFF949CAD); val sw = 2.dp.toPx(); if (icon == PickerIcon.Clock) { drawCircle(c, size.minDimension * .38f, style = Stroke(sw)); drawLine(c, center, Offset(center.x, size.height * .28f), sw, StrokeCap.Round); drawLine(c, center, Offset(size.width * .65f, size.height * .6f), sw, StrokeCap.Round) } else { drawRoundRect(c, Offset(size.width * .1f, size.height * .18f), Size(size.width * .8f, size.height * .72f), CornerRadius(3f), style = Stroke(sw)); drawLine(c, Offset(size.width * .1f, size.height * .4f), Offset(size.width * .9f, size.height * .4f), sw) } } }

@Composable private fun CalendarArtwork(modifier: Modifier) { Canvas(modifier) { val p = Color(0xFF756EF5); drawCircle(p.copy(.1f), size.width * .28f, Offset(size.width * .72f, size.height * .35f), style = Stroke(size.width * .06f)); drawRoundRect(p.copy(.16f), Offset(size.width * .05f, size.height * .4f), Size(size.width * .6f, size.height * .46f), CornerRadius(12f)); drawRoundRect(p.copy(.7f), Offset(size.width * .05f, size.height * .4f), Size(size.width * .6f, size.height * .13f), CornerRadius(10f)); drawLine(p.copy(.65f), Offset(size.width * .25f, size.height * .66f), Offset(size.width * .34f, size.height * .75f), 5.dp.toPx(), StrokeCap.Round); drawLine(p.copy(.65f), Offset(size.width * .34f, size.height * .75f), Offset(size.width * .5f, size.height * .59f), 5.dp.toPx(), StrokeCap.Round) } }
@Composable private fun EmptyCalendar(modifier: Modifier) { Canvas(modifier) { val p = Color(0xFFB5A5FF); drawRoundRect(p.copy(.22f), Offset(size.width * .08f, size.height * .18f), Size(size.width * .7f, size.height * .62f), CornerRadius(10f)); drawRoundRect(p.copy(.32f), Offset(size.width * .08f, size.height * .18f), Size(size.width * .7f, size.height * .18f), CornerRadius(8f)); drawCircle(p.copy(.48f), size.width * .22f, Offset(size.width * .73f, size.height * .7f)); drawLine(Color.White, Offset(size.width * .73f, size.height * .7f), Offset(size.width * .73f, size.height * .58f), 3.dp.toPx(), StrokeCap.Round); drawLine(Color.White, Offset(size.width * .73f, size.height * .7f), Offset(size.width * .82f, size.height * .76f), 3.dp.toPx(), StrokeCap.Round) } }

private fun formatSlotTime(slot: TimeSlot): String { val zone = ZoneId.systemDefault(); val start = Instant.ofEpochMilli(slot.startAt).atZone(zone); val end = Instant.ofEpochMilli(slot.endAt).atZone(zone); return "${start.format(SLOT_DATE_TIME_FORMATTER)} - ${end.format(TIME_FORMATTER)}" }
private fun statusLabel(status: String) = when (status) { TimeSlotStatus.AVAILABLE.name -> "예약 가능"; TimeSlotStatus.RESERVED.name -> "예약됨"; TimeSlotStatus.DISABLED.name -> "비활성화"; else -> status }
private fun formatTime(hour: Int, minute: Int) = "%02d:%02d".format(hour, minute)
private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val SLOT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
