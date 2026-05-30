package com.smarttrainner.feature.training.impl

import androidx.annotation.DrawableRes

internal data class ExerciseStepVisual(
    @DrawableRes val drawableResId: Int,
    val koLabel: String,
    val enLabel: String,
    val koInstruction: String,
    val enInstruction: String
)

internal val exerciseStepVisualExerciseIds: Set<String>
    get() = STEP_VISUALS.keys

internal fun exerciseStepVisuals(exerciseId: String): List<ExerciseStepVisual> =
    STEP_VISUALS[exerciseId].orEmpty()

internal fun exerciseArtNeedsQaReplacement(exerciseId: String): Boolean =
    exerciseId in ART_QA_REPLACEMENT_BLOCKLIST

@DrawableRes
internal fun exerciseThumbnailDrawableResId(exerciseId: String): Int? =
    if (exerciseId in THUMBNAIL_ASSET_BLOCKLIST || exerciseId in ART_QA_REPLACEMENT_BLOCKLIST) {
        null
    } else {
        THUMBNAIL_DRAWABLES[exerciseId]
    }

private val ART_QA_REPLACEMENT_BLOCKLIST = emptySet<String>()

private val THUMBNAIL_ASSET_BLOCKLIST = emptySet<String>()

private val THUMBNAIL_DRAWABLES: Map<String, Int> = GENERATED_EXERCISE_THUMBNAIL_DRAWABLES + mapOf(
    "arnold_press" to R.drawable.exercise_thumbnail_arnold_press_clean_v2,
    "assisted_dip" to R.drawable.exercise_thumbnail_assisted_dip_clean_v2,
    "assisted_pullup" to R.drawable.exercise_thumbnail_assisted_pullup_clean_v2,
    "back_extension" to R.drawable.exercise_thumbnail_back_extension_clean_v2,
    "barbell_back_squat" to R.drawable.exercise_thumbnail_barbell_back_squat_clean_v4,
    "barbell_bench_press" to R.drawable.exercise_thumbnail_barbell_bench_press_clean_v6,
    "barbell_bent_over_row" to R.drawable.exercise_thumbnail_barbell_bent_over_row_clean_v2,
    "barbell_overhead_press" to R.drawable.exercise_thumbnail_barbell_overhead_press_clean_v3,
    "battle_rope" to R.drawable.exercise_thumbnail_battle_rope_clean_v2,
    "bird_dog" to R.drawable.exercise_thumbnail_bird_dog_clean_v4,
    "box_squat" to R.drawable.exercise_thumbnail_box_squat_clean_v2,
    "cable_chest_press" to R.drawable.exercise_thumbnail_cable_chest_press_clean_v2,
    "cable_crunch" to R.drawable.exercise_thumbnail_cable_crunch_clean_v6,
    "cable_curl" to R.drawable.exercise_thumbnail_cable_curl_clean,
    "cable_fly" to R.drawable.exercise_thumbnail_cable_fly_clean,
    "cable_glute_kickback" to R.drawable.exercise_thumbnail_cable_glute_kickback_clean_v5,
    "cable_lateral_raise" to R.drawable.exercise_thumbnail_cable_lateral_raise_clean_v2,
    "cable_pullover" to R.drawable.exercise_thumbnail_cable_pullover_clean_v3,
    "cable_woodchop" to R.drawable.exercise_thumbnail_cable_woodchop_clean,
    "calf_raise" to R.drawable.exercise_thumbnail_calf_raise_clean,
    "chest_supported_row" to R.drawable.exercise_thumbnail_chest_supported_row_clean_v2,
    "close_grip_pushup" to R.drawable.exercise_thumbnail_close_grip_pushup_clean_v2,
    "conventional_deadlift" to R.drawable.exercise_thumbnail_conventional_deadlift_clean_v2,
    "dead_bug" to R.drawable.exercise_thumbnail_dead_bug_clean_v15,
    "dumbbell_bench_press" to R.drawable.exercise_thumbnail_dumbbell_bench_press_clean_v3,
    "dumbbell_curl" to R.drawable.exercise_thumbnail_dumbbell_curl_clean,
    "dumbbell_deadlift" to R.drawable.exercise_thumbnail_dumbbell_deadlift_clean_v2,
    "dumbbell_floor_press" to R.drawable.exercise_thumbnail_dumbbell_floor_press_clean_v2,
    "dumbbell_lateral_raise" to R.drawable.exercise_thumbnail_dumbbell_lateral_raise_clean,
    "dumbbell_shoulder_press" to R.drawable.exercise_thumbnail_dumbbell_shoulder_press_clean,
    "dumbbell_shrug" to R.drawable.exercise_thumbnail_dumbbell_shrug_clean_v2,
    "dumbbell_split_squat" to R.drawable.exercise_thumbnail_dumbbell_split_squat_clean,
    "dumbbell_step_up" to R.drawable.exercise_thumbnail_dumbbell_step_up_clean_v3,
    "elliptical" to R.drawable.exercise_thumbnail_elliptical_clean_v2,
    "face_pull" to R.drawable.exercise_thumbnail_face_pull_clean_v2,
    "farmer_carry" to R.drawable.exercise_thumbnail_farmer_carry_clean_v2,
    "front_raise" to R.drawable.exercise_thumbnail_front_raise_clean_v4,
    "glute_bridge" to R.drawable.exercise_thumbnail_glute_bridge_clean_v2,
    "goblet_squat" to R.drawable.exercise_thumbnail_goblet_squat_clean,
    "hack_squat" to R.drawable.exercise_thumbnail_hack_squat_clean_v4,
    "hammer_curl" to R.drawable.exercise_thumbnail_hammer_curl_clean_v2,
    "hanging_knee_raise" to R.drawable.exercise_thumbnail_hanging_knee_raise_clean_v2,
    "hip_abduction_machine" to R.drawable.exercise_thumbnail_hip_abduction_machine_clean_v3,
    "hip_adduction_machine" to R.drawable.exercise_thumbnail_hip_adduction_machine_clean_v3,
    "hip_thrust" to R.drawable.exercise_thumbnail_hip_thrust_clean_v8,
    "incline_dumbbell_press" to R.drawable.exercise_thumbnail_incline_dumbbell_press_clean_v4,
    "incline_machine_press" to R.drawable.exercise_thumbnail_incline_machine_press_clean_v2,
    "indoor_bike" to R.drawable.exercise_thumbnail_indoor_bike_clean_v2,
    "inverted_row" to R.drawable.exercise_thumbnail_inverted_row_clean_v2,
    "landmine_press" to R.drawable.exercise_thumbnail_landmine_press_clean_v2,
    "lat_pulldown" to R.drawable.exercise_thumbnail_lat_pulldown_clean_v4,
    "leg_curl" to R.drawable.exercise_thumbnail_leg_curl_clean,
    "leg_extension" to R.drawable.exercise_thumbnail_leg_extension_clean,
    "leg_press" to R.drawable.exercise_thumbnail_leg_press_clean,
    "machine_chest_press" to R.drawable.exercise_thumbnail_machine_chest_press_clean,
    "machine_row" to R.drawable.exercise_thumbnail_machine_row_clean_v3,
    "machine_shoulder_press" to R.drawable.exercise_thumbnail_machine_shoulder_press_clean_v2,
    "medicine_ball_slam" to R.drawable.exercise_thumbnail_medicine_ball_slam_clean_v3,
    "mountain_climber" to R.drawable.exercise_thumbnail_mountain_climber_clean_v2,
    "one_arm_dumbbell_row" to R.drawable.exercise_thumbnail_one_arm_dumbbell_row_clean_v2,
    "overhead_triceps_extension" to R.drawable.exercise_thumbnail_overhead_triceps_extension_clean_v2,
    "pallof_press" to R.drawable.exercise_thumbnail_pallof_press_clean_v5,
    "pec_deck_fly" to R.drawable.exercise_thumbnail_pec_deck_fly_clean_v3,
    "plank" to R.drawable.exercise_thumbnail_plank_clean,
    "preacher_curl_machine" to R.drawable.exercise_thumbnail_preacher_curl_machine_clean_v2,
    "prone_y_raise" to R.drawable.exercise_thumbnail_prone_y_raise_clean_v4,
    "pushup" to R.drawable.exercise_thumbnail_pushup_clean,
    "rear_delt_machine" to R.drawable.exercise_thumbnail_rear_delt_machine_clean,
    "reverse_crunch" to R.drawable.exercise_thumbnail_reverse_crunch_clean_v2,
    "reverse_curl" to R.drawable.exercise_thumbnail_reverse_curl_clean_v2,
    "romanian_deadlift" to R.drawable.exercise_thumbnail_romanian_deadlift_clean,
    "rope_overhead_triceps" to R.drawable.exercise_thumbnail_rope_overhead_triceps_clean_v3,
    "rowing_machine" to R.drawable.exercise_thumbnail_rowing_machine_clean_v2,
    "seated_cable_row" to R.drawable.exercise_thumbnail_seated_cable_row_clean_v5,
    "side_plank" to R.drawable.exercise_thumbnail_side_plank_clean_v2,
    "sled_push" to R.drawable.exercise_thumbnail_sled_push_clean_v3,
    "smith_machine_squat" to R.drawable.exercise_thumbnail_smith_machine_squat_clean_v6,
    "stair_climber" to R.drawable.exercise_thumbnail_stair_climber_clean_v2,
    "straight_arm_pulldown" to R.drawable.exercise_thumbnail_straight_arm_pulldown_clean_v3,
    "t_bar_row" to R.drawable.exercise_thumbnail_t_bar_row_clean_v2,
    "treadmill_walk" to R.drawable.exercise_thumbnail_treadmill_walk_clean_v2,
    "triceps_pushdown" to R.drawable.exercise_thumbnail_triceps_pushdown_clean,
    "walking_lunge" to R.drawable.exercise_thumbnail_walking_lunge_clean
)

