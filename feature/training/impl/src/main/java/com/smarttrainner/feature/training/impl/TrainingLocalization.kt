package com.smarttrainner.feature.training.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrainner.core.designsystem.SmartTrainnerColors
import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.PlannedExercise
import com.smarttrainner.core.model.RoutineFeeling
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineSource
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.WeeklyPlan
import com.smarttrainner.core.model.WeeklySummary
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun LocalDate.dayOfWeekShort(): String =
    if (isKoreanLocale()) {
        when (dayOfWeek.value) {
            1 -> "월"
            2 -> "화"
            3 -> "수"
            4 -> "목"
            5 -> "금"
            6 -> "토"
            else -> "일"
        }
    } else {
        dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    }

@Composable
internal fun isKoreanLocale(): Boolean =
    LocalConfiguration.current.locales[0].language.equals("ko", ignoreCase = true)

@Composable
internal fun Exercise.localizedName(): String =
    if (isKoreanLocale()) name else id.value.toExerciseTitle()

@Composable
internal fun Exercise.localizedSummary(): String =
    if (isKoreanLocale()) {
        summary
    } else {
        stringResource(
            R.string.training_exercise_summary_template,
            localizedName(),
            muscleGroup.localizedLabel().lowercase(Locale.ENGLISH),
            equipment.localizedLabel().lowercase(Locale.ENGLISH)
        )
    }

@Composable
internal fun Exercise.localizedInstructions(): List<String> =
    localizedStepItems().map { it.instruction }

@Composable
internal fun Exercise.localizedStepItems(): List<LocalizedExerciseStep> {
    val visuals = exerciseStepVisuals(id.value)
    if (visuals.isNotEmpty()) {
        val isKo = isKoreanLocale()
        if (id.value in GENERATED_EXERCISE_TEXT_BACKED_IDS) {
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
            val label = stringResource(R.string.training_step_number, index + 1)
            localizedExerciseStep(label = label, instruction = instruction)
        }
    } else {
        listOf(
            LocalizedExerciseStep(
                label = stringResource(R.string.training_step_number, 1),
                instruction = stringResource(R.string.training_instruction_setup_template, localizedName())
            ),
            LocalizedExerciseStep(
                label = stringResource(R.string.training_step_number, 2),
                instruction = stringResource(R.string.training_instruction_move_template)
            ),
            LocalizedExerciseStep(
                label = stringResource(R.string.training_step_number, 3),
                instruction = stringResource(R.string.training_instruction_return_template)
            )
        )
    }
}

internal fun Exercise.koreanSeedStepItems(): List<LocalizedExerciseStep> =
    instructions.mapIndexed { index, instruction ->
        val label = instruction.substringBefore(":").takeIf { it != instruction }
            ?: instruction.substringBefore("：").takeIf { it != instruction }
            ?: "${index + 1}단계"
        localizedExerciseStep(label = label, instruction = instruction)
    }

internal fun localizedExerciseStep(label: String, instruction: String): LocalizedExerciseStep =
    LocalizedExerciseStep(
        label = label,
        instruction = instructionWithoutRepeatedStepTitle(label, instruction)
    )

internal fun generatedEnglishStepItems(exerciseId: String): List<LocalizedExerciseStep> =
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
    if (isKoreanLocale()) {
        safetyCues
    } else {
        kettlebellEnglishSafetyCues(id.value) ?:
        listOf(
            stringResource(R.string.training_safety_pain),
            stringResource(R.string.training_safety_control)
        )
    }

