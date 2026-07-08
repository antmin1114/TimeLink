package com.kkm.timelink.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    currentUserId: String,
    uiState: HomeUiState,
    isSigningOut: Boolean,
    onProfileClick: () -> Unit,
    onTimeSlotsClick: () -> Unit,
    onOpenReservationLinkClick: (String) -> Unit,
    onReservationLinkInputChange: (String) -> Unit,
    onOpenReservationLinkInputClick: () -> Unit,
    onReceivedReservationsClick: () -> Unit,
    onMyReservationsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TimeLink",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "로그인됨: $currentUserId",
            modifier = Modifier.padding(top = 12.dp)
        )
        Button(
            onClick = onProfileClick,
            enabled = !isSigningOut,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(text = "프로필")
        }
        Button(
            onClick = onTimeSlotsClick,
            enabled = !isSigningOut,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(text = "시간 슬롯 관리")
        }
        Text(
            text = "예약 링크 ID: ${uiState.reservationLinkId.ifBlank { "불러오는 중" }}",
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = { onOpenReservationLinkClick(uiState.reservationLinkId) },
            enabled = !isSigningOut &&
                !uiState.isLoadingProfile &&
                uiState.reservationLinkId.isNotBlank(),
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(text = "내 예약 링크 열기")
        }
        Button(
            onClick = {
                clipboardManager.setText(AnnotatedString(uiState.reservationLinkId))
            },
            enabled = !isSigningOut &&
                !uiState.isLoadingProfile &&
                uiState.reservationLinkId.isNotBlank(),
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(text = "링크 ID 복사")
        }
        OutlinedTextField(
            value = uiState.reservationLinkInput,
            onValueChange = onReservationLinkInputChange,
            label = { Text("호스트 링크 ID") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        Button(
            onClick = onOpenReservationLinkInputClick,
            enabled = !isSigningOut &&
                !uiState.isOpeningReservationLink &&
                uiState.reservationLinkInput.isNotBlank(),
            modifier = Modifier.padding(top = 12.dp)
        ) {
            if (uiState.isOpeningReservationLink) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(text = "입력한 ID로 예약하기")
            }
        }
        Button(
            onClick = onReceivedReservationsClick,
            enabled = !isSigningOut,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(text = "받은 예약")
        }
        Button(
            onClick = onMyReservationsClick,
            enabled = !isSigningOut,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(text = "내 예약")
        }
        Button(
            onClick = onSignOutClick,
            enabled = !isSigningOut,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(text = if (isSigningOut) "로그아웃 중" else "로그아웃")
        }
        if (isSigningOut) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Text(text = "로그아웃 처리 중입니다")
            }
        }
    }
}
