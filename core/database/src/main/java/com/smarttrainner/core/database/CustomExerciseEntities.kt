package com.smarttrainner.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

const val CUSTOM_EXERCISE_SYNCED = "synced"
const val CUSTOM_EXERCISE_SYNC_PENDING_UPSERT = "pending_upsert"
const val CUSTOM_EXERCISE_SYNC_PENDING_UPDATE = "pending_update"
const val CUSTOM_EXERCISE_SYNC_PENDING_DELETE = "pending_delete"

@Entity(
    tableName = "custom_exercises",
    primaryKeys = ["ownerSessionId", "id"],
    indices = [
        Index(value = ["ownerSessionId", "syncState"]),
        Index(value = ["ownerSessionId", "name"]),
        Index(value = ["ownerSessionId", "archivedAt"])
    ]
)
data class CustomExerciseEntity(
    val id: String,
    val ownerSessionId: String,
    val source: String,
    val originExerciseId: String?,
    val name: String,
    val primaryMuscleGroup: String,
    val secondaryMuscleGroups: String,
    val equipment: String,
    val difficulty: String,
    val imageKey: String,
    val imageUri: String?,
    val summary: String,
    val instructions: String,
    val safetyCues: String,
    val defaultSets: Int,
    val repRangeStart: Int?,
    val repRangeEnd: Int?,
    val defaultDurationMinutes: Int?,
    val restSeconds: Int,
    val createdAt: String,
    val updatedAt: String,
    val archivedAt: String?,
    @ColumnInfo(defaultValue = "'pending_upsert'")
    val syncState: String = CUSTOM_EXERCISE_SYNC_PENDING_UPSERT
)
