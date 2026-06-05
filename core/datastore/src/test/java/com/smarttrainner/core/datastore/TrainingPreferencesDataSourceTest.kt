package com.smarttrainner.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.ProfileGender
import com.smarttrainner.core.model.ProfileSetup
import com.smarttrainner.core.model.RoutineProgressPreference
import com.smarttrainner.core.model.TrainingExperience
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class TrainingPreferencesDataSourceTest {
    private val initialInstant = Instant.parse("2026-05-24T09:00:00Z")
    private val cycleInstant = Instant.parse("2026-05-25T12:00:00Z")
    private val mutableClock = MutableClock(initialInstant, ZoneOffset.UTC)
    private lateinit var context: Context
    private lateinit var dataSource: TrainingPreferencesDataSource

    @Before
    fun setUp() = runTest {
        context = RuntimeEnvironment.getApplication()
        context.trainingDataStore.edit { it.clear() }
        mutableClock.currentInstant = initialInstant
        dataSource = TrainingPreferencesDataSource(
            context = context,
            clock = mutableClock
        )
    }

    @After
    fun tearDown() = runTest {
        context.trainingDataStore.edit { it.clear() }
    }

    @Test
    fun setActiveRoutineTemplate_writesRoutineAndCycleStartFromClock() = runTest {
        dataSource.setActiveRoutineTemplate(sessionId = "session", templateId = "template")

        val progress = dataSource.activeRoutineProgress("session").first()

        assertThat(progress.templateId).isEqualTo("template")
        assertThat(progress.dayIndex).isEqualTo(0)
        assertThat(progress.cycleNumber).isEqualTo(1)
        assertThat(progress.startedAt).isEqualTo(initialInstant.toString())
        assertThat(progress.cycleStartedAt).isEqualTo(initialInstant.toString())
    }

    @Test
    fun setRoutineDayDate_persistsAssignedDateAndOptionalCycleStart() = runTest {
        dataSource.setActiveRoutineTemplate(sessionId = "session", templateId = "template")

        dataSource.setRoutineDayDate(
            sessionId = "session",
            routineDayInstanceId = "routine-day|template|cycle1|day1",
            assignedDate = LocalDate.of(2026, 5, 23).toString(),
            cycleStartedAt = cycleInstant.toString()
        )

        val progress = dataSource.activeRoutineProgress("session").first()

        assertThat(progress.routineDayDates)
            .containsEntry("routine-day|template|cycle1|day1", "2026-05-23")
        assertThat(progress.cycleStartedAt).isEqualTo(cycleInstant.toString())
    }

    @Test
    fun setActiveRoutineTemplate_clearsRoutineDayDates() = runTest {
        dataSource.setActiveRoutineTemplate(sessionId = "session", templateId = "template")
        dataSource.setRoutineDayDate(
            sessionId = "session",
            routineDayInstanceId = "routine-day|template|cycle1|day1",
            assignedDate = LocalDate.of(2026, 5, 23).toString()
        )

        dataSource.setActiveRoutineTemplate(sessionId = "session", templateId = "template")

        assertThat(dataSource.activeRoutineProgress("session").first().routineDayDates).isEmpty()
    }

    @Test
    fun markRoutineDayCompleted_withNewCycleStartUpdatesOnlyCycleBoundary() = runTest {
        dataSource.setActiveRoutineTemplate(sessionId = "session", templateId = "template")

        dataSource.markRoutineDayCompleted(
            sessionId = "session",
            completedDayIndex = 3,
            nextDayIndex = 0,
            completedAt = cycleInstant.toString(),
            newCycleStartedAt = cycleInstant.toString()
        )

        val progress = dataSource.activeRoutineProgress("session").first()

        assertThat(progress.dayIndex).isEqualTo(0)
        assertThat(progress.cycleNumber).isEqualTo(2)
        assertThat(progress.lastCompletedDayIndex).isEqualTo(3)
        assertThat(progress.lastCompletedAt).isEqualTo(cycleInstant.toString())
        assertThat(progress.lastCompletedCycleNumber).isEqualTo(1)
        assertThat(progress.lastCompletedPreviousCycleStartedAt).isEqualTo(initialInstant.toString())
        assertThat(progress.startedAt).isEqualTo(initialInstant.toString())
        assertThat(progress.cycleStartedAt).isEqualTo(cycleInstant.toString())
    }

    @Test
    fun markRoutineDayCompleted_withoutNewCycleStartKeepsCycleBoundary() = runTest {
        dataSource.setActiveRoutineTemplate(sessionId = "session", templateId = "template")

        dataSource.markRoutineDayCompleted(
            sessionId = "session",
            completedDayIndex = 1,
            nextDayIndex = 2,
            completedAt = cycleInstant.toString(),
            newCycleStartedAt = null
        )

        val progress = dataSource.activeRoutineProgress("session").first()

        assertThat(progress.dayIndex).isEqualTo(2)
        assertThat(progress.cycleNumber).isEqualTo(1)
        assertThat(progress.lastCompletedDayIndex).isEqualTo(1)
        assertThat(progress.startedAt).isEqualTo(initialInstant.toString())
        assertThat(progress.cycleStartedAt).isEqualTo(initialInstant.toString())
    }

    @Test
    fun cancelLatestRoutineDayCompletion_restoresPreviousDayAndClearsLatestSnapshot() = runTest {
        dataSource.setActiveRoutineTemplate(sessionId = "session", templateId = "template")
        dataSource.markRoutineDayCompleted(
            sessionId = "session",
            completedDayIndex = 3,
            nextDayIndex = 0,
            completedAt = cycleInstant.toString(),
            newCycleStartedAt = cycleInstant.toString()
        )

        dataSource.cancelLatestRoutineDayCompletion(
            sessionId = "session",
            restoredDayIndex = 3,
            restoredCycleNumber = 1,
            restoredCycleStartedAt = initialInstant.toString()
        )

        val progress = dataSource.activeRoutineProgress("session").first()

        assertThat(progress.dayIndex).isEqualTo(3)
        assertThat(progress.cycleNumber).isEqualTo(1)
        assertThat(progress.cycleStartedAt).isEqualTo(initialInstant.toString())
        assertThat(progress.lastCompletedDayIndex).isNull()
        assertThat(progress.lastCompletedAt).isNull()
        assertThat(progress.lastCompletedCycleNumber).isNull()
    }

    @Test
    fun cancelLatestRoutineDayCompletion_canKeepPreviousSameCycleCompletion() = runTest {
        dataSource.setActiveRoutineTemplate(sessionId = "session", templateId = "template")
        dataSource.markRoutineDayCompleted(
            sessionId = "session",
            completedDayIndex = 2,
            nextDayIndex = 3,
            completedAt = cycleInstant.toString(),
            newCycleStartedAt = null
        )

        dataSource.cancelLatestRoutineDayCompletion(
            sessionId = "session",
            restoredDayIndex = 2,
            restoredCycleNumber = 1,
            restoredCycleStartedAt = initialInstant.toString(),
            remainingLastCompletedDayIndex = 1,
            remainingLastCompletedAt = null,
            remainingLastCompletedCycleNumber = 1,
            remainingLastCompletedPreviousCycleStartedAt = initialInstant.toString()
        )

        val progress = dataSource.activeRoutineProgress("session").first()

        assertThat(progress.dayIndex).isEqualTo(2)
        assertThat(progress.lastCompletedDayIndex).isEqualTo(1)
        assertThat(progress.lastCompletedCycleNumber).isEqualTo(1)
        assertThat(progress.lastCompletedPreviousCycleStartedAt).isEqualTo(initialInstant.toString())
    }

    @Test
    fun setRoutineProgress_overwritesLocalRoutineFromServerSnapshot() = runTest {
        dataSource.setActiveRoutineTemplate(sessionId = "session", templateId = "local-template")

        dataSource.setRoutineProgress(
            sessionId = "session",
            progress = RoutineProgressPreference(
                templateId = "server-template",
                dayIndex = 2,
                cycleNumber = 4,
                startedAt = "2026-06-01T00:00:00Z",
                cycleStartedAt = "2026-06-08T00:00:00Z",
                lastCompletedDayIndex = 1,
                lastCompletedAt = "2026-06-08T10:00:00Z",
                lastCompletedCycleNumber = 4,
                lastCompletedPreviousCycleStartedAt = "2026-06-08T00:00:00Z"
            )
        )

        val progress = dataSource.activeRoutineProgress("session").first()

        assertThat(dataSource.selectedTemplateId("session").first()).isEqualTo("server-template")
        assertThat(progress.templateId).isEqualTo("server-template")
        assertThat(progress.dayIndex).isEqualTo(2)
        assertThat(progress.cycleNumber).isEqualTo(4)
        assertThat(progress.lastCompletedDayIndex).isEqualTo(1)
    }

    @Test
    fun startDefaultSession_setsLocalNicknameAndClearActiveSessionLogsOut() = runTest {
        val session = dataSource.startDefaultSession(
            nickname = "local-athlete",
            profileSetup = ProfileSetup(
                gender = ProfileGender.MALE,
                heightCm = 180,
                weightKg = 82.5
            )
        )

        assertThat(session.nickname).isEqualTo("local-athlete")
        assertThat(dataSource.activeSession.first()?.nickname).isEqualTo("local-athlete")

        dataSource.clearActiveSession()

        assertThat(dataSource.activeSession.first()).isNull()
    }

    @Test
    fun bodyProfile_keepsGenderAndStoresMeasurementsByDate() = runTest {
        dataSource.startDefaultSession(
            nickname = "Lift Kim",
            profileSetup = ProfileSetup(
                gender = ProfileGender.MALE,
                heightCm = 180,
                weightKg = 82.5
            )
        )

        mutableClock.currentInstant = Instant.parse("2026-05-25T10:00:00Z")
        dataSource.updateBodyProfile(
            sessionId = DEFAULT_USER_SESSION_ID,
            gender = ProfileGender.FEMALE,
            heightCm = 181,
            weightKg = 83.0
        )

        val profile = dataSource.activeSession.first()?.profile
        assertThat(profile?.gender).isEqualTo(ProfileGender.MALE)
        assertThat(profile?.bodyMeasurements?.map { it.recordedDate }).containsExactly(
            LocalDate.of(2026, 5, 24),
            LocalDate.of(2026, 5, 25)
        ).inOrder()
        assertThat(profile?.latestBodyMeasurement?.heightCm).isEqualTo(181)
        assertThat(profile?.latestBodyMeasurement?.weightKg).isEqualTo(83.0)
    }

    @Test
    fun bodyProfile_marksProfileSyncPendingUntilMarkedSynced() = runTest {
        dataSource.startDefaultSession(
            nickname = "Lift Kim",
            profileSetup = ProfileSetup(
                gender = ProfileGender.MALE,
                heightCm = 180,
                weightKg = 82.5
            )
        )

        assertThat(dataSource.profileSyncPending(DEFAULT_USER_SESSION_ID)).isFalse()

        dataSource.updateBodyProfile(
            sessionId = DEFAULT_USER_SESSION_ID,
            gender = ProfileGender.MALE,
            heightCm = 181,
            weightKg = 83.0
        )

        assertThat(dataSource.profileSyncPending(DEFAULT_USER_SESSION_ID)).isTrue()

        dataSource.markProfileSynced(DEFAULT_USER_SESSION_ID)

        assertThat(dataSource.profileSyncPending(DEFAULT_USER_SESSION_ID)).isFalse()
    }

    @Test
    fun selectedThemeTone_defaultsToBlueAndPersistsSelection() = runTest {
        assertThat(dataSource.selectedThemeTone.first()).isEqualTo("blue")

        dataSource.setSelectedThemeTone("black")

        assertThat(dataSource.selectedThemeTone.first()).isEqualTo("black")
    }

    @Test
    fun trainingExperience_defaultsToBeginnerAndPersistsPerSession() = runTest {
        assertThat(dataSource.activeTrainingExperience.first()).isEqualTo(TrainingExperience.BEGINNER)
        assertThat(dataSource.trainingExperience("session-a").first()).isEqualTo(TrainingExperience.BEGINNER)

        dataSource.setTrainingExperience("session-a", TrainingExperience.ADVANCED)
        dataSource.setTrainingExperience("session-b", TrainingExperience.INTERMEDIATE)

        assertThat(dataSource.trainingExperience("session-a").first()).isEqualTo(TrainingExperience.ADVANCED)
        assertThat(dataSource.trainingExperience("session-b").first()).isEqualTo(TrainingExperience.INTERMEDIATE)
    }
}

private class MutableClock(
    var currentInstant: Instant,
    private val currentZone: ZoneId
) : Clock() {
    override fun instant(): Instant = currentInstant

    override fun getZone(): ZoneId = currentZone

    override fun withZone(zone: ZoneId): Clock = MutableClock(currentInstant, zone)
}
