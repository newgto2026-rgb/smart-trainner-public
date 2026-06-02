package com.smarttrainner.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.RoutineProgressPreference
import java.time.Clock
import java.time.Instant
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
    private lateinit var context: Context
    private lateinit var dataSource: TrainingPreferencesDataSource

    @Before
    fun setUp() = runTest {
        context = RuntimeEnvironment.getApplication()
        context.trainingDataStore.edit { it.clear() }
        dataSource = TrainingPreferencesDataSource(
            context = context,
            clock = Clock.fixed(initialInstant, ZoneOffset.UTC)
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
        val session = dataSource.startDefaultSession()

        assertThat(session.nickname).isEqualTo("local-athlete")
        assertThat(dataSource.activeSession.first()?.nickname).isEqualTo("local-athlete")

        dataSource.clearActiveSession()

        assertThat(dataSource.activeSession.first()).isNull()
    }

    @Test
    fun selectedThemeTone_defaultsToBlueAndPersistsSelection() = runTest {
        assertThat(dataSource.selectedThemeTone.first()).isEqualTo("blue")

        dataSource.setSelectedThemeTone("black")

        assertThat(dataSource.selectedThemeTone.first()).isEqualTo("black")
    }
}
