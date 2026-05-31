package com.smarttrainner.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.common.truth.Truth.assertThat
import com.smarttrainner.core.model.AuthProvider
import com.smarttrainner.core.model.UserSession
import com.smarttrainner.core.model.UserSessionId
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
        assertThat(progress.lastCompletedDayIndex).isEqualTo(3)
        assertThat(progress.lastCompletedAt).isEqualTo(cycleInstant.toString())
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
        assertThat(progress.lastCompletedDayIndex).isEqualTo(1)
        assertThat(progress.startedAt).isEqualTo(initialInstant.toString())
        assertThat(progress.cycleStartedAt).isEqualTo(initialInstant.toString())
    }

    @Test
    fun setActiveSession_persistsLinkedAccountFields() = runTest {
        dataSource.setActiveSession(
            UserSession(
                id = UserSessionId("google-user-1"),
                displayName = "Kim",
                nickname = "Lift Kim",
                email = "kim@example.com",
                provider = AuthProvider.GOOGLE,
                providerAccountId = "google-subject-1",
                avatarUrl = "https://example.com/kim.png",
                linkedAt = initialInstant.toString()
            )
        )

        val session = dataSource.activeSession.first()

        assertThat(session?.id?.value).isEqualTo("google-user-1")
        assertThat(session?.nickname).isEqualTo("Lift Kim")
        assertThat(session?.provider).isEqualTo(AuthProvider.GOOGLE)
        assertThat(session?.providerAccountId).isEqualTo("google-subject-1")
        assertThat(session?.avatarUrl).isEqualTo("https://example.com/kim.png")
    }
}
