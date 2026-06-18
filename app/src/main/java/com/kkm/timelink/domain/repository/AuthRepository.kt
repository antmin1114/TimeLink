package com.kkm.timelink.domain.repository

interface AuthRepository {
    fun getCurrentUserId(): String?
    suspend fun signInWithGoogle(idToken: String): String
    suspend fun signOut()
}
