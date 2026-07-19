package com.kkm.timelink.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.kkm.timelink.R

private val HomeNavy = Color(0xFF151D3B)
private val HomePurple = Color(0xFF6467FF)
private val HomeMuted = Color(0xFF858B9E)

@Composable
fun HomeScreen(
    currentUserId: String,
    uiState: HomeUiState,
    isSigningOut: Boolean,
    onProfileClick: () -> Unit,
    onTimeSlotsClick: () -> Unit,
    onOpenReservationLinkClick: (String) -> Unit,
    onShareReservationLinkClick: (String) -> Unit,
    onReservationLinkInputChange: (String) -> Unit,
    onOpenReservationLinkInputClick: () -> Unit,
    onReceivedReservationsClick: () -> Unit,
    onMyReservationsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reservationLink = if (uiState.reservationLinkId.isBlank()) "" else {
        "${stringResource(R.string.reservation_link_base_url)}/${uiState.reservationLinkId}"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFF9F9FF), Color(0xFFF4F5FC)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 116.dp)
        ) {
            HomeTopBar()
            Spacer(modifier = Modifier.height(47.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            append("안녕하세요")
                            if (uiState.nickname.isNotBlank()) {
                                append(", ")
                                withStyle(SpanStyle(color = HomePurple)) {
                                    append("${uiState.nickname}님")
                                }
                            }
                            append("!")
                        },
                        color = HomeNavy,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "오늘도 소중한 시간을 연결해보세요.",
                        color = HomeMuted,
                        fontSize = 16.sp
                    )
                }
                HeroArtwork(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(130.dp)
                )
            }

            Spacer(modifier = Modifier.height(56.dp))
            MenuCard(
                icon = HomeIcon.Calendar,
                iconColor = Color(0xFF6C69F8),
                iconBackground = Color(0xFFF0EFFF),
                title = "내 예약 슬롯 관리",
                description = "예약 가능한 시간을 설정하고\n관리해보세요.",
                enabled = !isSigningOut,
                onClick = onTimeSlotsClick
            )
            Spacer(modifier = Modifier.height(10.dp))
            MenuCard(
                icon = HomeIcon.Clipboard,
                iconColor = Color(0xFF4C8DF7),
                iconBackground = Color(0xFFEDF5FF),
                title = "내 예약 리스트",
                description = "받은 예약을 확인하고\n관리해보세요.",
                enabled = !isSigningOut,
                onClick = onReceivedReservationsClick
            )
            Spacer(modifier = Modifier.height(10.dp))
            MenuCard(
                icon = HomeIcon.Profile,
                iconColor = Color(0xFF35CDA5),
                iconBackground = Color(0xFFEAFBF5),
                title = "내 프로필",
                description = "내 정보를 확인하고\n프로필을 관리해보세요.",
                enabled = !isSigningOut,
                onClick = onProfileClick
            )
            Spacer(modifier = Modifier.height(10.dp))
            MenuCard(
                icon = HomeIcon.Link,
                iconColor = Color(0xFFF5B44B),
                iconBackground = Color(0xFFFFF6E8),
                title = "내 예약링크 공유",
                description = "나만의 예약 링크를 공유하고\n예약을 받아보세요.",
                enabled = !isSigningOut && !uiState.isLoadingProfile && reservationLink.isNotBlank(),
                onClick = { onShareReservationLinkClick(reservationLink) }
            )
            Spacer(modifier = Modifier.height(20.dp))
            LogoutCard(
                isSigningOut = isSigningOut,
                onClick = onSignOutClick
            )
        }

        HomeBottomBar(
            onHomeClick = {},
            onReservationsClick = onMyReservationsClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandMark(modifier = Modifier.size(32.dp))
        Text(
            text = "timelink",
            color = HomeNavy,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        /*Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(25.dp)) {
                drawArc(HomeNavy, 195f, 150f, false, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                drawLine(HomeNavy, Offset(size.width * .2f, size.height * .72f), Offset(size.width * .8f, size.height * .72f), 2.dp.toPx(), StrokeCap.Round)
                drawCircle(HomeNavy, 1.8.dp.toPx(), Offset(size.width * .5f, size.height * .84f))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(7.dp)
                    .background(HomePurple, CircleShape)
            )
        }*/
    }
}

