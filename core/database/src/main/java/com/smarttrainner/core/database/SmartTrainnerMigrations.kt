package com.smarttrainner.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object SmartTrainnerMigrations {
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
                    PRIMARY KEY(`ownerSessionId`, `id`)
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

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `workout_logs` ADD COLUMN `clientLogId` TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL(
                "UPDATE `workout_logs` SET `clientLogId` = 'legacy-' || `id` WHERE `clientLogId` = ''"
            )
            db.execSQL("DROP INDEX IF EXISTS `index_workout_logs_sessionId_plannedExerciseId`")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_workout_logs_sessionId_clientLogId` " +
                    "ON `workout_logs` (`sessionId`, `clientLogId`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_workout_logs_sessionId_plannedExerciseId` " +
                    "ON `workout_logs` (`sessionId`, `plannedExerciseId`)"
            )
            db.execSQL("DROP INDEX IF EXISTS `index_workout_logs_performedDate`")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_workout_logs_sessionId_performedDate` " +
                    "ON `workout_logs` (`sessionId`, `performedDate`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_workout_logs_sessionId_exerciseId_performedAt` " +
                    "ON `workout_logs` (`sessionId`, `exerciseId`, `performedAt`)"
            )
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `workout_logs` ADD COLUMN `syncPending` INTEGER NOT NULL DEFAULT 1"
            )
            db.execSQL(
                "ALTER TABLE `custom_routines` ADD COLUMN `syncState` TEXT NOT NULL DEFAULT 'pending_upsert'"
            )
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `workout_logs` ADD COLUMN `routineDayInstanceId` TEXT"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_workout_logs_sessionId_routineDayInstanceId` " +
                    "ON `workout_logs` (`sessionId`, `routineDayInstanceId`)"
            )
        }
    }

    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `friend_connections` (
                    `ownerSessionId` TEXT NOT NULL,
                    `id` TEXT NOT NULL,
                    `friendSessionId` TEXT NOT NULL,
                    `friendNickname` TEXT NOT NULL,
                    `friendDisplayName` TEXT NOT NULL,
                    `friendAvatarUrl` TEXT,
                    `createdAt` TEXT NOT NULL,
                    `updatedAt` TEXT NOT NULL,
                    PRIMARY KEY(`ownerSessionId`, `id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_friend_connections_ownerSessionId` " +
                    "ON `friend_connections` (`ownerSessionId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_friend_connections_ownerSessionId_friendSessionId` " +
                    "ON `friend_connections` (`ownerSessionId`, `friendSessionId`)"
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `friend_requests` (
                    `ownerSessionId` TEXT NOT NULL,
                    `id` TEXT NOT NULL,
                    `requesterSessionId` TEXT NOT NULL,
                    `requesterNickname` TEXT NOT NULL,
                    `requesterDisplayName` TEXT NOT NULL,
                    `requesterAvatarUrl` TEXT,
                    `receiverSessionId` TEXT NOT NULL,
                    `receiverNickname` TEXT NOT NULL,
                    `receiverDisplayName` TEXT NOT NULL,
                    `receiverAvatarUrl` TEXT,
                    `direction` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `createdAt` TEXT NOT NULL,
                    `updatedAt` TEXT NOT NULL,
                    `respondedAt` TEXT,
                    PRIMARY KEY(`ownerSessionId`, `id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_friend_requests_ownerSessionId` " +
                    "ON `friend_requests` (`ownerSessionId`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_friend_requests_ownerSessionId_direction_status` " +
                    "ON `friend_requests` (`ownerSessionId`, `direction`, `status`)"
            )
        }
    }

    private val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `custom_exercises` (
                    `id` TEXT NOT NULL,
                    `ownerSessionId` TEXT NOT NULL,
                    `source` TEXT NOT NULL,
                    `originExerciseId` TEXT,
                    `name` TEXT NOT NULL,
                    `primaryMuscleGroup` TEXT NOT NULL,
                    `secondaryMuscleGroups` TEXT NOT NULL,
                    `equipment` TEXT NOT NULL,
                    `difficulty` TEXT NOT NULL,
                    `imageKey` TEXT NOT NULL,
                    `imageUri` TEXT,
                    `summary` TEXT NOT NULL,
                    `instructions` TEXT NOT NULL,
                    `safetyCues` TEXT NOT NULL,
                    `defaultSets` INTEGER NOT NULL,
                    `repRangeStart` INTEGER,
                    `repRangeEnd` INTEGER,
                    `defaultDurationMinutes` INTEGER,
                    `restSeconds` INTEGER NOT NULL,
                    `createdAt` TEXT NOT NULL,
                    `updatedAt` TEXT NOT NULL,
                    `archivedAt` TEXT,
                    `syncState` TEXT NOT NULL DEFAULT 'pending_upsert',
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_custom_exercises_ownerSessionId_syncState` " +
                    "ON `custom_exercises` (`ownerSessionId`, `syncState`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_custom_exercises_ownerSessionId_name` " +
                    "ON `custom_exercises` (`ownerSessionId`, `name`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_custom_exercises_ownerSessionId_archivedAt` " +
                    "ON `custom_exercises` (`ownerSessionId`, `archivedAt`)"
            )
        }
    }

    val ALL = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10
    )
}
