package com.kkm.timelink.data.reservation

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kkm.timelink.domain.model.Reservation
import com.kkm.timelink.domain.model.ReservationPurpose
import com.kkm.timelink.domain.model.ReservationStatus
import com.kkm.timelink.domain.model.TimeSlot
import com.kkm.timelink.domain.model.TimeSlotStatus
import com.kkm.timelink.domain.repository.ReservationRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreReservationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReservationRepository {

    override suspend fun requestReservation(
        hostId: String,
        guestId: String,
        slotIds: List<String>,
        purpose: ReservationPurpose,
        message: String
    ) {
        require(hostId.isNotBlank()) { "Host ID가 필요합니다." }
        require(guestId.isNotBlank()) { "Guest ID가 필요합니다." }
        require(slotIds.isNotEmpty()) { "시간 슬롯을 선택해 주세요." }
        require(message.isNotBlank()) { "예약 메시지를 입력해 주세요." }

        val selectedSlots = slotIds.map { slotId ->
            firestore.collection(TIME_SLOTS_COLLECTION)
                .document(slotId)
                .get()
                .await()
                .toObject(TimeSlot::class.java)
                ?: error("선택한 시간 슬롯을 찾을 수 없습니다.")
        }.sortedBy { it.startAt }

        check(selectedSlots.size == slotIds.distinct().size) {
            "선택한 시간 슬롯을 찾을 수 없습니다."
        }
        check(selectedSlots.all { it.hostId == hostId }) {
            "Host의 시간 슬롯만 예약할 수 있습니다."
        }
        check(selectedSlots.all { it.status == TimeSlotStatus.AVAILABLE.name }) {
            "예약 가능한 시간 슬롯만 선택할 수 있습니다."
        }
        check(selectedSlots.all { it.startAt > System.currentTimeMillis() }) {
            "지난 시간은 예약할 수 없습니다."
        }
        check(selectedSlots.zipWithNext().all { (current, next) -> current.endAt == next.startAt }) {
            "연속된 시간 슬롯만 예약할 수 있습니다."
        }

        val hasActiveReservation = selectedSlots.any { slot ->
            firestore.collection(RESERVATIONS_COLLECTION)
                .whereArrayContains(SLOT_IDS_FIELD, slot.id)
                .whereIn(
                    STATUS_FIELD,
                    listOf(ReservationStatus.PENDING.name, ReservationStatus.APPROVED.name)
                )
                .limit(1)
                .get()
                .await()
                .documents
                .isNotEmpty()
        }
        check(!hasActiveReservation) {
            "이미 예약 신청된 시간 슬롯이 포함되어 있습니다."
        }

        val now = System.currentTimeMillis()
        val document = firestore.collection(RESERVATIONS_COLLECTION).document()
        val reservation = Reservation(
            id = document.id,
            hostId = hostId,
            guestId = guestId,
            slotIds = selectedSlots.map { it.id },
            startAt = selectedSlots.first().startAt,
            endAt = selectedSlots.last().endAt,
            purpose = purpose.name,
            message = message.trim(),
            status = ReservationStatus.PENDING.name,
            rejectReason = null,
            createdAt = now,
            updatedAt = now
        )
        document.set(reservation).await()
    }

    override suspend fun getReceivedReservations(hostId: String): List<Reservation> {
        require(hostId.isNotBlank()) { "Host ID is required." }

        return firestore.collection(RESERVATIONS_COLLECTION)
            .whereEqualTo(HOST_ID_FIELD, hostId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Reservation::class.java)
    }

    override suspend fun getMyReservations(guestId: String): List<Reservation> {
        require(guestId.isNotBlank()) { "Guest ID is required." }

        return firestore.collection(RESERVATIONS_COLLECTION)
            .whereEqualTo(GUEST_ID_FIELD, guestId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Reservation::class.java)
    }

    override suspend fun getReservation(reservationId: String): Reservation? {
        require(reservationId.isNotBlank()) { "Reservation ID is required." }

        return firestore.collection(RESERVATIONS_COLLECTION)
            .document(reservationId)
            .get()
            .await()
            .toObject(Reservation::class.java)
    }

    private companion object {
        const val TIME_SLOTS_COLLECTION = "time_slots"
        const val RESERVATIONS_COLLECTION = "reservations"
        const val HOST_ID_FIELD = "hostId"
        const val GUEST_ID_FIELD = "guestId"
        const val SLOT_IDS_FIELD = "slotIds"
        const val STATUS_FIELD = "status"
        const val CREATED_AT_FIELD = "createdAt"
    }
}
