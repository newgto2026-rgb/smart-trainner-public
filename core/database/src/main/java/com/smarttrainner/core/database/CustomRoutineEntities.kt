package com.smarttrainner.core.database

import androidx.room.Embedded
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

const val CUSTOM_ROUTINE_SYNCED = "synced"
const val CUSTOM_ROUTINE_SYNC_PENDING_UPSERT = "pending_upsert"
const val CUSTOM_ROUTINE_SYNC_PENDING_DELETE = "pending_delete"

@Entity(
    tableName = "custom_routines",
    indices = [Index(value = ["sessionId"])]
)
data class CustomRoutineEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val name: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
    @ColumnInfo(defaultValue = "'pending_upsert'")
    val syncState: String = CUSTOM_ROUTINE_SYNC_PENDING_UPSERT
)

@Entity(
    tableName = "custom_routine_days",
    foreignKeys = [
        ForeignKey(
            entity = CustomRoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["routineId"]),
        Index(value = ["routineId", "dayIndex"], unique = true)
    ]
)
data class CustomRoutineDayEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val dayIndex: Int,
    val title: String,
    val focus: String,
    val primaryFocus: String,
    val secondaryFocuses: String,
    val minRecoveryHours: Int
)

@Entity(
    tableName = "custom_routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = CustomRoutineDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["dayId"]),
        Index(value = ["dayId", "slotIndex"], unique = true)
    ]
)
data class CustomRoutineExerciseEntity(
    @PrimaryKey val id: String,
    val dayId: String,
    val slotIndex: Int,
    val exerciseId: String,
    val sets: Int,
    val repRangeStart: Int?,
    val repRangeEnd: Int?,
    val durationMinutes: Int?,
    val restSeconds: Int,
    val note: String
)

data class CustomRoutineWithDays(
    @Embedded val routine: CustomRoutineEntity,
    @Relation(
        entity = CustomRoutineDayEntity::class,
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val days: List<CustomRoutineDayWithExercises>
)

data class CustomRoutineDayWithExercises(
    @Embedded val day: CustomRoutineDayEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "dayId"
    )
    val exercises: List<CustomRoutineExerciseEntity>
)

data class CustomRoutineDayWrite(
    val day: CustomRoutineDayEntity,
    val exercises: List<CustomRoutineExerciseEntity>
)
