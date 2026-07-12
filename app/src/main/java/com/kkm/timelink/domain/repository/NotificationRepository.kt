package com.kkm.timelink.domain.repository

interface NotificationRepository {
    suspend fun syncToken(uid: String)
    suspend fun saveToken(uid: String, token: String)
    suspend fun clearToken(uid: String)
}
