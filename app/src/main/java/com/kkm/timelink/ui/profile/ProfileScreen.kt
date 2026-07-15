package com.kkm.timelink.ui.profile

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNicknameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onProfileImageUrlChange: (String) -> Unit,
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
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        coroutineScope.launch {
            isProcessingImage = true
            imageErrorMessage = null
            runCatching {
                withContext(Dispatchers.IO) {
                    uri.toProfileImageDataUrl(context.contentResolver)
                }
            }.onSuccess(onProfileImageUrlChange)
                .onFailure {
                    imageErrorMessage = "이미지를 처리할 수 없습니다. 다른 이미지를 선택해 주세요."
                }
            isProcessingImage = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "프로필") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(text = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "공개 프로필",
                style = MaterialTheme.typography.headlineSmall
            )
            OutlinedTextField(
                value = uiState.nickname,
                onValueChange = onNicknameChange,
                label = { Text(text = "닉네임") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.bio,
                onValueChange = onBioChange,
                label = { Text(text = "한 줄 소개") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.profileImageUrl.isNotBlank()) {
                val imageModifier = Modifier
                    .fillMaxWidth(0.4f)
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                if (profileImageBitmap != null) {
                    Image(
                        bitmap = profileImageBitmap.asImageBitmap(),
                        contentDescription = "프로필 이미지 미리보기",
                        contentScale = ContentScale.Crop,
                        modifier = imageModifier
                    )
                } else if (!uiState.profileImageUrl.startsWith(PROFILE_IMAGE_DATA_PREFIX)) {
                    AsyncImage(
                        model = uiState.profileImageUrl,
                        contentDescription = "프로필 이미지 미리보기",
                        contentScale = ContentScale.Crop,
                        modifier = imageModifier
                    )
                } else {
                    Text(
                        text = "저장된 프로필 이미지를 표시할 수 없습니다.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Button(
                onClick = { imagePicker.launch("image/*") },
                enabled = !isProcessingImage && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isProcessingImage) "이미지 처리 중" else "이미지 선택")
            }
            imageErrorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onSaveClick,
                    enabled = !uiState.isSaving
                ) {
                    Text(text = if (uiState.isSaving) "저장 중" else "저장")
                }
            }
        }
    }
}

private fun String.toProfileImageBitmapOrNull() = runCatching {
    if (!startsWith(PROFILE_IMAGE_DATA_PREFIX)) return@runCatching null
    val encodedImage = substringAfter(',', missingDelimiterValue = "")
    if (encodedImage.isBlank()) return@runCatching null
    val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}.getOrNull()

private fun Uri.toProfileImageDataUrl(
    contentResolver: android.content.ContentResolver
): String {
    val source = ImageDecoder.createSource(contentResolver, this)
    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val width = info.size.width
        val height = info.size.height
        val scale = minOf(1f, PROFILE_IMAGE_MAX_DIMENSION.toFloat() / maxOf(width, height))
        decoder.setTargetSize(
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1)
        )
    }

    var quality = PROFILE_IMAGE_INITIAL_QUALITY
    var dataUrl: String
    do {
        val output = ByteArrayOutputStream()
        check(bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, output)) {
            "이미지 압축에 실패했습니다."
        }
        dataUrl = PROFILE_IMAGE_DATA_URL_PREFIX + Base64.encodeToString(
            output.toByteArray(),
            Base64.NO_WRAP
        )
        quality -= PROFILE_IMAGE_QUALITY_STEP
    } while (dataUrl.toByteArray(Charsets.UTF_8).size > PROFILE_IMAGE_MAX_DATA_SIZE && quality >= PROFILE_IMAGE_MIN_QUALITY)

    require(dataUrl.toByteArray(Charsets.UTF_8).size <= PROFILE_IMAGE_MAX_DATA_SIZE) {
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
