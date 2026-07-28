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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkm.timelink.domain.model.Reservation
import com.kkm.timelink.domain.model.ReservationStatus

private val DetailNavy = Color(0xFF171D3A)
private val DetailPurple = Color(0xFF6668F7)
private val DetailMuted = Color(0xFF7F8595)
private val DetailDivider = Color(0xFFE8EAF1)

@Composable
fun ReservationDetailScreen(
    uiState: ReservationDetailUiState,
    onApproveClick: () -> Unit,
    onRejectClick: (String) -> Unit,
    onCancelClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFBFBFF), Color(0xFFF6F7FD))
                )
            )
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                color = DetailPurple,
                modifier = Modifier.align(Alignment.Center)
            )

            uiState.reservation == null -> EmptyDetail(
                onBackClick = onBackClick,
                modifier = Modifier.fillMaxSize()
            )

            else -> ReservationDetailContent(
                reservation = uiState.reservation,
                currentUserId = uiState.currentUserId,
                isActionLoading = uiState.isActionLoading,
                onApproveClick = onApproveClick,
                onRejectClick = onRejectClick,
                onCancelClick = onCancelClick,
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun EmptyDetail(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
        DetailHeader(onBackClick)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("예약 정보를 찾을 수 없습니다.", color = DetailMuted, fontSize = 15.sp)
        }
    }
}

@Composable
private fun ReservationDetailContent(
    reservation: Reservation,
    currentUserId: String?,
    isActionLoading: Boolean,
    onApproveClick: () -> Unit,
    onRejectClick: (String) -> Unit,
    onCancelClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    val isHost = reservation.hostId == currentUserId
    val isGuest = reservation.guestId == currentUserId
    val canApproveOrReject = isHost && reservation.status == ReservationStatus.PENDING.name
    val canCancel = (isHost || isGuest) &&
        (reservation.status == ReservationStatus.PENDING.name ||
            reservation.status == ReservationStatus.APPROVED.name)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DetailHeader(onBackClick) }
        item { ReservationSummaryCard(reservation) }
        item {
            ReservationInformationCard(
                reservation = reservation,
                canApproveOrReject = canApproveOrReject,
                canCancel = canCancel,
                isActionLoading = isActionLoading,
                onApproveClick = onApproveClick,
                onRejectClick = { showRejectDialog = true },
                onCancelClick = onCancelClick
            )
        }
    }

    if (showRejectDialog) {
        RejectReasonDialog(
            isActionLoading = isActionLoading,
            onDismiss = { showRejectDialog = false },
            onConfirm = { reason ->
                showRejectDialog = false
                onRejectClick(reason)
            }
        )
    }
}

@Composable
private fun DetailHeader(onBackClick: () -> Unit) {
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
                tint = DetailNavy,
                modifier = Modifier.size(28.dp)
            )
        }
        Text("예약 상세", color = DetailNavy, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReservationSummaryCard(reservation: Reservation) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F8FF),
        border = BorderStroke(1.dp, Color(0xFFE8E7FF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                DetailStatusBadge(reservation.status)
                Spacer(Modifier.height(13.dp))
                Text(
                    purposeLabel(reservation.purpose),
                    color = DetailNavy,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    statusDescription(reservation.status),
                    color = DetailMuted,
                    fontSize = 14.sp
                )
            }
            DetailCalendarArtwork(Modifier.size(112.dp))
        }
    }
}

