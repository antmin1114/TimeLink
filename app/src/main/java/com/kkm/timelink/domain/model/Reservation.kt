package com.kkm.timelink.domain.model

enum class ReservationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}

enum class ReservationPurpose {
    COFFEE_CHAT,
    MEAL,
    STUDY,
    CONSULTING,
    ETC
}

data class Reservation(
    val id: String = "",
    val hostId: String = "",
    val guestId: String = "",
    val slotIds: List<String> = emptyList(),
    val startAt: Long = 0L,
    val endAt: Long = 0L,
    val purpose: String = ReservationPurpose.ETC.name,
    val message: String = "",
    val status: String = ReservationStatus.PENDING.name,
    val rejectReason: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
