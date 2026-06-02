package com.smarttrainner.feature.routine.impl

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
import com.smarttrainner.core.model.WorkoutLog
import com.smarttrainner.core.model.WorkoutSetLog
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun LocalDate.dayOfWeekShort(): String =
    dayOfWeek.getDisplayName(TextStyle.SHORT, currentLocale())

@Composable
internal fun isKoreanLocale(): Boolean =
    currentLocale().language.equals("ko", ignoreCase = true)

@Composable
private fun currentLocale(): Locale {
    val locales = LocalConfiguration.current.locales
    return if (locales.isEmpty) {
        Locale.getDefault()
    } else {
        locales[0] ?: Locale.getDefault()
    }
}

@Composable
internal fun Exercise.localizedName(): String =
    if (isKoreanLocale()) name else id.value.toExerciseTitle()

@Composable
internal fun Exercise.localizedTargetText(): String {
    val reps = defaultRepRange
    return if (reps != null) {
        stringResource(
            R.string.routine_target_reps,
            defaultSets,
            reps.first,
            reps.last
        )
    } else {
        stringResource(R.string.routine_target_duration, defaultSets, defaultDurationMinutes ?: 10)
    }
}

@Composable
internal fun Exercise.localizedTrainingDisplayText(latestLog: WorkoutLog?): String =
    latestLog?.localizedRecordDisplayText()?.let { recordText ->
        stringResource(R.string.routine_latest_record, recordText)
    } ?: stringResource(R.string.routine_recommended_record, localizedTargetText())

@Composable
internal fun PlannedExercise.localizedTargetText(): String {
    val reps = repRange
    return if (reps != null) {
        stringResource(R.string.routine_target_reps, sets, reps.first, reps.last)
    } else {
        stringResource(R.string.routine_target_duration, sets, durationMinutes ?: 10)
    }
}