@Composable
private fun DetailStatusBadge(status: String) {
    val (background, foreground) = when (status) {
        ReservationStatus.PENDING.name -> Color(0xFFF0EFFF) to DetailPurple
        ReservationStatus.APPROVED.name -> Color(0xFFEAF9F3) to Color(0xFF269B78)
        ReservationStatus.REJECTED.name -> Color(0xFFFFEEEE) to Color(0xFFE05A62)
        else -> Color(0xFFF0F1F5) to Color(0xFF7F8695)
    }
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(9.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            reservationStatusLabel(status),
            color = foreground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ReservationInformationCard(
    reservation: Reservation,
    canApproveOrReject: Boolean,
    canCancel: Boolean,
    isActionLoading: Boolean,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp)) {
            Text("예약 정보", color = DetailNavy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            InformationRow(DetailIcon.Calendar, "시간", formatReservationTime(reservation))
            InformationDivider()
            InformationRow(DetailIcon.Host, "호스트 ID", reservation.hostId)
            InformationDivider()
            InformationRow(DetailIcon.Guest, "예약자 ID", reservation.guestId)
            InformationDivider()
            InformationRow(DetailIcon.Slots, "슬롯 ID", reservation.slotIds.joinToString())
            InformationDivider()
            InformationRow(
                DetailIcon.Message,
                "메시지",
                reservation.message.ifBlank { "메시지 없음" }
            )
            if (!reservation.rejectReason.isNullOrBlank()) {
                InformationDivider()
                InformationRow(DetailIcon.Message, "거절 사유", reservation.rejectReason)
            }
            if (canApproveOrReject || canCancel) {
                Spacer(Modifier.height(20.dp))
                ReservationActions(
                    canApproveOrReject = canApproveOrReject,
                    canCancel = canCancel,
                    isActionLoading = isActionLoading,
                    onApproveClick = onApproveClick,
                    onRejectClick = onRejectClick,
                    onCancelClick = onCancelClick
                )
            }
        }
    }
}

private enum class DetailIcon { Calendar, Host, Guest, Slots, Message }

