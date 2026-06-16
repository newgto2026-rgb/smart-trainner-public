package com.smarttrainner.core.model

fun WorkoutLog.summaryVolumeKg(
    exercise: Exercise?,
    bodyWeightKg: Double?
): Double = when (exercise?.loadType) {
    ExerciseLoadType.ASSISTANCE_LOAD -> effectiveVolumeKg(exercise, bodyWeightKg) ?: 0.0
    else -> volumeKg
}

fun WorkoutLog.effectiveVolumeKg(
    exercise: Exercise,
    bodyWeightKg: Double?
): Double? = when (exercise.loadType) {
    ExerciseLoadType.EXTERNAL_LOAD -> volumeKg
    ExerciseLoadType.ASSISTANCE_LOAD -> {
        val bodyWeight = bodyWeightKg ?: return null
        val entries = normalizedSetEntries()
        var hasWeightedReps = false
        val total = entries.sumOf { entry ->
            val reps = entry.reps
            val assistanceKg = entry.weightKg
            if (reps == null || assistanceKg == null) {
                0.0
            } else {
                hasWeightedReps = true
                reps * (bodyWeight - assistanceKg).coerceAtLeast(0.0)
            }
        }
        total.takeIf { hasWeightedReps }
    }
}

fun WorkoutLog.recordedSetLoadsKg(): List<Double> =
    normalizedSetEntries().mapNotNull { it.weightKg }

fun WorkoutLog.effectiveSetLoadsKg(
    exercise: Exercise,
    bodyWeightKg: Double?
): List<Double> {
    if (exercise.loadType != ExerciseLoadType.ASSISTANCE_LOAD) return emptyList()
    val bodyWeight = bodyWeightKg ?: return emptyList()
    return recordedSetLoadsKg().map { assistanceKg ->
        (bodyWeight - assistanceKg).coerceAtLeast(0.0)
    }
}

private fun WorkoutLog.normalizedSetEntries(): List<WorkoutSetLog> =
    setEntries.takeIf { it.isNotEmpty() }
        ?: List(sets.coerceAtLeast(0)) { index ->
            WorkoutSetLog(
                order = index + 1,
                reps = reps,
                weightKg = weightKg,
                durationMinutes = durationMinutes
            )
        }
