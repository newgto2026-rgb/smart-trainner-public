package com.smarttrainner.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutLogId"]),
        Index(value = ["workoutLogId", "setIndex"], unique = true)
    ]
)
data class WorkoutSetLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutLogId: Long,
    val setIndex: Int,
    val reps: Int?,
    val weightKg: Double?,
    val durationMinutes: Int?,
    val restSeconds: Int? = null
)
