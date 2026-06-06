package com.smarttrainner.core.model

fun IntRange.toRecommendedDisplayRepRange(): IntRange =
    if (first == FIXED_REP_TARGET && last == FIXED_REP_TARGET) {
        RECOMMENDED_REP_DISPLAY_START..FIXED_REP_TARGET
    } else {
        this
    }

fun estimateExerciseSeconds(
    sets: Int,
    repRange: IntRange?,
    durationMinutes: Int?,
    restSeconds: Int,
    repDurationSeconds: Int = DEFAULT_REP_DURATION_SECONDS
): Int {
    val activeSecondsPerSet = durationMinutes?.let { it * SECONDS_PER_MINUTE }
        ?: ((repRange?.last ?: 0) * repDurationSeconds)
    return sets * (activeSecondsPerSet + restSeconds)
}

const val DEFAULT_REP_DURATION_SECONDS = 5

internal const val SECONDS_PER_MINUTE = 60

internal fun Int.roundUpToMinutes(): Int =
    if (this <= 0) 0 else (this + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE

private const val FIXED_REP_TARGET = 15
private const val RECOMMENDED_REP_DISPLAY_START = 12
