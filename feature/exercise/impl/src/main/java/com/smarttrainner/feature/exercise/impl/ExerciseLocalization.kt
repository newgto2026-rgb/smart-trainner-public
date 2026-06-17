package com.smarttrainner.feature.exercise.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.smarttrainner.core.exercisemedia.exerciseStepVisuals
import com.smarttrainner.core.exercisemedia.exerciseUsesGeneratedTextBackedVisuals
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseMovementPattern
import com.smarttrainner.core.model.ExerciseMuscleRole
import com.smarttrainner.core.model.ExerciseSource
import com.smarttrainner.core.model.MuscleGroup
import java.util.Locale

@Composable
private fun isKoreanLocale(): Boolean =
    LocalConfiguration.current.locales[0].language.equals("ko", ignoreCase = true)

@Composable
internal fun Exercise.localizedName(): String =
    if (isKoreanLocale() || source != ExerciseSource.SYSTEM) name else id.value.toExerciseTitle()

@Composable
internal fun Exercise.localizedSummary(): String =
    if (isKoreanLocale() || source != ExerciseSource.SYSTEM) {
        summary
    } else {
        stringResource(
            R.string.exercise_summary_template,
            localizedName(),
            muscleGroup.localizedLabel().lowercase(Locale.ENGLISH),
            equipment.localizedLabel().lowercase(Locale.ENGLISH)
        )
    }

@Composable
internal fun Exercise.localizedStepItems(): List<LocalizedExerciseStep> {
    if (source != ExerciseSource.SYSTEM) {
        return instructions.mapIndexed { index, instruction ->
            localizedExerciseStep(
                label = stringResource(R.string.exercise_step_number, index + 1),
                instruction = instruction
            )
        }
    }

    val visuals = exerciseStepVisuals(id.value)
    if (visuals.isNotEmpty()) {
        val isKo = isKoreanLocale()
        if (exerciseUsesGeneratedTextBackedVisuals(id.value)) {
            val copy = if (isKo) {
                koreanSeedStepItems()
            } else {
                generatedEnglishStepItems(id.value)
            }
            return visuals.mapIndexed { index, visual ->
                copy.getOrNull(index) ?: localizedExerciseStep(
                    label = if (isKo) visual.koLabel else visual.enLabel,
                    instruction = if (isKo) visual.koInstruction else visual.enInstruction
                )
            }
        }
        return visuals.map { visual ->
            localizedExerciseStep(
                label = if (isKo) visual.koLabel else visual.enLabel,
                instruction = if (isKo) visual.koInstruction else visual.enInstruction
            )
        }
    }

    if (!isKoreanLocale()) {
        kettlebellEnglishStepItems(id.value)?.let { return it }
    }

    return if (isKoreanLocale()) {
        instructions.mapIndexed { index, instruction ->
            val label = stringResource(R.string.exercise_step_number, index + 1)
            localizedExerciseStep(label = label, instruction = instruction)
        }
    } else {
        listOf(
            LocalizedExerciseStep(
                label = stringResource(R.string.exercise_step_number, 1),
                instruction = stringResource(R.string.exercise_instruction_setup_template, localizedName())
            ),
            LocalizedExerciseStep(
                label = stringResource(R.string.exercise_step_number, 2),
                instruction = stringResource(R.string.exercise_instruction_move_template)
            ),
            LocalizedExerciseStep(
                label = stringResource(R.string.exercise_step_number, 3),
                instruction = stringResource(R.string.exercise_instruction_return_template)
            )
        )
    }
}

@Composable
private fun Exercise.koreanSeedStepItems(): List<LocalizedExerciseStep> =
    instructions.mapIndexed { index, instruction ->
        val label = instruction.substringBefore(":").takeIf { it != instruction }
            ?: instruction.substringBefore("：").takeIf { it != instruction }
            ?: stringResource(R.string.exercise_step_number, index + 1)
        localizedExerciseStep(label = label, instruction = instruction)
    }

