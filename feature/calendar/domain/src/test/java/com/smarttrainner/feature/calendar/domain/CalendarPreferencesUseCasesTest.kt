package com.smarttrainner.feature.calendar.domain

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CalendarPreferencesUseCasesTest {
    @Test
    fun observeCalendarMonthExpandedUseCase_delegatesRepositoryFlow() = runTest {
        val repository = FakeCalendarPreferencesRepository(initialMonthExpanded = true)
        val useCase = ObserveCalendarMonthExpandedUseCase(repository)

        useCase().test {
            assertThat(awaitItem()).isTrue()

            repository.monthExpanded.value = false

            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateCalendarMonthExpandedUseCase_delegatesRepositoryWrite() = runTest {
        val repository = FakeCalendarPreferencesRepository(initialMonthExpanded = true)
        val useCase = UpdateCalendarMonthExpandedUseCase(repository)

        val result = useCase(false)

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.monthExpanded.value).isFalse()
        assertThat(repository.updates).containsExactly(false)
    }

    @Test
    fun updateCalendarMonthExpandedUseCase_returnsRepositoryFailure() = runTest {
        val failure = IllegalStateException("preferences unavailable")
        val repository = FakeCalendarPreferencesRepository(initialMonthExpanded = true).apply {
            nextUpdateResult = Result.failure(failure)
        }
        val useCase = UpdateCalendarMonthExpandedUseCase(repository)

        val result = useCase(false)

        assertThat(result.exceptionOrNull()).isSameInstanceAs(failure)
        assertThat(repository.updates).containsExactly(false)
    }
}

private class FakeCalendarPreferencesRepository(
    initialMonthExpanded: Boolean
) : CalendarPreferencesRepository {
    val monthExpanded = MutableStateFlow(initialMonthExpanded)
    val updates = mutableListOf<Boolean>()
    var nextUpdateResult: Result<Unit> = Result.success(Unit)

    override fun observeMonthExpanded(): Flow<Boolean> = monthExpanded

    override suspend fun setMonthExpanded(isExpanded: Boolean): Result<Unit> {
        updates += isExpanded
        return nextUpdateResult.also { result ->
            if (result.isSuccess) {
                monthExpanded.value = isExpanded
            }
            nextUpdateResult = Result.success(Unit)
        }
    }
}