@Composable
private fun InformationRow(icon: DetailIcon, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DetailIconCircle(icon, Modifier.size(48.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = DetailMuted, fontSize = 14.sp)
            Text(
                value,
                color = DetailNavy,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InformationDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .padding(start = 64.dp)
            .height(1.dp)
            .background(DetailDivider)
    )
}

@Composable
private fun DetailIconCircle(icon: DetailIcon, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFFF3F2FF), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(25.dp)) {
            val color = DetailPurple
            val stroke = 2.dp.toPx()
            when (icon) {
                DetailIcon.Calendar -> {
                    drawRoundRect(color, Offset(size.width * .12f, size.height * .18f), Size(size.width * .76f, size.height * .7f), CornerRadius(4f), style = Stroke(stroke))
                    drawLine(color, Offset(size.width * .12f, size.height * .4f), Offset(size.width * .88f, size.height * .4f), stroke)
                    drawLine(color, Offset(size.width * .32f, size.height * .08f), Offset(size.width * .32f, size.height * .28f), stroke, StrokeCap.Round)
                    drawLine(color, Offset(size.width * .68f, size.height * .08f), Offset(size.width * .68f, size.height * .28f), stroke, StrokeCap.Round)
                }
                DetailIcon.Host, DetailIcon.Guest -> {
                    drawCircle(color, size.width * .2f, Offset(center.x, size.height * .31f), style = Stroke(stroke))
                    drawArc(color, 195f, 150f, false, Offset(size.width * .16f, size.height * .5f), Size(size.width * .68f, size.height * .55f), style = Stroke(stroke, cap = StrokeCap.Round))
                    if (icon == DetailIcon.Host) drawCircle(color, size.width * .07f, Offset(size.width * .82f, size.height * .3f))
                }
                DetailIcon.Slots -> {
                    listOf(.12f to .12f, .58f to .12f, .12f to .58f, .58f to .58f).forEach { (x, y) ->
                        drawRoundRect(color, Offset(size.width * x, size.height * y), Size(size.width * .3f, size.height * .3f), CornerRadius(3f), style = Stroke(stroke))
                    }
                }
                DetailIcon.Message -> {
                    drawRoundRect(color, Offset(size.width * .08f, size.height * .16f), Size(size.width * .84f, size.height * .6f), CornerRadius(5f), style = Stroke(stroke))
                    drawLine(color, Offset(size.width * .3f, size.height * .76f), Offset(size.width * .22f, size.height * .92f), stroke, StrokeCap.Round)
                    drawLine(color, Offset(size.width * .3f, size.height * .43f), Offset(size.width * .7f, size.height * .43f), stroke, StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun ReservationActions(
    canApproveOrReject: Boolean,
    canCancel: Boolean,
    isActionLoading: Boolean,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (canApproveOrReject) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientActionButton(
                    text = "승인",
                    enabled = !isActionLoading,
                    onClick = onApproveClick,
                    modifier = Modifier.weight(1f)
                )
                OutlineActionButton(
                    text = "거절",
                    enabled = !isActionLoading,
                    onClick = onRejectClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (canCancel) {
            OutlineActionButton(
                text = "예약 취소",
                enabled = !isActionLoading,
                onClick = onCancelClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GradientActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF5C61F6), Color(0xFF7770F4))),
                RoundedCornerShape(13.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White.copy(alpha = if (enabled) 1f else .55f), fontSize = 17.sp)
    }
}

@Composable
private fun OutlineActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .border(1.dp, DetailPurple.copy(alpha = if (enabled) 1f else .4f), RoundedCornerShape(13.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = DetailPurple.copy(alpha = if (enabled) 1f else .45f), fontSize = 17.sp)
    }
}

@Composable
private fun DetailCalendarArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val purple = Color(0xFF7772F8)
        drawCircle(purple.copy(alpha = .12f), size.width * .29f, Offset(size.width * .7f, size.height * .67f))
        drawRoundRect(purple.copy(alpha = .14f), Offset(size.width * .1f, size.height * .23f), Size(size.width * .64f, size.height * .52f), CornerRadius(12f))
        drawRoundRect(purple.copy(alpha = .7f), Offset(size.width * .1f, size.height * .23f), Size(size.width * .64f, size.height * .15f), CornerRadius(10f))
        listOf(.25f, .48f, .7f).forEach { x ->
            drawLine(purple, Offset(size.width * x, size.height * .15f), Offset(size.width * x, size.height * .31f), 5.dp.toPx(), StrokeCap.Round)
        }
        drawLine(purple.copy(alpha = .85f), Offset(size.width * .29f, size.height * .53f), Offset(size.width * .4f, size.height * .64f), 5.dp.toPx(), StrokeCap.Round)
        drawLine(purple.copy(alpha = .85f), Offset(size.width * .4f, size.height * .64f), Offset(size.width * .58f, size.height * .45f), 5.dp.toPx(), StrokeCap.Round)
        drawCircle(Color(0xFFE6E4FF), size.width * .23f, Offset(size.width * .76f, size.height * .71f))
        drawCircle(purple.copy(alpha = .5f), size.width * .23f, Offset(size.width * .76f, size.height * .71f), style = Stroke(3.dp.toPx()))
        drawLine(purple, Offset(size.width * .76f, size.height * .71f), Offset(size.width * .76f, size.height * .59f), 3.dp.toPx(), StrokeCap.Round)
        drawLine(purple, Offset(size.width * .76f, size.height * .71f), Offset(size.width * .84f, size.height * .76f), 3.dp.toPx(), StrokeCap.Round)
    }
}

private fun statusDescription(status: String): String = when (status) {
    ReservationStatus.PENDING.name -> "예약 승인 대기 중입니다."
    ReservationStatus.APPROVED.name -> "예약이 승인되었습니다."
    ReservationStatus.REJECTED.name -> "예약이 거절되었습니다."
    ReservationStatus.CANCELLED.name -> "예약이 취소되었습니다."
    else -> "예약 상태를 확인해 주세요."
}

@Composable
private fun RejectReasonDialog(
    isActionLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isActionLoading) onDismiss() },
        title = { Text("예약 거절", color = DetailNavy, fontWeight = FontWeight.Bold) },
        text = {
            TextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("거절 사유") },
                enabled = !isActionLoading,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason.trim()) },
                enabled = !isActionLoading && reason.isNotBlank()
            ) {
                Text("거절", color = DetailPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isActionLoading) {
                Text("닫기", color = DetailMuted)
            }
        }
    )
}
