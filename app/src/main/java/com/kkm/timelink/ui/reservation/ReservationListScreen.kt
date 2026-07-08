package com.kkm.timelink.ui.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kkm.timelink.domain.model.Reservation
import com.kkm.timelink.domain.model.ReservationPurpose
import com.kkm.timelink.domain.model.ReservationStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationListScreen(
    uiState: ReservationListUiState,
    onReservationClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState.mode) {
                            ReservationListMode.RECEIVED -> "받은 예약"
                            ReservationListMode.MINE -> "내 예약"
                        }
                    )
                },
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

            uiState.reservations.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("예약이 없습니다.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.reservations,
                        key = { it.id }
                    ) { reservation ->
                        ReservationListItem(
                            reservation = reservation,
                            onClick = { onReservationClick(reservation.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationListItem(
    reservation: Reservation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = purposeLabel(reservation.purpose),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = reservationStatusLabel(reservation.status),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Text(
                text = formatReservationTime(reservation),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = reservation.message.ifBlank { "메시지 없음" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
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

private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
