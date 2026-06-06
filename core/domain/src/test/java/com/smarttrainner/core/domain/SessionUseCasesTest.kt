package com.smarttrainner.core.domain

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SessionUseCasesTest {
    @Test
    fun syncPendingTrainingData_returnsSuccessWhenThereAreNoSyncers() = runTest {
        val result = SyncPendingTrainingDataUseCase(emptySet()).invoke()

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun syncPendingTrainingData_returnsSuccessAfterRunningAllSuccessfulSyncers() = runTest {
        val syncers = linkedSetOf(
            RecordingTrainingDataSyncer(Result.success(Unit)),
            RecordingTrainingDataSyncer(Result.success(Unit))
        )

        val result = SyncPendingTrainingDataUseCase(syncers).invoke()

        assertThat(result.isSuccess).isTrue()
        assertThat(syncers.map { it.callCount }).containsExactly(1, 1).inOrder()
    }

    @Test
    fun syncPendingTrainingData_runsAllSyncersAndReturnsFirstFailure() = runTest {
        val firstFailure = IllegalStateException("routine sync failed")
        val syncers = linkedSetOf(
            RecordingTrainingDataSyncer(Result.success(Unit)),
            RecordingTrainingDataSyncer(Result.failure(firstFailure)),
            RecordingTrainingDataSyncer(Result.failure(IllegalArgumentException("workout sync failed")))
        )

        val result = SyncPendingTrainingDataUseCase(syncers).invoke()

        assertThat(result.exceptionOrNull()).isSameInstanceAs(firstFailure)
        assertThat(syncers.map { it.callCount }).containsExactly(1, 1, 1).inOrder()
    }
}

private class RecordingTrainingDataSyncer(
    private val result: Result<Unit>
) : TrainingDataSyncer {
    var callCount: Int = 0

    override suspend fun syncPendingTrainingData(): Result<Unit> {
        callCount += 1
        return result
    }
}
