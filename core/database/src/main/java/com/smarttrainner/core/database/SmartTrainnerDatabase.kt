package com.smarttrainner.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorkoutLogEntity::class,
        WorkoutSetLogEntity::class,
        CustomRoutineEntity::class,
        CustomRoutineDayEntity::class,
        CustomRoutineExerciseEntity::class,
        CustomExerciseEntity::class,
        FriendConnectionEntity::class,
        FriendRequestEntity::class
    ],
    version = 10,
    exportSchema = true
)
abstract class SmartTrainnerDatabase : RoomDatabase() {
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun customRoutineDao(): CustomRoutineDao
    abstract fun customExerciseDao(): CustomExerciseDao
    abstract fun friendDao(): FriendDao
}
