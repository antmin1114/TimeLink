package com.kkm.timelink.data.user

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kkm.timelink.domain.model.User
import com.kkm.timelink.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun createOrUpdateUser(user: User) {
        require(user.uid.isNotBlank()) { "사용자 ID가 필요합니다." }
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(user, SetOptions.merge())
            .await()
    }

    override suspend fun createUserIfMissing(uid: String) {
        require(uid.isNotBlank()) { "사용자 ID가 필요합니다." }

        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val snapshot = userRef.get().await()
        if (snapshot.exists()) return

        val now = System.currentTimeMillis()
        val user = User(
            uid = uid,
            nickname = "새 사용자",
            reservationLinkId = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now
        )
        userRef.set(user).await()
    }

    override suspend fun getUser(uid: String): User? {
        require(uid.isNotBlank()) { "사용자 ID가 필요합니다." }
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()
            .toObject(User::class.java)
    }

    override suspend fun updateProfile(
        uid: String,
        nickname: String,
        bio: String?,
        profileImageUrl: String?
    ) {
        require(uid.isNotBlank()) { "사용자 ID가 필요합니다." }
        require(nickname.isNotBlank()) { "닉네임을 입력해 주세요." }

        val updates = mutableMapOf<String, Any>(
            "nickname" to nickname.trim(),
            "updatedAt" to System.currentTimeMillis()
        )
        updates["bio"] = bio?.trim().takeUnless { it.isNullOrBlank() } ?: FieldValue.delete()
        updates["profileImageUrl"] = profileImageUrl?.trim().takeUnless {
            it.isNullOrBlank()
        } ?: FieldValue.delete()

        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .update(updates)
            .await()
    }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
