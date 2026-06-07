package com.smarttrainner.core.model

fun routineDayInstanceId(
    templateId: String,
    cycleNumber: Int,
    dayNumber: Int
): String = "${RoutineDayInstancePrefix}$templateId|cycle$cycleNumber|day$dayNumber"

fun routineDayInstancePrefix(
    templateId: String,
    cycleNumber: Int
): String = "${RoutineDayInstancePrefix}$templateId|cycle$cycleNumber|"

fun routineAdditionalExerciseIdPrefix(
    templateId: String,
    cycleNumber: Int,
    dayNumber: Int
): String = "${routineAdditionalExerciseCyclePrefix(templateId, cycleNumber)}day$dayNumber|"

fun routineAdditionalExerciseCyclePrefix(
    templateId: String,
    cycleNumber: Int
): String = "${RoutineAdditionalExerciseIdPrefix}$templateId|cycle$cycleNumber|"

fun PlannedExerciseId.isRoutineAdditionalExerciseId(): Boolean =
    value.startsWith(RoutineAdditionalExerciseIdPrefix)

private const val RoutineDayInstancePrefix = "routine-day|"
private const val RoutineAdditionalExerciseIdPrefix = "routine-added|"
