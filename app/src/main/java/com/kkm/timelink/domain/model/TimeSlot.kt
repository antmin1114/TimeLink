package com.kkm.timelink.domain.model

enum class TimeSlotStatus {
    AVAILABLE,
    RESERVED,
    DISABLED
}

data class TimeSlot(
    val id: String = "",
    val hostId: String = "",
    val startAt: Long = 0L,
    val endAt: Long = 0L,
    val durationMinutes: Int = 30,
    val status: String = TimeSlotStatus.AVAILABLE.name,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