private fun localizedExerciseStep(label: String, instruction: String): LocalizedExerciseStep =
    LocalizedExerciseStep(
        label = label,
        instruction = instructionWithoutRepeatedStepTitle(label, instruction)
    )

private fun generatedEnglishStepItems(exerciseId: String): List<LocalizedExerciseStep> =
    kettlebellEnglishStepItems(exerciseId) ?: when (exerciseId) {
        "dead_bug" -> listOf(
            LocalizedExerciseStep("Start tabletop", "Lie on your back, reach both arms over the shoulders, and keep hips and knees bent at 90 degrees."),
            LocalizedExerciseStep("Diagonal reach A", "Extend one arm overhead and the opposite leg toward the floor while the other arm and leg stay in tabletop."),
            LocalizedExerciseStep("Return tabletop", "Bring the moving arm and leg back slowly without lifting the lower back."),
            LocalizedExerciseStep("Diagonal reach B", "Repeat with the opposite arm and opposite leg, keeping the ribs down and pelvis quiet.")
        )
        else -> emptyList()
    }

@Composable
internal fun Exercise.localizedSafetyCues(): List<String> =
    if (isKoreanLocale() || source != ExerciseSource.SYSTEM) {
        safetyCues
    } else {
        kettlebellEnglishSafetyCues(id.value) ?: listOf(
            stringResource(R.string.exercise_safety_pain),
            stringResource(R.string.exercise_safety_control)
        )
    }

