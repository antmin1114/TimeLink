package com.kkm.timelink.domain.model

data class TimeSlotInterval(
    val startAt: Long,
    val endAt: Long
)

fun splitTimeSlotRange(
    startAt: Long,
    endAt: Long,
    durationMinutes: Int
): List<TimeSlotInterval> {
    require(durationMinutes == 30 || durationMinutes == 60) {
        "슬롯 단위는 30분 또는 60분이어야 합니다."
    }
    require(endAt > startAt) { "종료 시간은 시작 시간보다 늦어야 합니다." }

    val slotDurationMillis = durationMinutes * MILLIS_PER_MINUTE
    require((endAt - startAt) % slotDurationMillis == 0L) {
        "선택한 시간 범위는 슬롯 단위로 정확히 나누어져야 합니다."
    }

    return generateSequence(startAt) { previousStart ->
        (previousStart + slotDurationMillis).takeIf { it < endAt }
    }.map { slotStart ->
        TimeSlotInterval(
            startAt = slotStart,
            endAt = slotStart + slotDurationMillis
        )
    }.toList()
}

fun TimeSlotInterval.overlaps(timeSlot: TimeSlot): Boolean =
    startAt < timeSlot.endAt && endAt > timeSlot.startAt

private const val MILLIS_PER_MINUTE = 60_000L
