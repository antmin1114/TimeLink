package com.kkm.timelink.ui.profile

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private val ProfilePurple = Color(0xFF6652F5)
private val ProfilePurpleLight = Color(0xFF8A72FF)
private val ProfileText = Color(0xFF111221)
private val ProfileMuted = Color(0xFF898CA3)
private val ProfileBorder = Color(0xFFE8E8F2)

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNicknameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onProfileImageUrlChange: (String) -> Unit,
    onResetProfileImageClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var imageErrorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessingImage by remember { mutableStateOf(false) }
    val profileImageBitmap = remember(uiState.profileImageUrl) {
        uiState.profileImageUrl.toProfileImageBitmapOrNull()
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessingImage = true
                imageErrorMessage = null
                runCatching {
                    withContext(Dispatchers.IO) {
                        uri.toProfileImageDataUrl(context.contentResolver)
                    }
                }.onSuccess(onProfileImageUrlChange)
                    .onFailure { imageErrorMessage = "이미지를 처리할 수 없습니다. 다른 이미지를 선택해 주세요." }
                isProcessingImage = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFE))
            .statusBarsPadding()
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = ProfilePurple,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp,
                    top = 8.dp,
                    end = 20.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = ProfileText
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text("‹", fontSize = 40.sp, fontWeight = FontWeight.Light)
                        }
                        Text(
                            text = "프로필",
                            color = ProfileText,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }

                item { ProfileHeaderCard() }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFF0F0F6), RoundedCornerShape(22.dp))
                            .padding(16.dp)
                    ) {
                        ProfileLabel("닉네임")
                        Spacer(Modifier.height(12.dp))
                        NicknameField(uiState.nickname, onNicknameChange)
                        Spacer(Modifier.height(22.dp))

                        ProfileLabel("한 줄 소개")
                        Spacer(Modifier.height(12.dp))
                        BioField(uiState.bio, onBioChange)
                        Spacer(Modifier.height(18.dp))
                        HorizontalDivider(color = ProfileBorder)
                        Spacer(Modifier.height(18.dp))

                        ProfileLabel("프로필 이미지")
                        Spacer(Modifier.height(16.dp))
                        ProfileImage(
                            imageUrl = uiState.profileImageUrl,
                            bitmap = profileImageBitmap,
                            modifier = Modifier
                                .size(116.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(14.dp))

                        ProfileActionButton(
                            text = if (isProcessingImage) "이미지 처리 중..." else "이미지 선택",
                            outlined = false,
                            enabled = !isProcessingImage && !uiState.isSaving,
                            onClick = { imagePicker.launch("image/*") }
                        )
                        Spacer(Modifier.height(10.dp))
                        ProfileActionButton(
                            text = "기본 이미지로 변경",
                            outlined = true,
                            enabled = !uiState.isSaving,
                            onClick = onResetProfileImageClick
                        )

                        imageErrorMessage?.let {
                            Text(
                                text = it,
                                color = Color(0xFFD84747),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onSaveClick,
                            enabled = !uiState.isSaving && !isProcessingImage,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color(0xFFB9B2E8)
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF573CF2), Color(0xFF7A59F7))
                                        )
                                    )
                            ) {
                                Text(
                                    text = if (uiState.isSaving) "저장 중..." else "저장",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFF8F7FF), Color(0xFFF5F3FF))
                )
            )
            .border(1.dp, Color(0xFFF0EEFA), RoundedCornerShape(22.dp))
            .padding(horizontal = 22.dp, vertical = 28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "공개 프로필",
                color = ProfileText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(9.dp))
            Text(
                "내 정보를 확인하고\n관리해보세요.",
                color = Color(0xFF74778F),
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
        ProfileCardIllustration(Modifier.size(width = 130.dp, height = 100.dp))
    }
}

@Composable
private fun ProfileCardIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawRoundRect(
            brush = Brush.linearGradient(listOf(Color(0xFFF0EDFF), Color(0xFFD8D0FF))),
            topLeft = Offset(size.width * .15f, size.height * .25f),
            size = Size(size.width * .72f, size.height * .62f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
        )
        drawCircle(Color(0xFF9B86FA), size.minDimension * .25f, Offset(size.width * .42f, size.height * .35f))
        drawCircle(Color.White.copy(alpha = .92f), size.minDimension * .08f, Offset(size.width * .42f, size.height * .29f))
        drawOval(
            Color.White.copy(alpha = .92f),
            Offset(size.width * .34f, size.height * .37f),
            Size(size.width * .16f, size.height * .18f)
        )
        repeat(3) { index ->
            val y = size.height * (.39f + index * .17f)
            drawLine(
                Color.White.copy(alpha = .75f),
                Offset(size.width * .61f, y),
                Offset(size.width * .79f, y),
                strokeWidth = 7f,
                cap = StrokeCap.Round
            )
        }
        drawCircle(Color(0xFF8F76F6), 7f, Offset(size.width * .08f, size.height * .35f))
        drawCircle(Color(0xFFB09EFF), 9f, Offset(size.width * .04f, size.height * .78f))
    }
}