private fun kettlebellEnglishStepItems(exerciseId: String): List<LocalizedExerciseStep>? {
    fun steps(vararg items: Pair<String, String>) = items.map { (label, instruction) ->
        LocalizedExerciseStep(label = label, instruction = instruction)
    }
    return when (exerciseId) {
        "kettlebell_deadlift" -> steps(
            "Bell between feet" to "Place the kettlebell under your midline and press the whole foot into the floor.",
            "Hinge down" to "Slightly bend the knees, send the hips back, and keep the back neutral.",
            "Grip and brace" to "Pack the shoulders away from the ears and hold the handle firmly with both hands.",
            "Stand tall" to "Drive through the floor and keep the bell close as the hips and legs stand you up."
        )
        "kettlebell_romanian_deadlift" -> steps(
            "Tall start" to "Hold the kettlebell with both hands in front of the thighs and stand tall.",
            "Soft knees" to "Keep a small knee bend while bracing the trunk.",
            "Hips back" to "Slide the bell close to the legs until the hamstrings are loaded.",
            "Return" to "Press the floor away and squeeze the glutes to stand tall."
        )
        "kettlebell_sumo_deadlift" -> steps(
            "Wide stance" to "Open the toes slightly and center the kettlebell between the feet.",
            "Track knees" to "Keep the knees moving in the same direction as the toes.",
            "Tall grip" to "Reach down with a neutral spine and grip the handle.",
            "Vertical lift" to "Stand by pushing through the legs so the bell moves straight up and down."
        )
        "kettlebell_goblet_squat" -> steps(
            "Chest hold" to "Hold the kettlebell close to the chest with elbows near the body.",
            "Stack ribs" to "Brace the abs so the ribs stay stacked over the pelvis.",
            "Squat" to "Lower to a controlled depth with knees tracking over toes.",
            "Stand" to "Drive through the whole foot and keep the bell close as you stand."
        )
        "kettlebell_box_squat" -> steps(
            "Set box" to "Choose a stable box or bench height that allows a pain-free squat.",
            "Goblet hold" to "Hold the kettlebell close to the chest and set the feet.",
            "Light touch" to "Touch the box lightly without relaxing or collapsing onto it.",
            "Stand without bounce" to "Keep tension and stand by pressing through the whole foot."
        )
        "kettlebell_reverse_lunge" -> steps(
            "Choose hold" to "Hold the bell goblet-style or suitcase-style and stand tall.",
            "Right foot back" to "Step the right foot back while keeping weight over the front foot.",
            "Lower" to "Lower vertically with the front knee tracking over the toes.",
            "Alternate sides" to "Return to the start and repeat with the left foot for the same rep target."
        )
        "kettlebell_split_squat" -> steps(
            "Set stance" to "Fix a split stance and hold the kettlebell securely.",
            "Brace" to "Keep the pelvis square and the trunk quiet.",
            "Lower" to "Lower straight down while keeping the front foot grounded.",
            "Switch sides" to "Drive through the front foot, finish the set, then repeat the same reps on the other side."
        )
        "kettlebell_step_up" -> steps(
            "Check box" to "Use a stable box that is not too high for your hip and knee.",
            "Full foot" to "Place the full right foot on the box while the kettlebell stays quiet.",
            "Stand up" to "Drive through the top foot without bouncing from the floor leg.",
            "Step down" to "Step down under control and repeat the same reps on the other side."
        )
        "kettlebell_bent_over_row" -> steps(
            "Hinge" to "Hinge at the hips with a neutral spine.",
            "Bell under shoulder" to "Let the kettlebell hang below the shoulder without twisting.",
            "Row" to "Pull the elbow back toward the ribs.",
            "Lower" to "Lower the bell under control without swinging the torso."
        )
        "one_arm_kettlebell_row" -> steps(
            "Bench support" to "Support one hand and knee or foot on a bench.",
            "Square torso" to "Keep the shoulders and hips facing the floor.",
            "Pull" to "Row the kettlebell toward the ribs.",
            "Repeat other side" to "Lower without rotation, then complete the same reps on the other side."
        )
        "kettlebell_floor_press" -> steps(
            "Lie down" to "Lie on the floor with knees bent and the kettlebell stacked over the wrist.",
            "Set elbow" to "Find a comfortable elbow angle slightly away from the torso.",
            "Press" to "Press the kettlebell toward the ceiling while keeping the wrist straight.",
            "Switch sides" to "Lower until the elbow lightly touches the floor, then repeat the same reps on the other side."
        )
        "kettlebell_shoulder_press" -> steps(
            "Rack" to "Start in the rack position with a straight wrist and elbow near the body.",
            "Brace" to "Brace the abs and glutes so the ribs do not flare.",
            "Press" to "Press overhead without leaning back.",
            "Switch sides" to "Lower to the rack position, then repeat the same reps on the other side."
        )
        "half_kneeling_kettlebell_press" -> steps(
            "Half kneel" to "Set a half-kneeling stance with the pelvis square.",
            "Rack" to "Hold the kettlebell in a stable rack position.",
            "Press" to "Press overhead while the trunk stays vertical.",
            "Switch sides" to "Lower under control and repeat the same reps on the other side."
        )
        "kettlebell_halo" -> steps(
            "Light bell" to "Hold a light kettlebell upside down in front of the chest.",
            "Circle head" to "Circle it slowly around the head while the torso stays forward.",
            "Reverse direction" to "Complete the same number of reps in the opposite direction."
        )
        "kettlebell_suitcase_carry" -> steps(
            "Pick up" to "Pick up one kettlebell and stand tall.",
            "Stay vertical" to "Keep the ribs and pelvis stacked without leaning toward the bell.",
            "Walk" to "Walk with short controlled steps while the bell stays quiet.",
            "Switch sides" to "Set the bell down safely and repeat the same time or distance on the other side."
        )
        "kettlebell_farmer_carry" -> steps(
            "Set bells" to "Set two similar kettlebells beside the feet.",
            "Pick up" to "Hinge down and pick them up with a tall posture.",
            "Walk" to "Walk steadily without letting the bells swing.",
            "Set down" to "Hinge again and place the bells down quietly."
        )
        "kettlebell_rack_carry" -> steps(
            "Rack" to "Create a rack position with a straight wrist and the bell resting on the forearm.",
            "Brace" to "Keep the elbow close and the ribs down.",
            "Walk" to "Walk slowly while maintaining steady breathing.",
            "Switch sides" to "Set the bell down safely and repeat on the other side."
        )
        "two_hand_kettlebell_swing" -> steps(
            "Set bell" to "Place the bell in front of the feet and hinge to grip it with both hands.",
            "Hike pass" to "Hike the bell back between the legs to load the hamstrings.",
            "Hip snap" to "Snap the hips forward so the bell floats to chest height.",
            "Receive" to "Receive the bell by hinging again and continue with the same rhythm."
        )
        else -> null
    }
}

