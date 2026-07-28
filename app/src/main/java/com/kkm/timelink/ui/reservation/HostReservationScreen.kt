package com.kkm.timelink.ui.reservation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkm.timelink.domain.model.ReservationPurpose
import com.kkm.timelink.domain.model.TimeSlot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val RequestNavy = Color(0xFF171D3A)
private val RequestPurple = Color(0xFF6264F4)
private val RequestMuted = Color(0xFF858B9D)
private val RequestBorder = Color(0xFFDDE0EA)
private const val MESSAGE_MAX_LENGTH = 200

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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFBFBFF), Color(0xFFF6F7FD))))
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = RequestPurple,
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
        }

        val availableDates = uiState.availableSlots
            .map { it.startAt.toLocalDate() }
            .distinct()
        val selectedDateSlots = uiState.availableSlots.filter {
            it.startAt.toLocalDate() == uiState.selectedDate
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { RequestHeader(onBackClick) }
            item {
                HostIntroCard(
                    nickname = uiState.host?.nickname.orEmpty(),
                    bio = uiState.host?.bio.orEmpty()
                )
            }
            item {
                ReservationFormCard(
                    availableDates = availableDates,
                    selectedDate = uiState.selectedDate,
                    selectedDateSlots = selectedDateSlots,
                    selectedSlotIds = uiState.selectedSlotIds,
                    selectedPurpose = uiState.selectedPurpose,
                    message = uiState.message,
                    isSubmitting = uiState.isSubmitting,
                    onDateSelected = onDateSelected,
                    onSlotClick = onSlotClick,
                    onPurposeSelected = onPurposeSelected,
                    onMessageChange = onMessageChange,
                    onRequestClick = onRequestClick
                )
            }
        }
    }
}

