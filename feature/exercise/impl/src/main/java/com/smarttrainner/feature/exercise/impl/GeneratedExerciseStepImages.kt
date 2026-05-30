package com.smarttrainner.feature.exercise.impl

import androidx.annotation.DrawableRes

internal val GENERATED_EXERCISE_THUMBNAIL_DRAWABLES: Map<String, Int> = mapOf(
    "dead_bug" to R.drawable.exercise_thumbnail_dead_bug_clean_v15,
    "kettlebell_deadlift" to R.drawable.exercise_thumbnail_kettlebell_deadlift_clean_v2,
    "kettlebell_romanian_deadlift" to R.drawable.exercise_thumbnail_kettlebell_romanian_deadlift_clean_v2,
    "kettlebell_sumo_deadlift" to R.drawable.exercise_thumbnail_kettlebell_sumo_deadlift_clean_v2,
    "kettlebell_goblet_squat" to R.drawable.exercise_thumbnail_kettlebell_goblet_squat_clean_v2,
    "kettlebell_box_squat" to R.drawable.exercise_thumbnail_kettlebell_box_squat_clean_v2,
    "kettlebell_reverse_lunge" to R.drawable.exercise_thumbnail_kettlebell_reverse_lunge_clean_v3,
    "kettlebell_split_squat" to R.drawable.exercise_thumbnail_kettlebell_split_squat_clean_v2,
    "kettlebell_step_up" to R.drawable.exercise_thumbnail_kettlebell_step_up_clean_v2,
    "kettlebell_bent_over_row" to R.drawable.exercise_thumbnail_kettlebell_bent_over_row_clean_v3,
    "one_arm_kettlebell_row" to R.drawable.exercise_thumbnail_one_arm_kettlebell_row_clean_v6,
    "kettlebell_floor_press" to R.drawable.exercise_thumbnail_kettlebell_floor_press_clean_v6,
    "kettlebell_shoulder_press" to R.drawable.exercise_thumbnail_kettlebell_shoulder_press_clean_v5,
    "half_kneeling_kettlebell_press" to R.drawable.exercise_thumbnail_half_kneeling_kettlebell_press_clean_v5,
    "kettlebell_halo" to R.drawable.exercise_thumbnail_kettlebell_halo_clean_v6,
    "kettlebell_suitcase_carry" to R.drawable.exercise_thumbnail_kettlebell_suitcase_carry_clean_v3,
    "kettlebell_farmer_carry" to R.drawable.exercise_thumbnail_kettlebell_farmer_carry_clean_v3,
    "kettlebell_rack_carry" to R.drawable.exercise_thumbnail_kettlebell_rack_carry_clean_v2,
    "two_hand_kettlebell_swing" to R.drawable.exercise_thumbnail_two_hand_kettlebell_swing_clean_v11
)

