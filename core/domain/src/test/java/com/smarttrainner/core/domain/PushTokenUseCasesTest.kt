package com.smarttrainner.core.domain

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PushTokenUseCasesTest {
    @Test
    fun registerPushToken_returnsSuccessFromRepository() = runTest {
        val repository = RecordingPushTokenRepository(Result.success(Unit))
        val result = RegisterPushTokenUseCase(repository).invoke("fcm-token")

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.tokens).containsExactly("fcm-token")
    }

    @Test
    fun registerPushToken_returnsFailureFromRepository() = runTest {
        val failure = IllegalStateException("network unavailable")
        val repository = RecordingPushTokenRepository(Result.failure(failure))
        val result = RegisterPushTokenUseCase(repository).invoke("fcm-token")

        assertThat(result.exceptionOrNull()).isSameInstanceAs(failure)
        assertThat(repository.tokens).containsExactly("fcm-token")
    }
}

private class RecordingPushTokenRepository(
    private val result: Result<Unit>
) : PushTokenRepository {
    val tokens = mutableListOf<String>()

    override suspend fun registerToken(token: String): Result<Unit> {
        tokens += token
        return result
    }
}
