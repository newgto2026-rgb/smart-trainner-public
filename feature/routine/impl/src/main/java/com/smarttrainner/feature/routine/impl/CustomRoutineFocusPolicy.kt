package com.smarttrainner.feature.routine.impl

import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.RoutineFocus

private val allMuscleGroups = MuscleGroup.entries.toSet()
private val upperBodyMuscleGroups = setOf(
    MuscleGroup.BACK,
    MuscleGroup.CHEST,
    MuscleGroup.SHOULDERS,
    MuscleGroup.ARMS,
    MuscleGroup.BICEPS,
    MuscleGroup.TRICEPS,
    MuscleGroup.FOREARMS
)
private val pushMuscleGroups = setOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)
private val pullMuscleGroups = setOf(MuscleGroup.BACK, MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
private val chestMuscleGroups = setOf(MuscleGroup.CHEST)
private val backMuscleGroups = setOf(MuscleGroup.BACK)
private val lowerBodyMuscleGroups = setOf(MuscleGroup.LOWER_BODY)
private val shouldersMuscleGroups = setOf(MuscleGroup.SHOULDERS)
private val armsMuscleGroups = setOf(
    MuscleGroup.ARMS,
    MuscleGroup.BICEPS,
    MuscleGroup.TRICEPS,
    MuscleGroup.FOREARMS
)
private val bicepsMuscleGroups = setOf(MuscleGroup.BICEPS)
private val tricepsMuscleGroups = setOf(MuscleGroup.TRICEPS)
private val forearmsMuscleGroups = setOf(MuscleGroup.FOREARMS)
private val cardioMuscleGroups = setOf(MuscleGroup.CARDIO)
private val coreMuscleGroups = setOf(MuscleGroup.CORE)

internal fun allowedCustomRoutineMuscleGroups(focus: RoutineFocus?): Set<MuscleGroup> = when (focus) {
    null,
    RoutineFocus.FULL_BODY -> allMuscleGroups
    RoutineFocus.UPPER_BODY -> upperBodyMuscleGroups
    RoutineFocus.PUSH -> pushMuscleGroups
    RoutineFocus.PULL -> pullMuscleGroups
    RoutineFocus.CHEST -> chestMuscleGroups
    RoutineFocus.BACK -> backMuscleGroups
    RoutineFocus.LOWER_BODY -> lowerBodyMuscleGroups
    RoutineFocus.SHOULDERS -> shouldersMuscleGroups
    RoutineFocus.ARMS -> armsMuscleGroups
    RoutineFocus.BICEPS -> bicepsMuscleGroups
    RoutineFocus.TRICEPS -> tricepsMuscleGroups
    RoutineFocus.FOREARMS -> forearmsMuscleGroups
    RoutineFocus.CARDIO_CONDITIONING -> cardioMuscleGroups
    RoutineFocus.CORE -> coreMuscleGroups
}
