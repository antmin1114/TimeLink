package com.kkm.timelink.domain.repository

import com.kkm.timelink.domain.model.TimeSlot

interface TimeSlotRepository {
    /**
     * Registers an available time range and splits it into independent TimeSlot documents.
     */
    suspend fun createTimeSlot(
        hostId: String,
        startAt: Long,
        endAt: Long,
        durationMinutes: Int
    )

    suspend fun getHostTimeSlots(hostId: String): List<TimeSlot>

    suspend fun getAvailableTimeSlots(hostId: String): List<TimeSlot>

    suspend fun disableTimeSlot(slotId: String)

    suspend fun enableTimeSlot(slotId: String)
}
