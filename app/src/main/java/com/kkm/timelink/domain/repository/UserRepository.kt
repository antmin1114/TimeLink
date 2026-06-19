package com.kkm.timelink.domain.repository

import com.kkm.timelink.domain.model.User

interface UserRepository {
    suspend fun createOrUpdateUser(user: User)
    suspend fun createUserIfMissing(uid: String)
    suspend fun getUser(uid: String): User?
    suspend fun updateProfile(
        uid: String,
        nickname: String,
        bio: String?,
        profileImageUrl: String?
    )
}