@Composable
private fun ProfileLabel(text: String) {
    Text(text, color = ProfileText, fontSize = 17.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun NicknameField(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .border(1.dp, ProfileBorder, RoundedCornerShape(15.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserGlyph(Modifier.size(25.dp), ProfileMuted)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = ProfileText, fontSize = 17.sp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        )
        Text("✎", color = ProfilePurpleLight, fontSize = 23.sp)
    }
}

@Composable
private fun BioField(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(1.dp, ProfileBorder, RoundedCornerShape(15.dp))
            .padding(14.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= 100) onValueChange(it) },
            textStyle = TextStyle(color = ProfileText, fontSize = 16.sp, lineHeight = 23.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
        Text(
            "${value.length}/100",
            color = ProfileMuted,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun ProfileImage(
    imageUrl: String,
    bitmap: android.graphics.Bitmap?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFF5F3FF), Color(0xFFFAF9FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "프로필 이미지 미리보기",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            imageUrl.isNotBlank() && !imageUrl.startsWith(PROFILE_IMAGE_DATA_PREFIX) -> AsyncImage(
                model = imageUrl,
                contentDescription = "프로필 이미지 미리보기",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            else -> UserGlyph(Modifier.size(70.dp), Color(0xFFAA9CF5), filled = true)
        }
    }
}

@Composable
private fun UserGlyph(modifier: Modifier, color: Color, filled: Boolean = false) {
    Canvas(modifier) {
        val stroke = if (filled) null else Stroke(width = 2.3.dp.toPx())
        drawCircle(
            color = color,
            radius = size.minDimension * .19f,
            center = Offset(size.width / 2, size.height * .3f),
            style = stroke ?: androidx.compose.ui.graphics.drawscope.Fill
        )
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = filled,
            topLeft = Offset(size.width * .18f, size.height * .48f),
            size = Size(size.width * .64f, size.height * .46f),
            style = stroke ?: androidx.compose.ui.graphics.drawscope.Fill
        )
    }
}

@Composable
private fun ProfileActionButton(
    text: String,
    outlined: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (outlined) Color.White else Color(0xFFF7F5FF),
            contentColor = ProfilePurple,
            disabledContainerColor = Color(0xFFF2F1F7),
            disabledContentColor = ProfileMuted
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(
                if (outlined) Modifier.border(1.dp, Color(0xFFE2DDFC), RoundedCornerShape(14.dp))
                else Modifier
            )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

private fun String.toProfileImageBitmapOrNull() = runCatching {
    if (!startsWith(PROFILE_IMAGE_DATA_PREFIX)) return@runCatching null
    val encodedImage = substringAfter(',', missingDelimiterValue = "")
    if (encodedImage.isBlank()) return@runCatching null
    val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}.getOrNull()

private fun Uri.toProfileImageDataUrl(contentResolver: android.content.ContentResolver): String {
    val source = ImageDecoder.createSource(contentResolver, this)
    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val scale = minOf(
            1f,
            PROFILE_IMAGE_MAX_DIMENSION.toFloat() / maxOf(info.size.width, info.size.height)
        )
        decoder.setTargetSize(
            (info.size.width * scale).toInt().coerceAtLeast(1),
            (info.size.height * scale).toInt().coerceAtLeast(1)
        )
    }
    var quality = PROFILE_IMAGE_INITIAL_QUALITY
    var dataUrl: String
    do {
        val output = ByteArrayOutputStream()
        check(bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, output))
        dataUrl = PROFILE_IMAGE_DATA_URL_PREFIX +
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        quality -= PROFILE_IMAGE_QUALITY_STEP
    } while (
        dataUrl.toByteArray().size > PROFILE_IMAGE_MAX_DATA_SIZE &&
        quality >= PROFILE_IMAGE_MIN_QUALITY
    )
    require(dataUrl.toByteArray().size <= PROFILE_IMAGE_MAX_DATA_SIZE) {
        "프로필 이미지가 너무 큽니다."
    }
    return dataUrl
}

private const val PROFILE_IMAGE_MAX_DIMENSION = 512
private const val PROFILE_IMAGE_MAX_DATA_SIZE = 700_000
private const val PROFILE_IMAGE_INITIAL_QUALITY = 80
private const val PROFILE_IMAGE_MIN_QUALITY = 40
private const val PROFILE_IMAGE_QUALITY_STEP = 10
private const val PROFILE_IMAGE_DATA_URL_PREFIX = "data:image/jpeg;base64,"
private const val PROFILE_IMAGE_DATA_PREFIX = "data:image/"
