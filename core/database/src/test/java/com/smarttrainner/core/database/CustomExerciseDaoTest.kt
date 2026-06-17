package com.smarttrainner.core.database

import androidx.room.Room
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CustomExerciseDaoTest {
    private lateinit var database: SmartTrainnerDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            SmartTrainnerDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeActiveForOwnerKeepsCustomExercisesIsolatedByOwner() = runTest {
        val dao = database.customExerciseDao()
        dao.upsert(exercise(id = "custom-exercise-1", ownerSessionId = "local-default", name = "Mine"))
        dao.upsert(exercise(id = "custom-exercise-1", ownerSessionId = "friend-session", name = "Friend"))

        val localExercises = dao.observeActiveForOwner("local-default").first()
        val friendExercises = dao.observeActiveForOwner("friend-session").first()

        assertThat(localExercises.map { it.name }).containsExactly("Mine")
        assertThat(friendExercises.map { it.name }).containsExactly("Friend")
    }

    @Test
    fun observeActiveForOwnerExcludesArchivedExercisesAndMarksDeletePending() = runTest {
        val dao = database.customExerciseDao()
        dao.upsert(exercise(id = "custom-exercise-1", ownerSessionId = "local-default"))

        val updatedRows = dao.markPendingArchive(
            ownerSessionId = "local-default",
            id = "custom-exercise-1",
            archivedAt = "2026-06-17T00:00:00Z"
        )

        assertThat(updatedRows).isEqualTo(1)
        assertThat(dao.observeActiveForOwner("local-default").first()).isEmpty()
        assertThat(dao.pendingSyncForOwner("local-default").single().syncState)
            .isEqualTo(CUSTOM_EXERCISE_SYNC_PENDING_DELETE)
    }

    @Test
    fun pendingSyncForOwnerReturnsOnlyUnsyncedRowsForThatOwner() = runTest {
        val dao = database.customExerciseDao()
        dao.upsert(exercise(id = "pending-local", ownerSessionId = "local-default"))
        dao.upsert(
            exercise(
                id = "synced-local",
                ownerSessionId = "local-default",
                syncState = CUSTOM_EXERCISE_SYNCED
            )
        )
        dao.upsert(exercise(id = "pending-other", ownerSessionId = "other-session"))

        val pending = dao.pendingSyncForOwner("local-default")

        assertThat(pending.map { it.id }).containsExactly("pending-local")
    }

    private fun exercise(
        id: String,
        ownerSessionId: String,
        name: String = "Custom exercise",
        syncState: String = CUSTOM_EXERCISE_SYNC_PENDING_UPSERT
    ) = CustomExerciseEntity(
        id = id,
        ownerSessionId = ownerSessionId,
        source = "USER_CREATED",
        originExerciseId = null,
        name = name,
        primaryMuscleGroup = "FULL_BODY",
        secondaryMuscleGroups = "",
        equipment = "BODYWEIGHT",
        difficulty = "BEGINNER",
        imageKey = id,
        imageUri = null,
        summary = "summary",
        instructions = "step",
        safetyCues = "cue",
        defaultSets = 3,
        repRangeStart = 8,
        repRangeEnd = 12,
        defaultDurationMinutes = null,
        restSeconds = 90,
        createdAt = "2026-06-17T00:00:00Z",
        updatedAt = "2026-06-17T00:00:00Z",
        archivedAt = null,
        syncState = syncState
    )
}
