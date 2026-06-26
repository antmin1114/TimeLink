package com.kkm.timelink.domain.repository

import com.kkm.timelink.domain.model.ReservationPurpose

interface ReservationRepository {
    suspend fun requestReservation(
        hostId: String,
        guestId: String,
        slotIds: List<String>,
        purpose: ReservationPurpose,
        message: String
    )
}
