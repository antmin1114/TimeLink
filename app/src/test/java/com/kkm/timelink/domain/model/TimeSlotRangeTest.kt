package com.kkm.timelink.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeSlotRangeTest {

    @Test
    fun `two hour range is split into four 30 minute slots`() {
        val slots = splitTimeSlotRange(
            startAt = 0L,
            endAt = 120 * MILLIS_PER_MINUTE,
            durationMinutes = 30
        )

        assertEquals(4, slots.size)
        assertEquals(TimeSlotInterval(0L, 30 * MILLIS_PER_MINUTE), slots.first())
        assertEquals(
            TimeSlotInterval(90 * MILLIS_PER_MINUTE, 120 * MILLIS_PER_MINUTE),
            slots.last()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `range must be exactly divisible by slot unit`() {
        splitTimeSlotRange(
            startAt = 0L,
            endAt = 75 * MILLIS_PER_MINUTE,
            durationMinutes = 30
        )
    }

    @Test
    fun `overlap uses actual time interval`() {
        val candidate = TimeSlotInterval(
            startAt = 60 * MILLIS_PER_MINUTE,
            endAt = 90 * MILLIS_PER_MINUTE
        )

        assertTrue(
            candidate.overlaps(
                TimeSlot(startAt = 45 * MILLIS_PER_MINUTE, endAt = 75 * MILLIS_PER_MINUTE)
            )
        )
        assertFalse(
            candidate.overlaps(
                TimeSlot(startAt = 90 * MILLIS_PER_MINUTE, endAt = 120 * MILLIS_PER_MINUTE)
            )
        )
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
