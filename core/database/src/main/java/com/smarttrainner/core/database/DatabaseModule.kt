package com.smarttrainner.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SmartTrainnerDatabase = Room.databaseBuilder(
        context,
        SmartTrainnerDatabase::class.java,
        "smart_trainner.db"
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build()

    @Provides
    fun provideWorkoutLogDao(database: SmartTrainnerDatabase): WorkoutLogDao =
        database.workoutLogDao()

    @Provides
    fun provideCustomRoutineDao(database: SmartTrainnerDatabase): CustomRoutineDao =
        database.customRoutineDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `workout_set_logs` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `workoutLogId` INTEGER NOT NULL,
                    `setIndex` INTEGER NOT NULL,
                    `reps` INTEGER,
                    `weightKg` REAL,
                    `durationMinutes` INTEGER,
                    FOREIGN KEY(`workoutLogId`) REFERENCES `workout_logs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_workout_set_logs_workoutLogId` " +
                    "ON `workout_set_logs` (`workoutLogId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_workout_set_logs_workoutLogId_setIndex` " +
                    "ON `workout_set_logs` (`workoutLogId`, `setIndex`)"
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `workout_logs` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT 'local-default'"
            )
            db.execSQL("DROP INDEX IF EXISTS `index_workout_logs_plannedExerciseId`")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_workout_logs_sessionId` " +
                    "ON `workout_logs` (`sessionId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_workout_logs_sessionId_plannedExerciseId` " +
                    "ON `workout_logs` (`sessionId`, `plannedExerciseId`)"
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `custom_routines` (
                    `id` TEXT NOT NULL,
                    `sessionId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `createdAt` TEXT NOT NULL,
                    `updatedAt` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_custom_routines_sessionId` " +
                    "ON `custom_routines` (`sessionId`)"
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `custom_routine_days` (
                    `id` TEXT NOT NULL,
                    `routineId` TEXT NOT NULL,
                    `dayIndex` INTEGER NOT NULL,
                    `title` TEXT NOT NULL,
                    `focus` TEXT NOT NULL,
                    `primaryFocus` TEXT NOT NULL,
                    `secondaryFocuses` TEXT NOT NULL,
                    `minRecoveryHours` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`routineId`) REFERENCES `custom_routines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_custom_routine_days_routineId` " +
                    "ON `custom_routine_days` (`routineId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_custom_routine_days_routineId_dayIndex` " +
                    "ON `custom_routine_days` (`routineId`, `dayIndex`)"
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `custom_routine_exercises` (
                    `id` TEXT NOT NULL,
                    `dayId` TEXT NOT NULL,
                    `slotIndex` INTEGER NOT NULL,
                    `exerciseId` TEXT NOT NULL,
                    `sets` INTEGER NOT NULL,
                    `repRangeStart` INTEGER,
                    `repRangeEnd` INTEGER,
                    `durationMinutes` INTEGER,
                    `restSeconds` INTEGER NOT NULL,
                    `note` TEXT NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`dayId`) REFERENCES `custom_routine_days`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_custom_routine_exercises_dayId` " +
                    "ON `custom_routine_exercises` (`dayId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_custom_routine_exercises_dayId_slotIndex` " +
                    "ON `custom_routine_exercises` (`dayId`, `slotIndex`)"
            )
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `workout_set_logs` ADD COLUMN `restSeconds` INTEGER")
        }
    }
}
