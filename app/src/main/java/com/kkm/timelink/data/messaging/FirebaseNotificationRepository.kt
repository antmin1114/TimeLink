package com.kkm.timelink.data.messaging

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.kkm.timelink.domain.repository.NotificationRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
) : NotificationRepository {

    override suspend fun syncToken(uid: String) {
        saveToken(uid, firebaseMessaging.token.await())
    }

    override suspend fun saveToken(uid: String, token: String) {
        require(uid.isNotBlank()) { "사용자 ID가 필요합니다." }
        require(token.isNotBlank()) { "FCM 토큰이 필요합니다." }

        firestore.collection(USER_PRIVATE_COLLECTION)
            .document(uid)
            .set(
                mapOf(
                    UID_FIELD to uid,
                    FCM_TOKEN_FIELD to token,
                    UPDATED_AT_FIELD to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()
    }

    override suspend fun clearToken(uid: String) {
        require(uid.isNotBlank()) { "사용자 ID가 필요합니다." }

        firestore.collection(USER_PRIVATE_COLLECTION)
            .document(uid)
            .set(
                mapOf(
                    UID_FIELD to uid,
                    FCM_TOKEN_FIELD to FieldValue.delete(),
                    UPDATED_AT_FIELD to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()
    }

    private companion object {
        const val USER_PRIVATE_COLLECTION = "user_private"
        const val UID_FIELD = "uid"
        const val FCM_TOKEN_FIELD = "fcmToken"
        const val UPDATED_AT_FIELD = "updatedAt"
    }
}
