package com.smarttrainner.feature.routine.data

import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.RoutineProgressPreference
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Test

class RoutineProgressPreferenceMappingTest {
    @Test
    fun effectiveCycleStartedAt_usesAssignedDayOneStartInProvidedZone() {
        val zone = ZoneId.of("Asia/Seoul")
        val preference = routineProgressPreference(
            cycleStartedAt = "2026-06-03T04:02:20.308Z",
            routineDayDates = mapOf(
                "routine-day|custom-template|cycle1|day1" to "2026-06-01"
            )
        )

        val result = preference.effectiveCycleStartedAt(zone)

        assertThat(result).isEqualTo(
            LocalDate.of(2026, 6, 1)
                .atStartOfDay(zone)
                .toInstant()
                .toString()
        )
        assertThat(Instant.parse(result))
            .isEqualTo(Instant.parse("2026-05-31T15:00:00Z"))
    }

    @Test
    fun effectiveCycleStartedAt_keepsEarlierStoredCycleStart() {
        val storedCycleStart = "2026-05-31T12:00:00Z"
        val preference = routineProgressPreference(
            cycleStartedAt = storedCycleStart,
            routineDayDates = mapOf(
                "routine-day|custom-template|cycle1|day1" to "2026-06-01"
            )
        )

        val result = preference.effectiveCycleStartedAt(ZoneId.of("Asia/Seoul"))

        assertThat(result).isEqualTo(storedCycleStart)
    }

    private fun routineProgressPreference(
        cycleStartedAt: String?,
        routineDayDates: Map<String, String>
    ): RoutineProgressPreference = RoutineProgressPreference(
        templateId = "custom-template",
        dayIndex = 0,
        cycleNumber = 1,
        startedAt = "2026-06-03T04:02:20.308Z",
        cycleStartedAt = cycleStartedAt,
        lastCompletedDayIndex = null,
        lastCompletedAt = null,
        lastCompletedCycleNumber = null,
        lastCompletedPreviousCycleStartedAt = null,
        routineDayDates = routineDayDates
    )
}
