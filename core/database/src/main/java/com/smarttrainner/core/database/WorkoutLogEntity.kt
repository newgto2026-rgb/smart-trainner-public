package com.smarttrainner.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_logs",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["sessionId", "plannedExerciseId"], unique = true),
        Index(value = ["performedDate"])
    ]
)
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val plannedExerciseId: String,
    val exerciseId: String,
    val performedDate: String,
    val performedAt: String,
    val sets: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationMinutes: Int?,
    val memo: String,
    val completed: Boolean
)
