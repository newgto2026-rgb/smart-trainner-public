package com.smarttrainner.core.database

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutLogWithSets(
    @Embedded val log: WorkoutLogEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutLogId"
    )
    val setLogs: List<WorkoutSetLogEntity>
)
