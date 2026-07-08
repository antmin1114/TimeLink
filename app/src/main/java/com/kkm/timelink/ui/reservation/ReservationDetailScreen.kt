package com.kkm.timelink.ui.reservation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kkm.timelink.domain.model.Reservation
import com.kkm.timelink.domain.model.ReservationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    uiState: ReservationDetailUiState,
    onApproveClick: () -> Unit,
    onRejectClick: (String) -> Unit,
    onCancelClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("예약 상세") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.reservation == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("예약 정보가 없습니다.")
                }
            }

            else -> ReservationDetailContent(
                reservation = uiState.reservation,
                currentUserId = uiState.currentUserId,
                isActionLoading = uiState.isActionLoading,
                onApproveClick = onApproveClick,
                onRejectClick = onRejectClick,
                onCancelClick = onCancelClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
            )
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
    modifier: Modifier = Modifier
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    val isHost = reservation.hostId == currentUserId
    val isGuest = reservation.guestId == currentUserId
    val canApproveOrReject = isHost && reservation.status == ReservationStatus.PENDING.name
    val canCancel = (isHost || isGuest) &&
        (
            reservation.status == ReservationStatus.PENDING.name ||
                reservation.status == ReservationStatus.APPROVED.name
            )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = purposeLabel(reservation.purpose),
            style = MaterialTheme.typography.headlineSmall
        )
        DetailRow(label = "상태", value = reservationStatusLabel(reservation.status))
        DetailRow(label = "시간", value = formatReservationTime(reservation))
        DetailRow(label = "호스트 ID", value = reservation.hostId)
        DetailRow(label = "예약자 ID", value = reservation.guestId)
        DetailRow(label = "슬롯 ID", value = reservation.slotIds.joinToString())
        HorizontalDivider()
        DetailRow(label = "메시지", value = reservation.message.ifBlank { "메시지 없음" })
        if (!reservation.rejectReason.isNullOrBlank()) {
            DetailRow(label = "거절 사유", value = reservation.rejectReason)
        }
        if (canApproveOrReject || canCancel) {
            ReservationActions(
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
private fun ReservationActions(
    canApproveOrReject: Boolean,
    canCancel: Boolean,
    isActionLoading: Boolean,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (canApproveOrReject) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApproveClick,
                    enabled = !isActionLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("승인")
                }
                OutlinedButton(
                    onClick = onRejectClick,
                    enabled = !isActionLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("거절")
                }
            }
        }
        if (canCancel) {
            OutlinedButton(
                onClick = onCancelClick,
                enabled = !isActionLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("예약 취소")
            }
        }
    }
}

@Composable
private fun RejectReasonDialog(
    isActionLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            if (!isActionLoading) {
                onDismiss()
            }
        },
        title = { Text("예약 거절") },
        text = {
            TextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("사유") },
                enabled = !isActionLoading,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = !isActionLoading && reason.isNotBlank()
            ) {
                Text("거절")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isActionLoading
            ) {
                Text("닫기")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