private fun kettlebellEnglishSafetyCues(exerciseId: String): List<String>? = when (exerciseId) {
    "kettlebell_deadlift" -> listOf("Raise the bell on a box if the lower back rounds.", "Do not pull the bell from far in front of the body.")
    "kettlebell_romanian_deadlift" -> listOf("Prioritize a neutral back over depth.", "Lower the load if the shoulders round forward.")
    "kettlebell_sumo_deadlift" -> listOf("Narrow the stance if the knees collapse inward.", "Do not start the lift by yanking with the lower back.")
    "kettlebell_goblet_squat" -> listOf("Reduce depth if the lower back rounds.", "Lower the weight if the bell drifts away from the chest.")
    "kettlebell_box_squat" -> listOf("Do not collapse onto the box.", "Lower the load if the torso folds forward.")
    "kettlebell_reverse_lunge" -> listOf("Regress to bodyweight if balance is unstable.", "Shorten the step or depth if the front knee hurts.")
    "kettlebell_split_squat" -> listOf("Adjust stance if the front knee collapses inward.", "Start light until balance is reliable.")
    "kettlebell_step_up" -> listOf("Do not use the floor leg to bounce upward.", "Stop if the box shifts or feels unstable.")
    "kettlebell_bent_over_row" -> listOf("Do not swing the torso to move the bell.", "Use a supported row if the hinge bothers the back.")
    "one_arm_kettlebell_row" -> listOf("Do not twist open to finish the rep.", "Keep the wrist neutral.")
    "kettlebell_floor_press" -> listOf("Do not let the bell pull the wrist backward.", "Reduce range if the front of the shoulder pinches.")
    "kettlebell_shoulder_press" -> listOf("Do not turn the press into a backbend.", "Use a lighter load or machine if the shoulder hurts.")
    "half_kneeling_kettlebell_press" -> listOf("Use padding if the knee is uncomfortable.", "Lower the load if the ribs flare.")
    "kettlebell_halo" -> listOf("Keep the load light and the motion smooth.", "Do not crane the neck or arch the lower back.")
    "kettlebell_suitcase_carry" -> listOf("Lower the load if the body leans toward the bell.", "Do not arch the lower back while walking.")
    "kettlebell_farmer_carry" -> listOf("Lower the load if the shoulders round forward.", "Do not round the back when setting the bells down.")
    "kettlebell_rack_carry" -> listOf("Re-rack if the wrist folds.", "Do not lean back to support the load.")
    "two_hand_kettlebell_swing" -> listOf("Do not squat the swing.", "Stop if the lower back is doing the work.", "Use this only after the hinge pattern is consistent.")
    else -> null
}

@Composable
internal fun MuscleGroup.localizedLabel(): String = stringResource(
    when (this) {
        MuscleGroup.LOWER_BODY -> R.string.exercise_muscle_lower_body
        MuscleGroup.BACK -> R.string.exercise_muscle_back
        MuscleGroup.CHEST -> R.string.exercise_muscle_chest
        MuscleGroup.SHOULDERS -> R.string.exercise_muscle_shoulders
        MuscleGroup.ARMS -> R.string.exercise_muscle_arms
        MuscleGroup.BICEPS -> R.string.exercise_muscle_biceps
        MuscleGroup.TRICEPS -> R.string.exercise_muscle_triceps
        MuscleGroup.FOREARMS -> R.string.exercise_muscle_forearms
        MuscleGroup.CORE -> R.string.exercise_muscle_core
        MuscleGroup.CARDIO -> R.string.exercise_muscle_cardio
        MuscleGroup.FULL_BODY -> R.string.exercise_muscle_full_body
    }
)

