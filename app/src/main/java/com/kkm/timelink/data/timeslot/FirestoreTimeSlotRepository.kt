package com.kkm.timelink.data.timeslot

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.kkm.timelink.domain.model.TimeSlot
import com.kkm.timelink.domain.model.TimeSlotStatus
import com.kkm.timelink.domain.repository.TimeSlotRepository
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class FirestoreTimeSlotRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : TimeSlotRepository {

    override suspend fun createTimeSlot(
        hostId: String,
        startAt: Long,
        endAt: Long,
        durationMinutes: Int
    ) {
        require(hostId.isNotBlank()) { "사용자 ID가 필요합니다." }
        require(durationMinutes == 30 || durationMinutes == 60) {
            "시간 슬롯은 30분 또는 60분만 생성할 수 있습니다."
        }
        require(startAt > System.currentTimeMillis()) { "과거 시간에는 슬롯을 생성할 수 없습니다." }
        require(endAt == startAt + durationMinutes * MILLIS_PER_MINUTE) {
            "종료 시간이 슬롯 길이와 일치하지 않습니다."
        }

        val duplicate = firestore.collection(TIME_SLOTS_COLLECTION)
            .whereEqualTo(HOST_ID_FIELD, hostId)
            .whereEqualTo(START_AT_FIELD, startAt)
            .limit(1)
            .get()
            .await()

        check(duplicate.isEmpty) { "같은 시간에 이미 생성된 슬롯이 있습니다." }

        val document = firestore.collection(TIME_SLOTS_COLLECTION).document()
        val now = System.currentTimeMillis()
        val timeSlot = TimeSlot(
            id = document.id,
            hostId = hostId,
            startAt = startAt,
            endAt = endAt,
            durationMinutes = durationMinutes,
            status = TimeSlotStatus.AVAILABLE.name,
            createdAt = now,
            updatedAt = now
        )
        document.set(timeSlot).await()
    }

    override suspend fun getHostTimeSlots(hostId: String): List<TimeSlot> {
        require(hostId.isNotBlank()) { "사용자 ID가 필요합니다." }
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val query = firestore.collection(TIME_SLOTS_COLLECTION)
            .whereEqualTo(HOST_ID_FIELD, hostId)
            .whereGreaterThanOrEqualTo(START_AT_FIELD, startOfDay)
            .orderBy(START_AT_FIELD, Query.Direction.ASCENDING)

        return getTimeSlotsWithIndexFallback(query, hostId)
            .filter { it.startAt >= startOfDay }
            .sortedBy { it.startAt }
    }

    override suspend fun getAvailableTimeSlots(hostId: String): List<TimeSlot> {
        require(hostId.isNotBlank()) { "사용자 ID가 필요합니다." }
        val now = System.currentTimeMillis()
        val query = firestore.collection(TIME_SLOTS_COLLECTION)
            .whereEqualTo(HOST_ID_FIELD, hostId)
            .whereEqualTo(STATUS_FIELD, TimeSlotStatus.AVAILABLE.name)
            .whereGreaterThanOrEqualTo(START_AT_FIELD, now)
            .orderBy(START_AT_FIELD, Query.Direction.ASCENDING)

        return getTimeSlotsWithIndexFallback(query, hostId)
            .filter {
                it.status == TimeSlotStatus.AVAILABLE.name &&
                    it.startAt >= now
            }
            .sortedBy { it.startAt }
    }

    private suspend fun getTimeSlotsWithIndexFallback(
        indexedQuery: Query,
        hostId: String
    ): List<TimeSlot> {
        return try {
            indexedQuery.get().await().toObjects(TimeSlot::class.java)
        } catch (exception: FirebaseFirestoreException) {
            if (
                exception.code != FirebaseFirestoreException.Code.FAILED_PRECONDITION ||
                !exception.message.orEmpty().contains("requires an index", ignoreCase = true)
            ) {
                throw exception
            }
            firestore.collection(TIME_SLOTS_COLLECTION)
                .whereEqualTo(HOST_ID_FIELD, hostId)
                .get()
                .await()
                .toObjects(TimeSlot::class.java)
        }
    }

    override suspend fun disableTimeSlot(slotId: String) {
        require(slotId.isNotBlank()) { "시간 슬롯 ID가 필요합니다." }
        val slotRef = firestore.collection(TIME_SLOTS_COLLECTION).document(slotId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(slotRef)
            check(snapshot.exists()) { "시간 슬롯을 찾을 수 없습니다." }
            check(snapshot.getString(STATUS_FIELD) == TimeSlotStatus.AVAILABLE.name) {
                "예약 가능 상태의 슬롯만 비활성화할 수 있습니다."
            }
            transaction.update(
                slotRef,
                mapOf(
                    STATUS_FIELD to TimeSlotStatus.DISABLED.name,
                    UPDATED_AT_FIELD to System.currentTimeMillis()
                )
            )
        }.await()
    }

    override suspend fun enableTimeSlot(slotId: String) {
        require(slotId.isNotBlank()) { "시간 슬롯 ID가 필요합니다." }
        val slotRef = firestore.collection(TIME_SLOTS_COLLECTION).document(slotId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(slotRef)
            check(snapshot.exists()) { "시간 슬롯을 찾을 수 없습니다." }
            check(snapshot.getString(STATUS_FIELD) == TimeSlotStatus.DISABLED.name) {
                "비활성화 상태의 슬롯만 다시 활성화할 수 있습니다."
            }
            check((snapshot.getLong(START_AT_FIELD) ?: 0L) > System.currentTimeMillis()) {
                "과거 시간 슬롯은 다시 활성화할 수 없습니다."
            }
            transaction.update(
                slotRef,
                mapOf(
                    STATUS_FIELD to TimeSlotStatus.AVAILABLE.name,
                    UPDATED_AT_FIELD to System.currentTimeMillis()
                )
            )
        }.await()
    }

    private companion object {
        const val TIME_SLOTS_COLLECTION = "time_slots"
        const val HOST_ID_FIELD = "hostId"
        const val START_AT_FIELD = "startAt"
        const val STATUS_FIELD = "status"
        const val UPDATED_AT_FIELD = "updatedAt"
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