@Composable
internal fun PlannedExercise.localizedTrainingDisplayText(latestLog: WorkoutLog?): String =
    latestLog?.localizedRecordDisplayText()?.let { recordText ->
        stringResource(R.string.routine_latest_record, recordText)
    } ?: stringResource(
        R.string.routine_recommended_record,
        listOf(
            localizedTargetText(),
            stringResource(R.string.routine_rest, restSeconds)
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
        add(stringResource(R.string.routine_set_number, entries.size.coerceAtLeast(sets)))
        reps?.let { add(stringResource(R.string.routine_actual_reps, it)) }
        weights?.let { add(stringResource(R.string.routine_actual_weight, it)) }
        durations?.let { add(stringResource(R.string.routine_actual_duration, it)) }
        rests?.let { add(stringResource(R.string.routine_actual_rest, it)) }
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
    filter { it.exerciseId == exerciseId }.maxByOrNull { it.performedAt }

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
    "beginner-full-body-2day" -> R.string.routine_template_beginner_full_body_2day_name
    "beginner-full-body-3day" -> R.string.routine_template_beginner_full_body_3day_name
    "intermediate-balanced-4day" -> R.string.routine_template_intermediate_balanced_4day_name
    "intermediate-body-part-4day-30" -> R.string.routine_template_intermediate_body_part_4day_30_name
    "intermediate-body-part-4day" -> R.string.routine_template_intermediate_body_part_4day_name
    "intermediate-body-part-4day-60" -> R.string.routine_template_intermediate_body_part_4day_60_name
    "intermediate-body-part-5day" -> R.string.routine_template_intermediate_body_part_5day_name
    else -> null
}

@StringRes
internal fun routineTemplateDescriptionResource(templateId: String): Int? = when (templateId) {
    "beginner-full-body-2day" -> R.string.routine_template_beginner_full_body_2day_description
    "beginner-full-body-3day" -> R.string.routine_template_beginner_full_body_3day_description
    "intermediate-balanced-4day" -> R.string.routine_template_intermediate_balanced_4day_description
    "intermediate-body-part-4day-30" -> R.string.routine_template_intermediate_body_part_4day_30_description
    "intermediate-body-part-4day" -> R.string.routine_template_intermediate_body_part_4day_description
    "intermediate-body-part-4day-60" -> R.string.routine_template_intermediate_body_part_4day_60_description
    "intermediate-body-part-5day" -> R.string.routine_template_intermediate_body_part_5day_description
    else -> null
}

@Composable
internal fun String.localizedPlanDayTitle(): String =
    generatedKoreanDayNumber()?.let { dayNumber -> stringResource(R.string.routine_day_label, dayNumber) }
        ?: routineDayTitleResource(this)?.let { stringResource(it) }
        ?: this

@Composable
internal fun planDayDisplayTitle(title: String, dayNumber: Int): String =
    if (title.isBlank() || title.generatedKoreanDayNumber() == dayNumber) {
        stringResource(R.string.routine_day_label, dayNumber)
    } else {
        title.localizedPlanDayTitle()
    }

@Composable
internal fun planDayScheduleTitle(title: String, dayNumber: Int): String =
    if (title.isBlank() || title.generatedKoreanDayNumber() == dayNumber) {
        stringResource(R.string.routine_day_label, dayNumber)
    } else {
        stringResource(
            R.string.routine_routine_schedule_day_title,
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
    "입문 전신 A" -> R.string.routine_day_title_starter_full_body_a
    "입문 전신 B" -> R.string.routine_day_title_starter_full_body_b
    "전신 A" -> R.string.routine_day_title_full_body_a
    "전신 B" -> R.string.routine_day_title_full_body_b
    "전신 C" -> R.string.routine_day_title_full_body_c
    "상체 1" -> R.string.routine_day_title_upper_1
    "하체 1" -> R.string.routine_day_title_lower_1
    "상체 2" -> R.string.routine_day_title_upper_2
    "하체 2" -> R.string.routine_day_title_lower_2
    "등 집중" -> R.string.routine_day_title_back_focus
    "가슴 집중" -> R.string.routine_day_title_chest_focus
    "하체 집중" -> R.string.routine_day_title_lower_body_focus
    "어깨+팔 집중" -> R.string.routine_day_title_shoulders_arms_focus
    "어깨 집중" -> R.string.routine_day_title_shoulders_focus
    "팔+유산소" -> R.string.routine_day_title_arms_cardio
    else -> null
}

@Composable
internal fun String.localizedPlanFocus(): String =
    routineDayFocusResource(this)?.let { stringResource(it) } ?: this

@StringRes
internal fun routineDayFocusResource(focus: String): Int? = when (focus) {
    "기구 적응" -> R.string.routine_day_focus_machine_basics
    "균형과 후면" -> R.string.routine_day_focus_balance_posterior_chain
    "하체+밀기+당기기" -> R.string.routine_day_focus_lower_push_pull
    "스쿼트 패턴과 등" -> R.string.routine_day_focus_squat_pattern_back
    "어깨와 엉덩이" -> R.string.routine_day_focus_shoulders_glutes
    "기본 프레스와 로우" -> R.string.routine_day_focus_core_presses_rows
    "상체 균형" -> R.string.routine_day_focus_upper_balance
    "프레스와 힙힌지" -> R.string.routine_day_focus_press_hip_hinge
    "인클라인과 후면" -> R.string.routine_day_focus_incline_posterior
    "스쿼트와 둔근" -> R.string.routine_day_focus_squat_glutes
    "당기는 운동" -> R.string.routine_day_focus_pulling_workout
    "미는 운동" -> R.string.routine_day_focus_pushing_workout
    "스쿼트와 힙힌지" -> R.string.routine_day_focus_squat_hip_hinge
    "측면·후면 어깨와 팔" -> R.string.routine_day_focus_side_rear_delts_arms
    "측면·후면 어깨와 이두·삼두" -> R.string.routine_day_focus_side_rear_delts_biceps_triceps
    "프레스와 플라이" -> R.string.routine_day_focus_presses_flyes
    "수직·수평 당기기" -> R.string.routine_day_focus_vertical_horizontal_pulling
    "스쿼트와 후면" -> R.string.routine_day_focus_squat_posterior
    "측면·후면 우선" -> R.string.routine_day_focus_side_rear_delts_first
    "팔 보조와 컨디셔닝" -> R.string.routine_day_focus_arm_accessories_conditioning
    "이두·삼두 보조와 컨디셔닝" -> R.string.routine_day_focus_biceps_triceps_conditioning
    else -> null
}

@Composable
internal fun RoutineStructure.localizedLabel(): String = stringResource(
    when (this) {
        RoutineStructure.FULL_BODY -> R.string.routine_routine_structure_full_body
        RoutineStructure.BALANCED_SPLIT -> R.string.routine_routine_structure_balanced_split
        RoutineStructure.BODY_PART_SPLIT -> R.string.routine_routine_structure_body_part_split
    }
)

@Composable
internal fun RoutineFocus.localizedShortLabel(): String = stringResource(
    when (this) {
        RoutineFocus.FULL_BODY -> R.string.routine_muscle_full_body
        RoutineFocus.UPPER_BODY -> R.string.routine_muscle_upper_body
        RoutineFocus.PUSH -> R.string.routine_muscle_push
        RoutineFocus.PULL -> R.string.routine_muscle_pull
        RoutineFocus.CHEST -> R.string.routine_muscle_chest
        RoutineFocus.BACK -> R.string.routine_muscle_back
        RoutineFocus.LOWER_BODY -> R.string.routine_muscle_lower_body
        RoutineFocus.SHOULDERS -> R.string.routine_muscle_shoulders
        RoutineFocus.ARMS -> R.string.routine_muscle_arms
        RoutineFocus.BICEPS -> R.string.routine_muscle_biceps
        RoutineFocus.TRICEPS -> R.string.routine_muscle_triceps
        RoutineFocus.FOREARMS -> R.string.routine_muscle_forearms
        RoutineFocus.CARDIO_CONDITIONING -> R.string.routine_muscle_cardio
        RoutineFocus.CORE -> R.string.routine_muscle_core
    }
)

@Composable
internal fun RoutineFocus.localizedTodayFocusLabel(): String = stringResource(
    when (this) {
        RoutineFocus.FULL_BODY -> R.string.routine_today_focus_full_body
        RoutineFocus.UPPER_BODY -> R.string.routine_today_focus_upper_body
        RoutineFocus.PUSH -> R.string.routine_today_focus_push
        RoutineFocus.PULL -> R.string.routine_today_focus_pull
        RoutineFocus.CHEST -> R.string.routine_today_focus_chest
        RoutineFocus.BACK -> R.string.routine_today_focus_back
        RoutineFocus.LOWER_BODY -> R.string.routine_today_focus_lower_body
        RoutineFocus.SHOULDERS -> R.string.routine_today_focus_shoulders
        RoutineFocus.ARMS -> R.string.routine_today_focus_arms
        RoutineFocus.BICEPS -> R.string.routine_today_focus_biceps
        RoutineFocus.TRICEPS -> R.string.routine_today_focus_triceps
        RoutineFocus.FOREARMS -> R.string.routine_today_focus_forearms
        RoutineFocus.CARDIO_CONDITIONING -> R.string.routine_today_focus_cardio_conditioning
        RoutineFocus.CORE -> R.string.routine_today_focus_core
    }
)

@Composable
internal fun TrainingExperience.localizedLabel(): String = stringResource(
    when (this) {
        TrainingExperience.BEGINNER -> R.string.routine_experience_beginner
        TrainingExperience.INTERMEDIATE -> R.string.routine_experience_intermediate
        TrainingExperience.ADVANCED -> R.string.routine_experience_advanced
    }
)

@Composable
internal fun RoutineFeeling.localizedLabel(): String = stringResource(
    when (this) {
        RoutineFeeling.BALANCED_FULL_BODY -> R.string.routine_feeling_balanced_full_body
        RoutineFeeling.FOCUSED_BODY_PART -> R.string.routine_feeling_focused_body_part
        RoutineFeeling.APP_RECOMMENDED -> R.string.routine_feeling_app_recommended
    }
)

@Composable
internal fun PlanTemplate.localizedMeta(): String =
    if (source == RoutineSource.CUSTOM) {
        stringResource(R.string.routine_custom_template_meta, days.size)
    } else {
        stringResource(
            R.string.routine_template_meta,
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
                text = stringResource(R.string.routine_routine_flow_label),
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
                R.string.routine_routine_flow,
                labels.joinToString(separator = stringResource(R.string.routine_routine_flow_separator))
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
                R.string.routine_routine_schedule_day_title,
                dayNumber,
                focus.localizedTodayFocusLabel()
            )
        } ?: planDayScheduleTitle(title, dayNumber)
    } else {
        val primaryFocusLabel = primaryFocus?.localizedTodayFocusLabel()
        val planFocusLabel = focus.takeIf { it.isNotBlank() }?.localizedPlanFocus()
        stringResource(
            R.string.routine_routine_preview_day_title,
            dayNumber,
            primaryFocusLabel ?: planFocusLabel ?: planDayDisplayTitle(title, dayNumber),
            planFocusLabel ?: primaryFocusLabel ?: planDayDisplayTitle(title, dayNumber)
        )
    }

@Composable
internal fun MuscleGroup.localizedLabel(): String = stringResource(
    when (this) {
        MuscleGroup.LOWER_BODY -> R.string.routine_muscle_lower_body
        MuscleGroup.BACK -> R.string.routine_muscle_back
        MuscleGroup.CHEST -> R.string.routine_muscle_chest
        MuscleGroup.SHOULDERS -> R.string.routine_muscle_shoulders
        MuscleGroup.ARMS -> R.string.routine_muscle_arms
        MuscleGroup.BICEPS -> R.string.routine_muscle_biceps
        MuscleGroup.TRICEPS -> R.string.routine_muscle_triceps
        MuscleGroup.FOREARMS -> R.string.routine_muscle_forearms
        MuscleGroup.CORE -> R.string.routine_muscle_core
        MuscleGroup.CARDIO -> R.string.routine_muscle_cardio
        MuscleGroup.FULL_BODY -> R.string.routine_muscle_full_body
    }
)

@Composable
internal fun PlanLevel.localizedLabel(): String = stringResource(
    when (this) {
        PlanLevel.INTRO -> R.string.routine_level_intro
        PlanLevel.BEGINNER -> R.string.routine_level_beginner
        PlanLevel.INTERMEDIATE -> R.string.routine_level_intermediate
        PlanLevel.ADVANCED -> R.string.routine_level_advanced
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