@Composable
private fun MenuCard(
    icon: HomeIcon,
    iconColor: Color,
    iconBackground: Color,
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clickable(enabled = enabled, onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(iconBackground, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                HomeMenuIcon(icon, iconColor, Modifier.size(38.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 17.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(title, color = Color(0xFF11162B), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(description, color = HomeMuted, fontSize = 14.sp, lineHeight = 21.sp)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF969CAC),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun LogoutCard(isSigningOut: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(enabled = !isSigningOut, onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(15.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSigningOut) {
                CircularProgressIndicator(Modifier.size(22.dp), color = HomePurple, strokeWidth = 2.dp)
            } else {
                HomeMenuIcon(HomeIcon.Logout, Color(0xFF9BA1B0), Modifier.size(28.dp))
            }
            Text(
                text = if (isSigningOut) "로그아웃 중" else "로그아웃",
                color = Color(0xFF858B9A),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 15.dp)
            )
            Spacer(Modifier.weight(1f))
            Text("›", color = Color(0xFFA6ABBA), fontSize = 34.sp)
        }
    }
}

@Composable
fun HomeBottomBar(
    onHomeClick: () -> Unit,
    onReservationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxWidth(), color = Color.White, shadowElevation = 10.dp) {
        Row(
            modifier = Modifier
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem("홈", HomeIcon.Home, true, onHomeClick)
            BottomItem("예약", HomeIcon.Calendar, false, onReservationsClick)
            BottomItem("프로필", HomeIcon.Profile, false, onProfileClick)
        }
    }
}

@Composable
private fun BottomItem(label: String, icon: HomeIcon, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) HomePurple else Color(0xFFB8BDC9)
    Column(
        modifier = Modifier
            .size(width = 80.dp, height = 58.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HomeMenuIcon(icon, color, Modifier.size(25.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = color, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

private enum class HomeIcon { Calendar, Clipboard, Profile, Link, Logout, Home }

@Composable
private fun HomeMenuIcon(icon: HomeIcon, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val line = size.minDimension * .1f
        when (icon) {
            HomeIcon.Calendar, HomeIcon.Clipboard -> {
                drawRoundRect(color, Offset(size.width * .16f, size.height * .18f), Size(size.width * .68f, size.height * .68f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f), style = Stroke(line))
                drawLine(color, Offset(size.width * .28f, size.height * .1f), Offset(size.width * .28f, size.height * .28f), line, StrokeCap.Round)
                drawLine(color, Offset(size.width * .72f, size.height * .1f), Offset(size.width * .72f, size.height * .28f), line, StrokeCap.Round)
                if (icon == HomeIcon.Calendar) {
                    listOf(.32f to .48f, .55f to .48f, .32f to .68f, .55f to .68f).forEach { (x, y) -> drawCircle(color, line * .55f, Offset(size.width * x, size.height * y)) }
                } else {
                    drawLine(color, Offset(size.width * .38f, size.height * .48f), Offset(size.width * .68f, size.height * .48f), line * .55f, StrokeCap.Round)
                    drawLine(color, Offset(size.width * .38f, size.height * .67f), Offset(size.width * .68f, size.height * .67f), line * .55f, StrokeCap.Round)
                }
            }
            HomeIcon.Profile -> {
                drawCircle(color, size.width * .17f, Offset(size.width * .5f, size.height * .32f))
                drawArc(color, 180f, 180f, true, Offset(size.width * .18f, size.height * .52f), Size(size.width * .64f, size.height * .38f))
            }
            HomeIcon.Link -> {
                drawArc(color, 125f, 230f, false, Offset(size.width * .06f, size.height * .38f), Size(size.width * .5f, size.height * .36f), style = Stroke(line, cap = StrokeCap.Round))
                drawArc(color, -55f, 230f, false, Offset(size.width * .44f, size.height * .25f), Size(size.width * .5f, size.height * .36f), style = Stroke(line, cap = StrokeCap.Round))
                drawLine(color, Offset(size.width * .35f, size.height * .62f), Offset(size.width * .65f, size.height * .38f), line, StrokeCap.Round)
            }
            HomeIcon.Logout -> {
                drawLine(color, Offset(size.width * .18f, size.height * .18f), Offset(size.width * .18f, size.height * .82f), line, StrokeCap.Round)
                drawLine(color, Offset(size.width * .18f, size.height * .18f), Offset(size.width * .48f, size.height * .18f), line, StrokeCap.Round)
                drawLine(color, Offset(size.width * .18f, size.height * .82f), Offset(size.width * .48f, size.height * .82f), line, StrokeCap.Round)
                drawLine(color, Offset(size.width * .38f, size.height * .5f), Offset(size.width * .86f, size.height * .5f), line, StrokeCap.Round)
                drawLine(color, Offset(size.width * .68f, size.height * .34f), Offset(size.width * .86f, size.height * .5f), line, StrokeCap.Round)
                drawLine(color, Offset(size.width * .68f, size.height * .66f), Offset(size.width * .86f, size.height * .5f), line, StrokeCap.Round)
            }
            HomeIcon.Home -> {
                val path = Path().apply { moveTo(size.width * .12f, size.height * .48f); lineTo(size.width * .5f, size.height * .15f); lineTo(size.width * .88f, size.height * .48f); lineTo(size.width * .8f, size.height * .86f); lineTo(size.width * .2f, size.height * .86f); close() }
                drawPath(path, color)
                drawRoundRect(Color.White, Offset(size.width * .43f, size.height * .61f), Size(size.width * .14f, size.height * .25f))
            }
        }
    }
}

@Composable
private fun HeroArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val purple = Color(0xFF7772FF)
        drawCircle(purple.copy(.08f), size.width * .28f, Offset(size.width * .72f, size.height * .35f), style = Stroke(size.width * .055f))
        drawRoundRect(purple.copy(.16f), Offset(size.width * .08f, size.height * .42f), Size(size.width * .58f, size.height * .43f), androidx.compose.ui.geometry.CornerRadius(size.width * .06f))
        drawRoundRect(purple.copy(.7f), Offset(size.width * .08f, size.height * .42f), Size(size.width * .58f, size.height * .12f), androidx.compose.ui.geometry.CornerRadius(size.width * .05f))
        drawLine(purple.copy(.6f), Offset(size.width * .25f, size.height * .68f), Offset(size.width * .34f, size.height * .76f), size.width * .045f, StrokeCap.Round)
        drawLine(purple.copy(.6f), Offset(size.width * .34f, size.height * .76f), Offset(size.width * .48f, size.height * .62f), size.width * .045f, StrokeCap.Round)
        drawCircle(purple.copy(.35f), size.width * .045f, Offset(size.width * .02f, size.height * .88f))
        drawCircle(purple.copy(.25f), size.width * .05f, Offset(size.width * .72f, size.height * .08f))
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * .24f
        val brush = Brush.linearGradient(listOf(Color(0xFF5E60FF), Color(0xFFA8B7FF)))
        drawLine(brush, Offset(size.width * .15f, size.height * .32f), Offset(size.width * .72f, size.height * .32f), stroke, StrokeCap.Round)
        drawLine(brush, Offset(size.width * .4f, size.height * .1f), Offset(size.width * .4f, size.height * .68f), stroke, StrokeCap.Round)
        drawArc(brush, 100f, 170f, false, Offset(size.width * .28f, size.height * .48f), Size(size.width * .5f, size.height * .38f), style = Stroke(stroke, cap = StrokeCap.Round))
        drawCircle(Color(0xFF8177FF), size.width * .075f, Offset(size.width * .82f, size.height * .48f))
    }
}