private val STEP_VISUALS: Map<String, List<ExerciseStepVisual>> = GENERATED_EXERCISE_STEP_VISUALS + mapOf(
    "leg_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_leg_press_clean_step_1,
            koLabel = "패드 밀착",
            enLabel = "Brace on pad",
            koInstruction = "패드 밀착: 등과 엉덩이를 등받이에 붙이고 발바닥 전체가 플랫폼에 닿게 합니다.",
            enInstruction = "Keep your back and hips on the pad and place the whole foot on the platform."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_leg_press_clean_step_2,
            koLabel = "발 정렬",
            enLabel = "Align feet",
            koInstruction = "발 정렬: 무릎이 발끝 방향을 따라가도록 두고 복부에 힘을 줍니다.",
            enInstruction = "Align knees with toes and brace your trunk before pressing."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_leg_press_clean_step_3,
            koLabel = "밀고 복귀",
            enLabel = "Press and return",
            koInstruction = "밀고 복귀: 무릎을 잠그지 않고 밀었다가 허리와 엉덩이가 뜨지 않게 천천히 돌아옵니다.",
            enInstruction = "Press without locking the knees, then return slowly without letting hips or low back lift."
        )
    ),
    "goblet_squat" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_goblet_squat_clean_step_1,
            koLabel = "덤벨 위치",
            enLabel = "Set dumbbell",
            koInstruction = "덤벨 위치: 덤벨 한쪽 끝을 양손으로 감싸 가슴 앞에 세우고 팔꿈치를 몸 가까이 둡니다.",
            enInstruction = "Cup one end of the dumbbell in both hands, hold it upright at the chest, and keep elbows close."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_goblet_squat_clean_step_2,
            koLabel = "발·코어 정렬",
            enLabel = "Align feet and core",
            koInstruction = "발·코어 정렬: 발을 어깨너비로 두고 갈비뼈가 들리지 않게 복부를 조입니다.",
            enInstruction = "Set feet about shoulder-width and brace the abs so the ribs do not flare."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_goblet_squat_clean_step_3,
            koLabel = "내려가기",
            enLabel = "Lower",
            koInstruction = "내려가기: 무릎과 발끝 방향을 맞추며 덤벨이 가슴에서 멀어지지 않게 앉습니다.",
            enInstruction = "Lower while knees track with toes and the dumbbell stays close to the chest."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_goblet_squat_clean_step_4,
            koLabel = "올라오기",
            enLabel = "Stand up",
            koInstruction = "올라오기: 발바닥 전체로 바닥을 밀어 가슴과 골반이 함께 올라오게 합니다.",
            enInstruction = "Drive through the whole foot so the chest and hips rise together."
        )
    ),
    "box_squat" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_box_squat_clean_v2_step_1,
            koLabel = "박스 위치",
            enLabel = "Set box",
            koInstruction = "박스 위치: 엉덩이 바로 뒤에 박스나 벤치를 두고 발을 어깨너비로 섭니다.",
            enInstruction = "Stand with the box or bench directly behind the hips and feet about shoulder-width."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_box_squat_clean_v2_step_2,
            koLabel = "엉덩이 뒤로 보내기",
            enLabel = "Send hips back",
            koInstruction = "엉덩이 뒤로 보내기: 무릎이 발끝 방향을 따라가게 하며 엉덩이를 먼저 뒤로 보냅니다.",
            enInstruction = "Send the hips back first while the knees track in the same direction as the toes."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_box_squat_clean_v2_step_3,
            koLabel = "가볍게 터치",
            enLabel = "Light touch",
            koInstruction = "가볍게 터치: 박스에 털썩 앉지 말고 엉덩이만 살짝 닿은 상태로 긴장을 유지합니다.",
            enInstruction = "Lightly touch the box without relaxing fully or dropping the body weight onto it."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_box_squat_clean_v2_step_4,
            koLabel = "반동 없이 일어서기",
            enLabel = "Stand without bounce",
            koInstruction = "반동 없이 일어서기: 발바닥 전체로 바닥을 밀어 가슴과 엉덩이가 함께 올라오게 합니다.",
            enInstruction = "Drive through the whole foot so the chest and hips rise together without bouncing."
        )
    ),
    "dumbbell_split_squat" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_split_squat_clean_step_1,
            koLabel = "보폭 잡기",
            enLabel = "Set stance",
            koInstruction = "보폭 잡기: 양손 덤벨을 몸 옆에 두고 앞발과 뒷발을 나누어 안정적인 보폭을 만듭니다.",
            enInstruction = "Hold dumbbells at the sides and split the feet into a stable stance."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_split_squat_clean_step_2,
            koLabel = "앞발 중심",
            enLabel = "Load front foot",
            koInstruction = "앞발 중심: 몸통을 세우고 앞발 전체에 체중을 실어 뒤꿈치가 뜨지 않게 준비합니다.",
            enInstruction = "Keep the torso tall and load the whole front foot without letting the heel lift."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_split_squat_clean_step_3,
            koLabel = "내려가기",
            enLabel = "Lower",
            koInstruction = "내려가기: 뒤무릎을 바닥 가까이 내리며 앞무릎이 발끝 방향을 따라가게 합니다.",
            enInstruction = "Lower the back knee toward the floor while the front knee tracks with the toes."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_split_squat_clean_step_4,
            koLabel = "올라온 뒤 반대쪽",
            enLabel = "Drive up and switch",
            koInstruction = "앞발로 바닥을 밀어 올라온 뒤 같은 반복 수를 반대쪽 다리도 수행합니다.",
            enInstruction = "Drive through the front foot, then perform the same reps on the opposite side."
        )
    ),
    "walking_lunge" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_walking_lunge_clean_step_1,
            koLabel = "덤벨 들고 준비",
            enLabel = "Stand with dumbbells",
            koInstruction = "덤벨을 몸 옆에 들고 가슴을 세운 상태에서 보폭을 준비합니다.",
            enInstruction = "Hold dumbbells by the sides, stand tall, and prepare a controlled stride."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_walking_lunge_clean_step_2,
            koLabel = "오른발 앞으로 런지",
            enLabel = "Right foot forward",
            koInstruction = "오른발을 앞으로 내딛고 앞무릎이 안쪽으로 무너지지 않게 천천히 내려갑니다.",
            enInstruction = "Step the right foot forward and lower slowly while the front knee tracks over the toes."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_walking_lunge_clean_step_3,
            koLabel = "밀고 지나가기",
            enLabel = "Step through",
            koInstruction = "앞발로 바닥을 밀어 일어나며 몸통을 흔들지 않고 다음 걸음을 준비합니다.",
            enInstruction = "Drive through the front foot, step through, and keep the torso quiet."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_walking_lunge_clean_step_4,
            koLabel = "왼발 앞으로 반복",
            enLabel = "Left foot forward",
            koInstruction = "다음 반복은 왼발을 앞으로 내딛어 같은 깊이와 속도로 반복합니다.",
            enInstruction = "For the next rep, step the left foot forward and repeat with the same depth and tempo."
        )
    ),
    "leg_extension" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_leg_extension_clean_step_1,
            koLabel = "패드 조정",
            enLabel = "Adjust pad",
            koInstruction = "패드 조정: 등받이에 몸을 붙이고 발목 앞 패드가 발목 바로 위에 오도록 맞춥니다.",
            enInstruction = "Sit against the back pad and set the shin pad just above the ankles."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_leg_extension_clean_step_2,
            koLabel = "무릎 펴기",
            enLabel = "Extend knees",
            koInstruction = "무릎 펴기: 허벅지 앞쪽 힘으로 패드를 들어 올리되 엉덩이가 들리지 않게 고정합니다.",
            enInstruction = "Lift the pad with the front thighs while keeping the hips anchored."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_leg_extension_clean_step_3,
            koLabel = "통제 복귀",
            enLabel = "Controlled return",
            koInstruction = "통제 복귀: 무게추가 튕기지 않게 같은 경로로 천천히 내려 무릎을 굽힙니다.",
            enInstruction = "Lower along the same path so the weight stack does not bounce."
        )
    ),
    "leg_curl" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_leg_curl_clean_step_1,
            koLabel = "패드 조정",
            enLabel = "Adjust pad",
            koInstruction = "패드 조정: 발목 뒤쪽에 패드를 맞추고 엎드려 엉덩이가 패드에서 뜨지 않게 고정합니다.",
            enInstruction = "Set the pad behind the ankles and keep the hips pinned to the bench."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_leg_curl_clean_step_2,
            koLabel = "무릎 굽혀 당기기",
            enLabel = "Curl heels in",
            koInstruction = "무릎을 굽혀 패드를 엉덩이 쪽으로 당기고, 엉덩이가 들리지 않게 패드에 붙입니다.",
            enInstruction = "Bend the knees to curl the pad toward the hips while keeping the hips pinned to the bench."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_leg_curl_clean_step_3,
            koLabel = "천천히 복귀",
            enLabel = "Controlled return",
            koInstruction = "같은 경로로 천천히 다리를 내려 햄스트링 긴장을 유지한 채 다음 반복을 준비합니다.",
            enInstruction = "Lower the pad along the same path under control and keep hamstring tension for the next rep."
        )
    ),
    "romanian_deadlift" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_romanian_deadlift_clean_step_1,
            koLabel = "시작 위치",
            enLabel = "Start position",
            koInstruction = "시작 위치: 덤벨을 허벅지 앞에 두고 발을 골반너비로 세워 복부를 조입니다.",
            enInstruction = "Hold the dumbbells in front of the thighs, stand hip-width, and brace the abs."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_romanian_deadlift_clean_step_2,
            koLabel = "등 중립",
            enLabel = "Neutral back",
            koInstruction = "등 중립: 어깨를 낮추고 목부터 골반까지 긴 선을 유지한 채 무릎을 살짝 굽힙니다.",
            enInstruction = "Keep shoulders down, a long line from neck to pelvis, and a soft bend in the knees."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_romanian_deadlift_clean_step_3,
            koLabel = "힙힌지",
            enLabel = "Hip hinge",
            koInstruction = "힙힌지: 엉덩이를 뒤로 보내며 덤벨이 허벅지와 정강이 가까이 내려가게 합니다.",
            enInstruction = "Send the hips back and keep the dumbbells close to the thighs and shins."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_romanian_deadlift_clean_step_4,
            koLabel = "햄스트링 범위",
            enLabel = "Hamstring range",
            koInstruction = "햄스트링 범위: 허리가 말리기 전, 허벅지 뒤쪽이 당기는 지점에서 멈춥니다.",
            enInstruction = "Stop where the hamstrings stretch before the lower back rounds."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_romanian_deadlift_clean_step_5,
            koLabel = "둔근으로 복귀",
            enLabel = "Return with glutes",
            koInstruction = "둔근으로 복귀: 발바닥 전체를 누르고 엉덩이를 앞으로 보내 시작 자세로 돌아옵니다.",
            enInstruction = "Press through the whole foot and drive the hips forward to stand tall."
        )
    ),
    "hip_thrust" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hip_thrust_clean_v8_step_1,
            koLabel = "등 윗부분 지지",
            enLabel = "Set upper back",
            koInstruction = "등 윗부분 지지: 벤치 모서리에 견갑 아래쪽을 기대고 발바닥을 바닥에 붙입니다.",
            enInstruction = "Set upper back: brace the lower shoulder blades on the bench edge and keep both feet flat."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hip_thrust_clean_v8_step_2,
            koLabel = "발 위치와 복압",
            enLabel = "Set feet and brace",
            koInstruction = "발 위치와 복압: 무릎 아래에 발을 두고 갈비뼈를 내린 채 복부에 힘을 줍니다.",
            enInstruction = "Set feet and brace: keep feet under the knees, ribs down, and tension through the midsection."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hip_thrust_clean_v8_step_3,
            koLabel = "둔근으로 들어 올리기",
            enLabel = "Lift with glutes",
            koInstruction = "둔근으로 들어 올리기: 발바닥으로 밀어 어깨부터 무릎까지 거의 일직선이 되게 엉덩이를 올립니다.",
            enInstruction = "Lift with glutes: drive through the feet until shoulders, hips, and knees are nearly in one line."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hip_thrust_clean_v8_step_4,
            koLabel = "통제하며 내려오기",
            enLabel = "Lower with control",
            koInstruction = "통제하며 내려오기: 허리를 꺾지 않고 엉덩이를 천천히 내려 시작 자세로 돌아옵니다.",
            enInstruction = "Lower with control: bring the hips down slowly without arching the low back."
        )
    ),
    "calf_raise" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_calf_raise_clean_step_1,
            koLabel = "발 위치",
            enLabel = "Set feet",
            koInstruction = "발 위치: 발 앞쪽을 발판에 올리고 뒤꿈치가 자유롭게 내려갈 수 있게 섭니다.",
            enInstruction = "Place the balls of the feet on the platform so the heels can move freely."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_calf_raise_clean_step_2,
            koLabel = "올리기",
            enLabel = "Lift",
            koInstruction = "올리기: 종아리 힘으로 뒤꿈치를 들어 올려 발목을 끝범위까지 펴줍니다.",
            enInstruction = "Use the calves to lift the heels and fully extend the ankles."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_calf_raise_clean_step_3,
            koLabel = "멈추고 내리기",
            enLabel = "Pause and lower",
            koInstruction = "멈추고 내리기: 상단에서 잠깐 멈춘 뒤 뒤꿈치를 천천히 내려 종아리가 늘어나는 범위를 만듭니다.",
            enInstruction = "Pause at the top, then lower the heels slowly until the calves stretch."
        )
    ),
    "lat_pulldown" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_lat_pulldown_clean_v4_step_1,
            koLabel = "패드·가슴 세팅",
            enLabel = "Set pad and chest",
            koInstruction = "패드·가슴 세팅: 허벅지 패드로 몸을 고정하고 바를 얼굴 앞쪽 경로에 둔 채 가슴을 살짝 들어 준비합니다.",
            enInstruction = "Lock the thighs under the pad, keep the bar on the path in front of the face, and lift the chest slightly."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_lat_pulldown_clean_v4_step_2,
            koLabel = "상부 가슴 앞으로 당기기",
            enLabel = "Pull in front",
            koInstruction = "상부 가슴 앞으로 당기기: 목 뒤가 아니라 얼굴 앞을 지나 쇄골·상부 가슴 쪽으로 바를 당깁니다.",
            enInstruction = "Pull the bar down in front of the face toward the collarbone and upper chest, never behind the neck."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_lat_pulldown_clean_v4_step_3,
            koLabel = "하단 수축",
            enLabel = "Bottom squeeze",
            koInstruction = "하단 수축: 팔꿈치를 몸 옆 아래로 내리고 어깨를 낮춘 채 바를 상부 가슴 앞에서 짧게 통제합니다.",
            enInstruction = "Drive the elbows down beside the torso and briefly control the bar in front of the upper chest."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_lat_pulldown_clean_v4_step_4,
            koLabel = "어깨 낮춰 복귀",
            enLabel = "Return shoulders down",
            koInstruction = "어깨 낮춰 복귀: 어깨가 귀 쪽으로 끌려가지 않게 같은 경로로 천천히 올립니다.",
            enInstruction = "Return along the same path without letting the shoulders shrug toward the ears."
        )
    ),
    "seated_cable_row" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_seated_cable_row_clean_v5_step_1,
            koLabel = "발판·척추 세팅",
            enLabel = "Set feet and spine",
            koInstruction = "발판·척추 세팅: 발을 패드에 고정하고 허리를 중립으로 세운 뒤 팔을 길게 뻗어 손잡이를 잡습니다.",
            enInstruction = "Plant the feet on the pads, keep a neutral spine, and reach long to hold the handle."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_seated_cable_row_clean_v5_step_2,
            koLabel = "몸통 가까이 당기기",
            enLabel = "Row to torso",
            koInstruction = "몸통 가까이 당기기: 몸통을 뒤로 젖히지 않고 팔꿈치를 뒤로 보내 손잡이를 배꼽 가까이 당깁니다.",
            enInstruction = "Drive the elbows back and row the handle toward the navel without leaning the torso back."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_seated_cable_row_clean_v5_step_3,
            koLabel = "견갑 통제 복귀",
            enLabel = "Controlled return",
            koInstruction = "견갑 통제 복귀: 어깨가 귀 쪽으로 올라가지 않게 견갑을 통제하며 팔을 천천히 뻗습니다.",
            enInstruction = "Reach the arms forward under control while keeping the shoulders from shrugging upward."
        )
    ),
    "chest_supported_row" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_chest_supported_row_clean_v2_step_1,
            koLabel = "벤치에 가슴 지지",
            enLabel = "Support chest on bench",
            koInstruction = "벤치에 가슴 지지: 인클라인 벤치에 가슴을 붙이고 발끝으로 몸을 안정시킨 뒤 덤벨을 아래로 둡니다.",
            enInstruction = "Set the chest on the incline bench, brace through the feet, and let the dumbbells hang below the shoulders."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_chest_supported_row_clean_v2_step_2,
            koLabel = "팔꿈치 뒤로 당기기",
            enLabel = "Row elbows back",
            koInstruction = "팔꿈치 뒤로 당기기: 가슴이 벤치에서 뜨지 않게 유지하며 팔꿈치를 등 뒤로 보내 덤벨을 몸 옆으로 당깁니다.",
            enInstruction = "Keep the chest on the bench and drive the elbows behind the torso to row the dumbbells beside the body."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_chest_supported_row_clean_v2_step_3,
            koLabel = "통제 복귀",
            enLabel = "Controlled return",
            koInstruction = "통제 복귀: 어깨가 앞으로 끌려가지 않게 견갑을 통제하며 덤벨을 천천히 아래로 내립니다.",
            enInstruction = "Lower the dumbbells under control without letting the shoulders collapse forward."
        )
    ),
    "one_arm_dumbbell_row" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_one_arm_dumbbell_row_clean_v2_step_1,
            koLabel = "벤치 지지",
            enLabel = "Brace on bench",
            koInstruction = "벤치 지지: 한 손과 한쪽 무릎을 벤치에 두고 반대쪽 발은 바닥에 단단히 고정합니다.",
            enInstruction = "Place one hand and one knee on the bench and anchor the opposite foot firmly on the floor."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_one_arm_dumbbell_row_clean_v2_step_2,
            koLabel = "몸통 고정",
            enLabel = "Brace torso",
            koInstruction = "몸통 고정: 골반과 어깨가 바닥을 향하게 유지하고 덤벨을 어깨 아래에 둡니다.",
            enInstruction = "Keep the hips and shoulders square to the floor and let the dumbbell hang under the shoulder."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_one_arm_dumbbell_row_clean_v2_step_3,
            koLabel = "당기기",
            enLabel = "Pull",
            koInstruction = "당기기: 몸통을 비틀지 않고 팔꿈치를 뒤로 보내 덤벨을 옆구리 가까이 당깁니다.",
            enInstruction = "Drive the elbow back and row the dumbbell toward the ribs without twisting the torso."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_one_arm_dumbbell_row_clean_v2_step_4,
            koLabel = "회전 없이 내리기",
            enLabel = "Lower without rotation",
            koInstruction = "회전 없이 내리기: 어깨가 앞으로 끌려가지 않게 통제하며 덤벨을 시작 위치로 천천히 내립니다.",
            enInstruction = "Lower the dumbbell back to the start under control without letting the shoulder collapse forward."
        )
    ),
    "assisted_pullup" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_assisted_pullup_clean_v2_step_1,
            koLabel = "발판과 보조 패드 진입",
            enLabel = "Step onto assist pad",
            koInstruction = "발판에 올라 상단 손잡이 아래에 몸을 두고 한쪽 무릎씩 움직이는 보조 패드에 올립니다.",
            enInstruction = "Step onto the foot platform and place one knee at a time on the moving assist pad."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_assisted_pullup_clean_v2_step_2,
            koLabel = "상단 손잡이 잡기",
            enLabel = "Grip fixed handles",
            koInstruction = "손잡이는 머리 위 고정 위치에서 잡고 가슴을 살짝 들어 몸통을 흔들리지 않게 준비합니다.",
            enInstruction = "Grip the fixed overhead handles evenly, lift the chest slightly, and keep the body quiet."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_assisted_pullup_clean_v2_step_3,
            koLabel = "가슴을 손잡이 쪽으로 끌기",
            enLabel = "Pull chest upward",
            koInstruction = "턱만 빼지 말고 팔꿈치를 아래로 당기며 몸을 위로 올립니다.",
            enInstruction = "Pull the chest toward the handles by driving elbows down, without craning the neck."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_assisted_pullup_clean_v2_step_4,
            koLabel = "패드와 함께 천천히 하강",
            enLabel = "Lower with the pad",
            koInstruction = "몸과 보조 패드가 함께 아래로 내려오게 통제하며 팔을 펴고 다음 반복을 준비합니다.",
            enInstruction = "Lower your body and the assist pad together under control, then reset for the next rep."
        )
    ),
    "face_pull" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_face_pull_clean_v2_step_1,
            koLabel = "케이블 높이",
            enLabel = "Set cable height",
            koInstruction = "케이블 높이: 로프가 얼굴 높이에서 오도록 맞추고 팔을 뻗은 상태로 몸통을 세웁니다.",
            enInstruction = "Set the rope around face height and stand tall with the arms extended."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_face_pull_clean_v2_step_2,
            koLabel = "얼굴 옆으로 당기기",
            enLabel = "Pull beside face",
            koInstruction = "얼굴 옆으로 당기기: 팔꿈치를 높게 유지하고 로프 끝을 얼굴 양옆으로 벌리며 당깁니다.",
            enInstruction = "Keep the elbows high and pull the rope ends apart toward the sides of the face."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_face_pull_clean_v2_step_3,
            koLabel = "팔꿈치 유지 복귀",
            enLabel = "Return with elbows high",
            koInstruction = "팔꿈치 유지 복귀: 어깨가 말리지 않게 통제하며 같은 경로로 팔을 천천히 뻗습니다.",
            enInstruction = "Reach back along the same path under control without letting the shoulders roll forward."
        )
    ),
    "machine_chest_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_machine_chest_press_clean_step_1,
            koLabel = "좌석 조정",
            enLabel = "Adjust seat",
            koInstruction = "좌석 조정: 손잡이가 가슴 중간 높이에 오도록 좌석을 맞추고 등과 엉덩이를 패드에 붙입니다.",
            enInstruction = "Adjust the seat so the handles sit around mid-chest, then keep the back and hips on the pad."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_machine_chest_press_clean_step_2,
            koLabel = "밀기",
            enLabel = "Press",
            koInstruction = "밀기: 어깨가 들리지 않게 고정하고 손잡이를 같은 속도로 앞으로 밀어냅니다.",
            enInstruction = "Keep the shoulders down and press both handles forward at the same speed."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_machine_chest_press_clean_step_3,
            koLabel = "어깨 범위 내 복귀",
            enLabel = "Return in shoulder range",
            koInstruction = "어깨 범위 내 복귀: 가슴이 늘어나는 범위까지만 손잡이를 천천히 되돌립니다.",
            enInstruction = "Return the handles slowly only as far as the shoulders stay comfortable."
        )
    ),
    "machine_shoulder_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_machine_shoulder_press_clean_v2_step_1,
            koLabel = "어깨 옆 시작",
            enLabel = "Start beside shoulders",
            koInstruction = "등을 패드에 붙이고 손잡이가 어깨 옆에 오도록 앉아 몸통을 고정합니다.",
            enInstruction = "Sit with your back on the pad, hold the handles beside the shoulders, and brace the torso."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_machine_shoulder_press_clean_v2_step_2,
            koLabel = "같은 레버 궤도로 밀기",
            enLabel = "Press on same arc",
            koInstruction = "기구의 레버가 정해진 호를 따라 올라가도록 손잡이를 위로 밀고 허리를 젖히지 않습니다.",
            enInstruction = "Press the handles upward on the same lever arc without leaning the low back."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_machine_shoulder_press_clean_v2_step_3,
            koLabel = "머리 위 마무리",
            enLabel = "Finish overhead",
            koInstruction = "팔을 머리 위로 뻗은 뒤 무게추가 튕기지 않게 같은 경로로 천천히 내립니다.",
            enInstruction = "Finish overhead, then lower along the same path without letting the stack slam."
        )
    ),
    "dumbbell_bench_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_bench_press_clean_v3_step_1,
            koLabel = "눕고 덤벨 위치",
            enLabel = "Lie and set dumbbells",
            koInstruction = "눕고 덤벨 위치: 벤치에 누워 발을 고정하고 덤벨을 가슴 위에서 안정적으로 세팅합니다.",
            enInstruction = "Lie on the bench, plant the feet, and set the dumbbells steadily above the chest."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_bench_press_clean_v3_step_2,
            koLabel = "팔꿈치 각도",
            enLabel = "Set elbow angle",
            koInstruction = "팔꿈치 각도: 덤벨을 어깨선보다 살짝 아래로 내리고 팔꿈치가 과하게 벌어지지 않게 둡니다.",
            enInstruction = "Lower the dumbbells just below shoulder level while keeping the elbows from flaring too wide."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_bench_press_clean_v3_step_3,
            koLabel = "밀기",
            enLabel = "Press",
            koInstruction = "밀기: 양쪽 덤벨을 같은 속도로 밀어 올리며 손목을 팔꿈치 위에 세웁니다.",
            enInstruction = "Press both dumbbells up at the same speed while keeping the wrists stacked over the elbows."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_bench_press_clean_v3_step_4,
            koLabel = "통제 하강",
            enLabel = "Controlled lower",
            koInstruction = "통제 하강: 어깨 앞쪽이 불편하지 않은 범위까지만 덤벨을 천천히 내립니다.",
            enInstruction = "Lower the dumbbells slowly only as far as the front of the shoulders stays comfortable."
        )
    ),
    "incline_dumbbell_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_incline_dumbbell_press_clean_v4_step_1,
            koLabel = "벤치 각도 세팅",
            enLabel = "Set bench angle",
            koInstruction = "벤치 각도 세팅: 인클라인 벤치를 너무 세우지 않고 등과 머리를 패드에 붙입니다.",
            enInstruction = "Set the incline without making it too steep, then keep your back and head on the pad."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_incline_dumbbell_press_clean_v4_step_2,
            koLabel = "덤벨 시작 위치",
            enLabel = "Start dumbbells",
            koInstruction = "덤벨 시작 위치: 덤벨을 어깨 옆에 두고 팔꿈치를 살짝 아래로 내려 손목을 세웁니다.",
            enInstruction = "Hold the dumbbells beside the shoulders, keep the wrists stacked, and let the elbows sit slightly below the hands."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_incline_dumbbell_press_clean_v4_step_3,
            koLabel = "위로 밀기",
            enLabel = "Press",
            koInstruction = "위로 밀기: 양쪽 덤벨을 같은 속도로 위로 밀어 윗가슴과 어깨 전면을 통제합니다.",
            enInstruction = "Press both dumbbells upward at the same speed while controlling the upper chest and front shoulders."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_incline_dumbbell_press_clean_v4_step_4,
            koLabel = "어깨 범위 내 하강",
            enLabel = "Lower in shoulder range",
            koInstruction = "어깨 범위 내 하강: 어깨 앞쪽이 불편하지 않은 범위까지만 덤벨을 천천히 내립니다.",
            enInstruction = "Lower slowly only as far as the front of the shoulders stays comfortable."
        )
    ),
    "pushup" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_pushup_clean_step_1,
            koLabel = "손 위치",
            enLabel = "Set hands",
            koInstruction = "손 위치: 손을 어깨보다 살짝 넓게 두고 머리부터 발끝까지 일직선을 만듭니다.",
            enInstruction = "Place the hands slightly wider than the shoulders and make one straight line from head to heels."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_pushup_clean_step_2,
            koLabel = "몸통 일직선 하강",
            enLabel = "Lower as one line",
            koInstruction = "몸통 일직선 하강: 팔꿈치를 과하게 벌리지 않고 가슴을 바닥 가까이 천천히 내립니다.",
            enInstruction = "Lower the chest toward the floor while keeping the elbows from flaring too wide."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_pushup_clean_step_3,
            koLabel = "밀어 올라오기",
            enLabel = "Drive up",
            koInstruction = "밀어 올라오기: 복부와 엉덩이에 힘을 유지한 채 바닥을 밀어 시작 자세로 돌아옵니다.",
            enInstruction = "Push the floor away and return to the start while keeping the abs and glutes engaged."
        )
    ),
    "cable_fly" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_fly_clean_step_1,
            koLabel = "시작 자세",
            enLabel = "Start position",
            koInstruction = "시작 자세: 양쪽 케이블을 잡고 한 발을 앞으로 둔 뒤 팔을 옆으로 열어 가슴이 늘어나는 범위를 만듭니다.",
            enInstruction = "Hold both handles, stagger the stance, and open the arms until the chest is gently stretched."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_fly_clean_step_2,
            koLabel = "손 모으기",
            enLabel = "Bring hands together",
            koInstruction = "손 모으기: 팔꿈치를 살짝 굽힌 채 양손을 가슴 앞에서 모으며 케이블 장력을 유지합니다.",
            enInstruction = "Bring the hands together in front of the chest with a slight elbow bend and steady cable tension."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_fly_clean_step_3,
            koLabel = "가슴 늘림 범위 복귀",
            enLabel = "Return to chest stretch",
            koInstruction = "가슴 늘림 범위 복귀: 어깨가 과하게 뒤로 젖혀지기 전까지만 같은 경로로 천천히 돌아갑니다.",
            enInstruction = "Return along the same path only until the chest is stretched without pulling the shoulders too far back."
        )
    ),
    "dumbbell_shoulder_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_shoulder_press_clean_step_1,
            koLabel = "덤벨 시작 위치",
            enLabel = "Start dumbbells",
            koInstruction = "덤벨 시작 위치: 덤벨을 어깨 높이에 두고 손목을 팔꿈치 위에 세운 뒤 갈비뼈가 들리지 않게 고정합니다.",
            enInstruction = "Start with the dumbbells at shoulder height, wrists stacked over elbows, and ribs controlled."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_shoulder_press_clean_step_2,
            koLabel = "머리 위로 밀기",
            enLabel = "Press",
            koInstruction = "머리 위로 밀기: 양쪽 덤벨을 같은 속도로 밀어 올리고 어깨가 귀 쪽으로 솟지 않게 합니다.",
            enInstruction = "Press both dumbbells overhead at the same speed without shrugging the shoulders toward the ears."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_shoulder_press_clean_step_3,
            koLabel = "어깨 높이 복귀",
            enLabel = "Return to shoulder height",
            koInstruction = "어깨 높이 복귀: 같은 경로로 천천히 내려 덤벨을 어깨 높이에 되돌리고 몸통을 흔들지 않습니다.",
            enInstruction = "Lower along the same path back to shoulder height without letting the torso sway."
        )
    ),
    "dumbbell_lateral_raise" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_lateral_raise_clean_step_1,
            koLabel = "시작 자세",
            enLabel = "Start position",
            koInstruction = "시작 자세: 가벼운 덤벨을 허벅지 옆에 두고 무릎과 팔꿈치를 살짝 부드럽게 둡니다.",
            enInstruction = "Start with light dumbbells beside the thighs and keep the knees and elbows softly unlocked."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_lateral_raise_clean_step_2,
            koLabel = "어깨 높이까지 올리기",
            enLabel = "Raise to shoulder height",
            koInstruction = "어깨 높이까지 올리기: 반동 없이 양팔을 옆으로 들어 덤벨이 어깨 높이 근처에 오게 합니다.",
            enInstruction = "Raise both arms out to the sides without swinging until the dumbbells reach around shoulder height."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_lateral_raise_clean_step_3,
            koLabel = "통제 하강",
            enLabel = "Controlled lower",
            koInstruction = "통제 하강: 승모근에 힘이 몰리지 않게 천천히 내려 시작 자세로 돌아옵니다.",
            enInstruction = "Lower slowly back to the start without letting the upper traps take over."
        )
    ),
    "rear_delt_machine" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_rear_delt_machine_clean_step_1,
            koLabel = "패드 밀착",
            enLabel = "Brace on pad",
            koInstruction = "패드 밀착: 가슴을 패드에 붙이고 손잡이를 잡은 뒤 팔꿈치를 살짝 부드럽게 둡니다.",
            enInstruction = "Keep the chest on the pad, hold the handles, and leave the elbows softly unlocked."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_rear_delt_machine_clean_step_2,
            koLabel = "뒤로 벌리기",
            enLabel = "Open back",
            koInstruction = "뒤로 벌리기: 어깨가 올라가지 않게 손잡이를 양옆과 뒤쪽으로 벌려 후면 어깨를 조입니다.",
            enInstruction = "Open the handles out and slightly back without shrugging so the rear shoulders do the work."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_rear_delt_machine_clean_step_3,
            koLabel = "반동 없이 복귀",
            enLabel = "Return without swing",
            koInstruction = "반동 없이 복귀: 같은 경로로 천천히 손잡이를 앞으로 보내며 가슴 패드 접촉을 유지합니다.",
            enInstruction = "Return the handles forward along the same path while keeping the chest against the pad."
        )
    ),
    "triceps_pushdown" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_triceps_pushdown_clean_step_1,
            koLabel = "팔꿈치 고정",
            enLabel = "Pin elbows",
            koInstruction = "팔꿈치 고정: 손잡이를 잡고 팔꿈치를 몸 옆에 붙인 채 상완이 흔들리지 않게 둡니다.",
            enInstruction = "Hold the handle and pin the elbows beside the torso so the upper arms stay still."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_triceps_pushdown_clean_step_2,
            koLabel = "아래로 펴기",
            enLabel = "Press down",
            koInstruction = "아래로 펴기: 몸통으로 누르지 말고 팔꿈치를 펴며 손잡이를 허벅지 쪽으로 내립니다.",
            enInstruction = "Extend the elbows and press the handle toward the thighs without leaning the torso into it."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_triceps_pushdown_clean_step_3,
            koLabel = "위치 유지 복귀",
            enLabel = "Return with elbows pinned",
            koInstruction = "위치 유지 복귀: 케이블 장력을 유지하며 팔꿈치 위치를 고정한 채 천천히 다시 굽힙니다.",
            enInstruction = "Bend the elbows back up under control while keeping cable tension and the elbows pinned."
        )
    ),
    "overhead_triceps_extension" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_overhead_triceps_extension_clean_v2_step_1,
            koLabel = "머리 위 세팅",
            enLabel = "Set overhead",
            koInstruction = "덤벨 하나를 양손으로 잡고 팔꿈치를 귀 옆에 모은 채 몸통을 곧게 세웁니다.",
            enInstruction = "Hold one dumbbell with both hands, keep the elbows near the ears, and stand tall."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_overhead_triceps_extension_clean_v2_step_2,
            koLabel = "팔꿈치 굽히기",
            enLabel = "Bend elbows",
            koInstruction = "상완은 세워 둔 채 팔꿈치만 굽혀 덤벨을 머리 뒤로 천천히 내립니다.",
            enInstruction = "Keep the upper arms tall and bend only the elbows to lower the dumbbell behind the head."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_overhead_triceps_extension_clean_v2_step_3,
            koLabel = "펴기",
            enLabel = "Extend",
            koInstruction = "팔꿈치를 모은 채 팔 뒤쪽 힘으로 덤벨을 머리 위로 밀어 올립니다.",
            enInstruction = "Keep the elbows close and extend the arms overhead using the triceps."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_overhead_triceps_extension_clean_v2_step_4,
            koLabel = "상단 고정",
            enLabel = "Finish overhead",
            koInstruction = "허리를 젖히지 말고 팔꿈치를 잠그기 직전에서 멈춘 뒤 같은 경로로 반복합니다.",
            enInstruction = "Stop just before locking the elbows, avoid arching the back, and repeat along the same path."
        )
    ),
    "dumbbell_curl" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_curl_clean_step_1,
            koLabel = "팔꿈치 고정",
            enLabel = "Pin elbows",
            koInstruction = "팔꿈치 고정: 덤벨을 몸 옆에 두고 팔꿈치를 옆구리 가까이에 고정한 채 손목을 중립으로 둡니다.",
            enInstruction = "Hold the dumbbells beside the body, keep the elbows close to the ribs, and keep the wrists neutral."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_curl_clean_step_2,
            koLabel = "올리고 천천히 내리기",
            enLabel = "Lift and lower slowly",
            koInstruction = "올리고 천천히 내리기: 몸을 흔들지 않고 덤벨을 어깨 쪽으로 올린 뒤 같은 경로로 천천히 내립니다.",
            enInstruction = "Curl the dumbbells toward the shoulders without swinging, then lower along the same path under control."
        )
    ),
    "cable_curl" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_curl_clean_step_1,
            koLabel = "로우 풀리 시작",
            enLabel = "Low-pulley start",
            koInstruction = "로우 풀리 시작: 바를 잡고 팔꿈치를 옆구리 가까이에 둔 채 케이블 장력을 만듭니다.",
            enInstruction = "Hold the bar, keep the elbows close to the ribs, and set tension from the low pulley."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_curl_clean_step_2,
            koLabel = "끌어올리기",
            enLabel = "Curl up",
            koInstruction = "끌어올리기: 몸통을 흔들지 않고 팔꿈치를 굽혀 바를 가슴 아래쪽으로 끌어올립니다.",
            enInstruction = "Curl the bar toward the lower chest without swinging the torso."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_curl_clean_step_3,
            koLabel = "장력 유지 하강",
            enLabel = "Lower with tension",
            koInstruction = "장력 유지 하강: 케이블이 느슨해지지 않게 같은 경로로 천천히 내려 시작 자세로 돌아갑니다.",
            enInstruction = "Lower along the same path under control so the cable stays taut."
        )
    ),
    "plank" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_plank_clean_step_1,
            koLabel = "팔꿈치 위치 잡기",
            enLabel = "Set elbows",
            koInstruction = "팔꿈치 위치 잡기: 팔꿈치를 어깨 바로 아래에 두고 전완으로 바닥을 누른 채 발끝을 세웁니다.",
            enInstruction = "Place the elbows directly under the shoulders, press the forearms into the floor, and set the toes."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_plank_clean_step_2,
            koLabel = "일직선으로 버티기",
            enLabel = "Brace and breathe",
            koInstruction = "일직선으로 버티기: 머리부터 뒤꿈치까지 길게 유지하고, 엉덩이가 처지지 않게 복부에 힘을 준 채 호흡합니다.",
            enInstruction = "Keep a long line from head to heels, brace the core so the hips do not sag, and breathe steadily."
        )
    ),
    "dead_bug" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dead_bug_clean_v15_step_1,
            koLabel = "시작 자세",
            enLabel = "Start position",
            koInstruction = "시작 자세: 약간 측면에서 보이는 자세로 등을 대고 누워 양팔을 어깨 위로 뻗고, 무릎과 고관절을 90도로 들어 허리와 골반을 안정시킵니다.",
            enInstruction = "Lie on your back in a slight side-view tabletop, reach both arms over the shoulders, lift knees and hips to 90 degrees, and stabilize the lower back and pelvis."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dead_bug_clean_v15_step_2,
            koLabel = "한쪽 팔·반대 다리 뻗기",
            enLabel = "Reach side A",
            koInstruction = "한쪽 팔·반대 다리 뻗기: 한쪽 팔은 머리 뒤로 길게 보내고 반대쪽 다리는 바닥 가까이 뻗습니다. 허리가 뜨면 팔·다리 이동 범위를 줄입니다.",
            enInstruction = "Reach one arm behind the head and extend the opposite leg close to the floor. Shorten the range if the lower back starts to lift."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dead_bug_clean_v15_step_1,
            koLabel = "시작 자세로 복귀",
            enLabel = "Return to start",
            koInstruction = "시작 자세로 복귀: 움직인 팔과 다리를 천천히 시작 자세로 되돌리고, 허리와 골반이 흔들리지 않게 복부 긴장을 유지합니다.",
            enInstruction = "Return the moving arm and leg slowly to tabletop while keeping the lower back and pelvis steady."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dead_bug_clean_v15_step_3,
            koLabel = "반대쪽 팔·다리 뻗기",
            enLabel = "Reach side B",
            koInstruction = "반대쪽 팔·다리 뻗기: 이번에는 반대쪽 팔과 다리를 같은 방식으로 뻗어 좌우를 교대합니다. 허리가 뜨면 범위를 줄입니다.",
            enInstruction = "Repeat the same reach with the opposite arm and leg, alternating sides while keeping the lower back controlled."
        )
    ),
    "bird_dog" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_bird_dog_clean_v4_step_1,
            koLabel = "네발기기",
            enLabel = "Quadruped start",
            koInstruction = "네발기기: 손은 어깨 아래, 무릎은 골반 아래에 두고 목부터 골반까지 길게 정렬합니다.",
            enInstruction = "Set the hands under the shoulders and knees under the hips, then keep a long line from neck to pelvis."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_bird_dog_clean_v4_step_2,
            koLabel = "한쪽 뻗기",
            enLabel = "Reach one side",
            koInstruction = "한쪽 뻗기: 한쪽 팔과 반대쪽 다리를 길게 뻗고 골반이 돌아가거나 허리가 꺾이지 않게 버팁니다.",
            enInstruction = "Reach one arm and the opposite leg long while keeping the hips square and the lower back neutral."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_bird_dog_clean_v4_step_3,
            koLabel = "복귀 후 반대쪽",
            enLabel = "Return and switch",
            koInstruction = "천천히 네발기기 자세로 돌아온 뒤 반대쪽 팔과 다리도 같은 방식으로 반복합니다.",
            enInstruction = "Return slowly to quadruped, then repeat the same pattern on the opposite side."
        )
    ),
    "pallof_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_pallof_press_clean_v5_step_1,
            koLabel = "케이블 옆으로 서기",
            enLabel = "Stand beside cable",
            koInstruction = "케이블 옆으로 서기: 케이블을 가슴 높이에 맞추고 옆으로 서서 손잡이를 양손으로 잡습니다.",
            enInstruction = "Set the cable at chest height, stand side-on to the anchor, and hold the handle with both hands."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_pallof_press_clean_v5_step_2,
            koLabel = "가슴 앞 세팅",
            enLabel = "Set at chest",
            koInstruction = "가슴 앞 세팅: 손잡이를 가슴 중앙에 두고 갈비뼈와 골반이 케이블 쪽으로 돌아가지 않게 고정합니다.",
            enInstruction = "Hold the handle at the center of the chest and keep the ribs and pelvis from rotating toward the cable."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_pallof_press_clean_v5_step_3,
            koLabel = "앞으로 밀기",
            enLabel = "Press forward",
            koInstruction = "앞으로 밀기: 손을 가슴 앞에서 곧게 밀어내고 케이블이 몸통을 돌리려는 힘을 복부로 버팁니다.",
            enInstruction = "Press the hands straight forward and brace the core against the cable's pull."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_pallof_press_clean_v5_step_4,
            koLabel = "회전 버티며 복귀",
            enLabel = "Resist rotation",
            koInstruction = "회전 버티며 복귀: 같은 자세를 유지하며 손을 가슴으로 되돌리고, 세트를 마치면 반대 방향으로 서서 반복합니다.",
            enInstruction = "Return the hands to the chest without rotating, then turn around and repeat on the opposite side."
        )
    ),
    "cable_woodchop" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_woodchop_clean_step_1,
            koLabel = "상단 풀리 시작",
            enLabel = "High-pulley start",
            koInstruction = "상단 풀리 시작: 케이블을 높은 위치에 두고 손잡이를 어깨 위쪽에서 잡아 몸통을 길게 세웁니다.",
            enInstruction = "Set the cable high, hold the handle near the upper shoulder, and stand tall."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_woodchop_clean_step_2,
            koLabel = "대각선 당기기",
            enLabel = "Pull diagonally",
            koInstruction = "대각선 당기기: 팔만 휘두르지 말고 가슴과 골반을 함께 돌리며 손잡이를 대각선 아래로 당깁니다.",
            enInstruction = "Pull the handle diagonally down while rotating the chest and hips together, not just swinging the arms."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_woodchop_clean_step_3,
            koLabel = "무릎 바깥 마무리",
            enLabel = "Finish outside knee",
            koInstruction = "무릎 바깥 마무리: 손잡이를 반대쪽 무릎 바깥 방향까지 내리고 복부에 힘을 유지합니다.",
            enInstruction = "Finish with the handle outside the opposite knee while keeping the core braced."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_woodchop_clean_step_4,
            koLabel = "통제 복귀",
            enLabel = "Controlled return",
            koInstruction = "통제 복귀: 케이블이 끌고 가게 두지 말고 같은 대각선 경로로 천천히 돌아온 뒤 반대쪽도 반복합니다.",
            enInstruction = "Return slowly along the same diagonal path under control, then repeat on the opposite side."
        )
    ),
    "treadmill_walk" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_treadmill_walk_clean_v2_step_1,
            koLabel = "낮은 속도 시작",
            enLabel = "Start slow",
            koInstruction = "낮은 속도 시작: 벨트 중앙에 서서 손잡이를 가볍게 잡고 낮은 속도부터 걷기 시작합니다.",
            enInstruction = "Stand in the center of the belt, hold the rail lightly, and begin walking at a low speed."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_treadmill_walk_clean_v2_step_2,
            koLabel = "자연스럽게 걷기",
            enLabel = "Walk naturally",
            koInstruction = "자연스럽게 걷기: 시선은 앞을 보고 발은 벨트 중앙에 놓으며 팔과 어깨에 힘을 빼고 걷습니다.",
            enInstruction = "Look forward, keep the feet in the middle of the belt, and walk with relaxed shoulders and arms."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_treadmill_walk_clean_v2_step_3,
            koLabel = "1분 쿨다운",
            enLabel = "One-minute cooldown",
            koInstruction = "1분 쿨다운: 마지막 1분은 속도를 낮춰 호흡을 안정시키고 벨트가 멈춘 뒤 내려옵니다.",
            enInstruction = "For the final minute, lower the speed, settle your breathing, and step off only after the belt stops."
        )
    ),
    "indoor_bike" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_indoor_bike_clean_v2_step_1,
            koLabel = "탑승 세팅",
            enLabel = "Set up ride",
            koInstruction = "탑승 세팅: 안장에 앉아 양발을 페달에 올리고 무릎이 과하게 접히지 않는지 확인합니다.",
            enInstruction = "Sit on the saddle, place both feet on the pedals, and check that the knees are not overly bent."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_indoor_bike_clean_v2_step_2,
            koLabel = "부드럽게 페달링",
            enLabel = "Smooth pedal",
            koInstruction = "부드럽게 페달링: 상체는 편하게 세우고 페달을 끊기지 않게 일정한 리듬으로 밟습니다.",
            enInstruction = "Keep the upper body relaxed and pedal with a steady, continuous rhythm."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_indoor_bike_clean_v2_step_3,
            koLabel = "강도 유지",
            enLabel = "Hold intensity",
            koInstruction = "강도 유지: 숨이 차지만 대화가 가능한 정도로 저항을 맞추고 목과 허리에 힘이 들어가지 않게 유지합니다.",
            enInstruction = "Set resistance so breathing rises but conversation is still possible, keeping the neck and lower back relaxed."
        )
    ),
    "hack_squat" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hack_squat_clean_v4_step_1,
            koLabel = "패드 밀착",
            enLabel = "Brace on pad",
            koInstruction = "등과 어깨를 패드에 붙이고 양손으로 손잡이를 잡은 뒤 발판에 발을 고정합니다.",
            enInstruction = "Pin the back and shoulders to the pads, grip the handles, and set both feet on the platform."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hack_squat_clean_v4_step_2,
            koLabel = "발 위치",
            enLabel = "Set feet",
            koInstruction = "발바닥 전체를 누르고 무릎이 발끝 방향을 따라가도록 시작 자세를 만듭니다.",
            enInstruction = "Press through the whole foot and align the knees with the toes before descending."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hack_squat_clean_v4_step_3,
            koLabel = "내려가기",
            enLabel = "Lower",
            koInstruction = "패드에서 등이 뜨지 않게 하며 레일을 따라 천천히 내려갑니다.",
            enInstruction = "Lower along the rails while keeping the back connected to the pad."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hack_squat_clean_v4_step_4,
            koLabel = "잠그지 않고 밀기",
            enLabel = "Press",
            koInstruction = "발판을 밀어 올라오되 무릎을 세게 잠그지 않고 같은 레일 경로를 유지합니다.",
            enInstruction = "Drive the platform up without hard-locking the knees, keeping the same rail path."
        )
    ),
    "smith_machine_squat" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_smith_machine_squat_clean_v6_step_1,
            koLabel = "바 위치",
            enLabel = "Set bar",
            koInstruction = "스미스 바를 목이 아닌 어깨 뒤쪽에 두고 양손으로 좌우 균형 있게 잡습니다.",
            enInstruction = "Place the Smith bar across the upper back, not the neck, and grip it evenly."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_smith_machine_squat_clean_v6_step_2,
            koLabel = "발 위치",
            enLabel = "Set feet",
            koInstruction = "양발을 어깨너비로 두고 뒤꿈치가 뜨지 않는 위치에서 몸통을 고정합니다.",
            enInstruction = "Set the feet shoulder-width and brace where the heels can stay grounded."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_smith_machine_squat_clean_v6_step_3,
            koLabel = "내려가기",
            enLabel = "Lower",
            koInstruction = "바가 수직 레일을 따라 내려가게 하며 엉덩이와 무릎을 함께 굽힙니다.",
            enInstruction = "Let the bar travel on the vertical rails as the hips and knees bend together."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_smith_machine_squat_clean_v6_step_4,
            koLabel = "발바닥으로 밀기",
            enLabel = "Drive through feet",
            koInstruction = "발바닥 전체로 밀어 같은 수직 레일을 따라 바를 올립니다.",
            enInstruction = "Drive through the whole foot and raise the bar along the same vertical rails."
        )
    ),
    "barbell_back_squat" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_back_squat_clean_v4_step_1,
            koLabel = "랙 높이와 바 위치",
            enLabel = "Set rack and bar",
            koInstruction = "바를 윗가슴 높이 랙에 두고 목이 아닌 등 위쪽에 얹습니다.",
            enInstruction = "Set the bar around upper-chest rack height and place it across the upper back, not the neck."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_back_squat_clean_v4_step_2,
            koLabel = "언랙과 브레이싱",
            enLabel = "Unrack and brace",
            koInstruction = "양손을 고르게 잡고 복부에 힘을 준 뒤 두 걸음만 뒤로 나옵니다.",
            enInstruction = "Grip evenly, brace the core, stand tall, and take only two small steps back."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_back_squat_clean_v4_step_3,
            koLabel = "발과 시선 정렬",
            enLabel = "Set stance",
            koInstruction = "발은 어깨너비로 두고 발끝과 무릎 방향을 맞춥니다.",
            enInstruction = "Set feet about shoulder width, point toes slightly out, and align knees with toes."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_back_squat_clean_v4_step_4,
            koLabel = "통제 하강",
            enLabel = "Lower with control",
            koInstruction = "바가 중족부 위에 머물도록 엉덩이와 무릎을 함께 접습니다.",
            enInstruction = "Lower by bending hips and knees together while keeping the bar over mid-foot."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_back_squat_clean_v4_step_5,
            koLabel = "바닥 밀며 기립",
            enLabel = "Drive up",
            koInstruction = "허리가 말리기 전 범위에서 멈추고 반동 없이 일어섭니다.",
            enInstruction = "Stop before the low back rounds, then drive through the floor to stand without bouncing."
        )
    ),
    "barbell_bench_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_bench_press_clean_v6_step_1,
            koLabel = "랙과 눈 위치",
            enLabel = "Set rack and eyes",
            koInstruction = "눈이 바보다 살짝 뒤에 오게 눕고, 양발을 바닥에 고정한 뒤 양손을 고르게 잡습니다.",
            enInstruction = "Lie with the eyes slightly behind the bar, plant both feet, and grip evenly."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_bench_press_clean_v6_step_2,
            koLabel = "가슴 쪽 하강",
            enLabel = "Lower to chest",
            koInstruction = "팔꿈치를 과하게 벌리지 않고 바를 목이 아닌 가슴 라인으로 천천히 내립니다.",
            enInstruction = "Lower the bar toward the chest, not the neck, with elbows controlled instead of flared."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_bench_press_clean_v6_step_3,
            koLabel = "하단 통제",
            enLabel = "Control bottom",
            koInstruction = "바가 가슴 위에 가볍게 닿거나 멈추면 손목과 팔꿈치 정렬을 유지합니다.",
            enInstruction = "At the bottom, keep the bar over the chest and maintain stacked wrists and elbows."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_bench_press_clean_v6_step_4,
            koLabel = "밀어 마무리",
            enLabel = "Press to finish",
            koInstruction = "양발을 고정한 채 바를 어깨 위로 밀고 반복 후 안전하게 랙에 걸 준비를 합니다.",
            enInstruction = "Press back over the shoulders with the feet planted, then prepare to rack safely."
        )
    ),
    "barbell_overhead_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_overhead_press_clean_v3_step_1,
            koLabel = "어깨 앞 시작",
            enLabel = "Front rack start",
            koInstruction = "바를 어깨 앞 쇄골 높이에 두고 복부와 엉덩이에 힘을 줍니다.",
            enInstruction = "Hold the bar at the front of the shoulders and brace the abs and glutes."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_overhead_press_clean_v3_step_2,
            koLabel = "얼굴 지나 밀기",
            enLabel = "Press past face",
            koInstruction = "턱을 살짝 뒤로 빼고 바가 얼굴을 지나 수직으로 올라가게 밉니다.",
            enInstruction = "Move the chin slightly back and press the bar upward in a near-vertical path."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_overhead_press_clean_v3_step_3,
            koLabel = "머리 위 고정",
            enLabel = "Lock out overhead",
            koInstruction = "바가 어깨와 중족부 위에 오도록 팔을 곧게 세우고 허리를 젖히지 않습니다.",
            enInstruction = "Finish with the bar over shoulders and mid-foot without leaning back."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_overhead_press_clean_v3_step_4,
            koLabel = "통제 복귀",
            enLabel = "Controlled return",
            koInstruction = "같은 경로로 천천히 내려 어깨 앞 시작 위치로 돌아옵니다.",
            enInstruction = "Lower along the same path with control back to the front of the shoulders."
        )
    ),
    "dumbbell_step_up" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_step_up_clean_v3_step_1,
            koLabel = "발 전체 박스에 올리기",
            enLabel = "Whole foot on box",
            koInstruction = "덤벨을 몸 옆에 들고 한쪽 발 전체를 박스 위에 안정적으로 올립니다.",
            enInstruction = "Hold dumbbells by the sides and place the whole working foot securely on the box."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_step_up_clean_v3_step_2,
            koLabel = "박스 위로 서기",
            enLabel = "Stand on box",
            koInstruction = "바닥 발로 뛰지 않고 박스 위 발로 밀어 몸을 위로 올립니다.",
            enInstruction = "Drive through the foot on the box and rise without jumping from the floor."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_step_up_clean_v3_step_3,
            koLabel = "통제해서 내려오기",
            enLabel = "Lower with control",
            koInstruction = "같은 쪽 발로 균형을 잡으며 반동 없이 천천히 내려옵니다.",
            enInstruction = "Keep balance through the same working foot and step down slowly."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_step_up_clean_v3_step_4,
            koLabel = "반대발로 교대",
            enLabel = "Switch sides",
            koInstruction = "다음 반복은 반대쪽 발 전체를 박스 위에 올려 좌우를 번갈아 진행합니다.",
            enInstruction = "For the next rep, place the opposite foot fully on the box and alternate sides."
        )
    ),
    "glute_bridge" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_glute_bridge_clean_v2_step_1,
            koLabel = "누운 자세",
            enLabel = "Lie down",
            koInstruction = "누운 자세: 매트에 누워 무릎을 세우고 발을 엉덩이 가까이에 두어 뒤꿈치로 바닥을 누릅니다.",
            enInstruction = "Lie on the mat with knees bent, feet near the hips, and heels pressing into the floor."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_glute_bridge_clean_v2_step_2,
            koLabel = "골반 올리기",
            enLabel = "Lift hips",
            koInstruction = "골반 올리기: 갈비뼈를 낮춘 채 둔근으로 골반을 들어 어깨부터 무릎까지 긴 선을 만듭니다.",
            enInstruction = "Keep ribs down and lift the hips with the glutes until shoulders, hips, and knees line up."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_glute_bridge_clean_v2_step_3,
            koLabel = "둔근 수축 후 하강",
            enLabel = "Squeeze glutes then lower",
            koInstruction = "둔근 수축 후 하강: 상단에서 허리를 꺾지 않고 둔근을 조인 뒤 골반을 천천히 매트로 내립니다.",
            enInstruction = "Squeeze the glutes at the top without arching, then lower the hips slowly to the mat."
        )
    ),
    "cable_glute_kickback" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_glute_kickback_clean_v5_step_1,
            koLabel = "스트랩 연결",
            enLabel = "Attach strap",
            koInstruction = "스트랩 연결: 낮은 케이블에 발목 스트랩을 연결하고 손잡이나 기둥을 잡아 골반을 정면으로 둡니다.",
            enInstruction = "Attach the ankle strap to a low cable, hold the frame, and keep the pelvis square."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_glute_kickback_clean_v5_step_2,
            koLabel = "뒤로 밀기",
            enLabel = "Kick back",
            koInstruction = "뒤로 밀기: 무릎을 살짝 굽힌 채 발뒤꿈치를 뒤로 보내 엉덩이 뒤쪽으로 케이블을 밀어냅니다.",
            enInstruction = "With a soft knee, drive the heel back and move the cable from the glute."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_glute_kickback_clean_v5_step_3,
            koLabel = "골반 고정 복귀",
            enLabel = "Return with pelvis fixed",
            koInstruction = "골반 고정 복귀: 골반이 돌아가지 않게 버티며 다리를 천천히 몸 아래로 되돌립니다.",
            enInstruction = "Keep the pelvis from rotating and return the leg slowly under the body."
        )
    ),
    "hip_abduction_machine" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hip_abduction_machine_clean_v3_step_1,
            koLabel = "바깥 패드 맞추기",
            enLabel = "Fit outer pads",
            koInstruction = "바깥 패드 맞추기: 양 무릎과 허벅지 바깥쪽이 패드에 닿게 앉고 몸통을 세웁니다.",
            enInstruction = "Sit tall with the outside of both knees and thighs touching the outer pads."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hip_abduction_machine_clean_v3_step_2,
            koLabel = "바깥으로 벌리기",
            enLabel = "Open outward",
            koInstruction = "바깥으로 벌리기: 바깥쪽 패드를 밀어 양 무릎을 대칭으로 벌립니다.",
            enInstruction = "Press both knees outward into the outer pads while keeping your torso upright."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hip_abduction_machine_clean_v3_step_3,
            koLabel = "중앙으로 복귀",
            enLabel = "Return to center",
            koInstruction = "중앙으로 복귀: 패드가 튕기지 않게 통제하며 시작 위치로 돌아옵니다.",
            enInstruction = "Return to center slowly without letting the pads snap back."
        )
    ),
    "hip_adduction_machine" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hip_adduction_machine_clean_v3_step_1,
            koLabel = "벌린 시작 자세",
            enLabel = "Open start",
            koInstruction = "벌린 시작 자세: 다리를 벌린 상태에서 허벅지 안쪽 패드가 양쪽 다리에 붙어 있는지 확인합니다.",
            enInstruction = "Start with both legs open and confirm the inner-thigh pads stay flush against both thighs."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hip_adduction_machine_clean_v3_step_2,
            koLabel = "안쪽으로 모으기",
            enLabel = "Squeeze inward",
            koInstruction = "안쪽으로 모으기: 허벅지 안쪽 패드가 다리에 붙은 상태로 양 무릎을 중앙으로 모읍니다.",
            enInstruction = "Squeeze both knees inward while the inner pads stay flush against your thighs."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hip_adduction_machine_clean_v3_step_3,
            koLabel = "열린 자세로 복귀",
            enLabel = "Return open",
            koInstruction = "열린 자세로 복귀: 패드가 다리에서 떨어지지 않게 천천히 벌어진 시작 자세로 돌아옵니다.",
            enInstruction = "Open back to the start position under control while the pads stay against the inner thighs."
        )
    ),
    "back_extension" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_back_extension_clean_v2_step_1,
            koLabel = "상체 낮추기",
            enLabel = "Lower torso",
            koInstruction = "상체 낮추기: 발을 고정하고 엉덩이를 패드에 둔 채 등을 중립으로 유지합니다.",
            enInstruction = "Secure your feet, keep hips on the pad, and lower with a neutral spine."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_back_extension_clean_v2_step_2,
            koLabel = "일직선까지 펴기",
            enLabel = "Extend to straight",
            koInstruction = "일직선까지 펴기: 둔근과 햄스트링으로 몸을 들어 머리부터 발까지 길게 만듭니다.",
            enInstruction = "Extend through the hips until your body forms a straight line."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_back_extension_clean_v2_step_3,
            koLabel = "천천히 하강",
            enLabel = "Lower with control",
            koInstruction = "천천히 하강: 허리가 꺾이지 않게 같은 경로로 내려옵니다.",
            enInstruction = "Lower along the same path without overextending your lower back."
        )
    ),
    "straight_arm_pulldown" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_straight_arm_pulldown_clean_v3_step_1,
            koLabel = "케이블 높이",
            enLabel = "Set cable height",
            koInstruction = "케이블 높이: 높은 풀리에 바나 로프를 걸고 팔을 거의 편 상태로 엉덩이를 살짝 뒤로 보냅니다.",
            enInstruction = "Attach a bar or rope to the high pulley and hinge slightly with arms almost straight."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_straight_arm_pulldown_clean_v3_step_2,
            koLabel = "허벅지 쪽 당기기",
            enLabel = "Pull toward thighs",
            koInstruction = "허벅지 쪽 당기기: 팔꿈치 각도를 크게 바꾸지 않고 광배근으로 손잡이를 허벅지 앞까지 내립니다.",
            enInstruction = "Pull the handle toward the thighs with the lats while the elbow angle barely changes."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_straight_arm_pulldown_clean_v3_step_3,
            koLabel = "광배 긴장 복귀",
            enLabel = "Return with lats active",
            koInstruction = "광배 긴장 복귀: 어깨가 귀로 올라가지 않게 버티며 손잡이를 천천히 위로 되돌립니다.",
            enInstruction = "Return the handle upward slowly without letting the shoulders shrug."
        )
    ),
    "machine_row" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_machine_row_clean_v3_step_1,
            koLabel = "패드·손잡이 조정",
            enLabel = "Adjust pad and handles",
            koInstruction = "패드·손잡이 조정: 가슴 패드에 몸을 붙이고 팔을 뻗었을 때 손잡이가 편하게 잡히도록 좌석을 맞춥니다.",
            enInstruction = "Set the seat so your chest stays on the pad and the handles are comfortable at reach."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_machine_row_clean_v3_step_2,
            koLabel = "당기기",
            enLabel = "Pull",
            koInstruction = "당기기: 가슴이 패드에서 뜨지 않게 유지하며 팔꿈치를 뒤로 보내 손잡이를 몸 쪽으로 당깁니다.",
            enInstruction = "Pull the handles toward the body by driving elbows back while the chest stays on the pad."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_machine_row_clean_v3_step_3,
            koLabel = "통제 복귀",
            enLabel = "Controlled return",
            koInstruction = "통제 복귀: 어깨가 앞으로 말리지 않게 견갑을 통제하며 팔을 천천히 뻗습니다.",
            enInstruction = "Extend the arms slowly while controlling the shoulder blades."
        )
    ),
    "t_bar_row" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_t_bar_row_clean_v2_step_1,
            koLabel = "패드 밀착",
            enLabel = "Brace on pad",
            koInstruction = "패드 밀착: 가슴을 인클라인 패드에 붙이고 발을 지지대에 단단히 둔 채 손잡이를 잡습니다.",
            enInstruction = "Press the chest into the incline pad, brace the feet, and hold the handles."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_t_bar_row_clean_v2_step_2,
            koLabel = "당기기",
            enLabel = "Pull",
            koInstruction = "당기기: 가슴이 패드에서 뜨지 않게 팔꿈치를 뒤로 보내 손잡이를 하부 갈비뼈 쪽으로 당깁니다.",
            enInstruction = "Drive elbows back and pull the handles toward the lower ribs without lifting off the pad."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_t_bar_row_clean_v2_step_3,
            koLabel = "놓치지 않고 하강",
            enLabel = "Lower without losing tension",
            koInstruction = "놓치지 않고 하강: 견갑을 통제하며 손잡이를 시작 위치로 천천히 돌려놓습니다.",
            enInstruction = "Control the shoulder blades and return the handles slowly to the start."
        )
    ),
    "barbell_bent_over_row" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_bent_over_row_clean_v2_step_1,
            koLabel = "힙힌지 자세",
            enLabel = "Hinge setup",
            koInstruction = "무릎을 살짝 굽히고 등을 중립으로 둔 채 상체를 숙입니다.",
            enInstruction = "Hinge at the hips with soft knees and a neutral back before pulling."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_bent_over_row_clean_v2_step_2,
            koLabel = "갈비뼈 쪽 당기기",
            enLabel = "Row to ribs",
            koInstruction = "팔꿈치를 뒤로 보내며 바를 아랫갈비뼈 쪽으로 당깁니다.",
            enInstruction = "Drive elbows back and row the bar toward the lower ribs."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_bent_over_row_clean_v2_step_3,
            koLabel = "통제 하강",
            enLabel = "Controlled lower",
            koInstruction = "어깨가 앞으로 무너지지 않게 바를 천천히 내립니다.",
            enInstruction = "Lower the bar with control without letting the shoulders collapse forward."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_barbell_bent_over_row_clean_v2_step_4,
            koLabel = "자세 재정렬",
            enLabel = "Reset posture",
            koInstruction = "허리 긴장이 커지면 바를 내려놓고 다시 힙힌지를 잡습니다.",
            enInstruction = "Reset the hinge if back tension rises or posture starts to drift."
        )
    ),
    "conventional_deadlift" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_conventional_deadlift_clean_v2_step_1,
            koLabel = "바와 발 위치",
            enLabel = "Bar over midfoot",
            koInstruction = "바가 발 중간 위에 오게 서고 정강이를 바 가까이에 둡니다.",
            enInstruction = "Stand with the bar over midfoot and keep the shins close to the bar."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_conventional_deadlift_clean_v2_step_2,
            koLabel = "힙힌지와 그립",
            enLabel = "Hinge and grip",
            koInstruction = "엉덩이를 뒤로 접고 등을 중립으로 유지한 채 양손으로 바를 잡습니다.",
            enInstruction = "Hinge at the hips, keep a neutral back, and grip the bar with both hands."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_conventional_deadlift_clean_v2_step_3,
            koLabel = "브레이싱과 장력",
            enLabel = "Brace and take slack",
            koInstruction = "복부와 등에 힘을 주고 바가 몸에서 멀어지지 않게 장력을 만듭니다.",
            enInstruction = "Brace the torso, tighten the lats, and take slack out of the bar before lifting."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_conventional_deadlift_clean_v2_step_4,
            koLabel = "바닥 밀며 기립",
            enLabel = "Drive to stand",
            koInstruction = "바를 정강이와 허벅지 가까이 올리며 무릎과 엉덩이를 함께 폅니다.",
            enInstruction = "Drive through the floor and keep the bar close as knees and hips extend together."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_conventional_deadlift_clean_v2_step_5,
            koLabel = "통제 하강",
            enLabel = "Controlled lower",
            koInstruction = "엉덩이를 먼저 뒤로 보내며 바를 몸 가까이 따라 내려놓습니다.",
            enInstruction = "Send the hips back first and lower the bar close to the body with control."
        )
    ),
    "cable_pullover" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_pullover_clean_v3_step_1,
            koLabel = "케이블 높이",
            enLabel = "Set cable height",
            koInstruction = "케이블 높이: 높은 풀리 앞에서 엉덩이를 살짝 뒤로 빼고 팔을 길게 뻗어 손잡이를 잡습니다.",
            enInstruction = "Stand at the high pulley, hinge slightly, and reach long to hold the handle."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_pullover_clean_v3_step_2,
            koLabel = "팔 길게 당기기",
            enLabel = "Pull with long arms",
            koInstruction = "팔 길게 당기기: 팔꿈치를 살짝만 굽힌 채 손잡이를 허벅지 앞쪽으로 당기며 광배근을 조입니다.",
            enInstruction = "With only a slight elbow bend, pull the handle toward the front of the thighs with the lats."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_pullover_clean_v3_step_3,
            koLabel = "범위 내 복귀",
            enLabel = "Return in range",
            koInstruction = "범위 내 복귀: 허리가 꺾이지 않게 같은 경로로 손잡이를 천천히 올려 시작 자세로 돌아갑니다.",
            enInstruction = "Raise the handle back along the same path without arching the lower back."
        )
    ),
    "inverted_row" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_inverted_row_clean_v2_step_1,
            koLabel = "바 아래 자세",
            enLabel = "Set under bar",
            koInstruction = "바 아래 자세: 바 아래에 누워 양손으로 바를 잡고 발뒤꿈치부터 머리까지 긴 선을 만듭니다.",
            enInstruction = "Lie under the bar, grip it with both hands, and make one long line from heels to head."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_inverted_row_clean_v2_step_2,
            koLabel = "가슴을 바 쪽으로 당기기",
            enLabel = "Pull chest to bar",
            koInstruction = "가슴을 바 쪽으로 당기기: 몸통을 일직선으로 유지한 채 팔꿈치를 뒤로 보내 가슴을 바 가까이 당깁니다.",
            enInstruction = "Pull your chest toward the bar by driving the elbows back while keeping your body straight."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_inverted_row_clean_v2_step_3,
            koLabel = "통제 하강",
            enLabel = "Controlled lower",
            koInstruction = "통제 하강: 몸통을 일직선으로 유지한 채 팔을 다시 펴며 내려옵니다.",
            enInstruction = "Lower back to straight arms while keeping your body in one line."
        )
    ),
    "dumbbell_shrug" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_shrug_clean_v2_step_1,
            koLabel = "몸통 세우기",
            enLabel = "Stand tall",
            koInstruction = "몸통 세우기: 양손 덤벨을 몸 옆에 두고 복부를 조여 어깨가 앞으로 말리지 않게 섭니다.",
            enInstruction = "Hold dumbbells by the sides, brace the abs, and stand without letting shoulders roll forward."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_shrug_clean_v2_step_2,
            koLabel = "어깨 수직 상승",
            enLabel = "Shrug straight up",
            koInstruction = "어깨 수직 상승: 팔꿈치를 굽히지 않고 어깨만 귀 쪽으로 들어 올립니다.",
            enInstruction = "Lift only your shoulders straight upward while the dumbbells stay at your sides."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_shrug_clean_v2_step_3,
            koLabel = "천천히 내리기",
            enLabel = "Lower slowly",
            koInstruction = "천천히 내리기: 목을 길게 유지하며 어깨를 통제해서 내립니다.",
            enInstruction = "Lower the shoulders under control while keeping your neck neutral."
        )
    ),
    "pec_deck_fly" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_pec_deck_fly_clean_v3_step_1,
            koLabel = "패드 높이",
            enLabel = "Set pad height",
            koInstruction = "패드 높이: 팔꿈치나 전완이 패드에 편하게 닿도록 좌석 높이를 맞추고 가슴을 세웁니다.",
            enInstruction = "Adjust the seat so elbows or forearms sit comfortably on the pads, then lift the chest."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_pec_deck_fly_clean_v3_step_2,
            koLabel = "앞으로 모으기",
            enLabel = "Bring forward",
            koInstruction = "앞으로 모으기: 팔꿈치 각도를 유지한 채 양쪽 패드를 가슴 앞에서 모읍니다.",
            enInstruction = "Keep the elbow angle steady and bring both pads together in front of the chest."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_pec_deck_fly_clean_v3_step_3,
            koLabel = "가슴 범위 내 복귀",
            enLabel = "Return in chest range",
            koInstruction = "가슴 범위 내 복귀: 어깨가 과하게 뒤로 젖혀지기 전까지만 패드를 천천히 열어 돌아갑니다.",
            enInstruction = "Open the pads slowly only until the shoulders stay comfortable."
        )
    ),
    "incline_machine_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_incline_machine_press_clean_v2_step_1,
            koLabel = "손잡이 높이",
            enLabel = "Set handle height",
            koInstruction = "등을 기울어진 패드에 붙이고 손잡이가 윗가슴 옆에 오도록 앉습니다.",
            enInstruction = "Sit with the back on the angled pad and the handles beside the upper chest."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_incline_machine_press_clean_v2_step_2,
            koLabel = "대각선 밀기",
            enLabel = "Press diagonally",
            koInstruction = "등과 엉덩이를 패드에 붙인 채 손잡이를 가슴 앞 대각선 위로 밀어냅니다.",
            enInstruction = "Keep the back and hips on the pad and press the handles diagonally forward and upward."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_incline_machine_press_clean_v2_step_3,
            koLabel = "어깨 범위 내 복귀",
            enLabel = "Return in shoulder range",
            koInstruction = "어깨가 앞쪽으로 말리지 않는 범위에서 손잡이를 윗가슴 옆으로 천천히 되돌립니다.",
            enInstruction = "Return the handles beside the upper chest without letting the shoulders roll forward."
        )
    ),
    "dumbbell_floor_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_floor_press_clean_v2_step_1,
            koLabel = "바닥에 눕기",
            enLabel = "Lie on floor",
            koInstruction = "바닥에 눕기: 무릎을 세우고 양발을 바닥에 둔 채 덤벨을 가슴 양옆에 준비합니다.",
            enInstruction = "Lie on the floor with knees bent, feet planted, and dumbbells beside the chest."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_floor_press_clean_v2_step_2,
            koLabel = "팔꿈치·손목 정렬",
            enLabel = "Set elbows and wrists",
            koInstruction = "팔꿈치·손목 정렬: 팔꿈치는 몸통에서 30~45도 열고 손목을 덤벨 아래에 세웁니다.",
            enInstruction = "Set elbows about 30 to 45 degrees from the torso and stack wrists under the dumbbells."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_floor_press_clean_v2_step_3,
            koLabel = "위로 밀기",
            enLabel = "Press up",
            koInstruction = "위로 밀기: 등은 바닥에 둔 채 양덤벨을 가슴 위로 밀고 팔꿈치는 잠그지 않습니다.",
            enInstruction = "Press both dumbbells above the chest while the back stays on the floor and elbows do not lock."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_floor_press_clean_v2_step_4,
            koLabel = "팔꿈치 가볍게 하강",
            enLabel = "Lower to elbows",
            koInstruction = "팔꿈치 가볍게 하강: 팔꿈치가 바닥 가까이 오도록 천천히 내려 다음 반복을 준비합니다.",
            enInstruction = "Lower under control until the elbows lightly approach the floor before the next rep."
        )
    ),
    "assisted_dip" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_assisted_dip_clean_v2_step_1,
            koLabel = "보조 패드 진입",
            enLabel = "Enter assist pad",
            koInstruction = "보조 패드 진입: 손잡이를 잡고 한쪽 무릎씩 보조 패드에 올려 몸을 안정시킵니다.",
            enInstruction = "Hold the handles and place one knee at a time on the assist pad to stabilize."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_assisted_dip_clean_v2_step_2,
            koLabel = "손잡이 지지",
            enLabel = "Support handles",
            koInstruction = "손잡이 지지: 어깨를 낮추고 팔꿈치를 살짝 굽힌 채 몸통을 손잡이 사이에 세웁니다.",
            enInstruction = "Depress the shoulders, keep elbows softly bent, and stack the torso between the handles."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_assisted_dip_clean_v2_step_3,
            koLabel = "통증 없는 하강",
            enLabel = "Lower pain-free",
            koInstruction = "통증 없는 하강: 팔꿈치를 뒤로 접으며 어깨 앞쪽이 편한 범위까지만 몸을 내립니다.",
            enInstruction = "Bend the elbows back and lower only as far as the front of the shoulders stays comfortable."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_assisted_dip_clean_v2_step_4,
            koLabel = "밀어 올라오기",
            enLabel = "Drive up",
            koInstruction = "밀어 올라오기: 손잡이를 아래로 누르며 팔꿈치를 펴 보조 패드와 함께 올라옵니다.",
            enInstruction = "Press the handles down and extend the elbows as the assist pad rises with you."
        )
    ),
    "cable_chest_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_chest_press_clean_v2_step_1,
            koLabel = "케이블 높이",
            enLabel = "Set cable height",
            koInstruction = "케이블 높이: 손잡이가 가슴 중간 높이에서 당겨지도록 양쪽 케이블을 맞춥니다.",
            enInstruction = "Set both cable handles so they pull from mid-chest height."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_chest_press_clean_v2_step_2,
            koLabel = "스탠스 잡기",
            enLabel = "Set stance",
            koInstruction = "스탠스 잡기: 한 발을 앞으로 두고 양손 손잡이를 가슴 옆에 세워 몸통을 고정합니다.",
            enInstruction = "Step one foot forward and hold both handles beside the chest with the torso braced."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_chest_press_clean_v2_step_3,
            koLabel = "앞으로 밀기",
            enLabel = "Press forward",
            koInstruction = "앞으로 밀기: 어깨가 들리지 않게 양손을 같은 속도로 가슴 앞쪽으로 밀어냅니다.",
            enInstruction = "Press both hands forward evenly without letting the shoulders shrug."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_chest_press_clean_v2_step_4,
            koLabel = "몸통 고정 복귀",
            enLabel = "Return",
            koInstruction = "몸통 고정 복귀: 케이블 장력을 유지하며 손잡이를 가슴 옆 시작 위치로 천천히 되돌립니다.",
            enInstruction = "Keep cable tension and return the handles slowly beside the chest."
        )
    ),
    "close_grip_pushup" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_close_grip_pushup_clean_v2_step_1,
            koLabel = "손 위치",
            enLabel = "Set hands",
            koInstruction = "손 위치: 손을 가슴 아래 좁은 간격으로 두고 머리부터 뒤꿈치까지 일직선을 만듭니다.",
            enInstruction = "Place the hands narrowly under the chest and form a straight line from head to heels."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_close_grip_pushup_clean_v2_step_2,
            koLabel = "팔꿈치 붙여 하강",
            enLabel = "Lower",
            koInstruction = "팔꿈치 붙여 하강: 팔꿈치가 옆으로 벌어지지 않게 몸통 가까이 접으며 몸 전체를 함께 내립니다.",
            enInstruction = "Lower the whole body while the elbows bend back close to the ribs, not out to the sides."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_close_grip_pushup_clean_v2_step_3,
            koLabel = "하단 통제",
            enLabel = "Control bottom",
            koInstruction = "하단 통제: 가슴을 바닥 가까이 두고 손 위치와 몸통 일직선을 유지합니다.",
            enInstruction = "Pause near the floor while keeping the hands narrow and the body in one straight line."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_close_grip_pushup_clean_v2_step_4,
            koLabel = "밀어 올라오기",
            enLabel = "Drive up",
            koInstruction = "밀어 올라오기: 손바닥으로 바닥을 밀어 시작 자세로 돌아오되 허리가 처지지 않게 합니다.",
            enInstruction = "Press the floor away to return to the start while keeping the hips from sagging."
        )
    ),
    "arnold_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_arnold_press_clean_v2_step_1,
            koLabel = "얼굴 앞 시작",
            enLabel = "Start in front",
            koInstruction = "얼굴 앞 시작: 손바닥이 얼굴을 향하게 덤벨을 어깨 앞에 두고 복부를 조입니다.",
            enInstruction = "Hold dumbbells in front of the shoulders with palms facing you and brace the trunk."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_arnold_press_clean_v2_step_2,
            koLabel = "회전 시작",
            enLabel = "Begin rotation",
            koInstruction = "회전 시작: 팔꿈치를 벌리며 손바닥이 앞을 향하도록 덤벨을 바깥으로 돌립니다.",
            enInstruction = "Rotate the dumbbells outward as elbows open and palms turn forward."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_arnold_press_clean_v2_step_3,
            koLabel = "위로 밀기",
            enLabel = "Press up",
            koInstruction = "위로 밀기: 회전한 자세에서 덤벨을 머리 위로 밀되 허리를 젖히지 않습니다.",
            enInstruction = "Press overhead from the rotated position without leaning back."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_arnold_press_clean_v2_step_4,
            koLabel = "같은 경로 복귀",
            enLabel = "Return same path",
            koInstruction = "같은 경로 복귀: 덤벨을 어깨 앞으로 내리며 손바닥이 다시 얼굴을 향하게 돌립니다.",
            enInstruction = "Lower along the same path and rotate palms back toward the face."
        )
    ),
    "front_raise" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_front_raise_clean_v4_step_1,
            koLabel = "덤벨 시작 위치",
            enLabel = "Start dumbbells",
            koInstruction = "양손 덤벨을 허벅지 앞에 두고 몸통을 세운 뒤 복부에 힘을 줍니다.",
            enInstruction = "Hold the dumbbells in front of the thighs, stand tall, and brace the core."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_front_raise_clean_v4_step_2,
            koLabel = "정면으로 올리기",
            enLabel = "Raise forward",
            koInstruction = "팔꿈치를 살짝 굽힌 채 덤벨을 몸 앞쪽으로 천천히 들어 올립니다.",
            enInstruction = "With a soft elbow bend, raise both dumbbells forward in front of the body."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_front_raise_clean_v4_step_3,
            koLabel = "어깨 높이",
            enLabel = "Shoulder height",
            koInstruction = "덤벨이 어깨 높이에 오면 몸을 젖히지 않고 손목을 중립으로 유지합니다.",
            enInstruction = "At shoulder height, keep the torso still and the wrists neutral."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_front_raise_clean_v4_step_4,
            koLabel = "통제 하강",
            enLabel = "Lower with control",
            koInstruction = "반동 없이 같은 경로로 덤벨을 허벅지 앞까지 천천히 내립니다.",
            enInstruction = "Lower the dumbbells along the same path without swinging."
        )
    ),
    "cable_lateral_raise" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_lateral_raise_clean_v2_step_1,
            koLabel = "낮은 케이블 세팅",
            enLabel = "Set low cable",
            koInstruction = "낮은 케이블 세팅: 낮은 풀리에 손잡이를 연결하고 케이블이 몸 앞을 지나가게 옆으로 섭니다.",
            enInstruction = "Attach a handle to the low pulley and stand side-on so the cable crosses in front."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_lateral_raise_clean_v2_step_2,
            koLabel = "옆으로 올리기",
            enLabel = "Raise to side",
            koInstruction = "옆으로 올리기: 팔꿈치를 살짝 굽힌 채 손잡이를 어깨 높이까지 옆으로 들어 올립니다.",
            enInstruction = "With a soft elbow bend, raise the handle out to shoulder height."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_lateral_raise_clean_v2_step_3,
            koLabel = "장력 유지 하강",
            enLabel = "Lower with tension",
            koInstruction = "장력 유지 하강: 케이블이 느슨해지기 전까지 손잡이를 천천히 내려 어깨 긴장을 유지합니다.",
            enInstruction = "Lower the handle slowly while keeping cable tension on the shoulder."
        )
    ),
    "landmine_press" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_landmine_press_clean_v2_step_1,
            koLabel = "바 고정 확인",
            enLabel = "Check bar anchor",
            koInstruction = "바벨 한쪽 끝이 바닥 거치대에 고정됐는지 확인하고 반대쪽 끝을 양손으로 잡습니다.",
            enInstruction = "Confirm one end of the barbell is anchored low, then hold the free end with both hands."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_landmine_press_clean_v2_step_2,
            koLabel = "어깨 앞 세팅",
            enLabel = "Set at shoulder",
            koInstruction = "바 끝을 가슴·어깨 앞에 두고 발을 단단히 딛어 몸통을 고정합니다.",
            enInstruction = "Set the bar end in front of the chest and shoulder, then brace with a stable stance."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_landmine_press_clean_v2_step_3,
            koLabel = "대각선 밀기",
            enLabel = "Press diagonally",
            koInstruction = "바벨을 수직이 아니라 몸 앞 대각선 위쪽으로 밀어 팔을 뻗습니다.",
            enInstruction = "Press the bar diagonally forward and upward, not straight overhead."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_landmine_press_clean_v2_step_4,
            koLabel = "통제 복귀",
            enLabel = "Controlled return",
            koInstruction = "고정점이 흔들리지 않게 같은 대각선 경로로 바를 어깨 앞까지 되돌립니다.",
            enInstruction = "Keep the anchor stable and return the bar to the shoulder along the same diagonal path."
        )
    ),
    "prone_y_raise" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_prone_y_raise_clean_v4_step_1,
            koLabel = "벤치 엎드리기",
            enLabel = "Lie prone on bench",
            koInstruction = "가슴을 인클라인 벤치에 지지하고 목을 중립으로 둔 채 팔을 아래로 내립니다.",
            enInstruction = "Support the chest on the incline bench, keep the neck neutral, and let the arms hang down."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_prone_y_raise_clean_v4_step_2,
            koLabel = "Y 방향 올리기",
            enLabel = "Raise in Y shape",
            koInstruction = "엄지를 위로 둔 채 팔을 머리 앞 대각선 방향으로 들어 Y자 형태를 만듭니다.",
            enInstruction = "With thumbs up, raise the arms diagonally forward into a Y shape."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_prone_y_raise_clean_v4_step_3,
            koLabel = "어깨 낮춰 하강",
            enLabel = "Lower shoulders down",
            koInstruction = "승모근을 과하게 쓰지 않게 어깨를 낮추고 같은 Y 경로로 천천히 내립니다.",
            enInstruction = "Keep the shoulders down and lower along the same Y path without shrugging."
        )
    ),
    "hammer_curl" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hammer_curl_clean_v2_step_1,
            koLabel = "중립 그립",
            enLabel = "Neutral grip",
            koInstruction = "중립 그립: 양손 덤벨을 몸 옆에 두고 손바닥이 서로 마주 보게 세웁니다.",
            enInstruction = "Hold the dumbbells by your sides with palms facing each other."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hammer_curl_clean_v2_step_2,
            koLabel = "중립 그립으로 올리기",
            enLabel = "Curl with neutral grip",
            koInstruction = "중립 그립으로 올리기: 팔꿈치를 옆구리에 두고 손바닥이 서로 마주 보게 올립니다.",
            enInstruction = "Curl upward with palms facing each other and elbows pinned near your ribs."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hammer_curl_clean_v2_step_3,
            koLabel = "상단에서 통제",
            enLabel = "Control at top",
            koInstruction = "상단에서 통제: 어깨가 들리지 않게 멈춘 뒤 천천히 내려옵니다.",
            enInstruction = "Pause near the shoulders, then lower without swinging your upper arms."
        )
    ),
    "preacher_curl_machine" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_preacher_curl_machine_clean_v2_step_1,
            koLabel = "상완 패드 고정",
            enLabel = "Fix upper arms",
            koInstruction = "상완 패드 고정: 상완을 패드에 붙이고 손목이 꺾이지 않게 손잡이를 잡습니다.",
            enInstruction = "Pin the upper arms to the pad and hold the handles with straight wrists."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_preacher_curl_machine_clean_v2_step_2,
            koLabel = "말아 올리기",
            enLabel = "Curl up",
            koInstruction = "말아 올리기: 팔꿈치를 패드에 고정한 채 손잡이를 어깨 쪽으로 당깁니다.",
            enInstruction = "Curl the handles toward your shoulders while keeping both elbows anchored on the pad."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_preacher_curl_machine_clean_v2_step_3,
            koLabel = "통제 하강",
            enLabel = "Control down",
            koInstruction = "통제 하강: 팔을 완전히 잠그기 전까지 천천히 내리고 반동 없이 다음 반복을 준비합니다.",
            enInstruction = "Lower slowly before the elbows fully lock, then prepare the next rep without swinging."
        )
    ),
    "rope_overhead_triceps" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_rope_overhead_triceps_clean_v3_step_1,
            koLabel = "세팅·팔꿈치 고정",
            enLabel = "Set and fix elbows",
            koInstruction = "높은 풀리의 로프를 잡고 케이블이 뒤에서 당겨지는 위치에 선 뒤 팔꿈치를 머리 옆에 고정합니다.",
            enInstruction = "Hold the rope from a high pulley, let the cable pull from behind, and fix the elbows near the head."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_rope_overhead_triceps_clean_v3_step_2,
            koLabel = "위로 펴기",
            enLabel = "Extend overhead",
            koInstruction = "상완을 크게 흔들지 말고 팔꿈치를 펴 로프 끝을 머리 위 앞으로 보냅니다.",
            enInstruction = "Extend the elbows and send the rope ends forward overhead without swinging the upper arms."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_rope_overhead_triceps_clean_v3_step_3,
            koLabel = "장력 유지 복귀",
            enLabel = "Return with tension",
            koInstruction = "케이블이 느슨해지지 않게 같은 경로로 로프를 머리 뒤쪽까지 천천히 되돌립니다.",
            enInstruction = "Keep the cable taut and return the rope behind the head along the same path."
        )
    ),
    "reverse_curl" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_reverse_curl_clean_v2_step_1,
            koLabel = "손등 위 그립",
            enLabel = "Overhand grip",
            koInstruction = "손등 위 그립: 손등이 위를 향하게 바를 잡고 팔꿈치를 옆구리 가까이에 둡니다.",
            enInstruction = "Grip the bar palms-down and keep elbows close to the ribs."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_reverse_curl_clean_v2_step_2,
            koLabel = "손등 위로 컬",
            enLabel = "Curl overhand",
            koInstruction = "손등 위로 컬: 손등이 위를 향한 채 팔꿈치를 고정하고 바를 올립니다.",
            enInstruction = "Curl the bar upward with palms down and elbows fixed near your ribs."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_reverse_curl_clean_v2_step_3,
            koLabel = "전완 긴장 유지",
            enLabel = "Hold forearm tension",
            koInstruction = "전완 긴장 유지: 손목을 꺾지 않고 상단에서 통제한 뒤 내려옵니다.",
            enInstruction = "Keep wrists straight at the top, then lower with control."
        )
    ),
    "side_plank" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_side_plank_clean_v2_step_1,
            koLabel = "팔꿈치 위치",
            enLabel = "Set elbow",
            koInstruction = "팔꿈치 위치: 팔꿈치를 어깨 바로 아래에 두고 옆으로 누워 다리를 길게 포갭니다.",
            enInstruction = "Lie on your side with the elbow under the shoulder and legs stacked long."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_side_plank_clean_v2_step_2,
            koLabel = "골반 들어 일직선",
            enLabel = "Lift hips into line",
            koInstruction = "골반 들어 일직선: 팔꿈치와 발 옆면으로 바닥을 밀어 머리부터 발끝까지 긴 선을 만듭니다.",
            enInstruction = "Press through the elbow and the sides of the feet to form a long head-to-heel line."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_side_plank_clean_v2_step_3,
            koLabel = "호흡 유지",
            enLabel = "Keep breathing",
            koInstruction = "호흡 유지: 골반이 뒤로 빠지거나 아래로 떨어지지 않게 버티며 천천히 호흡합니다.",
            enInstruction = "Breathe slowly while keeping the hips from dropping or rotating backward."
        )
    ),
    "reverse_crunch" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_reverse_crunch_clean_v2_step_1,
            koLabel = "누운 자세",
            enLabel = "Lie down",
            koInstruction = "누운 자세: 매트에 누워 무릎을 굽히고 허리가 뜨지 않게 갈비뼈를 낮춥니다.",
            enInstruction = "Lie on the mat with knees bent and ribs down so the lower back does not pop up."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_reverse_crunch_clean_v2_step_2,
            koLabel = "골반 말기",
            enLabel = "Curl pelvis",
            koInstruction = "골반 말기: 복부 힘으로 골반을 말아 무릎이 가슴 쪽으로 가까워지게 합니다.",
            enInstruction = "Use the abs to curl the pelvis so the knees move closer to the chest."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_reverse_crunch_clean_v2_step_3,
            koLabel = "반동 없이 하강",
            enLabel = "Lower without momentum",
            koInstruction = "반동 없이 하강: 허리가 튀어 오르지 않게 골반과 다리를 천천히 내려놓습니다.",
            enInstruction = "Lower the pelvis and legs slowly without letting the lower back spring upward."
        )
    ),
    "cable_crunch" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_crunch_clean_v6_step_1,
            koLabel = "무릎 꿇고 로프 잡기",
            enLabel = "Kneel and grip rope",
            koInstruction = "무릎 꿇고 로프 잡기: 높은 풀리 앞에 무릎을 꿇고 로프를 관자놀이 옆에 둔 채 엉덩이와 골반 위치를 고정합니다.",
            enInstruction = "Kneel in front of the high pulley, hold the rope beside the temples, and keep the hips and pelvis fixed."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_crunch_clean_v6_step_2,
            koLabel = "갈비뼈 말기",
            enLabel = "Curl ribs down",
            koInstruction = "갈비뼈 말기: 팔로 당기지 말고 복부로 갈비뼈를 골반 쪽으로 말아 내리며 척추를 둥글게 접습니다.",
            enInstruction = "Do not pull with the arms; use the abdominals to curl the ribs toward the pelvis and round the spine."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_cable_crunch_clean_v6_step_3,
            koLabel = "복부 긴장 복귀",
            enLabel = "Return with abs engaged",
            koInstruction = "복부 긴장 복귀: 케이블 장력을 유지한 채 같은 경로로 천천히 올라와 다음 반복을 준비합니다.",
            enInstruction = "Keep cable tension, return slowly along the same path, and prepare for the next rep."
        )
    ),
    "hanging_knee_raise" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hanging_knee_raise_clean_v2_step_1,
            koLabel = "매달리기",
            enLabel = "Hang tall",
            koInstruction = "매달리기: 양손으로 바를 잡고 어깨를 안정시킨 채 다리를 아래로 둡니다.",
            enInstruction = "Hang from the bar with shoulders stable and legs together."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hanging_knee_raise_clean_v2_step_2,
            koLabel = "무릎 올리기",
            enLabel = "Raise knees",
            koInstruction = "무릎 올리기: 반동 없이 양 무릎을 가슴 쪽으로 끌어올립니다.",
            enInstruction = "Raise both knees toward your chest without swinging."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_hanging_knee_raise_clean_v2_step_3,
            koLabel = "통제 하강",
            enLabel = "Controlled lower",
            koInstruction = "통제 하강: 다리를 천천히 내려 시작 자세로 돌아옵니다.",
            enInstruction = "Lower your legs slowly back to the start position."
        )
    ),
    "mountain_climber" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_mountain_climber_clean_v2_step_1,
            koLabel = "푸시업 자세",
            enLabel = "Push-up stance",
            koInstruction = "푸시업 자세: 양손을 어깨 아래에 두고 머리부터 뒤꿈치까지 일직선을 만듭니다.",
            enInstruction = "Place both hands under the shoulders and form a straight line from head to heels."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_mountain_climber_clean_v2_step_2,
            koLabel = "오른무릎 당기기",
            enLabel = "Drive right knee",
            koInstruction = "오른무릎 당기기: 양손을 고정한 채 오른무릎을 가슴 쪽으로 당기고 왼다리는 뒤로 뻗습니다.",
            enInstruction = "Keep both hands planted, drive the right knee toward the chest, and keep the left leg long."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_mountain_climber_clean_v2_step_3,
            koLabel = "몸통 유지",
            enLabel = "Hold torso",
            koInstruction = "몸통 유지: 오른다리를 뒤로 보내 다시 일직선 플랭크를 만들고 엉덩이를 낮게 유지합니다.",
            enInstruction = "Return the right leg back to a straight plank while keeping the hips low and level."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_mountain_climber_clean_v2_step_4,
            koLabel = "왼무릎 당기기",
            enLabel = "Drive left knee",
            koInstruction = "왼무릎 당기기: 이번에는 왼무릎을 가슴 쪽으로 당기고 오른다리를 뒤로 뻗어 교대합니다.",
            enInstruction = "Drive the left knee toward the chest while the right leg stays extended behind you."
        )
    ),
    "farmer_carry" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_farmer_carry_clean_v2_step_1,
            koLabel = "키 크게 서기",
            enLabel = "Stand tall",
            koInstruction = "키 크게 서기: 양손 덤벨을 몸 옆에 들고 어깨를 낮춰 중심을 잡습니다.",
            enInstruction = "Stand tall with both dumbbells hanging at your sides."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_farmer_carry_clean_v2_step_2,
            koLabel = "첫 걸음",
            enLabel = "First step",
            koInstruction = "첫 걸음: 몸통을 세운 채 짧고 안정적인 보폭으로 걷기 시작합니다.",
            enInstruction = "Take a controlled step while the weights stay beside your thighs."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_farmer_carry_clean_v2_step_3,
            koLabel = "수평 유지",
            enLabel = "Stay level",
            koInstruction = "수평 유지: 몸이 한쪽으로 기울지 않게 어깨와 골반을 평행하게 유지합니다.",
            enInstruction = "Keep shoulders and hips level as you continue walking."
        )
    ),
    "elliptical" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_elliptical_clean_v2_step_1,
            koLabel = "발·손잡이 세팅",
            enLabel = "Set feet and handles",
            koInstruction = "발·손잡이 세팅: 양발을 긴 페달 위에 올리고 양손으로 움직이는 손잡이를 가볍게 잡습니다.",
            enInstruction = "Place both feet on the long pedals and lightly hold the moving handles."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_elliptical_clean_v2_step_2,
            koLabel = "오른발 전방 보폭",
            enLabel = "Right stride forward",
            koInstruction = "오른발 전방 보폭: 오른발 페달이 앞아래로 움직일 때 반대 손잡이를 밀고 당기며 리듬을 만듭니다.",
            enInstruction = "As the right pedal moves forward and down, push and pull the handles in a smooth rhythm."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_elliptical_clean_v2_step_3,
            koLabel = "왼발 전방 보폭",
            enLabel = "Left stride forward",
            koInstruction = "왼발 전방 보폭: 왼발 페달이 앞아래로 움직이게 교대하며 상체는 세우고 발을 페달에서 떼지 않습니다.",
            enInstruction = "Alternate so the left pedal moves forward and down while the torso stays tall and feet stay planted."
        )
    ),
    "stair_climber" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_stair_climber_clean_v2_step_1,
            koLabel = "난간 보조",
            enLabel = "Use rails lightly",
            koInstruction = "손은 난간에 가볍게 올리고 몸통을 세운 채 첫 계단을 천천히 밟습니다.",
            enInstruction = "Rest the hands lightly on the rails, keep the torso tall, and step onto the first stair with control."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_stair_climber_clean_v2_step_2,
            koLabel = "발 전체로 오르기",
            enLabel = "Step with full foot",
            koInstruction = "발바닥 전체를 계단에 올리고 반대쪽 발을 들어 다음 계단으로 자연스럽게 이어갑니다.",
            enInstruction = "Place the whole foot on the stair and lift the opposite foot into the next step without leaning on the rails."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_stair_climber_clean_v2_step_3,
            koLabel = "속도 조절",
            enLabel = "Control speed",
            koInstruction = "속도를 올리더라도 상체를 흔들지 말고 계단 리듬과 호흡을 일정하게 유지합니다.",
            enInstruction = "As speed increases, keep the torso steady and maintain a consistent stair rhythm and breathing pattern."
        )
    ),
    "rowing_machine" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_rowing_machine_clean_v2_step_1,
            koLabel = "캐치 자세",
            enLabel = "Catch position",
            koInstruction = "캐치 자세: 발을 스트랩에 고정하고 무릎을 굽힌 채 팔을 길게 뻗어 손잡이를 잡습니다.",
            enInstruction = "Strap the feet in, bend the knees, hinge slightly forward, and hold the handle with long arms."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_rowing_machine_clean_v2_step_2,
            koLabel = "다리로 밀기",
            enLabel = "Drive with legs",
            koInstruction = "다리로 밀기: 팔은 거의 편 채 다리 힘으로 시트를 뒤로 보내고 손잡이를 배 쪽으로 이동시킵니다.",
            enInstruction = "Push with the legs first while the arms stay nearly straight and the handle moves toward the abdomen."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_rowing_machine_clean_v2_step_3,
            koLabel = "피니시 당기기",
            enLabel = "Finish the pull",
            koInstruction = "피니시 당기기: 다리를 거의 편 뒤 몸통을 살짝 뒤로 두고 손잡이를 갈비뼈 아래까지 당깁니다.",
            enInstruction = "Finish with legs almost straight, a slight backward lean, and the handle at the lower ribs."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_rowing_machine_clean_v2_step_4,
            koLabel = "팔-몸통-다리 복귀",
            enLabel = "Arms-body-legs return",
            koInstruction = "팔-몸통-다리 복귀: 팔을 먼저 펴고 몸통을 세운 뒤 무릎을 굽혀 캐치 자세로 돌아옵니다.",
            enInstruction = "Extend the arms first, return the torso, then bend the knees back into the catch."
        )
    ),
    "battle_rope" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_battle_rope_clean_v2_step_1,
            koLabel = "스탠스 잡기",
            enLabel = "Set stance",
            koInstruction = "스탠스 잡기: 무릎과 엉덩이를 살짝 굽히고 로프 끝을 양손으로 잡아 몸통을 낮춥니다.",
            enInstruction = "Bend hips and knees slightly, hold the rope ends, and set a low athletic stance."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_battle_rope_clean_v2_step_2,
            koLabel = "몸통 고정",
            enLabel = "Brace torso",
            koInstruction = "몸통 고정: 복부를 조여 어깨와 골반이 흔들리지 않게 하고 로프를 가볍게 긴장시킵니다.",
            enInstruction = "Brace the torso so shoulders and hips stay quiet while the ropes stay lightly tensioned."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_battle_rope_clean_v2_step_3,
            koLabel = "팔 교대 웨이브",
            enLabel = "Alternate waves",
            koInstruction = "팔 교대 웨이브: 한 손씩 빠르게 올리고 내려 로프 파형이 좌우로 교대되게 합니다.",
            enInstruction = "Raise and lower one hand at a time so the rope waves alternate side to side."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_battle_rope_clean_v2_step_4,
            koLabel = "자세 유지 종료",
            enLabel = "Finish tall",
            koInstruction = "자세 유지 종료: 마지막 파형까지 허리를 꺾지 않고 같은 낮은 자세를 유지합니다.",
            enInstruction = "Keep the low stance through the final waves without arching the lower back."
        )
    ),
    "sled_push" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_sled_push_clean_v3_step_1,
            koLabel = "공간·중량 확인",
            enLabel = "Check space and load",
            koInstruction = "이동할 공간을 확인하고 슬레드 중앙 기둥에 원판이 안정적으로 꽂혔는지 봅니다.",
            enInstruction = "Check the lane and confirm the plates are secured on the sled post."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_sled_push_clean_v3_step_2,
            koLabel = "손잡이 잡기",
            enLabel = "Grip handles",
            koInstruction = "양손으로 세로 손잡이를 잡고 팔을 길게 편 채 몸을 슬레드 쪽으로 기울입니다.",
            enInstruction = "Grip the vertical handles with both hands and lean into the sled with long arms."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_sled_push_clean_v3_step_3,
            koLabel = "몸 기울이기",
            enLabel = "Lean body",
            koInstruction = "어깨부터 엉덩이까지 단단한 사선을 만들고 짧은 보폭으로 바닥을 밀기 시작합니다.",
            enInstruction = "Create a firm diagonal line from shoulders to hips and start pushing with short steps."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_sled_push_clean_v3_step_4,
            koLabel = "짧은 보폭으로 밀기",
            enLabel = "Push short steps",
            koInstruction = "손잡이를 놓지 말고 무릎을 빠르게 교대하며 슬레드를 앞으로 계속 밀어냅니다.",
            enInstruction = "Keep the handles in hand and drive the sled forward with quick alternating steps."
        )
    ),
    "dumbbell_deadlift" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_deadlift_clean_v2_step_1,
            koLabel = "덤벨 위치",
            enLabel = "Set dumbbell",
            koInstruction = "덤벨 위치: 양손 덤벨을 허벅지 옆에 두고 발을 골반너비로 세워 등 중립을 만듭니다.",
            enInstruction = "Stand hip-width with a dumbbell in each hand beside the thighs and a neutral spine."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_deadlift_clean_v2_step_2,
            koLabel = "힙힌지",
            enLabel = "Hip hinge",
            koInstruction = "힙힌지: 무릎을 살짝 굽히고 엉덩이를 뒤로 보내며 덤벨을 다리 가까이 내립니다.",
            enInstruction = "Bend the knees slightly, send the hips back, and keep the dumbbells close to the legs."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_deadlift_clean_v2_step_3,
            koLabel = "하단 힌지",
            enLabel = "Bottom hinge",
            koInstruction = "하단 힌지: 덤벨을 정강이 앞쪽까지 내리되 깊은 스쿼트처럼 앉지 않고 등을 곧게 유지합니다.",
            enInstruction = "Lower the dumbbells near the shins without turning the movement into a deep squat."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_dumbbell_deadlift_clean_v2_step_4,
            koLabel = "바닥 밀며 기립",
            enLabel = "Stand by pushing floor",
            koInstruction = "바닥 밀며 기립: 엉덩이를 앞으로 밀어 덤벨이 몸 가까이 올라오게 하며 시작 자세로 돌아옵니다.",
            enInstruction = "Drive the hips forward and let the dumbbells travel close to the body back to standing."
        )
    ),
    "medicine_ball_slam" to listOf(
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_medicine_ball_slam_clean_v3_step_1,
            koLabel = "가슴 앞 준비",
            enLabel = "Ball at chest",
            koInstruction = "가슴 앞 준비: 발을 어깨너비로 두고 메디신볼을 가슴 앞에서 잡습니다.",
            enInstruction = "Stand with feet shoulder-width and hold the medicine ball at your chest."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_medicine_ball_slam_clean_v3_step_2,
            koLabel = "머리 위로 올리기",
            enLabel = "Lift overhead",
            koInstruction = "머리 위로 올리기: 갈비뼈가 과하게 들리지 않게 공을 머리 위로 올립니다.",
            enInstruction = "Raise the ball overhead while keeping your ribs controlled."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_medicine_ball_slam_clean_v3_step_3,
            koLabel = "바닥으로 슬램",
            enLabel = "Slam down",
            koInstruction = "바닥으로 슬램: 엉덩이와 무릎을 접으며 공을 발 앞 바닥으로 강하게 내립니다.",
            enInstruction = "Hinge and bend the knees as you slam the ball down in front of your feet."
        ),
        ExerciseStepVisual(
            drawableResId = R.drawable.exercise_medicine_ball_slam_clean_v3_step_4,
            koLabel = "낮은 자세 마무리",
            enLabel = "Finish low",
            koInstruction = "낮은 자세 마무리: 공이 바닥에 닿은 뒤 등 중립을 유지하며 다음 반복을 준비합니다.",
            enInstruction = "Finish in a low athletic stance with a neutral spine before the next rep."
        )
    ),
)
