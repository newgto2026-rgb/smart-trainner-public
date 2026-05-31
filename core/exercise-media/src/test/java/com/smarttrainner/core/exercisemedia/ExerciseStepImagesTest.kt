package com.smarttrainner.core.exercisemedia

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExerciseStepImagesTest {
    @Test
    fun everySeedExerciseIdHasVariableStepVisuals() {
        val expectedExerciseIds = setOf(
            "bodyweight_squat",
            "leg_press",
            "goblet_squat",
            "box_squat",
            "dumbbell_split_squat",
            "bulgarian_split_squat",
            "walking_lunge",
            "leg_extension",
            "leg_curl",
            "romanian_deadlift",
            "hip_thrust",
            "calf_raise",
            "lat_pulldown",
            "seated_cable_row",
            "chest_supported_row",
            "one_arm_dumbbell_row",
            "assisted_pullup",
            "pullup",
            "face_pull",
            "machine_chest_press",
            "dumbbell_bench_press",
            "incline_dumbbell_press",
            "pushup",
            "cable_fly",
            "machine_shoulder_press",
            "dumbbell_shoulder_press",
            "dumbbell_lateral_raise",
            "rear_delt_machine",
            "triceps_pushdown",
            "overhead_triceps_extension",
            "dumbbell_curl",
            "cable_curl",
            "plank",
            "bird_dog",
            "pallof_press",
            "cable_woodchop",
            "treadmill_walk",
            "indoor_bike",
            "hack_squat",
            "smith_machine_squat",
            "barbell_back_squat",
            "barbell_bench_press",
            "conventional_deadlift",
            "barbell_romanian_deadlift",
            "barbell_overhead_press",
            "dumbbell_step_up",
            "glute_bridge",
            "cable_glute_kickback",
            "hip_abduction_machine",
            "hip_adduction_machine",
            "back_extension",
            "straight_arm_pulldown",
            "machine_row",
            "t_bar_row",
            "barbell_bent_over_row",
            "cable_pullover",
            "inverted_row",
            "dumbbell_shrug",
            "pec_deck_fly",
            "incline_machine_press",
            "dumbbell_floor_press",
            "assisted_dip",
            "dip",
            "cable_chest_press",
            "close_grip_pushup",
            "arnold_press",
            "front_raise",
            "cable_lateral_raise",
            "landmine_press",
            "prone_y_raise",
            "hammer_curl",
            "preacher_curl_machine",
            "rope_overhead_triceps",
            "reverse_curl",
            "side_plank",
            "reverse_crunch",
            "cable_crunch",
            "hanging_knee_raise",
            "mountain_climber",
            "farmer_carry",
            "elliptical",
            "stair_climber",
            "rowing_machine",
            "battle_rope",
            "sled_push",
            "dumbbell_deadlift",
            "medicine_ball_slam",
            "dead_bug",
            "kettlebell_deadlift",
            "kettlebell_romanian_deadlift",
            "kettlebell_sumo_deadlift",
            "kettlebell_goblet_squat",
            "kettlebell_box_squat",
            "kettlebell_reverse_lunge",
            "kettlebell_split_squat",
            "kettlebell_step_up",
            "kettlebell_bent_over_row",
            "one_arm_kettlebell_row",
            "kettlebell_floor_press",
            "kettlebell_shoulder_press",
            "half_kneeling_kettlebell_press",
            "kettlebell_halo",
            "kettlebell_suitcase_carry",
            "kettlebell_farmer_carry",
            "kettlebell_rack_carry",
            "two_hand_kettlebell_swing"
        )

        assertThat(exerciseStepVisualExerciseIds).containsExactlyElementsIn(expectedExerciseIds)
        expectedExerciseIds.forEach { exerciseId ->
            val visuals = exerciseStepVisuals(exerciseId)

            assertThat(visuals.size).isAtLeast(2)
            assertThat(visuals.size).isAtMost(5)
            if (exerciseId != "dead_bug") {
                assertThat(visuals.map { it.drawableResId }.distinct()).hasSize(visuals.size)
            }
            visuals.forEach { visual ->
                assertThat(visual.koLabel.trim()).isNotEmpty()
                assertThat(visual.enLabel.trim()).isNotEmpty()
                assertThat(visual.koInstruction.trim()).isNotEmpty()
                assertThat(visual.enInstruction.trim()).isNotEmpty()
            }
        }
    }

    @Test
    fun trainerAuditExamplesKeepTheirExpectedStepCounts() {
        assertThat(exerciseStepVisuals("dumbbell_curl")).hasSize(2)
        assertThat(exerciseStepVisuals("leg_press")).hasSize(3)
        assertThat(exerciseStepVisuals("goblet_squat")).hasSize(4)
        assertThat(exerciseStepVisuals("bodyweight_squat")).hasSize(4)
        assertThat(exerciseStepVisuals("bulgarian_split_squat")).hasSize(4)
        assertThat(exerciseStepVisuals("romanian_deadlift")).hasSize(5)
        assertThat(exerciseStepVisuals("dead_bug")).hasSize(4)
        assertThat(exerciseStepVisuals("pullup")).hasSize(4)
        assertThat(exerciseStepVisuals("dip")).hasSize(4)
        assertThat(exerciseStepVisuals("barbell_back_squat")).hasSize(5)
        assertThat(exerciseStepVisuals("barbell_bench_press")).hasSize(4)
        assertThat(exerciseStepVisuals("conventional_deadlift")).hasSize(5)
        assertThat(exerciseStepVisuals("barbell_romanian_deadlift")).hasSize(4)
        assertThat(exerciseStepVisuals("dumbbell_deadlift")).hasSize(4)
        assertThat(exerciseStepVisuals("dumbbell_floor_press")).hasSize(4)
        assertThat(exerciseStepVisuals("elliptical")).hasSize(3)
        assertThat(exerciseStepVisuals("front_raise")).hasSize(4)
        assertThat(exerciseStepVisuals("hack_squat")).hasSize(4)
        assertThat(exerciseStepVisuals("incline_machine_press")).hasSize(3)
        assertThat(exerciseStepVisuals("landmine_press")).hasSize(4)
        assertThat(exerciseStepVisuals("overhead_triceps_extension")).hasSize(4)
        assertThat(exerciseStepVisuals("prone_y_raise")).hasSize(3)
        assertThat(exerciseStepVisuals("rope_overhead_triceps")).hasSize(3)
        assertThat(exerciseStepVisuals("sled_push")).hasSize(4)
        assertThat(exerciseStepVisuals("smith_machine_squat")).hasSize(4)
        assertThat(exerciseStepVisuals("barbell_overhead_press")).hasSize(4)
        assertThat(exerciseStepVisuals("barbell_bent_over_row")).hasSize(4)
        assertThat(exerciseStepVisuals("box_squat")).hasSize(4)
        assertThat(exerciseStepVisuals("close_grip_pushup")).hasSize(4)
        assertThat(exerciseStepVisuals("lat_pulldown")).hasSize(4)
        assertThat(exerciseStepVisuals("mountain_climber")).hasSize(4)
        assertThat(exerciseStepVisuals("rowing_machine")).hasSize(4)
        assertThat(exerciseStepVisuals("stair_climber")).hasSize(3)
        assertThat(exerciseStepVisuals("kettlebell_halo")).hasSize(3)
        assertThat(exerciseStepVisuals("kettlebell_deadlift")).hasSize(4)
        assertThat(exerciseStepVisuals("two_hand_kettlebell_swing")).hasSize(4)
        assertThat(exerciseStepVisuals("medicine_ball_slam")).hasSize(4)
    }

    @Test
    fun directStepInstructionsDoNotUsePlaceholderCopy() {
        val placeholderPhrases = listOf(
            "장비와 몸의 기준점을 맞춘 뒤 호흡을 정리합니다.",
            "움직임을 작게 시작해 균형과 속도를 먼저 통제합니다.",
            "통증 없는 범위에서 천천히 움직이고 반동을 쓰지 않습니다.",
            "목표 근육에 힘을 유지하고 몸통이 흔들리지 않게 움직입니다.",
            "목과 허리에 과한 긴장이 들어가지 않게 안정적으로 유지합니다.",
            "같은 경로로 천천히 돌아와 다음 반복을 준비합니다."
        )
        val directExerciseIds = exerciseStepVisualExerciseIds - GENERATED_EXERCISE_TEXT_BACKED_IDS

        directExerciseIds.forEach { exerciseId ->
            exerciseStepVisuals(exerciseId).forEach { visual ->
                placeholderPhrases.forEach { placeholder ->
                    assertThat(visual.koInstruction).doesNotContain(placeholder)
                }
            }
        }
    }

    @Test
    fun deadBugUsesStartReachReturnOppositeReachFlow() {
        val visuals = exerciseStepVisuals("dead_bug")

        assertThat(visuals.map { it.drawableResId }).containsExactly(
            R.drawable.exercise_dead_bug_clean_v15_step_1,
            R.drawable.exercise_dead_bug_clean_v15_step_2,
            R.drawable.exercise_dead_bug_clean_v15_step_1,
            R.drawable.exercise_dead_bug_clean_v15_step_3
        ).inOrder()
        assertThat(visuals.map { it.koLabel }).containsExactly(
            "시작 자세",
            "한쪽 팔·반대 다리 뻗기",
            "시작 자세로 복귀",
            "반대쪽 팔·다리 뻗기"
        ).inOrder()
        assertThat(exerciseThumbnailDrawableResId("dead_bug"))
            .isEqualTo(R.drawable.exercise_thumbnail_dead_bug_clean_v15)
    }

    @Test
    fun visuallyRejectedAssetsStayQuarantined() {
        val quarantinedExerciseIds = emptySet<String>()

        quarantinedExerciseIds.forEach { exerciseId ->
            assertThat(exerciseArtNeedsQaReplacement(exerciseId)).isTrue()
            assertThat(exerciseThumbnailDrawableResId(exerciseId)).isNull()
        }
    }
}
