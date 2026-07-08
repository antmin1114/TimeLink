package com.kkm.timelink.data.reservation

import com.google.firebase.auth.FirebaseAuth
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
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ReservationRepository {

    override suspend fun requestReservation(
        hostId: String,
        guestId: String,
        slotIds: List<String>,
        purpose: ReservationPurpose,
        message: String
    ) {
        require(hostId.isNotBlank()) { "호스트 ID가 필요합니다." }
        require(guestId.isNotBlank()) { "예약자 ID가 필요합니다." }
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
            "호스트의 시간 슬롯만 예약할 수 있습니다."
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
            "이미 예약 신청이 있는 시간 슬롯이 포함되어 있습니다."
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
        require(hostId.isNotBlank()) { "호스트 ID가 필요합니다." }

        return firestore.collection(RESERVATIONS_COLLECTION)
            .whereEqualTo(HOST_ID_FIELD, hostId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Reservation::class.java)
    }

    override suspend fun getMyReservations(guestId: String): List<Reservation> {
        require(guestId.isNotBlank()) { "예약자 ID가 필요합니다." }

        return firestore.collection(RESERVATIONS_COLLECTION)
            .whereEqualTo(GUEST_ID_FIELD, guestId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(Reservation::class.java)
    }

    override suspend fun getReservation(reservationId: String): Reservation? {
        require(reservationId.isNotBlank()) { "예약 ID가 필요합니다." }

        return firestore.collection(RESERVATIONS_COLLECTION)
            .document(reservationId)
            .get()
            .await()
            .toObject(Reservation::class.java)
    }

    override suspend fun approveReservation(reservationId: String) {
        require(reservationId.isNotBlank()) { "예약 ID가 필요합니다." }
        val uid = requireCurrentUserId()
        val reservationRef = firestore.collection(RESERVATIONS_COLLECTION).document(reservationId)

        firestore.runTransaction { transaction ->
            val reservation = transaction.get(reservationRef).toObject(Reservation::class.java)
                ?: error("예약 정보를 찾을 수 없습니다.")
            check(reservation.hostId == uid) {
                "호스트만 이 예약을 승인할 수 있습니다."
            }
            check(reservation.status == ReservationStatus.PENDING.name) {
                "승인 대기 중인 예약만 승인할 수 있습니다."
            }

            val slotRefs = reservation.slotIds.map { slotId ->
                firestore.collection(TIME_SLOTS_COLLECTION).document(slotId)
            }
            val slots = slotRefs.map { slotRef ->
                transaction.get(slotRef).toObject(TimeSlot::class.java)
                    ?: error("시간 슬롯을 찾을 수 없습니다.")
            }
            check(slots.isNotEmpty()) {
                "선택된 시간 슬롯이 없는 예약입니다."
            }
            check(slots.all { it.hostId == reservation.hostId }) {
                "예약에 올바르지 않은 시간 슬롯이 포함되어 있습니다."
            }
            check(slots.all { it.status == TimeSlotStatus.AVAILABLE.name }) {
                "예약 가능한 시간 슬롯만 예약 확정할 수 있습니다."
            }

            val now = System.currentTimeMillis()
            transaction.update(
                reservationRef,
                mapOf(
                    STATUS_FIELD to ReservationStatus.APPROVED.name,
                    REJECT_REASON_FIELD to null,
                    UPDATED_AT_FIELD to now
                )
            )
            slotRefs.forEach { slotRef ->
                transaction.update(
                    slotRef,
                    mapOf(
                        STATUS_FIELD to TimeSlotStatus.RESERVED.name,
                        UPDATED_AT_FIELD to now
                    )
                )
            }
        }.await()
    }

    override suspend fun rejectReservation(
        reservationId: String,
        reason: String
    ) {
        require(reservationId.isNotBlank()) { "예약 ID가 필요합니다." }
        val trimmedReason = reason.trim()
        require(trimmedReason.isNotBlank()) { "거절 사유가 필요합니다." }
        val uid = requireCurrentUserId()
        val reservationRef = firestore.collection(RESERVATIONS_COLLECTION).document(reservationId)

        firestore.runTransaction { transaction ->
            val reservation = transaction.get(reservationRef).toObject(Reservation::class.java)
                ?: error("예약 정보를 찾을 수 없습니다.")
            check(reservation.hostId == uid) {
                "호스트만 이 예약을 거절할 수 있습니다."
            }
            check(reservation.status == ReservationStatus.PENDING.name) {
                "승인 대기 중인 예약만 거절할 수 있습니다."
            }

            transaction.update(
                reservationRef,
                mapOf(
                    STATUS_FIELD to ReservationStatus.REJECTED.name,
                    REJECT_REASON_FIELD to trimmedReason,
                    UPDATED_AT_FIELD to System.currentTimeMillis()
                )
            )
        }.await()
    }

    override suspend fun cancelReservation(reservationId: String) {
        require(reservationId.isNotBlank()) { "예약 ID가 필요합니다." }
        val uid = requireCurrentUserId()
        val reservationRef = firestore.collection(RESERVATIONS_COLLECTION).document(reservationId)

        firestore.runTransaction { transaction ->
            val reservation = transaction.get(reservationRef).toObject(Reservation::class.java)
                ?: error("예약 정보를 찾을 수 없습니다.")
            check(reservation.hostId == uid || reservation.guestId == uid) {
                "호스트 또는 예약자만 이 예약을 취소할 수 있습니다."
            }
            check(
                reservation.status == ReservationStatus.PENDING.name ||
                    reservation.status == ReservationStatus.APPROVED.name
            ) {
                "승인 대기 또는 승인 완료 상태의 예약만 취소할 수 있습니다."
            }

            val slotRefs = if (reservation.status == ReservationStatus.APPROVED.name) {
                reservation.slotIds.map { slotId ->
                    firestore.collection(TIME_SLOTS_COLLECTION).document(slotId)
                }
            } else {
                emptyList()
            }
            val slots = slotRefs.map { slotRef ->
                transaction.get(slotRef).toObject(TimeSlot::class.java)
                    ?: error("시간 슬롯을 찾을 수 없습니다.")
            }
            check(slots.all { it.hostId == reservation.hostId }) {
                "예약에 올바르지 않은 시간 슬롯이 포함되어 있습니다."
            }

            val now = System.currentTimeMillis()
            transaction.update(
                reservationRef,
                mapOf(
                    STATUS_FIELD to ReservationStatus.CANCELLED.name,
                    UPDATED_AT_FIELD to now
                )
            )
            slotRefs.forEach { slotRef ->
                transaction.update(
                    slotRef,
                    mapOf(
                        STATUS_FIELD to TimeSlotStatus.AVAILABLE.name,
                        UPDATED_AT_FIELD to now
                    )
                )
            }
        }.await()
    }

    private fun requireCurrentUserId(): String {
        return requireNotNull(firebaseAuth.currentUser?.uid) {
            "로그인이 필요합니다."
        }
    }

    private companion object {
        const val TIME_SLOTS_COLLECTION = "time_slots"
        const val RESERVATIONS_COLLECTION = "reservations"
        const val HOST_ID_FIELD = "hostId"
        const val GUEST_ID_FIELD = "guestId"
        const val SLOT_IDS_FIELD = "slotIds"
        const val STATUS_FIELD = "status"
        const val REJECT_REASON_FIELD = "rejectReason"
        const val CREATED_AT_FIELD = "createdAt"
        const val UPDATED_AT_FIELD = "updatedAt"
    }
}
