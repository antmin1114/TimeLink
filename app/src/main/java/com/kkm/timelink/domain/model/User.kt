package com.kkm.timelink.domain.model

data class User(
    val uid: String = "",
    val nickname: String = "",
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val reservationLinkId: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