@Composable
private fun RequestHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clickable(onClick = onBackClick),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로",
                tint = RequestNavy,
                modifier = Modifier.size(28.dp)
            )
        }
        Text("예약 신청", color = RequestNavy, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HostIntroCard(nickname: String, bio: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(142.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F8FF),
        border = BorderStroke(1.dp, Color(0xFFE7E6FF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nickname.ifBlank { "호스트" },
                    color = RequestNavy,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    bio.ifBlank { "예약 가능한 시간을 확인해 보세요." },
                    color = RequestMuted,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
            RequestCalendarArtwork(Modifier.size(108.dp))
        }
    }
}

@Composable
private fun ReservationFormCard(
    availableDates: List<LocalDate>,
    selectedDate: LocalDate?,
    selectedDateSlots: List<TimeSlot>,
    selectedSlotIds: List<String>,
    selectedPurpose: ReservationPurpose,
    message: String,
    isSubmitting: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onSlotClick: (String) -> Unit,
    onPurposeSelected: (ReservationPurpose) -> Unit,
    onMessageChange: (String) -> Unit,
    onRequestClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
            Text("예약 가능한 시간", color = RequestNavy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Text("날짜", color = RequestNavy, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(9.dp))
            DateSelector(
                dates = availableDates,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
            Spacer(Modifier.height(14.dp))

            when {
                availableDates.isEmpty() -> EmptySlotsText("예약 가능한 시간 슬롯이 없습니다.")
                selectedDateSlots.isEmpty() -> EmptySlotsText("선택한 날짜에 예약 가능한 시간이 없습니다.")
                else -> TimeSlotGrid(
                    slots = selectedDateSlots,
                    selectedSlotIds = selectedSlotIds,
                    onSlotClick = onSlotClick
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("예약 목적", color = RequestNavy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            PurposeSelector(
                selectedPurpose = selectedPurpose,
                onPurposeSelected = onPurposeSelected
            )
            Spacer(Modifier.height(24.dp))
            Text("메시지", color = RequestNavy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            MessageInput(
                value = message,
                onValueChange = onMessageChange
            )
            Spacer(Modifier.height(16.dp))
            RequestButton(
                isSubmitting = isSubmitting,
                enabled = !isSubmitting && selectedSlotIds.isNotEmpty() && message.isNotBlank(),
                onClick = onRequestClick
            )
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (dates.isNotEmpty()) expanded = it }
    ) {
        Row(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = dates.isNotEmpty())
                .fillMaxWidth()
                .height(54.dp)
                .border(1.dp, RequestBorder, RoundedCornerShape(13.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarGlyph(Modifier.size(23.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                selectedDate?.format(DATE_FORMATTER) ?: "예약 가능한 날짜 없음",
                color = if (selectedDate == null) RequestMuted else RequestNavy,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "날짜 선택",
                tint = Color(0xFF9AA0B2)
            )
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
private fun TimeSlotGrid(
    slots: List<TimeSlot>,
    selectedSlotIds: List<String>,
    onSlotClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        slots.chunked(2).forEach { rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowSlots.forEach { slot ->
                    TimeSlotButton(
                        slot = slot,
                        selected = slot.id in selectedSlotIds,
                        onClick = { onSlotClick(slot.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowSlots.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TimeSlotButton(
    slot: TimeSlot,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .then(
                if (selected) {
                    Modifier.background(
                        Brush.horizontalGradient(listOf(Color(0xFF5D62F5), Color(0xFF7770F4))),
                        RoundedCornerShape(13.dp)
                    )
                } else {
                    Modifier.border(1.dp, Color(0xFFCCD0DD), RoundedCornerShape(13.dp))
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            formatSlotTimeOnly(slot),
            color = if (selected) Color.White else RequestNavy,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun EmptySlotsText(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(Color(0xFFF8F8FC), RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = RequestMuted, fontSize = 14.sp)
    }
}

@Composable
private fun PurposeSelector(
    selectedPurpose: ReservationPurpose,
    onPurposeSelected: (ReservationPurpose) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        ReservationPurpose.entries.forEach { purpose ->
            PurposeButton(
                purpose = purpose,
                selected = selectedPurpose == purpose,
                onClick = { onPurposeSelected(purpose) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PurposeButton(
    purpose: ReservationPurpose,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .then(
                if (selected) {
                    Modifier.background(
                        Brush.horizontalGradient(listOf(Color(0xFF5D62F5), Color(0xFF766EF4))),
                        RoundedCornerShape(13.dp)
                    )
                } else {
                    Modifier.border(1.dp, RequestBorder, RoundedCornerShape(13.dp))
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PurposeGlyph(
                purpose = purpose,
                color = if (selected) Color.White else RequestPurple,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                purposeLabel(purpose),
                color = if (selected) Color.White else RequestNavy,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MessageInput(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = { if (it.length <= MESSAGE_MAX_LENGTH) onValueChange(it) },
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .border(1.dp, RequestBorder, RoundedCornerShape(13.dp))
            .padding(14.dp),
        textStyle = TextStyle(color = RequestNavy, fontSize = 15.sp, lineHeight = 21.sp),
        cursorBrush = SolidColor(RequestPurple),
        decorationBox = { innerTextField ->
            Box(Modifier.fillMaxSize()) {
                if (value.isEmpty()) {
                    Text("메시지를 입력해주세요.", color = Color(0xFF9BA1B2), fontSize = 15.sp)
                }
                innerTextField()
                Text(
                    "${value.length}/$MESSAGE_MAX_LENGTH",
                    color = Color(0xFF9298AA),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    )
}

@Composable
private fun RequestButton(isSubmitting: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF5D61F5).copy(alpha = if (enabled) 1f else .45f),
                        Color(0xFF8267ED).copy(alpha = if (enabled) 1f else .45f)
                    )
                ),
                RoundedCornerShape(13.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        } else {
            Text("예약 신청", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CalendarGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val color = Color(0xFF9AA0B2)
        val stroke = 1.8.dp.toPx()
        drawRoundRect(color, Offset(size.width * .08f, size.height * .16f), Size(size.width * .84f, size.height * .76f), CornerRadius(4f), style = Stroke(stroke))
        drawLine(color, Offset(size.width * .08f, size.height * .4f), Offset(size.width * .92f, size.height * .4f), stroke)
        drawLine(color, Offset(size.width * .3f, size.height * .07f), Offset(size.width * .3f, size.height * .26f), stroke, StrokeCap.Round)
        drawLine(color, Offset(size.width * .7f, size.height * .07f), Offset(size.width * .7f, size.height * .26f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun PurposeGlyph(purpose: ReservationPurpose, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = 1.8.dp.toPx()
        when (purpose) {
            ReservationPurpose.COFFEE_CHAT -> {
                drawRoundRect(color, Offset(size.width * .08f, size.height * .12f), Size(size.width * .74f, size.height * .58f), CornerRadius(7f), style = Stroke(stroke))
                drawLine(color, Offset(size.width * .28f, size.height * .7f), Offset(size.width * .2f, size.height * .88f), stroke, StrokeCap.Round)
                drawCircle(color, size.width * .025f, Offset(size.width * .3f, size.height * .4f))
                drawCircle(color, size.width * .025f, Offset(size.width * .47f, size.height * .4f))
                drawCircle(color, size.width * .025f, Offset(size.width * .64f, size.height * .4f))
            }
            ReservationPurpose.MEAL -> {
                drawLine(color, Offset(size.width * .25f, size.height * .12f), Offset(size.width * .25f, size.height * .88f), stroke, StrokeCap.Round)
                drawLine(color, Offset(size.width * .1f, size.height * .12f), Offset(size.width * .1f, size.height * .42f), stroke)
                drawLine(color, Offset(size.width * .4f, size.height * .12f), Offset(size.width * .4f, size.height * .42f), stroke)
                drawLine(color, Offset(size.width * .1f, size.height * .42f), Offset(size.width * .4f, size.height * .42f), stroke)
                drawArc(color, 180f, 180f, false, Offset(size.width * .58f, size.height * .1f), Size(size.width * .3f, size.height * .5f), style = Stroke(stroke))
                drawLine(color, Offset(size.width * .73f, size.height * .35f), Offset(size.width * .73f, size.height * .88f), stroke)
            }
            ReservationPurpose.STUDY -> {
                drawLine(color, Offset(center.x, size.height * .2f), Offset(center.x, size.height * .86f), stroke)
                drawRoundRect(color, Offset(size.width * .08f, size.height * .14f), Size(size.width * .42f, size.height * .68f), CornerRadius(3f), style = Stroke(stroke))
                drawRoundRect(color, Offset(size.width * .5f, size.height * .14f), Size(size.width * .42f, size.height * .68f), CornerRadius(3f), style = Stroke(stroke))
            }
            ReservationPurpose.CONSULTING -> {
                drawCircle(color, size.width * .18f, Offset(center.x, size.height * .29f), style = Stroke(stroke))
                drawArc(color, 195f, 150f, false, Offset(size.width * .17f, size.height * .48f), Size(size.width * .66f, size.height * .5f), style = Stroke(stroke, cap = StrokeCap.Round))
            }
            ReservationPurpose.ETC -> {
                drawCircle(color, size.width * .4f, center, style = Stroke(stroke))
                listOf(.32f, .5f, .68f).forEach { x ->
                    drawCircle(color, size.width * .035f, Offset(size.width * x, center.y))
                }
            }
        }
    }
}

@Composable
private fun RequestCalendarArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val purple = Color(0xFF7772F8)
        drawCircle(purple.copy(alpha = .12f), size.width * .29f, Offset(size.width * .7f, size.height * .67f))
        drawRoundRect(purple.copy(alpha = .16f), Offset(size.width * .1f, size.height * .23f), Size(size.width * .64f, size.height * .52f), CornerRadius(12f))
        drawRoundRect(purple.copy(alpha = .72f), Offset(size.width * .1f, size.height * .23f), Size(size.width * .64f, size.height * .15f), CornerRadius(10f))
        listOf(.25f, .48f, .7f).forEach { x ->
            drawLine(purple, Offset(size.width * x, size.height * .15f), Offset(size.width * x, size.height * .31f), 5.dp.toPx(), StrokeCap.Round)
        }
        drawLine(purple, Offset(size.width * .29f, size.height * .53f), Offset(size.width * .4f, size.height * .64f), 5.dp.toPx(), StrokeCap.Round)
        drawLine(purple, Offset(size.width * .4f, size.height * .64f), Offset(size.width * .58f, size.height * .45f), 5.dp.toPx(), StrokeCap.Round)
        drawCircle(Color(0xFFE5E3FF), size.width * .23f, Offset(size.width * .76f, size.height * .71f))
        drawCircle(purple.copy(alpha = .5f), size.width * .23f, Offset(size.width * .76f, size.height * .71f), style = Stroke(3.dp.toPx()))
        drawLine(purple, Offset(size.width * .76f, size.height * .71f), Offset(size.width * .76f, size.height * .59f), 3.dp.toPx(), StrokeCap.Round)
        drawLine(purple, Offset(size.width * .76f, size.height * .71f), Offset(size.width * .84f, size.height * .76f), 3.dp.toPx(), StrokeCap.Round)
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

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
