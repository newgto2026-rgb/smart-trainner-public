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

    @Test
    fun withLocalRoutineDayDateState_preservesAssignedDatesForSameCycleServerSnapshot() {
        val serverSnapshot = routineProgressPreference(
            cycleStartedAt = "2026-06-08T04:02:20.308Z",
            routineDayDates = emptyMap()
        )
        val localProgress = routineProgressPreference(
            cycleStartedAt = null,
            routineDayDates = mapOf(
                "routine-day|custom-template|cycle1|day1" to "2026-06-07"
            )
        )

        val result = serverSnapshot.withLocalRoutineDayDateState(
            localProgress = localProgress,
            zoneId = ZoneId.of("Asia/Seoul")
        )

        assertThat(result.routineDayDates).containsExactly(
            "routine-day|custom-template|cycle1|day1",
            "2026-06-07"
        )
        assertThat(result.cycleStartedAt).isEqualTo("2026-06-06T15:00:00Z")
    }

    @Test
    fun withLocalRoutineDayDateState_keepsServerCycleStartWhenLocalDayOneIsUnassigned() {
        val serverCycleStartedAt = "2026-06-03T04:02:20.308Z"
        val serverSnapshot = routineProgressPreference(
            cycleStartedAt = serverCycleStartedAt,
            routineDayDates = emptyMap()
        )
        val localProgress = routineProgressPreference(
            cycleStartedAt = "2026-05-25T00:00:00Z",
            routineDayDates = mapOf(
                "routine-day|custom-template|cycle1|day2" to "2026-06-08"
            )
        )

        val result = serverSnapshot.withLocalRoutineDayDateState(
            localProgress = localProgress,
            zoneId = ZoneId.of("Asia/Seoul")
        )

        assertThat(result.routineDayDates).containsExactly(
            "routine-day|custom-template|cycle1|day2",
            "2026-06-08"
        )
        assertThat(result.cycleStartedAt).isEqualTo(serverCycleStartedAt)
    }

    @Test
    fun withLocalRoutineDayDateState_dropsAssignedDatesAcrossDifferentCycles() {
        val serverSnapshot = routineProgressPreference(
            cycleNumber = 2,
            cycleStartedAt = "2026-06-03T04:02:20.308Z",
            routineDayDates = emptyMap()
        )
        val localProgress = routineProgressPreference(
            cycleNumber = 1,
            cycleStartedAt = "2026-06-03T04:02:20.308Z",
            routineDayDates = mapOf(
                "routine-day|custom-template|cycle1|day1" to "2026-06-07"
            )
        )

        val result = serverSnapshot.withLocalRoutineDayDateState(
            localProgress = localProgress,
            zoneId = ZoneId.of("Asia/Seoul")
        )

        assertThat(result.routineDayDates).isEmpty()
    }

    private fun routineProgressPreference(
        cycleNumber: Int = 1,
        cycleStartedAt: String?,
        routineDayDates: Map<String, String>
    ): RoutineProgressPreference = RoutineProgressPreference(
        templateId = "custom-template",
        dayIndex = 0,
        cycleNumber = cycleNumber,
        startedAt = "2026-06-03T04:02:20.308Z",
        cycleStartedAt = cycleStartedAt,
        lastCompletedDayIndex = null,
        lastCompletedAt = null,
        lastCompletedCycleNumber = null,
        lastCompletedPreviousCycleStartedAt = null,
        routineDayDates = routineDayDates
    )
}