internal val GENERATED_EXERCISE_STEP_VISUALS: Map<String, List<ExerciseStepVisual>> = mapOf(
    "dead_bug" to steps(
        R.drawable.exercise_dead_bug_clean_v15_step_1 to labels("시작 자세", "Start tabletop"),
        R.drawable.exercise_dead_bug_clean_v15_step_2 to labels("한쪽 팔·반대 다리 뻗기", "Reach side A"),
        R.drawable.exercise_dead_bug_clean_v15_step_1 to labels("시작 자세 복귀", "Return tabletop"),
        R.drawable.exercise_dead_bug_clean_v15_step_3 to labels("반대쪽 팔·다리 뻗기", "Reach side B")
    ),
    "kettlebell_deadlift" to steps(
        R.drawable.exercise_kettlebell_deadlift_clean_v2_step_1 to labels("발 사이 벨 세팅", "Bell between feet"),
        R.drawable.exercise_kettlebell_deadlift_clean_v2_step_2 to labels("힙힌지로 내려가기", "Hip hinge down"),
        R.drawable.exercise_kettlebell_deadlift_clean_v2_step_3 to labels("등 중립으로 잡기", "Grip with neutral back"),
        R.drawable.exercise_kettlebell_deadlift_clean_v2_step_4 to labels("바닥 밀며 일어서기", "Stand tall")
    ),
    "kettlebell_romanian_deadlift" to steps(
        R.drawable.exercise_kettlebell_romanian_deadlift_clean_v2_step_1 to labels("서서 벨 잡기", "Tall start"),
        R.drawable.exercise_kettlebell_romanian_deadlift_clean_v2_step_2 to labels("무릎 살짝 굽힘", "Soft knees"),
        R.drawable.exercise_kettlebell_romanian_deadlift_clean_v2_step_3 to labels("엉덩이 뒤로 보내기", "Hinge back"),
        R.drawable.exercise_kettlebell_romanian_deadlift_clean_v2_step_4 to labels("둔근으로 복귀", "Return with glutes")
    ),
    "kettlebell_sumo_deadlift" to steps(
        R.drawable.exercise_kettlebell_sumo_deadlift_clean_v2_step_1 to labels("넓은 스탠스", "Wide stance"),
        R.drawable.exercise_kettlebell_sumo_deadlift_clean_v2_step_2 to labels("무릎·발끝 정렬", "Align knees and toes"),
        R.drawable.exercise_kettlebell_sumo_deadlift_clean_v2_step_3 to labels("수직으로 잡기", "Grip vertically"),
        R.drawable.exercise_kettlebell_sumo_deadlift_clean_v2_step_4 to labels("바닥 밀며 기립", "Stand through floor")
    ),
    "kettlebell_goblet_squat" to steps(
        R.drawable.exercise_kettlebell_goblet_squat_clean_v2_step_1 to labels("벨 가슴 앞 고정", "Chest hold"),
        R.drawable.exercise_kettlebell_goblet_squat_clean_v2_step_2 to labels("갈비뼈와 코어 정렬", "Brace ribs and core"),
        R.drawable.exercise_kettlebell_goblet_squat_clean_v2_step_3 to labels("앉기", "Squat down"),
        R.drawable.exercise_kettlebell_goblet_squat_clean_v2_step_4 to labels("발바닥으로 상승", "Stand through feet")
    ),
    "kettlebell_box_squat" to steps(
        R.drawable.exercise_kettlebell_box_squat_clean_v2_step_1 to labels("박스 높이 확인", "Check box height"),
        R.drawable.exercise_kettlebell_box_squat_clean_v2_step_2 to labels("고블릿 홀드", "Goblet hold"),
        R.drawable.exercise_kettlebell_box_squat_clean_v2_step_3 to labels("가볍게 터치", "Light box touch"),
        R.drawable.exercise_kettlebell_box_squat_clean_v2_step_4 to labels("반동 없이 일어서기", "Stand without bounce")
    ),
    "kettlebell_reverse_lunge" to steps(
        R.drawable.exercise_kettlebell_reverse_lunge_clean_v3_step_1 to labels("고블릿으로 준비", "Goblet setup"),
        R.drawable.exercise_kettlebell_reverse_lunge_clean_v3_step_2 to labels("오른발 뒤로 내려가기", "Lower with right foot back"),
        R.drawable.exercise_kettlebell_reverse_lunge_clean_v3_step_3 to labels("원위치 복귀", "Return to stand"),
        R.drawable.exercise_kettlebell_reverse_lunge_clean_v3_step_4 to labels("왼발 뒤로 내려가기", "Lower with left foot back")
    ),
    "kettlebell_split_squat" to steps(
        R.drawable.exercise_kettlebell_split_squat_clean_v2_step_1 to labels("보폭 고정", "Set stance"),
        R.drawable.exercise_kettlebell_split_squat_clean_v2_step_2 to labels("몸통 세우기", "Brace tall torso"),
        R.drawable.exercise_kettlebell_split_squat_clean_v2_step_3 to labels("수직 하강", "Lower straight down"),
        R.drawable.exercise_kettlebell_split_squat_clean_v2_step_4 to labels("앞발로 밀기", "Press through front foot")
    ),
    "kettlebell_step_up" to steps(
        R.drawable.exercise_kettlebell_step_up_clean_v2_step_1 to labels("박스 높이 확인", "Check box height"),
        R.drawable.exercise_kettlebell_step_up_clean_v2_step_2 to labels("한발 전체 올리기", "Full foot on box"),
        R.drawable.exercise_kettlebell_step_up_clean_v2_step_3 to labels("위로 서기", "Stand on box"),
        R.drawable.exercise_kettlebell_step_up_clean_v2_step_4 to labels("천천히 내려오기", "Controlled step down")
    ),
    "kettlebell_bent_over_row" to steps(
        R.drawable.exercise_kettlebell_bent_over_row_clean_v3_step_1 to labels("힙힌지 자세", "Hip hinge"),
        R.drawable.exercise_kettlebell_bent_over_row_clean_v3_step_2 to labels("벨 아래 정렬", "Bell under shoulders"),
        R.drawable.exercise_kettlebell_bent_over_row_clean_v3_step_3 to labels("팔꿈치 뒤로 당기기", "Row elbows back"),
        R.drawable.exercise_kettlebell_bent_over_row_clean_v3_step_4 to labels("통제 하강", "Controlled lower")
    ),
    "one_arm_kettlebell_row" to steps(
        R.drawable.exercise_one_arm_kettlebell_row_clean_v6_step_1 to labels("벤치 지지", "Bench support"),
        R.drawable.exercise_one_arm_kettlebell_row_clean_v6_step_2 to labels("몸통 고정", "Brace torso"),
        R.drawable.exercise_one_arm_kettlebell_row_clean_v6_step_3 to labels("벨 당기기", "Row the bell"),
        R.drawable.exercise_one_arm_kettlebell_row_clean_v6_step_4 to labels("회전 없이 내리기", "Lower without twist")
    ),
    "kettlebell_floor_press" to steps(
        R.drawable.exercise_kettlebell_floor_press_clean_v6_step_1 to labels("바닥에 눕기", "Lie down"),
        R.drawable.exercise_kettlebell_floor_press_clean_v6_step_2 to labels("팔꿈치 각도 잡기", "Set elbow angle"),
        R.drawable.exercise_kettlebell_floor_press_clean_v6_step_3 to labels("밀어 올리기", "Press up"),
        R.drawable.exercise_kettlebell_floor_press_clean_v6_step_4 to labels("팔꿈치 터치", "Touch elbow down")
    ),
    "kettlebell_shoulder_press" to steps(
        R.drawable.exercise_kettlebell_shoulder_press_clean_v5_step_1 to labels("랙 포지션", "Rack position"),
        R.drawable.exercise_kettlebell_shoulder_press_clean_v5_step_2 to labels("복부 고정", "Brace core"),
        R.drawable.exercise_kettlebell_shoulder_press_clean_v5_step_3 to labels("수직 프레스", "Vertical press"),
        R.drawable.exercise_kettlebell_shoulder_press_clean_v5_step_4 to labels("통제 하강", "Controlled lower")
    ),
    "half_kneeling_kettlebell_press" to steps(
        R.drawable.exercise_half_kneeling_kettlebell_press_clean_v5_step_1 to labels("반무릎 세팅", "Half-kneeling setup"),
        R.drawable.exercise_half_kneeling_kettlebell_press_clean_v5_step_2 to labels("랙 포지션", "Rack position"),
        R.drawable.exercise_half_kneeling_kettlebell_press_clean_v5_step_3 to labels("머리 위 프레스", "Overhead press"),
        R.drawable.exercise_half_kneeling_kettlebell_press_clean_v5_step_4 to labels("어깨로 복귀", "Return to shoulder")
    ),
    "kettlebell_halo" to steps(
        R.drawable.exercise_kettlebell_halo_clean_v6_step_1 to labels("가벼운 벨", "Light bell"),
        R.drawable.exercise_kettlebell_halo_clean_v6_step_2 to labels("머리 뒤로 돌리기", "Circle behind head"),
        R.drawable.exercise_kettlebell_halo_clean_v6_step_3 to labels("가슴 앞으로 복귀", "Return to chest")
    ),
    "kettlebell_suitcase_carry" to steps(
        R.drawable.exercise_kettlebell_suitcase_carry_clean_v3_step_1 to labels("한손 벨 들기", "Pick up"),
        R.drawable.exercise_kettlebell_suitcase_carry_clean_v3_step_2 to labels("몸통 수직 유지", "Stand vertical"),
        R.drawable.exercise_kettlebell_suitcase_carry_clean_v3_step_3 to labels("천천히 걷기", "Walk slowly"),
        R.drawable.exercise_kettlebell_suitcase_carry_clean_v3_step_4 to labels("반대쪽 반복", "Switch sides")
    ),
    "kettlebell_farmer_carry" to steps(
        R.drawable.exercise_kettlebell_farmer_carry_clean_v3_step_1 to labels("양손 벨 세팅", "Set two bells"),
        R.drawable.exercise_kettlebell_farmer_carry_clean_v3_step_2 to labels("키 크게 서기", "Stand tall"),
        R.drawable.exercise_kettlebell_farmer_carry_clean_v3_step_3 to labels("흔들림 없이 걷기", "Walk without swing"),
        R.drawable.exercise_kettlebell_farmer_carry_clean_v3_step_4 to labels("안전하게 내려놓기", "Set down safely")
    ),
    "kettlebell_rack_carry" to steps(
        R.drawable.exercise_kettlebell_rack_carry_clean_v2_step_1 to labels("랙 포지션 만들기", "Rack position"),
        R.drawable.exercise_kettlebell_rack_carry_clean_v2_step_2 to labels("갈비뼈 내리기", "Ribs down"),
        R.drawable.exercise_kettlebell_rack_carry_clean_v2_step_3 to labels("짧게 걷기", "Short steps"),
        R.drawable.exercise_kettlebell_rack_carry_clean_v2_step_4 to labels("반대쪽 반복", "Switch sides")
    ),
    "two_hand_kettlebell_swing" to steps(
        R.drawable.exercise_two_hand_kettlebell_swing_clean_v11_step_1 to labels("벨 앞 세팅", "Bell in front"),
        R.drawable.exercise_two_hand_kettlebell_swing_clean_v11_step_2 to labels("하이크 패스", "Hike pass"),
        R.drawable.exercise_two_hand_kettlebell_swing_clean_v11_step_3 to labels("힙 스냅", "Hip snap"),
        R.drawable.exercise_two_hand_kettlebell_swing_clean_v11_step_4 to labels("힌지로 받기", "Catch in hinge")
    )
)

internal val GENERATED_EXERCISE_TEXT_BACKED_IDS: Set<String> = GENERATED_EXERCISE_STEP_VISUALS.keys

private data class StepLabels(
    val koLabel: String,
    val enLabel: String
)

private fun labels(koLabel: String, enLabel: String) = StepLabels(koLabel, enLabel)

private fun steps(vararg entries: Pair<Int, StepLabels>): List<ExerciseStepVisual> =
    entries.map { (drawableResId, copy) ->
        generatedStep(drawableResId, copy.koLabel, copy.enLabel)
    }

private fun generatedStep(
    @DrawableRes drawableResId: Int,
    koLabel: String,
    enLabel: String
): ExerciseStepVisual =
    ExerciseStepVisual(
        drawableResId = drawableResId,
        koLabel = koLabel,
        enLabel = enLabel,
        koInstruction = "$koLabel: 자세와 장비 위치를 유지하며 천천히 통제합니다.",
        enInstruction = "$enLabel: move with control while keeping posture and equipment position stable."
    )
