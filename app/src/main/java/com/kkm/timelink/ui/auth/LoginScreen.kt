package com.kkm.timelink.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BrandBlue = Color(0xFF606CFF)
private val BrandNavy = Color(0xFF17245C)
private val MutedText = Color(0xFF888D9E)

@Composable
fun LoginScreen(
    isLoading: Boolean,
    onGoogleSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFF7F6FF)),
                    center = Offset(180f, 220f),
                    radius = 900f
                )
            )
    ) {
        HeaderArtwork(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .padding(top = 112.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(modifier = Modifier.size(38.dp))
                Text(
                    text = "timelink",
                    color = BrandNavy,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.8).sp,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = buildAnnotatedString {
                    append("시간을 연결하고,\n소중한 순간을 ")
                    withStyle(SpanStyle(color = BrandBlue, fontWeight = FontWeight.Medium)) {
                        append("이어가세요.")
                    }
                },
                color = BrandNavy,
                fontSize = 20.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Medium
            )
        }

        LoginPanel(
            isLoading = isLoading,
            onGoogleSignInClick = onGoogleSignInClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun LoginPanel(
    isLoading: Boolean,
    onGoogleSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .padding(horizontal = 30.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandMark(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "환영합니다!",
                color = Color(0xFF171717),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Google 계정으로 간편하게 로그인하세요.",
                color = MutedText,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(36.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .border(1.dp, Color(0xFFE4E6EC), RoundedCornerShape(15.dp))
                    .clickable(
                        enabled = !isLoading,
                        role = Role.Button,
                        onClick = onGoogleSignInClick
                    )
                    .semantics { contentDescription = "Google 계정으로 로그인" },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = BrandBlue,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    GoogleMark()
                    Text(
                        text = "Google 계정으로 로그인",
                        color = Color(0xFF151515),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.24f
            val glassBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF625BFF),
                    Color(0xFF7775FF),
                    Color(0xFFB8C9FF)
                ),
                start = Offset(size.width * 0.15f, size.height * 0.1f),
                end = Offset(size.width * 0.9f, size.height * 0.9f)
            )
            val x = size.width * 0.43f

            // Reference image's long, softly rounded crossbar.
            drawLine(
                brush = glassBrush,
                start = Offset(size.width * 0.14f, size.height * 0.33f),
                end = Offset(size.width * 0.76f, size.height * 0.33f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )

            // Stem and hooked lower bowl are kept as separate strokes for a glassy overlap.
            drawLine(
                brush = glassBrush,
                start = Offset(x, size.height * 0.12f),
                end = Offset(x, size.height * 0.66f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )

            val hook = Path().apply {
                moveTo(x, size.height * 0.58f)
                cubicTo(
                    x,
                    size.height * 0.91f,
                    size.width * 0.70f,
                    size.height * 0.98f,
                    size.width * 0.78f,
                    size.height * 0.67f
                )
            }
            drawPath(
                path = hook,
                brush = glassBrush,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // The detached dot is a defining detail of the supplied mark.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFD9E2FF), Color(0xFF776EFF)),
                    center = Offset(size.width * 0.74f, size.height * 0.43f),
                    radius = size.minDimension * 0.2f
                ),
                radius = size.minDimension * 0.105f,
                center = Offset(size.width * 0.82f, size.height * 0.48f)
            )

            // Fine translucent highlights mimic the reference's glossy rim without a bitmap.
            drawLine(
                color = Color.White.copy(alpha = 0.38f),
                start = Offset(x - stroke * 0.22f, size.height * 0.13f),
                end = Offset(x - stroke * 0.22f, size.height * 0.58f),
                strokeWidth = size.minDimension * 0.018f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.48f),
                radius = size.minDimension * 0.025f,
                center = Offset(size.width * 0.79f, size.height * 0.44f)
            )
        }
    }
}

@Composable
private fun GoogleMark() {
    Text(
        text = "G",
        color = Color(0xFF4285F4),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun HeaderArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val lavender = Color(0xFF7A79FF)
        drawCircle(lavender.copy(alpha = 0.14f), radius = 13.dp.toPx(), center = Offset(size.width * .68f, size.height * .20f))
        drawCircle(lavender.copy(alpha = 0.13f), radius = 11.dp.toPx(), center = Offset(size.width * .91f, size.height * .33f))
        drawCircle(
            color = lavender.copy(alpha = 0.09f),
            radius = 74.dp.toPx(),
            center = Offset(size.width * .84f, size.height * .23f),
            style = Stroke(width = 8.dp.toPx())
        )
        drawCircle(
            color = lavender.copy(alpha = 0.08f),
            radius = 68.dp.toPx(),
            center = Offset(size.width * .67f, size.height * .30f),
            style = Stroke(width = 7.dp.toPx())
        )
        drawCircle(
            color = lavender.copy(alpha = 0.12f),
            radius = 37.dp.toPx(),
            center = Offset(size.width * .83f, size.height * .245f),
            style = Stroke(width = 2.dp.toPx())
        )
        drawLine(
            color = lavender.copy(alpha = 0.25f),
            start = Offset(size.width * .83f, size.height * .245f),
            end = Offset(size.width * .84f, size.height * .22f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = lavender.copy(alpha = 0.25f),
            start = Offset(size.width * .83f, size.height * .245f),
            end = Offset(size.width * .85f, size.height * .255f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