@Composable
internal fun ExerciseMuscleRole.localizedLabel(): String = stringResource(
    when (this) {
        ExerciseMuscleRole.PRIMARY -> R.string.exercise_muscle_role_primary
        ExerciseMuscleRole.SECONDARY -> R.string.exercise_muscle_role_secondary
    }
)

@Composable
internal fun ExerciseMovementPattern.localizedLabel(): String = stringResource(
    when (this) {
        ExerciseMovementPattern.SQUAT -> R.string.exercise_movement_squat
        ExerciseMovementPattern.LEG_PRESS -> R.string.exercise_movement_leg_press
        ExerciseMovementPattern.HINGE -> R.string.exercise_movement_hinge
        ExerciseMovementPattern.LUNGE -> R.string.exercise_movement_lunge
        ExerciseMovementPattern.STEP_UP -> R.string.exercise_movement_step_up
        ExerciseMovementPattern.HIP_EXTENSION -> R.string.exercise_movement_hip_extension
        ExerciseMovementPattern.KNEE_EXTENSION -> R.string.exercise_movement_knee_extension
        ExerciseMovementPattern.KNEE_FLEXION -> R.string.exercise_movement_knee_flexion
        ExerciseMovementPattern.CALF_RAISE -> R.string.exercise_movement_calf_raise
        ExerciseMovementPattern.VERTICAL_PULL -> R.string.exercise_movement_vertical_pull
        ExerciseMovementPattern.HORIZONTAL_PULL -> R.string.exercise_movement_horizontal_pull
        ExerciseMovementPattern.HORIZONTAL_PUSH -> R.string.exercise_movement_horizontal_push
        ExerciseMovementPattern.VERTICAL_PUSH -> R.string.exercise_movement_vertical_push
        ExerciseMovementPattern.CHEST_ISOLATION -> R.string.exercise_movement_chest_isolation
        ExerciseMovementPattern.SHOULDER_ISOLATION -> R.string.exercise_movement_shoulder_isolation
        ExerciseMovementPattern.ARM_ISOLATION -> R.string.exercise_movement_arm_isolation
        ExerciseMovementPattern.CORE_STABILITY -> R.string.exercise_movement_core_stability
        ExerciseMovementPattern.CORE_FLEXION -> R.string.exercise_movement_core_flexion
        ExerciseMovementPattern.CORE_ROTATION -> R.string.exercise_movement_core_rotation
        ExerciseMovementPattern.CARRY -> R.string.exercise_movement_carry
        ExerciseMovementPattern.CONDITIONING -> R.string.exercise_movement_conditioning
        ExerciseMovementPattern.CARDIO -> R.string.exercise_movement_cardio
        ExerciseMovementPattern.ACCESSORY -> R.string.exercise_movement_accessory
    }
)

@Composable
internal fun EquipmentType.localizedLabel(): String = stringResource(
    when (this) {
        EquipmentType.BODYWEIGHT -> R.string.exercise_equipment_bodyweight
        EquipmentType.DUMBBELL -> R.string.exercise_equipment_dumbbell
        EquipmentType.KETTLEBELL -> R.string.exercise_equipment_kettlebell
        EquipmentType.BARBELL -> R.string.exercise_equipment_barbell
        EquipmentType.MACHINE -> R.string.exercise_equipment_machine
        EquipmentType.CABLE -> R.string.exercise_equipment_cable
        EquipmentType.BENCH -> R.string.exercise_equipment_bench
        EquipmentType.CARDIO_MACHINE -> R.string.exercise_equipment_cardio_machine
    }
)

@Composable
internal fun DifficultyLevel.localizedLabel(): String = stringResource(
    when (this) {
        DifficultyLevel.BEGINNER -> R.string.exercise_difficulty_beginner
        DifficultyLevel.INTERMEDIATE -> R.string.exercise_difficulty_intermediate
        DifficultyLevel.ADVANCED -> R.string.exercise_difficulty_advanced
    }
)

private fun String.toExerciseTitle(): String =
    split("_", "-")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.ENGLISH) else char.toString()
            }
        }
