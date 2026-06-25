package com.kkm.timelink.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    currentUserId: String,
    isSigningOut: Boolean,
    onProfileClick: () -> Unit,
    onTimeSlotsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
