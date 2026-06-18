package com.kkm.timelink.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    currentUserId: String,
    isLoading: Boolean,
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
            onClick = onSignOutClick,
            enabled = !isLoading,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(text = "로그아웃")
        }
    }
}
