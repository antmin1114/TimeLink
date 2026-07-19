package com.kkm.timelink.ui.reservation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkm.timelink.domain.model.Reservation
import com.kkm.timelink.domain.model.ReservationPurpose
import com.kkm.timelink.domain.model.ReservationStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ReservationNavy = Color(0xFF171D3A)
private val ReservationPurple = Color(0xFF6868F7)
private val ReservationMuted = Color(0xFF858B9D)
private val ReservationBackground = Color(0xFFF8F9FF)

@Composable
fun ReservationListScreen(
    uiState: ReservationListUiState,
    onReservationClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(ReservationBackground),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ReservationHeader(
                title = if (uiState.mode == ReservationListMode.RECEIVED) "받은 예약" else "신청한 예약",
                onBackClick = onBackClick
            )
        }
        item { ReservationIntroCard(uiState.mode) }
        item { ReservationListHeader() }

        when {
            uiState.isLoading -> item { ReservationLoadingCard() }
            uiState.reservations.isEmpty() -> item { EmptyReservationCard(uiState.mode) }
            else -> items(uiState.reservations, key = { it.id }) { reservation ->
                ReservationListItem(
                    reservation = reservation,
                    onClick = { onReservationClick(reservation.id) }
                )
            }
        }
    }
}

@Composable
private fun ReservationHeader(title: String, onBackClick: () -> Unit) {
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
                tint = ReservationNavy,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(title, color = ReservationNavy, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReservationIntroCard(mode: ReservationListMode) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(132.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F8FF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5FF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (mode == ReservationListMode.RECEIVED) "받은 예약 목록" else "신청한 예약 목록",
                    color = ReservationNavy,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (mode == ReservationListMode.RECEIVED) {
                        "예약한 일정을 확인하고\n승인 또는 거절할 수 있어요."
                    } else {
                        "내가 신청한 예약 일정과\n진행 상태를 확인해보세요."
                    },
                    color = ReservationMuted,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
            ReservationArtwork(Modifier.size(105.dp))
        }
    }
}

