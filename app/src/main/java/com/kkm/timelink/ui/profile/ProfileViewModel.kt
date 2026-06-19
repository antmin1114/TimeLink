package com.kkm.timelink.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkm.timelink.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val uid: String = "",
    val nickname: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)

sealed interface ProfileEvent {
    data object Saved : ProfileEvent
    data class Error(val message: String) : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
) : ViewModel() {

    private val uid: String = checkNotNull(savedStateHandle["uid"])

    private val _uiState = MutableStateFlow(ProfileUiState(uid = uid))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        loadProfile()
    }

    fun updateNickname(value: String) {
        _uiState.update { it.copy(nickname = value) }
    }

    fun updateBio(value: String) {
        _uiState.update { it.copy(bio = value) }
    }

    fun updateProfileImageUrl(value: String) {
        _uiState.update { it.copy(profileImageUrl = value) }
    }

    fun saveProfile() {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.nickname.isBlank()) {
                _events.emit(ProfileEvent.Error("닉네임을 입력해 주세요."))
                return@launch
            }

            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                userRepository.updateProfile(
                    uid = uid,
                    nickname = state.nickname,
                    bio = state.bio,
                    profileImageUrl = state.profileImageUrl
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(ProfileEvent.Saved)
            }.onFailure { throwable ->
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(ProfileEvent.Error(toProfileSaveMessage(throwable)))
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                userRepository.createUserIfMissing(uid)
                userRepository.getUser(uid)
            }.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        nickname = user?.nickname.orEmpty(),
                        bio = user?.bio.orEmpty(),
                        profileImageUrl = user?.profileImageUrl.orEmpty(),
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(ProfileEvent.Error(toProfileLoadMessage(throwable)))
            }
        }
    }

    private fun toProfileLoadMessage(throwable: Throwable): String {
        val message = throwable.message.orEmpty()
        return if (message.contains("offline", ignoreCase = true)) {
            "프로필을 불러올 수 없습니다. 네트워크 연결을 확인해 주세요."
        } else {
            "프로필을 불러오지 못했습니다."
        }
    }

    private fun toProfileSaveMessage(throwable: Throwable): String {
        val message = throwable.message.orEmpty()
        return if (message.contains("offline", ignoreCase = true)) {
            "프로필을 저장할 수 없습니다. 네트워크 연결을 확인해 주세요."
        } else {
            "프로필 저장에 실패했습니다."
        }
    }
}