internal fun kettlebellEnglishStepItems(exerciseId: String): List<LocalizedExerciseStep>? {
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

internal fun kettlebellEnglishSafetyCues(exerciseId: String): List<String>? = when (exerciseId) {
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
internal fun Exercise.localizedTargetText(): String {
    val reps = defaultRepRange
    return if (reps != null) {
        stringResource(
            R.string.training_target_reps,
            defaultSets,
            reps.first,
            reps.last
        )
    } else {
        stringResource(R.string.training_target_duration, defaultSets, defaultDurationMinutes ?: 10)
    }
}

@Composable
internal fun Exercise.localizedTrainingDisplayText(latestLog: WorkoutLog?): String =
    latestLog?.localizedRecordDisplayText()?.let { recordText ->
        stringResource(R.string.training_latest_record, recordText)
    } ?: stringResource(R.string.training_recommended_record, localizedTargetText())

@Composable
internal fun PlannedExercise.localizedTargetText(): String {
    val reps = repRange
    return if (reps != null) {
        stringResource(R.string.training_target_reps, sets, reps.first, reps.last)
    } else {
        stringResource(R.string.training_target_duration, sets, durationMinutes ?: 10)
    }
}

@Composable
internal fun PlannedExercise.localizedTrainingDisplayText(latestLog: WorkoutLog?): String =
    latestLog?.localizedRecordDisplayText()?.let { recordText ->
        stringResource(R.string.training_latest_record, recordText)
    } ?: stringResource(
        R.string.training_recommended_record,
        listOf(
            localizedTargetText(),
            stringResource(R.string.training_rest, restSeconds)
        ).joinToString(" · ")
    )

@Composable
internal fun WorkoutLog.localizedRecordDisplayText(): String {
    val entries = displaySetEntries()
    val reps = entries.mapNotNull { it.reps }.toCollapsedText()
    val weights = entries.mapNotNull { it.weightKg }.map { it.toRecordInput() }.toCollapsedText()
    val durations = entries.mapNotNull { it.durationMinutes }.toCollapsedText()
    val rests = entries.mapNotNull { it.restSeconds }.toCollapsedText()
    val parts = buildList {
        add(stringResource(R.string.training_set_number, entries.size.coerceAtLeast(sets)))
        reps?.let { add(stringResource(R.string.training_actual_reps, it)) }
        weights?.let { add(stringResource(R.string.training_actual_weight, it)) }
        durations?.let { add(stringResource(R.string.training_actual_duration, it)) }
        rests?.let { add(stringResource(R.string.training_actual_rest, it)) }
    }
    return parts.joinToString(" · ")
}

internal fun WorkoutLog.displaySetEntries(): List<WorkoutSetLog> =
    setEntries.takeIf { it.isNotEmpty() }
        ?: List(sets.coerceIn(1, 12)) { index ->
            WorkoutSetLog(
                order = index + 1,
                reps = reps,
                weightKg = weightKg,
                durationMinutes = durationMinutes
            )
        }

internal fun <T> List<T>.toCollapsedText(): String? =
    takeIf { it.isNotEmpty() }?.let { values ->
        val distinctValues = values.distinct()
        if (distinctValues.size == 1) distinctValues.single().toString() else values.joinToString("/")
    }

internal fun Double.toRecordInput(): String =
    if (rem(1.0) == 0.0) toLong().toString() else toString()

internal fun List<WorkoutLog>.latestForExercise(exerciseId: ExerciseId): WorkoutLog? =
    firstOrNull { it.exerciseId == exerciseId }
        ?: filter { it.exerciseId == exerciseId }.maxByOrNull { it.performedAt }

@Composable
internal fun WeeklyPlan.localizedName(): String =
    routineTemplateNameResource(templateId)?.let { stringResource(it) } ?: name

@Composable
internal fun PlanTemplate.localizedName(): String =
    routineTemplateNameResource(id)?.let { stringResource(it) }
        ?: if (isKoreanLocale()) name else id.toExerciseTitle()

@Composable
internal fun PlanTemplate.localizedDescription(): String =
    routineTemplateDescriptionResource(id)?.let { stringResource(it) } ?: description

@StringRes
internal fun routineTemplateNameResource(templateId: String): Int? = when (templateId) {
    "beginner-full-body-2day" -> R.string.training_template_beginner_full_body_2day_name
    "beginner-full-body-3day" -> R.string.training_template_beginner_full_body_3day_name
    "intermediate-balanced-4day" -> R.string.training_template_intermediate_balanced_4day_name
    "intermediate-body-part-4day-30" -> R.string.training_template_intermediate_body_part_4day_30_name
    "intermediate-body-part-4day" -> R.string.training_template_intermediate_body_part_4day_name
    "intermediate-body-part-4day-60" -> R.string.training_template_intermediate_body_part_4day_60_name
    "intermediate-body-part-5day" -> R.string.training_template_intermediate_body_part_5day_name
    else -> null
}

@StringRes
internal fun routineTemplateDescriptionResource(templateId: String): Int? = when (templateId) {
    "beginner-full-body-2day" -> R.string.training_template_beginner_full_body_2day_description
    "beginner-full-body-3day" -> R.string.training_template_beginner_full_body_3day_description
    "intermediate-balanced-4day" -> R.string.training_template_intermediate_balanced_4day_description
    "intermediate-body-part-4day-30" -> R.string.training_template_intermediate_body_part_4day_30_description
    "intermediate-body-part-4day" -> R.string.training_template_intermediate_body_part_4day_description
    "intermediate-body-part-4day-60" -> R.string.training_template_intermediate_body_part_4day_60_description
    "intermediate-body-part-5day" -> R.string.training_template_intermediate_body_part_5day_description
    else -> null
}

@Composable
internal fun String.localizedPlanDayTitle(): String =
    generatedKoreanDayNumber()?.let { dayNumber -> stringResource(R.string.training_day_label, dayNumber) }
        ?: routineDayTitleResource(this)?.let { stringResource(it) }
        ?: this

@Composable
internal fun planDayDisplayTitle(title: String, dayNumber: Int): String =
    if (title.isBlank() || title.generatedKoreanDayNumber() == dayNumber) {
        stringResource(R.string.training_day_label, dayNumber)
    } else {
        title.localizedPlanDayTitle()
    }

@Composable
internal fun planDayScheduleTitle(title: String, dayNumber: Int): String =
    if (title.isBlank() || title.generatedKoreanDayNumber() == dayNumber) {
        stringResource(R.string.training_day_label, dayNumber)
    } else {
        stringResource(
            R.string.training_routine_schedule_day_title,
            dayNumber,
            title.localizedPlanDayTitle()
        )
    }

internal fun String.hasMeaningfulPlanDayTitle(dayNumber: Int): Boolean =
    isNotBlank() && generatedKoreanDayNumber() != dayNumber

internal fun String.generatedKoreanDayNumber(): Int? {
    val trimmed = trim()
    if (!trimmed.endsWith("일차")) return null
    return trimmed.removeSuffix("일차").toIntOrNull()?.takeIf { it > 0 }
}

@StringRes
internal fun routineDayTitleResource(title: String): Int? = when (title) {
    "입문 전신 A" -> R.string.training_day_title_starter_full_body_a
    "입문 전신 B" -> R.string.training_day_title_starter_full_body_b
    "전신 A" -> R.string.training_day_title_full_body_a
    "전신 B" -> R.string.training_day_title_full_body_b
    "전신 C" -> R.string.training_day_title_full_body_c
    "상체 1" -> R.string.training_day_title_upper_1
    "하체 1" -> R.string.training_day_title_lower_1
    "상체 2" -> R.string.training_day_title_upper_2
    "하체 2" -> R.string.training_day_title_lower_2
    "등 집중" -> R.string.training_day_title_back_focus
    "가슴 집중" -> R.string.training_day_title_chest_focus
    "하체 집중" -> R.string.training_day_title_lower_body_focus
    "어깨+팔 집중" -> R.string.training_day_title_shoulders_arms_focus
    "어깨 집중" -> R.string.training_day_title_shoulders_focus
    "팔+유산소" -> R.string.training_day_title_arms_cardio
    else -> null
}

@Composable
internal fun String.localizedPlanFocus(): String =
    routineDayFocusResource(this)?.let { stringResource(it) } ?: this

@StringRes
internal fun routineDayFocusResource(focus: String): Int? = when (focus) {
    "기구 적응" -> R.string.training_day_focus_machine_basics
    "균형과 후면" -> R.string.training_day_focus_balance_posterior_chain
    "하체+밀기+당기기" -> R.string.training_day_focus_lower_push_pull
    "스쿼트 패턴과 등" -> R.string.training_day_focus_squat_pattern_back
    "어깨와 엉덩이" -> R.string.training_day_focus_shoulders_glutes
    "기본 프레스와 로우" -> R.string.training_day_focus_core_presses_rows
    "상체 균형" -> R.string.training_day_focus_upper_balance
    "프레스와 힙힌지" -> R.string.training_day_focus_press_hip_hinge
    "인클라인과 후면" -> R.string.training_day_focus_incline_posterior
    "스쿼트와 둔근" -> R.string.training_day_focus_squat_glutes
    "당기는 운동" -> R.string.training_day_focus_pulling_workout
    "미는 운동" -> R.string.training_day_focus_pushing_workout
    "스쿼트와 힙힌지" -> R.string.training_day_focus_squat_hip_hinge
    "측면·후면 어깨와 팔" -> R.string.training_day_focus_side_rear_delts_arms
    "측면·후면 어깨와 이두·삼두" -> R.string.training_day_focus_side_rear_delts_biceps_triceps
    "프레스와 플라이" -> R.string.training_day_focus_presses_flyes
    "수직·수평 당기기" -> R.string.training_day_focus_vertical_horizontal_pulling
    "스쿼트와 후면" -> R.string.training_day_focus_squat_posterior
    "측면·후면 우선" -> R.string.training_day_focus_side_rear_delts_first
    "팔 보조와 컨디셔닝" -> R.string.training_day_focus_arm_accessories_conditioning
    "이두·삼두 보조와 컨디셔닝" -> R.string.training_day_focus_biceps_triceps_conditioning
    else -> null
}

@Composable
internal fun WeeklySummary.localizedInsight(): String {
    if (isKoreanLocale()) return insight
    val weakestMuscle = MuscleGroup.entries
        .filterNot {
            it == MuscleGroup.CARDIO ||
                it == MuscleGroup.ARMS ||
                it == MuscleGroup.FULL_BODY
        }
        .minByOrNull { muscleBalance[it] ?: 0 }
    return when {
        plannedExerciseCount == 0 -> stringResource(R.string.training_insight_empty_plan)
        completedExerciseCount == 0 -> stringResource(R.string.training_insight_no_logs)
        completionRate >= 80 -> stringResource(R.string.training_insight_good_rate)
        totalVolumeKg > 0 && weakestMuscle != null -> stringResource(
            R.string.training_insight_balance,
            weakestMuscle.localizedLabel()
        )
        else -> stringResource(R.string.training_insight_steady)
    }
}

@Composable
internal fun RoutineStructure.localizedLabel(): String = stringResource(
    when (this) {
        RoutineStructure.FULL_BODY -> R.string.training_routine_structure_full_body
        RoutineStructure.BALANCED_SPLIT -> R.string.training_routine_structure_balanced_split
        RoutineStructure.BODY_PART_SPLIT -> R.string.training_routine_structure_body_part_split
    }
)

@Composable
internal fun RoutineFocus.localizedShortLabel(): String = stringResource(
    when (this) {
        RoutineFocus.FULL_BODY -> R.string.training_muscle_full_body
        RoutineFocus.UPPER_BODY -> R.string.training_muscle_upper_body
        RoutineFocus.PUSH -> R.string.training_muscle_push
        RoutineFocus.PULL -> R.string.training_muscle_pull
        RoutineFocus.CHEST -> R.string.training_muscle_chest
        RoutineFocus.BACK -> R.string.training_muscle_back
        RoutineFocus.LOWER_BODY -> R.string.training_muscle_lower_body
        RoutineFocus.SHOULDERS -> R.string.training_muscle_shoulders
        RoutineFocus.ARMS -> R.string.training_muscle_arms
        RoutineFocus.BICEPS -> R.string.training_muscle_biceps
        RoutineFocus.TRICEPS -> R.string.training_muscle_triceps
        RoutineFocus.FOREARMS -> R.string.training_muscle_forearms
        RoutineFocus.CARDIO_CONDITIONING -> R.string.training_muscle_cardio
        RoutineFocus.CORE -> R.string.training_muscle_core
    }
)

@Composable
internal fun RoutineFocus.localizedTodayFocusLabel(): String = stringResource(
    when (this) {
        RoutineFocus.FULL_BODY -> R.string.training_today_focus_full_body
        RoutineFocus.UPPER_BODY -> R.string.training_today_focus_upper_body
        RoutineFocus.PUSH -> R.string.training_today_focus_push
        RoutineFocus.PULL -> R.string.training_today_focus_pull
        RoutineFocus.CHEST -> R.string.training_today_focus_chest
        RoutineFocus.BACK -> R.string.training_today_focus_back
        RoutineFocus.LOWER_BODY -> R.string.training_today_focus_lower_body
        RoutineFocus.SHOULDERS -> R.string.training_today_focus_shoulders
        RoutineFocus.ARMS -> R.string.training_today_focus_arms
        RoutineFocus.BICEPS -> R.string.training_today_focus_biceps
        RoutineFocus.TRICEPS -> R.string.training_today_focus_triceps
        RoutineFocus.FOREARMS -> R.string.training_today_focus_forearms
        RoutineFocus.CARDIO_CONDITIONING -> R.string.training_today_focus_cardio_conditioning
        RoutineFocus.CORE -> R.string.training_today_focus_core
    }
)

@Composable
internal fun TrainingExperience.localizedLabel(): String = stringResource(
    when (this) {
        TrainingExperience.BEGINNER -> R.string.training_experience_beginner
        TrainingExperience.INTERMEDIATE -> R.string.training_experience_intermediate
    }
)

@Composable
internal fun RoutineFeeling.localizedLabel(): String = stringResource(
    when (this) {
        RoutineFeeling.BALANCED_FULL_BODY -> R.string.training_feeling_balanced_full_body
        RoutineFeeling.FOCUSED_BODY_PART -> R.string.training_feeling_focused_body_part
        RoutineFeeling.APP_RECOMMENDED -> R.string.training_feeling_app_recommended
    }
)

@Composable
internal fun PlanTemplate.localizedMeta(): String =
    if (source == RoutineSource.CUSTOM) {
        stringResource(R.string.training_custom_template_meta, days.size)
    } else {
        stringResource(
            R.string.training_template_meta,
            level.localizedLabel(),
            daysPerWeek,
            sessionMinutes
        )
    }

@Composable
internal fun RoutineFocusFlow(template: PlanTemplate) {
    val labels = template.focusFlowLabels()
    if (labels.isEmpty()) return
    if (template.source == RoutineSource.CUSTOM) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.training_routine_flow_label),
                color = SmartTrainnerColors.Muted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.testTag("training_routine_flow_${template.id}"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                labels.withIndex().chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowItems.forEach { item ->
                            RoutineFocusFlowChip(
                                label = item.value,
                                modifier = Modifier.testTag(
                                    "training_routine_flow_${template.id}_day_${item.index + 1}"
                                )
                            )
                        }
                    }
                }
            }
        }
    } else {
        Text(
            text = stringResource(
                R.string.training_routine_flow,
                labels.joinToString(separator = stringResource(R.string.training_routine_flow_separator))
            ),
            modifier = Modifier.testTag("training_routine_flow_${template.id}"),
            color = SmartTrainnerColors.Muted,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun RoutineFocusFlowChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SmartTrainnerColors.GreenSoft
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            color = SmartTrainnerColors.Ink,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun PlanTemplate.focusFlowLabels(): List<String> =
    days.mapNotNull { it.primaryFocus?.localizedTodayFocusLabel() }

@Composable
internal fun PlanTemplateDay.previewTitle(source: RoutineSource): String =
    if (source == RoutineSource.CUSTOM) {
        primaryFocus?.let { focus ->
            stringResource(
                R.string.training_routine_schedule_day_title,
                dayNumber,
                focus.localizedTodayFocusLabel()
            )
        } ?: planDayScheduleTitle(title, dayNumber)
    } else {
        stringResource(
            R.string.training_routine_preview_day_title,
            dayNumber,
            requireNotNull(primaryFocus).localizedTodayFocusLabel(),
            focus.localizedPlanFocus()
        )
    }

@Composable
internal fun MuscleGroup.localizedLabel(): String = stringResource(
    when (this) {
        MuscleGroup.LOWER_BODY -> R.string.training_muscle_lower_body
        MuscleGroup.BACK -> R.string.training_muscle_back
        MuscleGroup.CHEST -> R.string.training_muscle_chest
        MuscleGroup.SHOULDERS -> R.string.training_muscle_shoulders
        MuscleGroup.ARMS -> R.string.training_muscle_arms
        MuscleGroup.BICEPS -> R.string.training_muscle_biceps
        MuscleGroup.TRICEPS -> R.string.training_muscle_triceps
        MuscleGroup.FOREARMS -> R.string.training_muscle_forearms
        MuscleGroup.CORE -> R.string.training_muscle_core
        MuscleGroup.CARDIO -> R.string.training_muscle_cardio
        MuscleGroup.FULL_BODY -> R.string.training_muscle_full_body
    }
)

internal val armDetailGroups = listOf(
    MuscleGroup.BICEPS,
    MuscleGroup.TRICEPS,
    MuscleGroup.FOREARMS
)

@Composable
internal fun EquipmentType.localizedLabel(): String = stringResource(
    when (this) {
        EquipmentType.BODYWEIGHT -> R.string.training_equipment_bodyweight
        EquipmentType.DUMBBELL -> R.string.training_equipment_dumbbell
        EquipmentType.KETTLEBELL -> R.string.training_equipment_kettlebell
        EquipmentType.BARBELL -> R.string.training_equipment_barbell
        EquipmentType.MACHINE -> R.string.training_equipment_machine
        EquipmentType.CABLE -> R.string.training_equipment_cable
        EquipmentType.BENCH -> R.string.training_equipment_bench
        EquipmentType.CARDIO_MACHINE -> R.string.training_equipment_cardio_machine
    }
)

@Composable
internal fun DifficultyLevel.localizedLabel(): String = stringResource(
    when (this) {
        DifficultyLevel.BEGINNER -> R.string.training_difficulty_beginner
        DifficultyLevel.INTERMEDIATE -> R.string.training_difficulty_intermediate
        DifficultyLevel.ADVANCED -> R.string.training_difficulty_advanced
    }
)

@Composable
internal fun PlanLevel.localizedLabel(): String = stringResource(
    when (this) {
        PlanLevel.INTRO -> R.string.training_level_intro
        PlanLevel.BEGINNER -> R.string.training_level_beginner
        PlanLevel.INTERMEDIATE -> R.string.training_level_intermediate
    }
)

internal fun String.toExerciseTitle(): String =
    split('_', '-')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            when (word.lowercase(Locale.ENGLISH)) {
                "lat" -> "Lat"
                "rpe" -> "RPE"
                "y" -> "Y"
                "pushup" -> "Push-up"
                else -> word.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.ENGLISH) else char.toString()
                }
            }
        }