@Composable
private fun ReservationListHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "예약 목록",
            color = ReservationNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text("최신순", color = ReservationMuted, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = ReservationMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ReservationListItem(reservation: Reservation, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PurposeIcon(reservation.purpose, Modifier.size(54.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                StatusBadge(reservation.status)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = purposeLabel(reservation.purpose),
                    color = ReservationNavy,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                ReservationInfoRow(InfoIcon.Clock, formatReservationTime(reservation))
                Spacer(Modifier.height(9.dp))
                ReservationInfoRow(
                    InfoIcon.Message,
                    reservation.message.ifBlank { "메시지 없음" }
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "예약 상세 보기",
                tint = Color(0xFF7D8496),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (background, foreground) = when (status) {
        ReservationStatus.PENDING.name -> Color(0xFFF1F0FF) to ReservationPurple
        ReservationStatus.APPROVED.name -> Color(0xFFEAF9F3) to Color(0xFF269B78)
        ReservationStatus.REJECTED.name -> Color(0xFFFFEEEE) to Color(0xFFE05A62)
        ReservationStatus.CANCELLED.name -> Color(0xFFF0F1F5) to Color(0xFF7F8695)
        else -> Color(0xFFF0F1F5) to ReservationMuted
    }
    Box(
        modifier = Modifier.background(background, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(reservationStatusLabel(status), color = foreground, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private enum class InfoIcon { Clock, Message }

@Composable
private fun ReservationInfoRow(icon: InfoIcon, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(18.dp)) {
            val color = Color(0xFFA2A9BA)
            val stroke = 1.7.dp.toPx()
            when (icon) {
                InfoIcon.Clock -> {
                    drawCircle(color, size.minDimension * .42f, style = Stroke(stroke))
                    drawLine(color, center, Offset(center.x, size.height * .27f), stroke, StrokeCap.Round)
                    drawLine(color, center, Offset(size.width * .68f, size.height * .58f), stroke, StrokeCap.Round)
                }
                InfoIcon.Message -> {
                    drawRoundRect(color, Offset(size.width * .08f, size.height * .14f), Size(size.width * .82f, size.height * .6f), CornerRadius(3f), style = Stroke(stroke))
                    drawLine(color, Offset(size.width * .28f, size.height * .74f), Offset(size.width * .2f, size.height * .9f), stroke, StrokeCap.Round)
                    drawLine(color, Offset(size.width * .3f, size.height * .4f), Offset(size.width * .7f, size.height * .4f), stroke, StrokeCap.Round)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(text, color = ReservationMuted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PurposeIcon(purpose: String, modifier: Modifier = Modifier) {
    Box(modifier.background(Color(0xFFF4F3FF), CircleShape), contentAlignment = Alignment.Center) {
        Text(
            text = when (purpose) {
                ReservationPurpose.COFFEE_CHAT.name -> "☕"
                ReservationPurpose.MEAL.name -> "🍚"
                ReservationPurpose.STUDY.name -> "📚"
                ReservationPurpose.CONSULTING.name -> "👨‍💻"
                else -> "···"
            },
            color = ReservationPurple,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ReservationArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val purple = Color(0xFF7772F8)
        drawCircle(purple.copy(alpha = .12f), size.width * .29f, Offset(size.width * .7f, size.height * .63f))
        drawRoundRect(purple.copy(alpha = .18f), Offset(size.width * .13f, size.height * .23f), Size(size.width * .62f, size.height * .5f), CornerRadius(12f))
        drawRoundRect(purple.copy(alpha = .7f), Offset(size.width * .13f, size.height * .23f), Size(size.width * .62f, size.height * .15f), CornerRadius(10f))
        listOf(.28f, .5f, .72f).forEach { x ->
            drawLine(purple, Offset(size.width * x, size.height * .17f), Offset(size.width * x, size.height * .31f), 5.dp.toPx(), StrokeCap.Round)
        }
        drawLine(purple.copy(alpha = .8f), Offset(size.width * .3f, size.height * .52f), Offset(size.width * .4f, size.height * .62f), 5.dp.toPx(), StrokeCap.Round)
        drawLine(purple.copy(alpha = .8f), Offset(size.width * .4f, size.height * .62f), Offset(size.width * .57f, size.height * .44f), 5.dp.toPx(), StrokeCap.Round)
        drawCircle(Color(0xFFE5E3FF), size.width * .24f, Offset(size.width * .76f, size.height * .7f))
        drawCircle(purple.copy(alpha = .45f), size.width * .24f, Offset(size.width * .76f, size.height * .7f), style = Stroke(3.dp.toPx()))
        drawLine(purple, Offset(size.width * .76f, size.height * .7f), Offset(size.width * .76f, size.height * .58f), 3.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun ReservationLoadingCard() {
    Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = ReservationPurple)
    }
}

@Composable
private fun EmptyReservationCard(mode: ReservationListMode) {
    Surface(Modifier.fillMaxWidth().height(160.dp), shape = RoundedCornerShape(18.dp), color = Color.White) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                if (mode == ReservationListMode.RECEIVED) "받은 예약이 없어요." else "신청한 예약이 없어요.",
                color = ReservationMuted,
                fontSize = 15.sp
            )
        }
    }
}

internal fun purposeLabel(value: String): String = when (value) {
    ReservationPurpose.COFFEE_CHAT.name -> "커피챗"
    ReservationPurpose.MEAL.name -> "식사"
    ReservationPurpose.STUDY.name -> "스터디"
    ReservationPurpose.CONSULTING.name -> "상담"
    ReservationPurpose.ETC.name -> "기타"
    else -> value
}

internal fun reservationStatusLabel(value: String): String = when (value) {
    ReservationStatus.PENDING.name -> "승인 대기"
    ReservationStatus.APPROVED.name -> "승인 완료"
    ReservationStatus.REJECTED.name -> "거절됨"
    ReservationStatus.CANCELLED.name -> "취소됨"
    else -> value
}

internal fun formatReservationTime(reservation: Reservation): String {
    val zoneId = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(reservation.startAt).atZone(zoneId)
    val end = Instant.ofEpochMilli(reservation.endAt).atZone(zoneId)
    return "${start.format(DATE_TIME_FORMATTER)} - ${end.format(TIME_FORMATTER)}"
}

private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd (E) HH:mm")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
