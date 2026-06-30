package com.kkm.timelink.domain.repository

import com.kkm.timelink.domain.model.Reservation
import com.kkm.timelink.domain.model.ReservationPurpose

interface ReservationRepository {
    suspend fun requestReservation(
        hostId: String,
        guestId: String,
        slotIds: List<String>,
        purpose: ReservationPurpose,
        message: String
    )

    suspend fun getReceivedReservations(hostId: String): List<Reservation>

    suspend fun getMyReservations(guestId: String): List<Reservation>

    suspend fun getReservation(reservationId: String): Reservation?
}
