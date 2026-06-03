package com.smarttrainner.core.domain

import com.smarttrainner.core.model.DifficultyLevel
import com.smarttrainner.core.model.EquipmentType
import com.smarttrainner.core.model.Exercise
import com.smarttrainner.core.model.ExerciseId
import com.smarttrainner.core.model.MuscleGroup
import com.smarttrainner.core.model.PlanLevel
import com.smarttrainner.core.model.PlanTemplate
import com.smarttrainner.core.model.PlanTemplateDay
import com.smarttrainner.core.model.RoutineFocus
import com.smarttrainner.core.model.RoutineStructure
import com.smarttrainner.core.model.TemplateExercise
import com.smarttrainner.core.model.TrainingExperience
import com.smarttrainner.core.model.estimateExerciseSeconds

object SeedTrainingContent {
    private val FAST_CONDITIONING_EXERCISE_IDS = setOf(
        "two_hand_kettlebell_swing",
        "medicine_ball_slam"
    )
    private const val DEFAULT_REP_SETS = 3
    private const val DEFAULT_TIME_BASED_SETS = 3
    private val DEFAULT_REP_RANGE = 15..15
    private val SQUAT_PATTERN_SECONDARY_GROUPS = listOf(MuscleGroup.BACK, MuscleGroup.CORE)
    private val UNILATERAL_LOWER_SECONDARY_GROUPS = listOf(MuscleGroup.CORE)
    private val HINGE_SECONDARY_GROUPS = listOf(MuscleGroup.BACK, MuscleGroup.CORE)
    private val PULL_SECONDARY_GROUPS = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS)
    private val ROW_SECONDARY_GROUPS = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS, MuscleGroup.CORE)
    private val PRESS_SECONDARY_GROUPS = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS)
    private val OVERHEAD_PRESS_SECONDARY_GROUPS = listOf(MuscleGroup.TRICEPS, MuscleGroup.CORE)

    val exercises: List<Exercise> = listOf(
        exercise("bodyweight_squat", "맨몸 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.BODYWEIGHT, DifficultyLevel.BEGINNER, "bodyweight_squat", "장비 없이 스쿼트 정렬과 하체 기본 힘을 익히는 대표 운동입니다.", listOf("발 위치: 발을 어깨너비 정도로 두고 발바닥 전체가 바닥에 닿게 섭니다.", "코어 정렬: 갈비뼈가 들리지 않게 복부를 조이고 무릎과 발끝 방향을 맞춥니다.", "앉기: 엉덩이를 뒤로 보내며 통제 가능한 깊이까지 내려갑니다.", "발바닥으로 상승: 뒤꿈치가 뜨지 않게 바닥을 밀어 가슴과 골반이 함께 올라오게 합니다."), listOf("허리가 말리면 깊이를 줄이세요.", "무릎이 안쪽으로 모이지 않게 하세요.", "반동으로 빠르게 튕기지 마세요."), 3, 10..15, null, 90),
        exercise("leg_press", "레그 프레스", MuscleGroup.LOWER_BODY, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "leg_press", "허리 부담을 줄이고 하체 전체를 익히기 좋은 머신 운동입니다.", listOf("패드 밀착: 등과 엉덩이를 등받이에 붙이고 발바닥 전체가 플랫폼에 닿게 합니다.", "발 정렬: 무릎이 발끝 방향을 따라가도록 두고 복부에 힘을 줍니다.", "밀고 복귀: 무릎을 잠그지 않고 밀었다가 허리와 엉덩이가 뜨지 않게 천천히 돌아옵니다."), listOf("허리나 엉덩이가 뜨면 범위를 줄이세요.", "무릎이 안쪽으로 모이지 않게 하세요.", "플랫폼을 튕기지 말고 통제해서 복귀하세요."), 3, 10..12, null, 90),
        exercise("goblet_squat", "고블릿 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.DUMBBELL, DifficultyLevel.BEGINNER, "goblet_squat", "덤벨 하나로 스쿼트 패턴을 안전하게 익히는 운동입니다.", listOf("덤벨 위치: 덤벨 한쪽 끝을 양손으로 감싸 가슴 앞에 세우고 팔꿈치를 몸 가까이 둡니다.", "발·코어 정렬: 발을 어깨너비로 두고 갈비뼈가 들리지 않게 복부를 조입니다.", "내려가기: 무릎과 발끝 방향을 맞추며 덤벨이 가슴에서 멀어지지 않게 앉습니다.", "올라오기: 발바닥 전체로 바닥을 밀어 가슴과 골반이 함께 올라오게 합니다."), listOf("허리가 말리면 깊이를 줄이세요.", "무릎과 발끝 방향을 맞추세요."), 3, 8..12, null, 90),
        exercise("box_squat", "박스 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.BENCH, DifficultyLevel.BEGINNER, "box_squat", "벤치 높이로 깊이를 조절해 초보자가 안정적으로 연습하기 좋습니다.", listOf("박스 위치: 엉덩이 바로 뒤에 박스나 벤치를 두고 발을 어깨너비로 섭니다.", "엉덩이 뒤로 보내기: 무릎이 발끝 방향을 따라가게 하며 엉덩이를 먼저 뒤로 보냅니다.", "가볍게 터치: 박스에 털썩 앉지 말고 엉덩이만 살짝 닿은 상태로 긴장을 유지합니다.", "반동 없이 일어서기: 발바닥 전체로 바닥을 밀어 가슴과 엉덩이가 함께 올라오게 합니다."), listOf("벤치에 털썩 주저앉지 마세요.", "상체가 과하게 숙여지면 속도를 늦추세요.", "무릎이 안쪽으로 무너지지 않게 발끝 방향을 따라가세요."), 3, 8..10, null, 90),
        exercise("dumbbell_split_squat", "덤벨 스플릿 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "dumbbell_split_squat", "한쪽 다리씩 균형과 하체 힘을 기르는 운동입니다.", listOf("보폭 잡기: 양손 덤벨을 몸 옆에 두고 앞발과 뒷발을 나누어 안정적인 보폭을 만듭니다.", "앞발 중심: 몸통을 세우고 앞발 전체에 체중을 실어 뒤꿈치가 뜨지 않게 준비합니다.", "내려가기: 뒤무릎을 바닥 가까이 내리며 앞무릎이 발끝 방향을 따라가게 합니다.", "올라온 뒤 반대쪽: 앞발로 바닥을 밀어 올라온 뒤 같은 반복 수를 반대쪽 다리도 수행합니다."), listOf("무릎이 안쪽으로 꺾이면 중량을 낮추세요.", "균형이 흔들리면 맨몸부터 시작하세요."), 3, 8..10, null, 90),
        exercise("bulgarian_split_squat", "불가리안 스플릿 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "bulgarian_split_squat", "뒷발을 벤치에 올려 한쪽 다리 힘과 균형을 강하게 쓰는 하체 운동입니다.", listOf("앞발 위치 잡기: 뒷발등을 벤치에 올리고 앞발은 뒤꿈치가 뜨지 않는 거리로 둡니다.", "몸통 세우기: 덤벨을 몸 옆에 두고 골반이 돌아가지 않게 복부를 조입니다.", "수직 하강: 앞무릎이 발끝 방향을 따라가게 하며 뒷무릎을 아래로 내립니다.", "앞발로 밀기: 앞발 전체로 바닥을 밀어 올라온 뒤 같은 반복 수를 반대쪽도 수행합니다."), listOf("앞무릎 통증이 있으면 보폭과 깊이를 줄이세요.", "균형이 흔들리면 덤벨 없이 시작하세요.", "뒷발로 벤치를 세게 밀어 튀어 오르지 마세요."), 3, 8..10, null, 90),
        exercise("walking_lunge", "워킹 런지", MuscleGroup.LOWER_BODY, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "walking_lunge", "하체와 균형 감각을 함께 쓰는 이동형 운동입니다.", listOf("오른발 앞으로: 덤벨을 몸 옆에 두고 오른발을 앞으로 내딛어 보폭을 잡습니다.", "수직 하강: 앞무릎이 안쪽으로 무너지지 않게 하며 뒤무릎을 바닥 가까이 천천히 내립니다.", "밀고 일어서기: 오른발로 바닥을 밀어 몸을 세우고 뒤쪽 발을 앞으로 가져옵니다.", "왼발로 교대 반복: 다음 걸음은 왼발을 앞으로 내딛어 같은 흐름으로 좌우를 번갈아 진행합니다."), listOf("보폭을 급하게 바꾸지 마세요.", "무릎 통증이 있으면 스플릿 스쿼트로 대체하세요."), 2, 10..12, null, 90),
        exercise("leg_extension", "레그 익스텐션", MuscleGroup.LOWER_BODY, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "leg_extension", "허벅지 앞쪽을 고립해서 느끼기 쉬운 머신 운동입니다.", listOf("패드 조정: 등받이에 몸을 붙이고 발목 앞 패드가 발목 바로 위에 오도록 맞춥니다.", "무릎 펴기: 허벅지 앞쪽 힘으로 패드를 들어 올리되 엉덩이가 들리지 않게 고정합니다.", "통제 복귀: 무게추가 튕기지 않게 같은 경로로 천천히 내려 무릎을 굽힙니다."), listOf("반동으로 차올리지 마세요.", "무릎 앞쪽 통증이 있으면 범위를 줄이세요."), 3, 10..15, null, 60),
        exercise("leg_curl", "레그 컬", MuscleGroup.LOWER_BODY, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "leg_curl", "허벅지 뒤쪽을 안전하게 강화하는 머신 운동입니다.", listOf("패드 조정: 발목 뒤쪽에 패드를 맞추고 엎드려 손잡이를 잡은 뒤 엉덩이를 패드에 고정합니다.", "무릎 굽혀 당기기: 무릎을 굽혀 패드를 엉덩이 쪽으로 당기고 허리가 꺾이지 않게 버팁니다.", "천천히 복귀: 같은 경로로 다리를 내려 햄스트링 긴장을 유지한 채 다음 반복을 준비합니다."), listOf("허리를 꺾어 당기지 마세요.", "가동 범위보다 통제감을 우선하세요."), 3, 10..15, null, 60),
        exercise("romanian_deadlift", "루마니안 데드리프트", MuscleGroup.LOWER_BODY, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "romanian_deadlift", "엉덩이와 햄스트링을 쓰는 힙힌지 운동입니다.", listOf("시작 위치: 덤벨을 허벅지 앞에 두고 발을 골반너비로 세워 복부를 조입니다.", "등 중립: 어깨를 낮추고 목부터 골반까지 긴 선을 유지한 채 무릎을 살짝 굽힙니다.", "힙힌지: 엉덩이를 뒤로 보내며 덤벨이 허벅지와 정강이 가까이 내려가게 합니다.", "햄스트링 범위: 허리가 말리기 전, 허벅지 뒤쪽이 당기는 지점에서 멈춥니다.", "둔근으로 복귀: 발바닥 전체를 누르고 엉덩이를 앞으로 보내 시작 자세로 돌아옵니다."), listOf("허리가 말리면 즉시 범위를 줄이세요.", "무거운 중량보다 등 중립을 우선하세요."), 3, 8..10, null, 90),
        exercise("hip_thrust", "힙 쓰러스트", MuscleGroup.LOWER_BODY, EquipmentType.BENCH, DifficultyLevel.INTERMEDIATE, "hip_thrust", "벤치에 등을 기대고 둔근을 강하게 수축하는 하체 후면 운동입니다.", listOf("등 윗부분 지지: 벤치 모서리에 견갑 아래쪽을 기대고 발바닥을 바닥에 붙입니다.", "발 위치와 복압: 무릎 아래에 발을 두고 갈비뼈를 내린 채 복부에 힘을 줍니다.", "둔근으로 들어 올리기: 발바닥으로 밀어 어깨부터 무릎까지 거의 일직선이 되게 엉덩이를 올립니다.", "통제하며 내려오기: 허리를 꺾지 않고 엉덩이를 천천히 내려 시작 자세로 돌아옵니다."), listOf("허리를 꺾어 높이를 만들지 마세요.", "발 위치가 너무 멀거나 가깝지 않게 조정하세요.", "목을 젖히지 말고 턱을 살짝 당겨 유지하세요."), 3, 8..12, null, 90),
        exercise("calf_raise", "카프 레이즈", MuscleGroup.LOWER_BODY, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "calf_raise", "종아리 근육을 단순하고 안전하게 자극합니다.", listOf("발 위치: 발 앞쪽을 발판에 올리고 뒤꿈치가 자유롭게 내려갈 수 있게 섭니다.", "올리기: 종아리 힘으로 뒤꿈치를 들어 올려 발목을 끝범위까지 펴줍니다.", "멈추고 내리기: 상단에서 잠깐 멈춘 뒤 뒤꿈치를 천천히 내려 종아리가 늘어나는 범위를 만듭니다."), listOf("짧게 튕기지 말고 끝범위를 사용하세요.", "발목 통증이 있으면 범위를 줄이세요."), 3, 12..15, null, 60),
        exercise("lat_pulldown", "랫 풀다운", MuscleGroup.BACK, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "lat_pulldown", "등 운동을 처음 배울 때 가장 접근성이 좋은 당기기 운동입니다.", listOf("패드·가슴 세팅: 허벅지 패드로 몸을 고정하고 바를 얼굴 앞쪽 경로에 둔 채 가슴을 살짝 들어 준비합니다.", "상부 가슴 앞으로 당기기: 목 뒤가 아니라 얼굴 앞을 지나 쇄골·상부 가슴 쪽으로 바를 당깁니다.", "하단 수축: 팔꿈치를 몸 옆 아래로 내리고 어깨를 낮춘 채 바를 상부 가슴 앞에서 짧게 통제합니다.", "어깨 낮춰 복귀: 어깨가 귀 쪽으로 끌려가지 않게 같은 경로로 천천히 올립니다."), listOf("목 뒤로 당기지 마세요.", "상체를 과하게 젖히지 마세요.", "팔만 쓰는 느낌이면 중량을 낮추세요."), 3, 8..12, null, 90),
        exercise("seated_cable_row", "시티드 케이블 로우", MuscleGroup.BACK, EquipmentType.CABLE, DifficultyLevel.BEGINNER, "seated_cable_row", "등 중앙과 자세 안정에 도움이 되는 수평 당기기 운동입니다.", listOf("허리 세팅: 발판에 발을 고정하고 허리를 중립으로 세운 뒤 팔을 길게 뻗어 손잡이를 잡습니다.", "팔꿈치 당기기: 몸통을 뒤로 젖히지 않고 팔꿈치를 갈비뼈 옆으로 당겨 손잡이를 몸 가까이 가져옵니다.", "견갑 통제 복귀: 어깨가 앞으로 끌려가지 않게 버티며 팔을 천천히 뻗어 시작 위치로 돌아갑니다."), listOf("상체를 크게 젖혀 반동을 만들지 마세요.", "어깨를 으쓱하지 마세요."), 3, 8..12, null, 90),
        exercise("chest_supported_row", "덤벨 체스트 서포티드 로우", MuscleGroup.BACK, EquipmentType.DUMBBELL, DifficultyLevel.BEGINNER, "chest_supported_row", "인클라인 벤치에 가슴을 지지해 허리 부담을 줄이고 등 당기기 감각을 익히는 덤벨 운동입니다.", listOf("벤치에 가슴 지지: 인클라인 벤치에 가슴을 붙이고 발끝으로 몸을 안정시킨 뒤 덤벨을 아래로 둡니다.", "팔꿈치 뒤로 당기기: 가슴이 벤치에서 뜨지 않게 유지하며 팔꿈치를 등 뒤로 보내 덤벨을 몸 옆으로 당깁니다.", "통제 복귀: 어깨가 앞으로 끌려가지 않게 견갑을 통제하며 덤벨을 천천히 아래로 내립니다."), listOf("목이 앞으로 빠지지 않게 하세요.", "가슴이 벤치에서 뜨면 중량을 낮추세요.", "허리 반동으로 덤벨을 끌어올리지 마세요."), 3, 8..12, null, 90),
        exercise("one_arm_dumbbell_row", "원암 덤벨 로우", MuscleGroup.BACK, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "one_arm_dumbbell_row", "한쪽씩 등 수축과 몸통 회전 억제를 연습하기 좋은 덤벨 운동입니다.", listOf("벤치 지지: 한 손과 한쪽 무릎을 벤치에 두고 반대쪽 발은 바닥에 단단히 고정합니다.", "몸통 고정: 골반과 어깨가 바닥을 향하게 유지하고 덤벨을 어깨 아래에 둡니다.", "당기기: 몸통을 비틀지 않고 팔꿈치를 뒤로 보내 덤벨을 옆구리 가까이 당깁니다.", "회전 없이 내리기: 어깨가 앞으로 끌려가지 않게 통제하며 덤벨을 시작 위치로 천천히 내립니다."), listOf("몸을 비틀어 올리지 마세요.", "어깨가 귀 쪽으로 올라가면 중량을 낮추세요.", "허리가 불편하면 머신 로우로 대체하세요."), 3, 8..12, null, 90),
        exercise("assisted_pullup", "어시스티드 풀업", MuscleGroup.BACK, EquipmentType.MACHINE, DifficultyLevel.INTERMEDIATE, "assisted_pullup", "보조 중량으로 풀업 패턴을 익히는 운동입니다.", listOf("발판과 보조 패드 진입: 발판에 올라 상단 손잡이 아래에 몸을 두고 한쪽 무릎씩 움직이는 보조 패드에 올립니다.", "상단 손잡이 잡기: 손잡이는 머리 위 고정 위치에서 잡고 가슴을 살짝 들어 몸통을 흔들리지 않게 준비합니다.", "가슴을 손잡이 쪽으로 끌기: 턱만 빼지 말고 팔꿈치를 아래로 당기며 몸을 위로 올립니다.", "패드와 함께 천천히 하강: 몸과 보조 패드가 함께 아래로 내려오게 통제하며 팔을 펴고 다음 반복을 준비합니다."), listOf("어깨 앞쪽 통증이 있으면 중단하세요.", "목을 빼서 턱만 올리지 마세요.", "보조 중량은 클수록 더 많은 도움을 줍니다.", "발판과 무릎 패드에서 균형이 흔들리면 중량을 조정하세요."), 3, 6..10, null, 120),
        exercise("pullup", "풀업", MuscleGroup.BACK, EquipmentType.BODYWEIGHT, DifficultyLevel.INTERMEDIATE, "pullup", "자기 체중으로 등과 팔 당기기 힘을 기르는 대표 맨몸 운동입니다.", listOf("손잡이 잡기: 바를 어깨보다 약간 넓게 잡고 몸이 흔들리지 않게 매달립니다.", "어깨 낮추기: 어깨가 귀로 올라가지 않게 낮추고 복부에 힘을 줍니다.", "가슴을 바 쪽으로 당기기: 턱만 빼지 말고 팔꿈치를 아래로 끌어 몸을 올립니다.", "통제 하강: 반동 없이 팔을 천천히 펴며 시작 위치로 돌아옵니다."), listOf("어깨나 팔꿈치 통증이 있으면 어시스티드 풀업으로 대체하세요.", "다리를 차서 반동을 만들지 마세요.", "목만 빼서 턱을 바에 맞추지 마세요."), 3, 4..8, null, 120),
        exercise("face_pull", "페이스 풀", MuscleGroup.SHOULDERS, EquipmentType.CABLE, DifficultyLevel.BEGINNER, "face_pull", "후면 어깨와 상부 등을 가볍게 깨우는 로프 케이블 운동입니다.", listOf("케이블 높이: 로프가 얼굴 높이에서 오도록 맞추고 팔을 뻗은 상태로 몸통을 세웁니다.", "얼굴 옆으로 당기기: 팔꿈치를 높게 유지하고 로프 끝을 얼굴 양옆으로 벌리며 당깁니다.", "팔꿈치 유지 복귀: 어깨가 말리지 않게 통제하며 같은 경로로 팔을 천천히 뻗습니다."), listOf("허리를 젖혀 당기지 마세요.", "팔꿈치가 아래로 떨어지면 중량을 낮추세요.", "가벼운 중량으로 움직임을 먼저 익히세요."), 3, 12..15, null, 60),
        exercise("machine_chest_press", "머신 체스트 프레스", MuscleGroup.CHEST, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "machine_chest_press", "가슴 밀기 패턴을 안정적으로 배우는 머신 운동입니다.", listOf("좌석 조정: 손잡이가 가슴 중간 높이에 오도록 좌석을 맞추고 등과 엉덩이를 패드에 붙입니다.", "밀기: 어깨가 들리지 않게 고정하고 손잡이를 같은 속도로 앞으로 밀어냅니다.", "어깨 범위 내 복귀: 가슴이 늘어나는 범위까지만 손잡이를 천천히 되돌립니다."), listOf("손목을 꺾지 마세요.", "어깨 통증이 있으면 범위를 줄이세요.", "반동으로 손잡이를 튕기지 마세요."), 3, 8..12, null, 90),
        exercise("dumbbell_bench_press", "덤벨 벤치 프레스", MuscleGroup.CHEST, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "dumbbell_bench_press", "좌우 균형과 가슴 힘을 함께 기르는 프레스 운동입니다.", listOf("눕고 덤벨 위치: 벤치에 누워 발을 고정하고 덤벨을 가슴 위에서 안정적으로 세팅합니다.", "팔꿈치 각도: 덤벨을 어깨선보다 살짝 아래로 내리고 팔꿈치가 과하게 벌어지지 않게 둡니다.", "밀기: 양쪽 덤벨을 같은 속도로 밀어 올리며 손목을 팔꿈치 위에 세웁니다.", "통제 하강: 어깨 앞쪽이 불편하지 않은 범위까지만 덤벨을 천천히 내립니다."), listOf("덤벨이 어깨선 뒤로 과하게 내려가지 않게 하세요.", "혼자 무리한 중량을 들지 마세요.", "좌우 속도가 달라지면 중량을 낮추세요."), 3, 8..12, null, 120),
        exercise("incline_dumbbell_press", "인클라인 덤벨 프레스", MuscleGroup.CHEST, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "incline_dumbbell_press", "윗가슴과 어깨 전면을 함께 쓰는 프레스 운동입니다.", listOf("벤치 각도 세팅: 인클라인 벤치를 너무 세우지 않고 등과 머리를 패드에 붙입니다.", "덤벨 시작 위치: 덤벨을 어깨 옆에 두고 팔꿈치를 살짝 아래로 내려 손목을 세웁니다.", "위로 밀기: 양쪽 덤벨을 같은 속도로 위로 밀어 윗가슴과 어깨 전면을 통제합니다.", "어깨 범위 내 하강: 어깨 앞쪽이 불편하지 않은 범위까지만 덤벨을 천천히 내립니다."), listOf("벤치 각도를 너무 세우지 마세요.", "어깨 앞쪽 통증이 있으면 머신으로 대체하세요."), 3, 8..10, null, 120),
        exercise("pushup", "푸시업", MuscleGroup.CHEST, EquipmentType.BODYWEIGHT, DifficultyLevel.BEGINNER, "pushup", "어디서나 할 수 있는 기본 가슴·코어 운동입니다.", listOf("손 위치: 손을 어깨보다 살짝 넓게 두고 머리부터 발끝까지 일직선을 만듭니다.", "몸통 일직선 하강: 팔꿈치를 과하게 벌리지 않고 가슴을 바닥 가까이 천천히 내립니다.", "밀어 올라오기: 복부와 엉덩이에 힘을 유지한 채 바닥을 밀어 시작 자세로 돌아옵니다."), listOf("허리가 처지면 무릎 푸시업으로 바꾸세요.", "어깨가 귀로 올라가지 않게 하세요."), 3, 6..12, null, 90),
        exercise("cable_fly", "케이블 플라이", MuscleGroup.CHEST, EquipmentType.CABLE, DifficultyLevel.INTERMEDIATE, "cable_fly", "가슴을 모으는 감각을 배우는 보조 운동입니다.", listOf("시작 자세: 양쪽 케이블을 잡고 한 발을 앞으로 둔 뒤 팔을 옆으로 열어 가슴이 늘어나는 범위를 만듭니다.", "손 모으기: 팔꿈치를 살짝 굽힌 채 양손을 가슴 앞에서 모으며 케이블 장력을 유지합니다.", "가슴 늘림 범위 복귀: 어깨가 과하게 뒤로 젖혀지기 전까지만 같은 경로로 천천히 돌아갑니다."), listOf("어깨가 과하게 뒤로 젖혀지지 않게 하세요.", "무거운 중량보다 통제감을 우선하세요."), 2, 10..15, null, 60),
        exercise("machine_shoulder_press", "머신 숄더 프레스", MuscleGroup.SHOULDERS, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "machine_shoulder_press", "어깨 밀기 패턴을 안정적으로 익히는 운동입니다.", listOf("어깨 옆 시작: 손잡이가 어깨 옆에 오도록 좌석을 맞추고 등과 엉덩이를 패드에 붙입니다.", "같은 레버 궤도로 밀기: 양쪽 손잡이를 같은 속도로 밀어 머신 레버가 정해진 호를 따라 올라가게 합니다.", "머리 위 마무리와 통제 복귀: 팔꿈치를 잠그기 직전까지 밀고, 같은 레버 궤도로 천천히 내려 중량 스택이 떨어지지 않게 합니다."), listOf("허리를 젖혀 밀지 마세요.", "통증 없는 범위에서만 진행하세요."), 3, 8..12, null, 90),
        exercise("dumbbell_shoulder_press", "덤벨 숄더 프레스", MuscleGroup.SHOULDERS, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "dumbbell_shoulder_press", "어깨와 코어 안정성을 함께 요구하는 프레스 운동입니다.", listOf("덤벨 시작 위치: 덤벨을 어깨 높이에 두고 손목을 팔꿈치 위에 세운 뒤 갈비뼈가 들리지 않게 고정합니다.", "머리 위로 밀기: 양쪽 덤벨을 같은 속도로 밀어 올리고 어깨가 귀 쪽으로 솟지 않게 합니다.", "어깨 높이 복귀: 같은 경로로 천천히 내려 덤벨을 어깨 높이에 되돌리고 몸통을 흔들지 않습니다."), listOf("팔꿈치를 지나치게 뒤로 빼지 마세요.", "허리가 꺾이면 앉아서 진행하세요."), 3, 8..10, null, 120),
        exercise("dumbbell_lateral_raise", "덤벨 레터럴 레이즈", MuscleGroup.SHOULDERS, EquipmentType.DUMBBELL, DifficultyLevel.BEGINNER, "dumbbell_lateral_raise", "어깨 측면을 가볍게 자극해 라인을 만드는 운동입니다.", listOf("시작 자세: 가벼운 덤벨을 허벅지 옆에 두고 무릎과 팔꿈치를 살짝 부드럽게 둡니다.", "어깨 높이까지 올리기: 반동 없이 양팔을 옆으로 들어 덤벨이 어깨 높이 근처에 오게 합니다.", "통제 하강: 승모근에 힘이 몰리지 않게 천천히 내려 시작 자세로 돌아옵니다."), listOf("몸을 흔들어 올리지 마세요.", "승모근이 과하게 긴장하면 중량을 낮추세요."), 3, 12..15, null, 60),
        exercise("rear_delt_machine", "리어 델트 머신", MuscleGroup.SHOULDERS, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "rear_delt_machine", "후면 어깨를 쉽게 분리해 연습하는 머신 운동입니다.", listOf("패드 밀착: 가슴을 패드에 붙이고 손잡이를 잡은 뒤 팔꿈치를 살짝 부드럽게 둡니다.", "뒤로 벌리기: 어깨가 올라가지 않게 손잡이를 양옆과 뒤쪽으로 벌려 후면 어깨를 조입니다.", "반동 없이 복귀: 같은 경로로 천천히 손잡이를 앞으로 보내며 가슴 패드 접촉을 유지합니다."), listOf("목과 승모근으로 당기지 마세요.", "가동 범위보다 어깨 뒤쪽 느낌을 우선하세요."), 3, 12..15, null, 60),
        exercise("triceps_pushdown", "케이블 트라이셉스 푸시다운", MuscleGroup.TRICEPS, EquipmentType.CABLE, DifficultyLevel.BEGINNER, "triceps_pushdown", "팔 뒤쪽을 안전하게 강화하는 케이블 운동입니다.", listOf("팔꿈치 고정: 손잡이를 잡고 팔꿈치를 몸 옆에 붙인 채 상완이 흔들리지 않게 둡니다.", "아래로 펴기: 몸통으로 누르지 말고 팔꿈치를 펴며 손잡이를 허벅지 쪽으로 내립니다.", "위치 유지 복귀: 케이블 장력을 유지하며 팔꿈치 위치를 고정한 채 천천히 다시 굽힙니다."), listOf("몸통으로 눌러 내리지 마세요.", "손목을 중립으로 유지하세요."), 3, 10..15, null, 60),
        exercise("overhead_triceps_extension", "덤벨 오버헤드 익스텐션", MuscleGroup.TRICEPS, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "overhead_triceps_extension", "팔 뒤쪽 긴 머리를 늘려 쓰는 보조 운동입니다.", listOf("머리 위 세팅: 덤벨 하나를 양손으로 잡고 팔꿈치를 귀 옆에 모은 채 몸통을 곧게 세웁니다.", "팔꿈치 굽히기: 상완은 세워 둔 채 팔꿈치만 굽혀 덤벨을 머리 뒤로 천천히 내립니다.", "펴기: 팔꿈치를 모은 채 팔 뒤쪽 힘으로 덤벨을 머리 위로 밀어 올립니다.", "상단 고정: 허리를 젖히지 말고 팔꿈치를 잠그기 직전에서 멈춘 뒤 같은 경로로 반복합니다."), listOf("어깨가 불편하면 푸시다운으로 대체하세요.", "허리를 젖혀 버티지 마세요.", "덤벨이 얼굴 앞으로 내려오면 중량을 낮추세요."), 2, 10..12, null, 60),
        exercise("dumbbell_curl", "덤벨 컬", MuscleGroup.BICEPS, EquipmentType.DUMBBELL, DifficultyLevel.BEGINNER, "dumbbell_curl", "팔 앞쪽을 단순하게 강화하는 기본 운동입니다.", listOf("팔꿈치 고정: 덤벨을 몸 옆에 두고 팔꿈치를 옆구리 가까이에 고정한 채 손목을 중립으로 둡니다.", "올리고 천천히 내리기: 몸을 흔들지 않고 덤벨을 어깨 쪽으로 올린 뒤 같은 경로로 천천히 내립니다."), listOf("몸을 흔들어 올리지 마세요.", "손목이 꺾이지 않게 하세요."), 3, 10..12, null, 60),
        exercise("cable_curl", "케이블 컬", MuscleGroup.BICEPS, EquipmentType.CABLE, DifficultyLevel.BEGINNER, "cable_curl", "일정한 장력으로 팔 앞쪽을 연습하는 운동입니다.", listOf("로우 풀리 시작: 바를 잡고 팔꿈치를 옆구리 가까이에 둔 채 케이블 장력을 만듭니다.", "끌어올리기: 몸통을 흔들지 않고 팔꿈치를 굽혀 바를 가슴 아래쪽으로 끌어올립니다.", "장력 유지 하강: 케이블이 느슨해지지 않게 같은 경로로 천천히 내려 시작 자세로 돌아갑니다."), listOf("어깨가 앞으로 말리지 않게 하세요.", "상체 반동을 줄이세요."), 3, 10..15, null, 60),
        exercise("plank", "플랭크", MuscleGroup.CORE, EquipmentType.BODYWEIGHT, DifficultyLevel.BEGINNER, "plank", "허리와 골반을 안정시키는 대표 코어 운동입니다.", listOf("팔꿈치 위치 잡기: 팔꿈치를 어깨 바로 아래에 두고 전완으로 바닥을 누른 채 발끝을 세웁니다.", "일직선으로 버티기: 머리부터 뒤꿈치까지 길게 유지하고, 엉덩이가 처지지 않게 복부에 힘을 준 채 호흡합니다."), listOf("허리가 처지면 즉시 쉬세요.", "숨을 참지 마세요."), 2, null, 1, 60),
        exercise("dead_bug", "데드 버그", MuscleGroup.CORE, EquipmentType.BODYWEIGHT, DifficultyLevel.BEGINNER, "dead_bug", "허리 부담을 낮추며 양팔과 다리를 번갈아 뻗어 코어 조절을 배우는 운동입니다.", listOf("시작 자세: 등을 대고 누워 양팔을 어깨 위로 수직으로 뻗고, 양무릎과 고관절을 90도로 들어 정강이가 바닥과 평행하게 준비합니다. 갈비뼈를 내리고 허리와 골반을 안정시킵니다.", "한쪽 팔·반대 다리 뻗기: 숨을 내쉬며 한쪽 팔은 귀 옆으로 머리 뒤쪽에 길게 보내고, 반대쪽 다리는 무릎을 펴며 바닥 가까이 뻗습니다. 반대쪽 팔과 다리는 시작 위치를 유지합니다.", "시작 자세로 복귀: 팔과 다리를 천천히 시작 자세로 되돌립니다. 허리가 뜨거나 골반이 회전하지 않게 복부 긴장을 유지합니다.", "반대쪽 팔·다리 뻗기: 같은 방식으로 반대쪽 팔과 다리를 뻗어 좌우를 교대합니다. 움직임이 흔들리면 팔·다리 이동 범위를 줄입니다."), listOf("허리가 바닥에서 뜨면 팔·다리 이동 범위를 줄이세요.", "빠르게 흔들지 말고 숨을 내쉬며 천천히 움직이세요."), 2, 8..10, null, 60),
        exercise("bird_dog", "버드독", MuscleGroup.CORE, EquipmentType.BODYWEIGHT, DifficultyLevel.BEGINNER, "bird_dog", "등과 골반 안정성을 함께 기르는 코어 운동입니다.", listOf("네발기기: 손은 어깨 아래, 무릎은 골반 아래에 두고 목부터 골반까지 길게 정렬합니다.", "한쪽 뻗기: 한쪽 팔과 반대쪽 다리를 길게 뻗고 골반이 돌아가거나 허리가 꺾이지 않게 버팁니다.", "복귀 후 반대쪽: 천천히 네발기기 자세로 돌아온 뒤 반대쪽 팔과 다리도 같은 방식으로 반복합니다."), listOf("허리를 꺾어 다리를 높이지 마세요.", "천천히 균형을 잡으세요."), 2, 8..10, null, 60),
        exercise("pallof_press", "팔로프 프레스", MuscleGroup.CORE, EquipmentType.CABLE, DifficultyLevel.BEGINNER, "pallof_press", "몸통 회전을 버티는 안정성 운동입니다.", listOf("케이블 옆으로 서기: 케이블을 가슴 높이에 맞추고 옆으로 서서 손잡이를 양손으로 잡습니다.", "가슴 앞 세팅: 손잡이를 가슴 중앙에 두고 갈비뼈와 골반이 케이블 쪽으로 돌아가지 않게 고정합니다.", "앞으로 밀기: 손을 가슴 앞에서 곧게 밀어내고 케이블이 몸통을 돌리려는 힘을 복부로 버팁니다.", "회전 버티며 복귀: 같은 자세를 유지하며 손을 가슴으로 되돌리고, 세트를 마치면 반대 방향으로 서서 반복합니다."), listOf("허리만 비틀어 버티지 마세요.", "가벼운 중량으로 정면 유지에 집중하세요."), 3, 10..12, null, 60),
        exercise("cable_woodchop", "케이블 우드찹", MuscleGroup.CORE, EquipmentType.CABLE, DifficultyLevel.INTERMEDIATE, "cable_woodchop", "코어와 몸통 회전을 통제하며 쓰는 운동입니다.", listOf("상단 풀리 시작: 케이블을 높은 위치에 두고 손잡이를 어깨 위쪽에서 잡아 몸통을 길게 세웁니다.", "대각선 당기기: 팔만 휘두르지 말고 가슴과 골반을 함께 돌리며 손잡이를 대각선 아래로 당깁니다.", "무릎 바깥 마무리: 손잡이를 반대쪽 무릎 바깥 방향까지 내리고 복부에 힘을 유지합니다.", "통제 복귀: 케이블이 끌고 가게 두지 말고 같은 대각선 경로로 천천히 돌아온 뒤 반대쪽도 반복합니다."), listOf("허리만 급하게 비틀지 마세요.", "통증이 있으면 팔로프 프레스로 대체하세요."), 2, 10..12, null, 60),
        exercise("treadmill_walk", "트레드밀 걷기", MuscleGroup.CARDIO, EquipmentType.CARDIO_MACHINE, DifficultyLevel.BEGINNER, "treadmill_walk", "운동 전 몸을 데우고 지속성을 만들기 좋은 유산소입니다.", listOf("낮은 속도 시작: 벨트 중앙에 서서 손잡이를 가볍게 잡고 낮은 속도부터 걷기 시작합니다.", "자연스럽게 걷기: 시선은 앞을 보고 발은 벨트 중앙에 놓으며 팔과 어깨에 힘을 빼고 걷습니다.", "1분 쿨다운: 마지막 1분은 속도를 낮춰 호흡을 안정시키고 벨트가 멈춘 뒤 내려옵니다."), listOf("어지러움이나 흉통이 있으면 즉시 멈추세요.", "처음에는 경사를 낮게 두세요."), 1, null, 5, 30),
        exercise("indoor_bike", "실내 자전거", MuscleGroup.CARDIO, EquipmentType.CARDIO_MACHINE, DifficultyLevel.BEGINNER, "indoor_bike", "무릎 부담을 낮추며 심박을 올리는 유산소입니다.", listOf("탑승 세팅: 안장에 앉아 양발을 페달에 올리고 무릎이 과하게 접히지 않는지 확인합니다.", "부드럽게 페달링: 상체는 편하게 세우고 페달을 끊기지 않게 일정한 리듬으로 밟습니다.", "강도 유지: 숨이 차지만 대화가 가능한 정도로 저항을 맞추고 목과 허리에 힘이 들어가지 않게 유지합니다."), listOf("무릎이 많이 접히면 안장을 높이세요.", "처음부터 강한 저항을 걸지 마세요."), 1, null, 5, 30),
        exercise("hack_squat", "핵 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.MACHINE, DifficultyLevel.INTERMEDIATE, "hack_squat", "상체를 지지한 상태로 스쿼트 패턴과 허벅지 자극을 늘리는 머신 운동입니다.", listOf("패드 밀착: 등과 어깨를 패드에 붙이고 양손으로 손잡이를 잡은 뒤 발판에 발을 고정합니다.", "발 위치: 발바닥 전체를 누르고 무릎이 발끝 방향을 따라가도록 시작 자세를 만듭니다.", "내려가기: 패드에서 등이 뜨지 않게 하며 레일을 따라 천천히 내려갑니다.", "잠그지 않고 밀기: 발판을 밀어 올라오되 무릎을 세게 잠그지 않고 같은 레일 경로를 유지합니다."), listOf("무릎이 안쪽으로 모이지 않게 하세요.", "허리가 뜨면 깊이를 줄이세요.", "발판에서 뒤꿈치가 뜨면 발 위치를 조정하세요."), 3, 8..12, null, 120),
        exercise("smith_machine_squat", "스미스 머신 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.MACHINE, DifficultyLevel.INTERMEDIATE, "smith_machine_squat", "고정 궤도로 스쿼트 볼륨을 조금 더 쌓기 좋은 운동입니다.", listOf("바 위치: 스미스 바를 목이 아닌 어깨 뒤쪽에 두고 양손으로 좌우 균형 있게 잡습니다.", "발 위치: 양발을 어깨너비로 두고 뒤꿈치가 뜨지 않는 위치에서 몸통을 고정합니다.", "내려가기: 바가 수직 레일을 따라 내려가게 하며 엉덩이와 무릎을 함께 굽힙니다.", "발바닥으로 밀기: 발바닥 전체로 밀어 같은 수직 레일을 따라 바를 올립니다."), listOf("고정 궤도에 몸을 억지로 맞추지 마세요.", "허리나 무릎 통증이 있으면 머신 프레스로 대체하세요.", "바가 목을 누르면 위치를 다시 잡으세요."), 3, 8..12, null, 120),
        exercise("barbell_back_squat", "바벨 백 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.BARBELL, DifficultyLevel.INTERMEDIATE, "barbell_back_squat", "랙과 안전바를 사용해 하체 힘과 전신 브레이싱을 함께 훈련하는 기본 바벨 운동입니다.", listOf("랙 높이와 바 위치: 바를 윗가슴 높이 랙에 두고 목이 아닌 등 위쪽에 얹습니다.", "언랙과 브레이싱: 양손을 고르게 잡고 복부에 힘을 준 뒤 두 걸음만 뒤로 나옵니다.", "발과 시선 정렬: 발은 어깨너비로 두고 발끝과 무릎 방향을 맞춥니다.", "통제 하강: 바가 중족부 위에 머물도록 엉덩이와 무릎을 함께 접습니다.", "바닥 밀며 기립: 허리가 말리기 전 범위에서 멈추고 반동 없이 일어섭니다."), listOf("빈 바나 아주 가벼운 중량으로 시작하세요.", "랙의 안전바 또는 스팟터 암을 반드시 맞추세요.", "허리 말림이나 날카로운 무릎·고관절·허리 통증이 있으면 중단하세요."), 3, 5..8, null, 150),
        exercise("barbell_bench_press", "바벨 벤치 프레스", MuscleGroup.CHEST, EquipmentType.BARBELL, DifficultyLevel.INTERMEDIATE, "barbell_bench_press", "수평 밀기 힘을 키우는 대표 바벨 운동으로, 랙 세팅과 보조자 또는 안전바 사용이 중요합니다.", listOf("랙과 눈 위치: 벤치에 누워 눈이 바 아래에 오게 하고 발을 바닥에 고정합니다.", "견갑 고정과 그립: 어깨뼈를 모아 내리고 손목이 꺾이지 않게 바를 잡습니다.", "가슴 쪽 하강: 팔꿈치를 과하게 벌리지 않고 바를 가슴 아래쪽으로 천천히 내립니다.", "수직에 가깝게 밀기: 발을 바닥에 고정한 채 바를 어깨 위로 밀어 올립니다.", "안전하게 랙인: 반복 후 팔을 잠그기보다 통제한 상태로 랙에 걸어 종료합니다."), listOf("혼자 무리한 중량을 시도하지 말고 안전바나 보조자를 사용하세요.", "어깨 앞쪽 통증이 있으면 가동 범위와 중량을 줄이세요.", "바가 목 쪽으로 내려오지 않게 하세요."), 3, 5..8, null, 150),
        exercise("conventional_deadlift", "컨벤셔널 데드리프트", MuscleGroup.FULL_BODY, EquipmentType.BARBELL, DifficultyLevel.INTERMEDIATE, "conventional_deadlift", "바닥의 바벨을 들어 올리며 하체와 등, 코어 브레이싱을 함께 쓰는 전신 리프트입니다.", listOf("바와 발 위치: 바를 발 중간 위에 두고 정강이를 가깝게 세팅합니다.", "힙힌지와 그립: 등을 중립으로 유지한 채 엉덩이를 접고 바를 양손으로 잡습니다.", "브레이싱과 장력 만들기: 바를 당기기 전 겨드랑이와 복부에 힘을 주어 바가 몸에 붙게 합니다.", "바닥 밀며 기립: 바가 몸 가까이 지나가게 하며 무릎과 엉덩이를 함께 폅니다.", "통제 하강: 등을 둥글게 말지 않고 같은 경로로 바를 바닥에 내려놓습니다."), listOf("허리가 말리면 즉시 중량을 낮추고 시작 높이를 올리세요.", "바가 몸에서 멀어지지 않게 하세요.", "통증이 있으면 덤벨 데드리프트나 루마니안 데드리프트로 대체하세요."), 3, 3..6, null, 180),
        exercise("barbell_romanian_deadlift", "바벨 루마니안 데드리프트", MuscleGroup.LOWER_BODY, EquipmentType.BARBELL, DifficultyLevel.INTERMEDIATE, "barbell_romanian_deadlift", "바벨을 몸 가까이 두고 햄스트링과 둔근을 길게 쓰는 힙힌지 운동입니다.", listOf("서서 바 잡기: 바벨을 허벅지 앞에 두고 발을 골반너비로 세운 뒤 어깨를 낮춥니다.", "무릎 살짝 굽히기: 무릎 각도를 크게 바꾸지 않고 복부와 등을 단단히 고정합니다.", "엉덩이 뒤로 보내기: 바가 다리 가까이 내려가게 하며 햄스트링이 당기는 범위까지만 갑니다.", "둔근으로 복귀: 발바닥 전체를 누르고 엉덩이를 앞으로 보내 바를 허벅지 앞까지 되돌립니다."), listOf("허리가 말리면 즉시 범위를 줄이세요.", "바가 몸에서 멀어지지 않게 하세요.", "깊은 스쿼트처럼 앉지 말고 엉덩이를 뒤로 보내세요."), 3, 6..10, null, 120),
        exercise("barbell_bent_over_row", "바벨 벤트오버 로우", MuscleGroup.BACK, EquipmentType.BARBELL, DifficultyLevel.INTERMEDIATE, "barbell_bent_over_row", "힙힌지를 유지한 상태에서 등 중앙과 광배근을 함께 쓰는 바벨 당기기 운동입니다.", listOf("힙힌지 자세: 무릎을 살짝 굽히고 등을 중립으로 둔 채 상체를 숙입니다.", "바 잡고 몸통 고정: 바를 어깨너비 정도로 잡고 복부에 힘을 유지합니다.", "갈비뼈 쪽 당기기: 팔꿈치를 뒤로 보내며 바를 아랫갈비뼈 쪽으로 당깁니다.", "통제 하강: 어깨가 앞으로 무너지지 않게 바를 천천히 내립니다.", "자세 재정렬: 허리 긴장이 커지면 바를 내려놓고 다시 힙힌지를 잡습니다."), listOf("상체를 튕겨 반동으로 당기지 마세요.", "허리가 둥글게 말리면 중량을 낮추세요.", "햄스트링 유연성이 부족하면 체스트 서포티드 로우로 대체하세요."), 3, 6..10, null, 120),
        exercise("barbell_overhead_press", "바벨 오버헤드 프레스", MuscleGroup.SHOULDERS, EquipmentType.BARBELL, DifficultyLevel.INTERMEDIATE, "barbell_overhead_press", "서서 머리 위로 바벨을 밀며 어깨 힘과 몸통 안정성을 함께 요구하는 기본 프레스입니다.", listOf("랙과 시작 위치: 바를 어깨 앞쪽 쇄골 높이에 두고 손목을 중립에 가깝게 잡습니다.", "몸통 브레이싱: 엉덩이와 복부에 힘을 주고 갈비뼈가 들리지 않게 합니다.", "얼굴을 지나 밀기: 턱을 살짝 뒤로 빼며 바를 수직으로 밀어 올립니다.", "머리 위 고정: 바가 어깨와 중족부 위에 오도록 팔을 곧게 세웁니다.", "어깨 높이 복귀: 같은 경로로 천천히 내려 다음 반복을 준비합니다."), listOf("허리를 젖혀 밀지 마세요.", "어깨 통증이 있으면 머신 또는 덤벨 숄더 프레스로 대체하세요.", "처음에는 빈 바나 가벼운 바부터 시작하세요."), 3, 5..8, null, 150),
        exercise("dumbbell_step_up", "덤벨 스텝업", MuscleGroup.LOWER_BODY, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "dumbbell_step_up", "한쪽 다리 힘과 균형을 함께 기르는 하체 운동입니다.", listOf("오른발 올리기: 덤벨을 몸 옆에 두고 오른발 전체를 박스 위에 안정적으로 올립니다.", "박스 위로 서기: 바닥 발로 뛰지 말고 오른발로 박스를 밀어 몸을 위로 올립니다.", "통제해서 내려오기: 같은 오른발로 균형을 잡으며 천천히 바닥으로 내려옵니다.", "왼발로 교대 반복: 다음 반복은 왼발을 박스 위에 올려 좌우를 번갈아 진행합니다."), listOf("박스가 너무 높으면 골반이 흔들립니다.", "반동으로 뛰어오르지 마세요."), 2, 8..10, null, 90),
        exercise("glute_bridge", "글루트 브릿지", MuscleGroup.LOWER_BODY, EquipmentType.BODYWEIGHT, DifficultyLevel.BEGINNER, "glute_bridge", "엉덩이 수축을 배우기 좋은 낮은 난이도 운동입니다.", listOf("누운 자세: 매트에 등을 대고 누워 무릎을 세운 뒤 발바닥을 엉덩이 가까이에 둡니다.", "골반 올리기: 발바닥으로 바닥을 누르며 엉덩이를 들어 어깨부터 무릎까지 길게 만듭니다.", "둔근 수축 후 하강: 허리를 꺾지 않고 엉덩이를 조인 뒤 천천히 매트로 내려옵니다."), listOf("허리를 과하게 꺾지 마세요.", "목과 어깨에 힘을 주지 마세요."), 3, 10..15, null, 60),
        exercise("cable_glute_kickback", "케이블 글루트 킥백", MuscleGroup.LOWER_BODY, EquipmentType.CABLE, DifficultyLevel.INTERMEDIATE, "cable_glute_kickback", "엉덩이 뒤쪽 수축을 집중해서 느끼는 보조 운동입니다.", listOf("스트랩 연결: 낮은 케이블을 발목 스트랩에 연결하고 기둥을 가볍게 잡아 골반을 정면으로 둡니다.", "뒤로 밀기: 무릎을 살짝 굽힌 채 다리를 뒤로 밀어 엉덩이를 조이고 허리는 꺾지 않습니다.", "골반 고정 복귀: 케이블 장력을 유지하며 다리를 시작 위치로 천천히 되돌립니다."), listOf("허리 반동으로 차지 마세요.", "가벼운 무게로 가동 범위를 먼저 잡으세요."), 2, 10..15, null, 60),
        exercise("hip_abduction_machine", "힙 어브덕션 머신", MuscleGroup.LOWER_BODY, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "hip_abduction_machine", "엉덩이 측면과 골반 안정에 도움을 주는 머신 운동입니다.", listOf("바깥 패드 맞추기: 양 무릎과 허벅지 바깥쪽이 패드에 닿게 앉고 몸통을 세웁니다.", "바깥으로 벌리기: 바깥쪽 패드를 밀어 양 무릎을 대칭으로 벌립니다.", "중앙으로 복귀: 패드가 튕기지 않게 통제하며 시작 위치로 돌아옵니다."), listOf("상체를 크게 흔들지 마세요.", "엉덩이 옆쪽 느낌이 사라지면 중량을 낮추세요.", "어덕션처럼 안쪽 패드를 모으는 동작과 혼동하지 마세요."), 2, 12..15, null, 60),
        exercise("hip_adduction_machine", "힙 어덕션 머신", MuscleGroup.LOWER_BODY, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "hip_adduction_machine", "허벅지 안쪽을 안전하게 보조 강화하는 운동입니다.", listOf("벌린 시작 자세: 다리를 벌린 상태에서 허벅지 안쪽 패드가 양쪽 다리에 붙어 있는지 확인합니다.", "안쪽으로 모으기: 패드가 다리에서 떨어지지 않게 양 무릎을 중앙으로 모읍니다.", "열린 자세로 복귀: 패드가 튕기지 않도록 천천히 벌어진 시작 자세로 돌아갑니다."), listOf("가동 범위를 억지로 넓히지 마세요.", "골반이 들리거나 허리가 뜨지 않게 하세요.", "패드가 허벅지에서 떨어지면 중량을 낮추고 범위를 줄이세요."), 2, 12..15, null, 60),
        exercise("back_extension", "백 익스텐션", MuscleGroup.LOWER_BODY, EquipmentType.MACHINE, DifficultyLevel.INTERMEDIATE, "back_extension", "둔근과 허리 주변을 통제해서 강화하는 후면 체인 운동입니다.", listOf("패드 위치: 발을 고정하고 엉덩이 접히는 지점이 패드 위에 오도록 몸을 맞춥니다.", "상체 낮추기: 등을 중립으로 유지하며 둔근과 허벅지 뒤쪽이 늘어나는 범위까지 상체를 낮춥니다.", "일직선까지 펴기: 둔근과 햄스트링으로 몸을 들어 머리부터 발까지 길게 만듭니다.", "천천히 하강: 허리가 꺾이지 않게 같은 경로로 내려와 다음 반복을 준비합니다."), listOf("허리를 뒤로 과하게 젖히지 마세요.", "허리 통증이 있으면 제외하세요."), 2, 10..12, null, 90),
        exercise("straight_arm_pulldown", "스트레이트 암 풀다운", MuscleGroup.BACK, EquipmentType.CABLE, DifficultyLevel.BEGINNER, "straight_arm_pulldown", "팔꿈치를 거의 편 상태로 광배근 감각을 익히는 운동입니다.", listOf("케이블 높이: 높은 풀리에 바나 로프를 걸고 팔을 거의 편 상태로 엉덩이를 살짝 뒤로 보냅니다.", "허벅지 쪽 당기기: 팔꿈치 각도를 크게 바꾸지 않고 광배근으로 손잡이를 허벅지 앞까지 내립니다.", "광배 긴장 복귀: 어깨가 귀로 올라가지 않게 버티며 손잡이를 천천히 위로 되돌립니다."), listOf("허리를 젖혀 당기지 마세요.", "팔꿈치를 과하게 굽히면 로우가 됩니다."), 2, 10..15, null, 60),
        exercise("machine_row", "머신 로우", MuscleGroup.BACK, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "machine_row", "궤도가 안정되어 초보자가 등 당기기를 익히기 좋은 운동입니다.", listOf("패드·손잡이 조정: 가슴 패드에 몸을 붙이고 팔을 뻗었을 때 손잡이가 편하게 잡히도록 좌석을 맞춥니다.", "당기기: 가슴이 패드에서 뜨지 않게 유지하며 팔꿈치를 뒤로 보내 손잡이를 몸 쪽으로 당깁니다.", "통제 복귀: 어깨가 앞으로 말리지 않게 견갑을 통제하며 팔을 천천히 뻗습니다."), listOf("어깨를 으쓱하지 마세요.", "반동으로 몸을 뒤로 젖히지 마세요."), 3, 8..12, null, 90),
        exercise("t_bar_row", "체스트 서포티드 T바 로우", MuscleGroup.BACK, EquipmentType.MACHINE, DifficultyLevel.INTERMEDIATE, "t_bar_row", "가슴을 패드에 지지한 상태에서 등 중앙 볼륨을 늘리는 운동입니다.", listOf("패드 밀착: 가슴을 인클라인 패드에 붙이고 발을 지지대에 단단히 둔 채 손잡이를 잡습니다.", "하부 갈비뼈로 당기기: 몸통을 패드에서 떼지 않고 팔꿈치를 뒤로 보내 손잡이를 하부 갈비뼈 쪽으로 당깁니다.", "놓치지 않고 하강: 견갑을 통제하며 손잡이를 시작 위치로 천천히 돌려놓습니다."), listOf("목을 앞으로 빼지 마세요.", "허리로 들어 올리지 마세요.", "가슴이 패드에서 떨어지면 중량을 낮추세요."), 3, 8..10, null, 120),
        exercise("cable_pullover", "케이블 풀오버", MuscleGroup.BACK, EquipmentType.CABLE, DifficultyLevel.INTERMEDIATE, "cable_pullover", "광배근을 길게 쓰는 보조 당기기 운동입니다.", listOf("케이블 높이: 높은 풀리 앞에서 엉덩이를 살짝 뒤로 빼고 팔을 길게 뻗어 손잡이를 잡습니다.", "팔 길게 당기기: 팔꿈치를 살짝만 굽힌 채 손잡이를 허벅지 앞쪽으로 당기며 광배근을 조입니다.", "범위 내 복귀: 허리가 꺾이지 않게 같은 경로로 손잡이를 천천히 올려 시작 자세로 돌아갑니다."), listOf("어깨 앞쪽이 찝히면 범위를 줄이세요.", "허리 반동을 쓰지 마세요.", "팔꿈치를 많이 굽혀 푸시다운처럼 만들지 마세요."), 2, 10..15, null, 60),
        exercise("inverted_row", "인버티드 로우", MuscleGroup.BACK, EquipmentType.BODYWEIGHT, DifficultyLevel.INTERMEDIATE, "inverted_row", "몸무게로 수평 당기기 힘을 기르는 운동입니다.", listOf("바 아래 자세: 바 아래에 누워 손을 어깨너비로 잡고 몸통을 머리부터 발끝까지 일직선으로 만듭니다.", "가슴을 바 쪽으로 당기기: 팔꿈치를 뒤로 보내 가슴을 바 가까이 당기고 엉덩이가 처지지 않게 유지합니다.", "흔들림 없이 하강: 같은 몸통 라인을 유지하며 팔을 천천히 펴 시작 위치로 내려옵니다."), listOf("허리가 처지면 무릎을 굽혀 난이도를 낮추세요.", "목으로 바에 닿으려 하지 마세요.", "어깨를 으쓱해 당기지 마세요."), 3, 6..10, null, 90),
        exercise("dumbbell_shrug", "덤벨 슈러그", MuscleGroup.BACK, EquipmentType.DUMBBELL, DifficultyLevel.BEGINNER, "dumbbell_shrug", "승모근과 견갑 안정 보조를 위한 단순 운동입니다.", listOf("몸통 세우기: 양손에 덤벨을 들고 팔을 편 채 몸 옆에 둔 뒤 목과 허리를 길게 세웁니다.", "어깨 수직 상승: 팔꿈치를 굽히거나 어깨를 돌리지 않고 어깨만 귀 쪽으로 곧게 들어 올립니다.", "천천히 내리기: 목을 길게 유지하며 어깨를 통제해서 시작 위치로 내립니다."), listOf("어깨를 돌리지 마세요.", "목 통증이 있으면 제외하세요.", "팔로 덤벨을 당기지 말고 어깨만 움직이세요."), 2, 10..15, null, 60),
        exercise("pec_deck_fly", "펙덱 플라이", MuscleGroup.CHEST, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "pec_deck_fly", "가슴을 모으는 감각을 안전하게 배우는 머신 운동입니다.", listOf("패드 높이: 등과 엉덩이를 패드에 붙이고 팔꿈치와 손잡이가 가슴 높이에 오도록 앉습니다.", "앞으로 모으기: 어깨가 들리지 않게 손잡이를 가슴 앞에서 모으며 가슴을 조입니다.", "가슴 범위 내 복귀: 어깨가 과하게 뒤로 젖혀지기 전까지만 천천히 열어 시작 자세로 돌아갑니다."), listOf("어깨를 과하게 뒤로 보내지 마세요.", "무게보다 움직임 통제를 우선하세요.", "허리를 띄워 손잡이를 억지로 모으지 마세요."), 2, 10..15, null, 60),
        exercise("incline_machine_press", "인클라인 머신 프레스", MuscleGroup.CHEST, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "incline_machine_press", "윗가슴 프레스를 안정적으로 연습하는 머신 운동입니다.", listOf("손잡이 높이: 등을 기울어진 패드에 붙이고 손잡이가 윗가슴 옆에 오도록 앉습니다.", "대각선 밀기: 등과 엉덩이를 패드에 붙인 채 손잡이를 가슴 앞 대각선 위로 밀어냅니다.", "어깨 범위 내 복귀: 어깨가 앞쪽으로 말리지 않는 범위에서 손잡이를 윗가슴 옆으로 천천히 되돌립니다."), listOf("허리를 심하게 띄우지 마세요.", "어깨 전면 통증이 있으면 중단하세요.", "손잡이가 머리 위로만 올라가면 좌석 높이를 다시 맞추세요."), 3, 8..12, null, 90),
        exercise("dumbbell_floor_press", "덤벨 플로어 프레스", MuscleGroup.CHEST, EquipmentType.DUMBBELL, DifficultyLevel.BEGINNER, "dumbbell_floor_press", "바닥이 범위를 제한해 어깨 부담을 줄인 프레스입니다.", listOf("바닥에 눕기: 무릎을 세우고 양발을 바닥에 둔 채 덤벨을 가슴 양옆에 준비합니다.", "팔꿈치·손목 정렬: 팔꿈치는 몸통에서 30~45도 열고 손목을 덤벨 아래에 세웁니다.", "위로 밀기: 등은 바닥에 둔 채 양덤벨을 가슴 위로 밀고 팔꿈치는 잠그지 않습니다.", "팔꿈치 가볍게 하강: 팔꿈치가 바닥 가까이 오도록 천천히 내려 다음 반복을 준비합니다."), listOf("팔꿈치를 바닥에 세게 찍지 마세요.", "손목을 중립으로 유지하세요.", "벤치프레스처럼 허리를 과하게 띄우지 마세요."), 3, 8..12, null, 90),
        exercise("assisted_dip", "어시스티드 딥", MuscleGroup.CHEST, EquipmentType.MACHINE, DifficultyLevel.INTERMEDIATE, "assisted_dip", "가슴과 삼두를 함께 쓰는 보조 딥 운동입니다.", listOf("보조 패드 진입: 손잡이를 잡고 발판을 딛은 뒤 한쪽 무릎부터 보조 패드에 올립니다.", "손잡이 지지: 양무릎을 패드에 두고 팔을 편 상태에서 어깨를 내리며 몸통을 세웁니다.", "통증 없는 하강: 팔꿈치를 굽혀 몸을 내리되 어깨 앞쪽이 찝히기 전까지만 내려갑니다.", "밀어 올라오기: 손잡이를 밀어 팔을 펴며 시작 위치로 올라오고 패드와 몸통을 흔들지 않습니다."), listOf("어깨 앞쪽 통증이 있으면 제외하세요.", "너무 깊이 내려가지 마세요.", "보조 패드가 흔들리면 중량 보조를 늘리세요."), 2, 6..10, null, 120),
        exercise("dip", "딥스", MuscleGroup.CHEST, EquipmentType.BODYWEIGHT, DifficultyLevel.INTERMEDIATE, "dip", "평행봉에서 가슴과 삼두를 함께 쓰는 대표 맨몸 밀기 운동입니다.", listOf("손잡이 지지: 평행봉을 잡고 팔을 편 상태에서 어깨를 귀에서 멀리 내립니다.", "몸통 각도 잡기: 몸통을 살짝 앞으로 기울이고 복부에 힘을 줘 흔들림을 줄입니다.", "통증 없는 하강: 팔꿈치를 굽혀 몸을 내리되 어깨 앞쪽이 불편하기 전까지만 갑니다.", "밀어 올라오기: 손잡이를 밀어 시작 위치로 돌아오고 팔꿈치를 세게 잠그지 않습니다."), listOf("어깨 앞쪽 통증이 있으면 어시스티드 딥이나 푸시업으로 대체하세요.", "너무 깊이 내려가지 마세요.", "반동으로 몸을 튕기지 마세요."), 3, 6..10, null, 120),
        exercise("cable_chest_press", "케이블 체스트 프레스", MuscleGroup.CHEST, EquipmentType.CABLE, DifficultyLevel.INTERMEDIATE, "cable_chest_press", "케이블 장력으로 가슴 밀기와 몸통 안정성을 함께 쓰는 운동입니다.", listOf("케이블 높이: 양쪽 손잡이가 가슴 중간 높이에 오도록 맞추고 케이블 중앙에 섭니다.", "스탠스 잡기: 한 발을 앞에 둔 스플릿 스탠스로 서서 손잡이를 가슴 옆에 둡니다.", "앞으로 밀기: 몸통을 고정한 채 양손 손잡이를 가슴 앞쪽으로 밀어 팔을 거의 폅니다.", "몸통 고정 복귀: 케이블 장력을 유지하며 손잡이를 가슴 옆으로 천천히 되돌립니다."), listOf("허리를 꺾어 밀지 마세요.", "균형이 흔들리면 머신으로 대체하세요.", "케이블이 팔을 뒤로 끌고 가지 않게 통제하세요."), 2, 8..12, null, 90),
        exercise("close_grip_pushup", "클로즈 그립 푸시업", MuscleGroup.TRICEPS, EquipmentType.BODYWEIGHT, DifficultyLevel.INTERMEDIATE, "close_grip_pushup", "가슴과 삼두를 함께 쓰는 맨몸 보조 운동입니다.", listOf("손 위치: 손을 가슴 아래 좁은 간격으로 두고 머리부터 뒤꿈치까지 일직선을 만듭니다.", "팔꿈치 붙여 하강: 팔꿈치가 옆으로 벌어지지 않게 몸통 가까이 접으며 몸 전체를 함께 내립니다.", "하단 통제: 가슴을 바닥 가까이 두고 손 위치와 몸통 일직선을 유지합니다.", "밀어 올라오기: 손바닥으로 바닥을 밀어 시작 자세로 돌아오되 허리가 처지지 않게 합니다."), listOf("손목 통증이 있으면 손 위치를 넓히세요.", "허리가 처지면 무릎을 대고 진행하세요.", "팔꿈치가 옆으로 벌어지면 난이도를 낮추세요."), 2, 6..12, null, 90),
        exercise("arnold_press", "아놀드 프레스", MuscleGroup.SHOULDERS, EquipmentType.DUMBBELL, DifficultyLevel.INTERMEDIATE, "arnold_press", "어깨 전면과 측면을 넓은 범위로 쓰는 프레스입니다.", listOf("얼굴 앞 시작: 덤벨을 얼굴 앞에 세우고 손바닥이 몸쪽을 보게 하며 등을 벤치에 붙입니다.", "어깨 옆 회전: 팔꿈치를 옆으로 열며 덤벨을 어깨 높이로 돌려 손바닥이 앞을 보게 합니다.", "위로 밀기: 몸통을 젖히지 않고 덤벨을 머리 위로 밀어 팔을 거의 폅니다.", "같은 경로 복귀: 같은 회전 경로로 천천히 얼굴 앞 시작 자세까지 돌아옵니다."), listOf("어깨가 찝히면 일반 숄더 프레스로 바꾸세요.", "무거운 중량보다 부드러운 회전을 우선하세요.", "허리를 젖혀 덤벨을 밀어 올리지 마세요."), 2, 8..10, null, 120),
        exercise("front_raise", "덤벨 프론트 레이즈", MuscleGroup.SHOULDERS, EquipmentType.DUMBBELL, DifficultyLevel.BEGINNER, "front_raise", "어깨 전면을 가볍게 보조 강화하는 운동입니다.", listOf("덤벨 시작 위치: 양손 덤벨을 허벅지 앞에 두고 몸통을 세운 뒤 복부에 힘을 줍니다.", "정면으로 올리기: 팔꿈치를 살짝 굽힌 채 덤벨을 몸 앞쪽으로 천천히 들어 올립니다.", "어깨 높이: 덤벨이 어깨 높이에 오면 몸을 젖히지 않고 손목을 중립으로 유지합니다.", "통제 하강: 반동 없이 같은 경로로 덤벨을 허벅지 앞까지 천천히 내립니다."), listOf("허리를 젖혀 올리지 마세요.", "어깨 통증이 있으면 제외하세요.", "옆으로 벌리면 레터럴 레이즈가 되므로 몸 앞쪽 경로를 유지하세요."), 2, 10..12, null, 60),
        exercise("cable_lateral_raise", "케이블 레터럴 레이즈", MuscleGroup.SHOULDERS, EquipmentType.CABLE, DifficultyLevel.INTERMEDIATE, "cable_lateral_raise", "일정한 장력으로 어깨 측면을 자극하는 운동입니다.", listOf("낮은 케이블 세팅: 낮은 풀리에 손잡이를 연결하고 케이블 옆에 서서 손잡이를 몸 앞쪽에 둡니다.", "옆으로 올리기: 팔꿈치를 살짝 굽힌 채 케이블 장력을 유지하며 팔을 어깨 높이까지 옆으로 들어 올립니다.", "장력 유지 하강: 반동 없이 같은 경로로 천천히 내려 케이블 장력이 끊기기 전 다음 반복을 준비합니다."), listOf("몸을 기울여 반동을 만들지 마세요.", "가벼운 무게로 시작하세요.", "손목으로 당기지 말고 팔꿈치가 옆으로 올라간다고 생각하세요."), 2, 12..15, null, 60),
        exercise("landmine_press", "랜드마인 프레스", MuscleGroup.SHOULDERS, EquipmentType.BARBELL, DifficultyLevel.INTERMEDIATE, "landmine_press", "대각선 궤도로 어깨 부담을 낮춘 프레스 변형입니다.", listOf("바 고정 확인: 바벨 한쪽 끝이 바닥 거치대에 고정됐는지 확인하고 반대쪽 끝을 양손으로 잡습니다.", "어깨 앞 세팅: 바 끝을 가슴·어깨 앞에 두고 발을 단단히 딛어 몸통을 고정합니다.", "대각선 밀기: 바벨을 수직이 아니라 몸 앞 대각선 위쪽으로 밀어 팔을 뻗습니다.", "통제 복귀: 고정점이 흔들리지 않게 같은 대각선 경로로 바를 어깨 앞까지 되돌립니다."), listOf("허리를 젖혀 밀지 마세요.", "바 고정이 불안정하면 진행하지 마세요.", "수직 오버헤드 프레스처럼 밀지 말고 대각선 경로를 유지하세요."), 3, 8..10, null, 90),
        exercise("prone_y_raise", "프론 Y 레이즈", MuscleGroup.SHOULDERS, EquipmentType.BENCH, DifficultyLevel.BEGINNER, "prone_y_raise", "후면 어깨와 하부 승모근을 가볍게 깨우는 자세 보조 운동입니다.", listOf("벤치 엎드리기: 가슴을 인클라인 벤치에 지지하고 목을 중립으로 둔 채 팔을 아래로 내립니다.", "Y 방향 올리기: 엄지를 위로 둔 채 팔을 머리 앞 대각선 방향으로 들어 Y자 형태를 만듭니다.", "어깨 낮춰 하강: 승모근을 과하게 쓰지 않게 어깨를 낮추고 같은 Y 경로로 천천히 내립니다."), listOf("무게 없이 시작해도 충분합니다.", "허리를 꺾어 팔을 들지 마세요.", "팔을 옆으로만 벌리면 리버스 플라이가 되므로 Y 각도를 유지하세요."), 2, 10..12, null, 60),
        exercise("hammer_curl", "해머 컬", MuscleGroup.BICEPS, EquipmentType.DUMBBELL, DifficultyLevel.BEGINNER, "hammer_curl", "팔 앞쪽과 전완을 함께 강화하는 컬 변형입니다.", listOf("중립 그립: 양손 덤벨을 몸 옆에 두고 손바닥이 서로 마주 보게 세운 뒤 팔꿈치를 옆구리 가까이에 둡니다.", "중립 그립으로 올리기: 손목을 세운 채 덤벨을 어깨 쪽으로 말아 올리고 팔꿈치가 앞으로 밀리지 않게 합니다.", "상단 통제 후 하강: 어깨가 들리지 않게 잠깐 멈춘 뒤 같은 경로로 천천히 내립니다."), listOf("몸통 반동을 쓰지 마세요.", "팔꿈치가 앞으로 밀리지 않게 하세요.", "손목이 꺾이면 무게를 낮추세요."), 3, 10..12, null, 60),
        exercise("preacher_curl_machine", "프리처 컬 머신", MuscleGroup.BICEPS, EquipmentType.MACHINE, DifficultyLevel.BEGINNER, "preacher_curl_machine", "팔꿈치를 지지해 이두 자극을 쉽게 느끼는 운동입니다.", listOf("상완 패드 고정: 가슴과 상완을 패드에 붙이고 손잡이를 잡아 팔꿈치가 들리지 않게 고정합니다.", "말아 올리기: 팔꿈치를 패드에 둔 채 손잡이를 어깨 쪽으로 당기고 손목을 중립으로 유지합니다.", "통제 하강: 팔이 완전히 잠기기 전까지 천천히 내리고 반동 없이 다음 반복을 준비합니다."), listOf("아래에서 팔을 튕기지 마세요.", "손목을 꺾지 마세요.", "어깨를 들어 손잡이를 당기지 마세요."), 2, 10..12, null, 60),
        exercise("rope_overhead_triceps", "로프 오버헤드 트라이셉스", MuscleGroup.TRICEPS, EquipmentType.CABLE, DifficultyLevel.INTERMEDIATE, "rope_overhead_triceps", "케이블로 팔 뒤쪽을 길게 쓰는 보조 운동입니다.", listOf("세팅·팔꿈치 고정: 높은 풀리의 로프를 잡고 케이블이 뒤에서 당겨지는 위치에 선 뒤 팔꿈치를 머리 옆에 고정합니다.", "위로 펴기: 상완을 크게 흔들지 말고 팔꿈치를 펴 로프 끝을 머리 위 앞으로 보냅니다.", "장력 유지 복귀: 케이블이 느슨해지지 않게 같은 경로로 로프를 머리 뒤쪽까지 천천히 되돌립니다."), listOf("허리를 꺾어 버티지 마세요.", "어깨가 불편하면 푸시다운으로 대체하세요.", "몸 앞 아래로 누르면 푸시다운이 되므로 케이블이 뒤에서 당겨지게 유지하세요."), 2, 10..15, null, 60),
        exercise("reverse_curl", "리버스 컬", MuscleGroup.FOREARMS, EquipmentType.BARBELL, DifficultyLevel.INTERMEDIATE, "reverse_curl", "전완과 팔꿈치 주변 보조 근육을 강화하는 운동입니다.", listOf("손등 위 그립: 바를 어깨너비로 잡고 손등이 위를 보게 한 뒤 팔꿈치를 옆구리 가까이에 둡니다.", "올리기: 손목을 꺾지 않고 팔꿈치를 고정한 채 바를 가슴 아래까지 말아 올립니다.", "전완 긴장 유지 하강: 전완 긴장을 유지하며 같은 경로로 천천히 내려 다음 반복을 준비합니다."), listOf("손목이 꺾이지 않게 하세요.", "팔꿈치 통증이 있으면 제외하세요.", "몸통을 뒤로 젖혀 바를 올리지 마세요."), 2, 10..12, null, 60),
        exercise("side_plank", "사이드 플랭크", MuscleGroup.CORE, EquipmentType.BODYWEIGHT, DifficultyLevel.BEGINNER, "side_plank", "옆구리와 골반 안정성을 기르는 코어 운동입니다.", listOf("팔꿈치 위치: 팔꿈치를 어깨 바로 아래에 두고 옆으로 누워 다리를 길게 뻗습니다.", "골반 들어 일직선: 팔꿈치와 발 옆면으로 지지하며 골반을 들어 머리부터 발끝까지 일직선을 만듭니다.", "호흡 유지: 골반이 뒤로 빠지거나 아래로 떨어지지 않게 버티며 천천히 호흡합니다."), listOf("어깨 통증이 있으면 시간을 줄이세요.", "골반이 뒤로 빠지지 않게 하세요.", "허리가 꺾이면 무릎을 대고 진행하세요."), 2, null, 1, 60),
        exercise("reverse_crunch", "리버스 크런치", MuscleGroup.CORE, EquipmentType.BODYWEIGHT, DifficultyLevel.BEGINNER, "reverse_crunch", "허리 부담을 낮추며 하복부 조절을 연습하는 운동입니다.", listOf("누운 자세: 매트에 누워 양무릎을 굽히고 정강이를 바닥과 거의 평행하게 둡니다.", "골반 말기: 반동 없이 복부로 골반을 말아 엉덩이가 매트에서 살짝 뜨게 무릎을 가슴 쪽으로 당깁니다.", "반동 없이 하강: 허리가 과하게 뜨지 않게 통제하며 시작 자세로 천천히 돌아옵니다."), listOf("다리를 휘둘러 반동을 만들지 마세요.", "목에 힘을 주지 마세요.", "허리가 꺾이면 범위를 줄이세요."), 2, 10..12, null, 60),
        exercise("cable_crunch", "케이블 크런치", MuscleGroup.CORE, EquipmentType.CABLE, DifficultyLevel.INTERMEDIATE, "cable_crunch", "복부를 굽히는 힘을 케이블로 강화하는 운동입니다.", listOf("무릎 꿇고 로프 잡기: 높은 풀리 앞에 무릎을 꿇고 로프를 머리 옆에 둔 채 엉덩이를 고정합니다.", "갈비뼈 말기: 팔로 당기지 말고 복부로 갈비뼈를 골반 쪽으로 말아 몸통을 둥글게 접습니다.", "복부 긴장 복귀: 케이블 장력을 유지하며 같은 경로로 천천히 올라와 다음 반복을 준비합니다."), listOf("팔로 로프를 당기지 마세요.", "허리 통증이 있으면 제외하세요.", "엉덩이가 뒤로 빠져 힙힌지가 되지 않게 하세요."), 2, 10..15, null, 60),
        exercise("hanging_knee_raise", "행잉 니 레이즈", MuscleGroup.CORE, EquipmentType.BODYWEIGHT, DifficultyLevel.INTERMEDIATE, "hanging_knee_raise", "매달린 자세에서 복부와 고관절을 함께 쓰는 운동입니다.", listOf("매달리기: 양손으로 바를 잡고 어깨를 귀에서 멀리 둔 채 다리를 모아 아래로 둡니다.", "무릎 끌어올리기: 반동 없이 복부로 골반을 살짝 말며 양무릎을 가슴 쪽으로 끌어올립니다.", "천천히 내리기: 몸통 흔들림을 줄이며 다리를 시작 자세까지 천천히 내려 다음 반복을 준비합니다."), listOf("허리가 아프면 리버스 크런치로 대체하세요.", "반동으로 다리를 던지지 마세요.", "어깨에 통증이 있으면 매달리는 시간을 줄이거나 중단하세요."), 2, 8..12, null, 90),
        exercise("mountain_climber", "마운틴 클라이머", MuscleGroup.CORE, EquipmentType.BODYWEIGHT, DifficultyLevel.INTERMEDIATE, "mountain_climber", "코어 안정과 심박 상승을 함께 만드는 맨몸 운동입니다.", listOf("푸시업 자세: 양손을 어깨 아래에 두고 머리부터 뒤꿈치까지 일직선을 만듭니다.", "오른무릎 당기기: 양손을 고정한 채 오른무릎을 가슴 쪽으로 당기고 왼다리는 뒤로 뻗습니다.", "몸통 유지: 오른다리를 뒤로 보내 다시 일직선 플랭크를 만들고 엉덩이를 낮게 유지합니다.", "왼무릎 당기기: 이번에는 왼무릎을 가슴 쪽으로 당기고 오른다리를 뒤로 뻗어 교대합니다."), listOf("허리가 처지면 속도를 늦추세요.", "손목 통증이 있으면 제외하세요.", "같은 무릎만 반복하지 말고 좌우를 교대하세요."), 2, null, 1, 60),
        exercise("farmer_carry", "덤벨 파머 캐리", MuscleGroup.FULL_BODY, EquipmentType.DUMBBELL, DifficultyLevel.BEGINNER, "farmer_carry", "악력, 코어, 자세 유지력을 동시에 기르는 걷기 운동입니다.", listOf("키 크게 서기: 양손 덤벨을 몸 옆에 두고 어깨를 귀에서 멀리 내린 채 복부를 조입니다.", "짧은 보폭으로 걷기: 덤벨이 몸 옆에서 흔들리지 않게 일정한 보폭으로 앞으로 걷습니다.", "자세 유지: 시선은 정면, 갈비뼈와 골반은 정렬한 채 끝 지점까지 같은 자세를 유지합니다."), listOf("몸이 한쪽으로 기울면 중량을 낮추세요.", "허리를 꺾고 걷지 마세요.", "덤벨을 허벅지에 부딪히게 흔들지 마세요."), 3, null, 1, 60),
        exercise("elliptical", "일립티컬", MuscleGroup.CARDIO, EquipmentType.CARDIO_MACHINE, DifficultyLevel.BEGINNER, "elliptical", "관절 충격을 낮추며 전신 리듬을 만드는 유산소입니다.", listOf("발·손잡이 세팅: 양발을 긴 페달 위에 올리고 양손으로 움직이는 손잡이를 가볍게 잡습니다.", "오른발 전방 보폭: 오른발 페달이 앞아래로 움직일 때 반대 손잡이를 밀고 당기며 리듬을 만듭니다.", "왼발 전방 보폭: 왼발 페달이 앞아래로 움직이게 교대하며 상체는 세우고 발을 페달에서 떼지 않습니다."), listOf("처음부터 높은 저항을 걸지 마세요.", "어지러우면 즉시 멈추세요.", "손잡이에 체중을 과하게 싣지 마세요."), 1, null, 12, 30),
        exercise("stair_climber", "스테어 클라이머", MuscleGroup.CARDIO, EquipmentType.CARDIO_MACHINE, DifficultyLevel.INTERMEDIATE, "stair_climber", "하체 지구력과 심박을 함께 올리는 유산소 머신입니다.", listOf("난간 보조: 손은 난간에 가볍게 올리고 몸통을 세운 채 첫 계단을 천천히 밟습니다.", "발 전체로 오르기: 발바닥 전체를 계단에 올리고 반대쪽 발을 들어 다음 계단으로 자연스럽게 이어갑니다.", "속도 조절: 속도를 올리더라도 상체를 흔들지 말고 계단 리듬과 호흡을 일정하게 유지합니다."), listOf("난간에 체중을 기대지 마세요.", "무릎 통증이 있으면 자전거로 대체하세요.", "계단을 발끝으로만 밟지 말고 발 전체를 사용하세요."), 1, null, 8, 30),
        exercise("rowing_machine", "로잉 머신", MuscleGroup.CARDIO, EquipmentType.CARDIO_MACHINE, DifficultyLevel.INTERMEDIATE, "rowing_machine", "하체-등-팔 순서의 전신 유산소 운동입니다.", listOf("캐치 자세: 발을 스트랩에 고정하고 무릎을 굽힌 채 팔을 길게 뻗어 손잡이를 잡습니다.", "다리로 밀기: 팔은 거의 편 채 다리 힘으로 시트를 뒤로 보내고 손잡이를 배 쪽으로 이동시킵니다.", "피니시 당기기: 다리를 거의 편 뒤 몸통을 살짝 뒤로 두고 손잡이를 갈비뼈 아래까지 당깁니다.", "팔-몸통-다리 복귀: 팔을 먼저 펴고 몸통을 세운 뒤 무릎을 굽혀 캐치 자세로 돌아옵니다."), listOf("허리를 둥글게 말고 당기지 마세요.", "처음에는 낮은 저항으로 순서를 익히세요.", "팔보다 다리 힘으로 먼저 밀어 순서를 지키세요."), 1, null, 8, 30),
        exercise("battle_rope", "배틀 로프", MuscleGroup.FULL_BODY, EquipmentType.CABLE, DifficultyLevel.INTERMEDIATE, "battle_rope", "짧은 시간 심박과 상체 지구력을 올리는 컨디셔닝 운동입니다.", listOf("스탠스 잡기: 무릎과 엉덩이를 살짝 굽히고 양손으로 로프 끝을 잡아 몸통을 낮춥니다.", "한쪽 로프 올리기: 몸통을 고정한 채 한 손을 빠르게 들어 한쪽 로프에 큰 파형을 만듭니다.", "반대쪽 로프 올리기: 바로 반대 손을 들어 로프 파형이 좌우로 교대되게 합니다.", "리듬 유지: 허리를 꺾지 않고 짧고 빠른 리듬으로 양팔 웨이브를 이어갑니다."), listOf("허리로 반동을 만들지 마세요.", "어깨 통증이 있으면 제외하세요.", "로프를 당기기보다 위아래 파형을 만든다고 생각하세요."), 3, null, 1, 60),
        exercise("sled_push", "슬레드 푸시", MuscleGroup.FULL_BODY, EquipmentType.MACHINE, DifficultyLevel.INTERMEDIATE, "sled_push", "하체 힘과 심폐 능력을 함께 쓰는 전신 밀기 운동입니다.", listOf("공간·중량 확인: 이동할 공간을 확인하고 슬레드 중앙 기둥에 원판이 안정적으로 꽂혔는지 봅니다.", "손잡이 잡기: 양손으로 세로 손잡이를 잡고 팔을 길게 편 채 몸을 슬레드 쪽으로 기울입니다.", "몸 기울이기: 어깨부터 엉덩이까지 단단한 사선을 만들고 짧은 보폭으로 바닥을 밀기 시작합니다.", "짧은 보폭으로 밀기: 손잡이를 놓지 말고 무릎을 빠르게 교대하며 슬레드를 앞으로 계속 밀어냅니다."), listOf("허리가 꺾이지 않게 복부를 조이세요.", "공간과 장비 안전을 먼저 확인하세요.", "슬레드를 끌지 말고 손잡이를 밀어 앞으로 이동하세요."), 3, null, 1, 90),
        exercise("dumbbell_deadlift", "덤벨 데드리프트", MuscleGroup.FULL_BODY, EquipmentType.DUMBBELL, DifficultyLevel.BEGINNER, "dumbbell_deadlift", "바벨 전 단계로 힙힌지와 들어올리기 패턴을 배우는 운동입니다.", listOf("덤벨 위치: 양손 덤벨을 허벅지 옆에 두고 발을 골반너비로 세워 등 중립을 만듭니다.", "힙힌지: 무릎을 살짝 굽히고 엉덩이를 뒤로 보내며 덤벨을 다리 가까이 내립니다.", "하단 힌지: 덤벨을 정강이 앞쪽까지 내리되 깊은 스쿼트처럼 앉지 않고 등을 곧게 유지합니다.", "바닥 밀며 기립: 엉덩이를 앞으로 밀어 덤벨이 몸 가까이 올라오게 하며 시작 자세로 돌아옵니다."), listOf("허리가 말리면 높이를 올려 시작하세요.", "무거운 중량보다 시작 자세를 우선하세요.", "스쿼트처럼 무릎을 깊게 접지 말고 엉덩이를 뒤로 보내세요."), 3, 8..10, null, 90),
        exercise("kettlebell_deadlift", "케틀벨 데드리프트", MuscleGroup.FULL_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.BEGINNER, "kettlebell_deadlift", "케틀벨 하나로 힙힌지와 안전한 들어올리기 패턴을 배우는 기본 운동입니다.", listOf("발 사이 벨 세팅: 케틀벨을 발 중앙선 아래에 두고 발바닥 전체로 바닥을 누릅니다.", "힙힌지로 내려가기: 무릎을 살짝 굽히고 엉덩이를 뒤로 보내며 등을 중립으로 유지합니다.", "등 중립으로 잡기: 어깨를 귀에서 멀리 두고 양손으로 손잡이를 단단히 잡습니다.", "바닥 밀며 일어서기: 케틀벨이 몸에서 멀어지지 않게 하며 엉덩이와 다리 힘으로 일어섭니다."), listOf("허리가 말리면 케틀벨을 높은 박스 위에서 시작하세요.", "케틀벨을 몸 앞 멀리 두고 끌어올리지 마세요."), 3, 8..12, null, 90),
        exercise("kettlebell_romanian_deadlift", "케틀벨 루마니안 데드리프트", MuscleGroup.LOWER_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.INTERMEDIATE, "kettlebell_romanian_deadlift", "햄스트링과 둔근을 길게 쓰는 케틀벨 힙힌지 운동입니다.", listOf("서서 벨 잡기: 케틀벨을 양손으로 잡고 허벅지 앞에 둔 채 키를 크게 세웁니다.", "무릎 살짝 굽힘: 무릎 각도를 크게 바꾸지 않고 복부와 등을 고정합니다.", "엉덩이 뒤로 보내기: 케틀벨을 다리 가까이 내리며 햄스트링이 당기는 범위까지만 갑니다.", "둔근으로 복귀: 발바닥 전체로 바닥을 밀고 엉덩이를 조여 시작 자세로 돌아옵니다."), listOf("깊이보다 등 중립과 케틀벨 경로를 우선하세요.", "어깨가 앞으로 말리면 중량을 낮추세요."), 3, 8..10, null, 90),
        exercise("kettlebell_sumo_deadlift", "케틀벨 스모 데드리프트", MuscleGroup.LOWER_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.BEGINNER, "kettlebell_sumo_deadlift", "넓은 스탠스로 하체와 둔근을 안정적으로 쓰는 데드리프트 변형입니다.", listOf("넓은 스탠스: 발끝을 약간 바깥으로 열고 케틀벨을 몸 중앙 아래에 둡니다.", "무릎·발끝 정렬: 무릎이 발끝 방향을 따라가도록 복부를 고정합니다.", "수직으로 잡기: 상체를 세운 느낌으로 내려가 손잡이를 단단히 잡습니다.", "바닥 밀며 기립: 케틀벨이 위아래로 곧게 움직이도록 다리 힘으로 일어섭니다."), listOf("무릎이 안쪽으로 무너지면 보폭을 줄이세요.", "허리로 먼저 들어 올리지 마세요."), 3, 8..12, null, 90),
        exercise("kettlebell_goblet_squat", "케틀벨 고블릿 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.BEGINNER, "kettlebell_goblet_squat", "케틀벨을 가슴 앞에 들고 스쿼트 정렬을 배우는 대표 하체 운동입니다.", listOf("벨 가슴 앞 고정: 케틀벨을 뿔 또는 몸통 부분으로 잡고 팔꿈치를 몸 가까이 둡니다.", "갈비뼈와 코어 정렬: 갈비뼈가 들리지 않게 복부를 조이고 발바닥 전체를 누릅니다.", "앉기: 무릎과 발끝 방향을 맞추며 통제 가능한 깊이까지 내려갑니다.", "발바닥으로 상승: 가슴 앞 케틀벨 위치를 유지한 채 바닥을 밀어 일어섭니다."), listOf("허리가 말리면 깊이를 줄이세요.", "케틀벨이 몸에서 멀어지면 중량을 낮추세요."), 3, 8..12, null, 90),
        exercise("kettlebell_box_squat", "케틀벨 박스 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.BEGINNER, "kettlebell_box_squat", "박스로 깊이를 제한해 케틀벨 스쿼트를 안정적으로 연습합니다.", listOf("박스 높이 확인: 무릎과 허리가 편한 높이의 박스나 벤치를 뒤에 둡니다.", "고블릿 홀드: 케틀벨을 가슴 앞에 고정하고 발 위치를 맞춥니다.", "가볍게 터치: 박스에 털썩 앉지 않고 엉덩이만 가볍게 닿게 내려갑니다.", "반동 없이 일어서기: 긴장을 유지한 채 발바닥 전체로 바닥을 밀어 올라옵니다."), listOf("박스에 기대 쉬지 마세요.", "상체가 과하게 숙여지면 중량을 낮추세요."), 3, 8..10, null, 90),
        exercise("kettlebell_reverse_lunge", "케틀벨 리버스 런지", MuscleGroup.LOWER_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.INTERMEDIATE, "kettlebell_reverse_lunge", "케틀벨을 들고 뒤로 내딛어 하체와 균형을 함께 훈련합니다.", listOf("고블릿 또는 양손 캐리: 균형이 쉬운 방식으로 케틀벨을 잡고 몸통을 세웁니다.", "오른발 뒤로 내려가기: 오른발을 뒤로 내딛고 앞발 전체에 체중을 둔 채 뒤무릎을 바닥 가까이 내립니다.", "원위치 복귀: 앞발 전체로 바닥을 밀어 몸통을 세운 채 시작 자세로 돌아옵니다.", "왼발 뒤로 내려가기: 왼발도 같은 방식으로 뒤로 내딛고 앞무릎이 안쪽으로 무너지지 않게 반복합니다."), listOf("균형이 흔들리면 맨몸 리버스 런지부터 진행하세요.", "앞무릎 통증이 있으면 보폭과 깊이를 줄이세요."), 3, 8..10, null, 90),
        exercise("kettlebell_split_squat", "케틀벨 스플릿 스쿼트", MuscleGroup.LOWER_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.INTERMEDIATE, "kettlebell_split_squat", "고정된 보폭에서 한쪽 다리 힘과 골반 안정성을 키우는 운동입니다.", listOf("보폭 고정: 앞발과 뒷발을 나누어 서고 케틀벨을 안정적으로 잡습니다.", "몸통 세우기: 골반이 돌아가지 않게 복부를 조이고 시선은 정면에 둡니다.", "수직 하강: 앞발 중심을 유지하며 뒤무릎을 바닥 가까이 내립니다.", "앞발로 밀기: 앞발 전체로 바닥을 밀어 올라오고 같은 반복 수를 반대쪽도 수행합니다."), listOf("뒤꿈치가 들리거나 무릎이 안쪽으로 꺾이면 보폭을 조정하세요.", "처음부터 무거운 케틀벨을 들지 마세요."), 3, 8..10, null, 90),
        exercise("kettlebell_step_up", "케틀벨 스텝업", MuscleGroup.LOWER_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.INTERMEDIATE, "kettlebell_step_up", "케틀벨을 들고 박스 위로 올라 하체와 균형을 강화합니다.", listOf("박스 높이 확인: 무릎이 과하게 높아지지 않는 안정적인 박스를 선택합니다.", "한발 전체 올리기: 오른발 전체를 박스 위에 올리고 케틀벨이 흔들리지 않게 잡습니다.", "위로 서기: 오른발로 박스를 밀어 몸을 세우고 왼발을 가볍게 따라 올립니다.", "천천히 내려오기: 같은 발로 통제해 내려온 뒤 반대쪽도 같은 반복 수를 수행합니다."), listOf("박스가 흔들리면 진행하지 마세요.", "뒤쪽 발로 튀어 오르지 말고 위쪽 발 힘을 사용하세요."), 3, 8..10, null, 90),
        exercise("kettlebell_bent_over_row", "케틀벨 벤트오버 로우", MuscleGroup.BACK, EquipmentType.KETTLEBELL, DifficultyLevel.INTERMEDIATE, "kettlebell_bent_over_row", "힙힌지 자세에서 등과 코어를 함께 쓰는 케틀벨 로우입니다.", listOf("힙힌지 자세: 무릎을 살짝 굽히고 엉덩이를 뒤로 보내 등을 중립으로 둡니다.", "벨 아래 정렬: 케틀벨을 어깨 아래에 두고 몸통 흔들림을 줄입니다.", "팔꿈치 뒤로 당기기: 팔꿈치를 등 뒤로 보내며 케틀벨을 옆구리 방향으로 당깁니다.", "통제 하강: 어깨가 앞으로 끌려가지 않게 천천히 내립니다."), listOf("허리 반동으로 케틀벨을 당기지 마세요.", "힌지가 불편하면 원암 로우나 체스트 서포티드 로우로 대체하세요."), 3, 8..12, null, 90),
        exercise("one_arm_kettlebell_row", "원암 케틀벨 로우", MuscleGroup.BACK, EquipmentType.KETTLEBELL, DifficultyLevel.INTERMEDIATE, "one_arm_kettlebell_row", "한쪽씩 등 수축과 몸통 회전 억제를 연습하는 케틀벨 운동입니다.", listOf("벤치 지지: 한 손과 한쪽 무릎 또는 발을 벤치에 두고 몸통을 안정시킵니다.", "몸통 고정: 골반과 어깨가 바닥을 향하도록 유지합니다.", "벨 당기기: 팔꿈치를 뒤로 보내 케틀벨을 옆구리 가까이 당깁니다.", "회전 없이 내리기: 몸이 열리지 않게 케틀벨을 천천히 내려놓습니다."), listOf("몸통을 비틀어 올리지 마세요.", "손목이 꺾이면 그립을 다시 잡으세요."), 3, 8..12, null, 90),
        exercise("kettlebell_floor_press", "케틀벨 플로어 프레스", MuscleGroup.CHEST, EquipmentType.KETTLEBELL, DifficultyLevel.BEGINNER, "kettlebell_floor_press", "바닥에서 가슴과 삼두를 안전한 범위로 누르는 케틀벨 프레스입니다.", listOf("바닥에 눕기: 무릎을 세우고 케틀벨이 손목 위에 안정적으로 오게 잡습니다.", "팔꿈치 각도 잡기: 팔꿈치를 몸통에서 약간 떨어뜨려 어깨 앞쪽이 편한 위치를 찾습니다.", "밀어 올리기: 손목을 세운 채 케틀벨을 천장 방향으로 밀어 올립니다.", "팔꿈치 터치 후 반대쪽: 팔꿈치가 바닥에 가볍게 닿을 때까지 천천히 내리고 같은 반복 수를 반대쪽도 수행합니다."), listOf("케틀벨이 손목 뒤로 꺾이지 않게 하세요.", "어깨 앞쪽 통증이 있으면 범위를 줄이세요."), 3, 8..12, null, 90),
        exercise("kettlebell_shoulder_press", "케틀벨 숄더 프레스", MuscleGroup.SHOULDERS, EquipmentType.KETTLEBELL, DifficultyLevel.INTERMEDIATE, "kettlebell_shoulder_press", "랙 포지션에서 어깨와 코어 안정성을 함께 쓰는 프레스입니다.", listOf("랙 포지션: 케틀벨을 전완 바깥에 기대고 손목을 세운 채 팔꿈치를 몸 가까이 둡니다.", "복부 고정: 갈비뼈가 들리지 않게 복부를 조이고 엉덩이에 힘을 줍니다.", "수직 프레스: 팔꿈치가 옆으로 벌어지지 않게 케틀벨을 머리 위로 밀어 올립니다.", "통제 하강 후 반대쪽: 같은 경로로 랙 포지션으로 돌아오고 같은 반복 수를 반대쪽도 수행합니다."), listOf("허리를 젖혀 밀지 마세요.", "어깨 통증이 있으면 하프 니링 프레스나 머신 프레스로 대체하세요."), 3, 6..10, null, 120),
        exercise("half_kneeling_kettlebell_press", "하프 니링 케틀벨 프레스", MuscleGroup.SHOULDERS, EquipmentType.KETTLEBELL, DifficultyLevel.INTERMEDIATE, "half_kneeling_kettlebell_press", "반무릎 자세로 허리 반동을 줄이고 어깨 안정성을 키우는 프레스입니다.", listOf("반무릎 자세: 누른 쪽 반대 무릎을 세우고 골반이 돌아가지 않게 정렬합니다.", "랙 포지션: 케틀벨을 어깨 앞에 두고 손목과 팔꿈치를 안정시킵니다.", "복부 고정 프레스: 몸통이 기울지 않게 케틀벨을 머리 위로 밀어 올립니다.", "천천히 하강: 랙 포지션으로 내려온 뒤 같은 반복 수를 반대쪽도 수행합니다."), listOf("갈비뼈가 들리면 중량을 낮추세요.", "무릎이 불편하면 패드를 사용하세요."), 3, 6..10, null, 120),
        exercise("kettlebell_halo", "케틀벨 헤일로", MuscleGroup.SHOULDERS, EquipmentType.KETTLEBELL, DifficultyLevel.BEGINNER, "kettlebell_halo", "가벼운 케틀벨로 어깨 가동성과 코어 정렬을 연습하는 준비 운동입니다.", listOf("가벼운 벨 잡기: 케틀벨을 거꾸로 잡고 가슴 앞에서 목을 길게 세웁니다.", "머리 주변 회전: 케틀벨을 머리 가까이 천천히 돌리며 몸통은 정면을 유지합니다.", "방향 교대: 한 방향 반복을 마친 뒤 반대 방향도 같은 속도로 진행합니다."), listOf("무겁게 하지 말고 부드러운 범위를 우선하세요.", "목을 앞으로 빼거나 허리를 젖히지 마세요."), 2, 6..8, null, 60),
        exercise("kettlebell_suitcase_carry", "케틀벨 수트케이스 캐리", MuscleGroup.CORE, EquipmentType.KETTLEBELL, DifficultyLevel.BEGINNER, "kettlebell_suitcase_carry", "한 손으로 케틀벨을 들고 걷는 코어와 악력 안정 운동입니다.", listOf("한손 벨 들기: 케틀벨을 한 손으로 들고 반대쪽 어깨가 내려앉지 않게 섭니다.", "몸통 수직 유지: 갈비뼈와 골반을 쌓아 올린 느낌으로 키를 크게 세웁니다.", "천천히 걷기: 케틀벨이 허벅지 옆에서 흔들리지 않게 짧은 보폭으로 걷습니다.", "반대쪽 반복: 안전하게 내려놓고 같은 시간 또는 거리만큼 반대쪽도 수행합니다."), listOf("몸이 케틀벨 쪽으로 기울면 중량을 낮추세요.", "허리를 꺾고 걷지 마세요."), 3, null, 1, 60),
        exercise("kettlebell_farmer_carry", "케틀벨 파머 캐리", MuscleGroup.FULL_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.BEGINNER, "kettlebell_farmer_carry", "양손 케틀벨로 악력, 자세 유지력, 코어를 함께 강화합니다.", listOf("양손 벨 세팅: 양쪽에 비슷한 무게의 케틀벨을 두고 힙힌지로 잡습니다.", "키 크게 서기: 어깨를 귀에서 멀리 두고 복부를 조여 몸을 세웁니다.", "흔들림 없이 걷기: 케틀벨이 몸 옆에서 크게 흔들리지 않게 일정한 보폭으로 걷습니다.", "안전하게 내려놓기: 힙힌지로 내려가 케틀벨을 발 옆에 조용히 놓습니다."), listOf("어깨가 말리면 무게를 줄이세요.", "놓을 때 허리를 둥글게 말지 마세요."), 3, null, 1, 60),
        exercise("kettlebell_rack_carry", "케틀벨 랙 캐리", MuscleGroup.FULL_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.INTERMEDIATE, "kettlebell_rack_carry", "랙 포지션을 유지하며 호흡과 몸통 안정성을 훈련합니다.", listOf("랙 포지션 만들기: 케틀벨을 전완 바깥에 기대고 손목을 세웁니다.", "갈비뼈 내리기: 복부를 조이고 팔꿈치를 몸 가까이 둡니다.", "짧게 걷기: 몸통이 기울지 않게 천천히 걷고 호흡을 유지합니다.", "반대쪽 반복: 안전하게 내려놓은 뒤 반대쪽도 같은 시간 진행합니다."), listOf("손목이 접히면 즉시 다시 잡으세요.", "허리를 젖혀 버티지 마세요."), 3, null, 1, 60),
        exercise("two_hand_kettlebell_swing", "투핸드 케틀벨 스윙", MuscleGroup.FULL_BODY, EquipmentType.KETTLEBELL, DifficultyLevel.INTERMEDIATE, "two_hand_kettlebell_swing", "힙 스냅으로 둔근과 햄스트링, 심폐를 함께 쓰는 중급 전신 운동입니다.", listOf("벨 앞 세팅: 케틀벨을 발 앞에 두고 힙힌지로 내려가 손잡이를 양손으로 잡습니다.", "하이크 패스: 케틀벨을 다리 사이로 당겨 햄스트링에 긴장을 만듭니다.", "힙 스냅: 팔로 들지 않고 엉덩이를 빠르게 펴 케틀벨이 가슴 높이까지 떠오르게 합니다.", "힌지로 받기: 케틀벨이 내려올 때 엉덩이를 뒤로 보내 같은 경로로 받아 다음 반복을 이어갑니다."), listOf("스쿼트처럼 앉아 올리지 마세요.", "허리로 케틀벨을 들어 올리면 즉시 중단하세요.", "초보 루틴에는 넣지 말고 힙힌지가 안정된 뒤 진행하세요."), 3, 10..15, null, 90),
        exercise("medicine_ball_slam", "메디신볼 슬램", MuscleGroup.FULL_BODY, EquipmentType.BODYWEIGHT, DifficultyLevel.INTERMEDIATE, "medicine_ball_slam", "전신 파워와 컨디셔닝을 짧게 넣는 운동입니다.", listOf("가슴 앞 준비: 발을 어깨너비로 두고 메디신볼을 가슴 앞에서 안정적으로 잡습니다.", "머리 위로 올리기: 갈비뼈가 과하게 들리지 않게 복부를 고정하고 공을 머리 위로 올립니다.", "바닥으로 슬램: 엉덩이와 무릎을 접으며 공을 발 앞 바닥으로 강하게 내립니다.", "낮은 자세 마무리: 공이 바닥에 닿은 뒤 등 중립을 유지하며 다음 반복을 준비합니다."), listOf("허리를 둥글게 말아 줍지 마세요.", "주변 공간을 반드시 확인하세요."), 3, 8..10, null, 60)
    )

    private val exercisesById: Map<String, Exercise> by lazy {
        exercises.associateBy { it.id.value }
    }

    val templates: List<PlanTemplate> by lazy {
        curatedTemplates + generatedCoverageTemplates
    }

    private val curatedTemplates: List<PlanTemplate> = listOf(
        PlanTemplate(
            id = "beginner-full-body-2day",
            name = "초보자 전신 2일 루틴",
            level = PlanLevel.INTRO,
            daysPerWeek = 2,
            description = "기구 사용이 낯선 사용자가 요일에 묶이지 않고 전신 A/B를 이어가는 보수적 루틴입니다.",
            days = listOf(
                day(
                    0,
                    "전신 A",
                    "기구 적응",
                    RoutineFocus.FULL_BODY,
                    "treadmill_walk",
                    "leg_press",
                    "machine_chest_press",
                    "lat_pulldown",
                    "plank",
                    secondaryFocuses = listOf(RoutineFocus.LOWER_BODY, RoutineFocus.CHEST, RoutineFocus.BACK)
                ),
                day(
                    1,
                    "전신 B",
                    "균형과 후면",
                    RoutineFocus.FULL_BODY,
                    "indoor_bike",
                    "goblet_squat",
                    "seated_cable_row",
                    "machine_shoulder_press",
                    "dead_bug",
                    secondaryFocuses = listOf(RoutineFocus.LOWER_BODY, RoutineFocus.BACK, RoutineFocus.SHOULDERS)
                )
            ),
            structure = RoutineStructure.FULL_BODY,
            recommendedExperience = TrainingExperience.BEGINNER,
            sessionMinutes = 30,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        ),
        PlanTemplate(
            id = "beginner-full-body-3day",
            name = "초보자 전신 3일 루틴",
            level = PlanLevel.BEGINNER,
            daysPerWeek = 3,
            description = "전신 주요 근육을 반복해서 익히면서 과한 피로를 피하는 기본 루틴입니다.",
            days = listOf(
                day(
                    0,
                    "전신 A",
                    "하체+밀기+당기기",
                    RoutineFocus.FULL_BODY,
                    "treadmill_walk",
                    "leg_press",
                    "machine_chest_press",
                    "lat_pulldown",
                    "leg_curl",
                    "dumbbell_lateral_raise",
                    "plank",
                    secondaryFocuses = listOf(
                        RoutineFocus.LOWER_BODY,
                        RoutineFocus.CHEST,
                        RoutineFocus.PUSH,
                        RoutineFocus.BACK,
                        RoutineFocus.PULL
                    )
                ),
                day(
                    1,
                    "전신 B",
                    "스쿼트 패턴과 등",
                    RoutineFocus.FULL_BODY,
                    "indoor_bike",
                    "goblet_squat",
                    "seated_cable_row",
                    "pushup",
                    "romanian_deadlift",
                    "triceps_pushdown",
                    "dead_bug",
                    secondaryFocuses = listOf(
                        RoutineFocus.LOWER_BODY,
                        RoutineFocus.BACK,
                        RoutineFocus.ARMS,
                        RoutineFocus.TRICEPS
                    )
                ),
                day(
                    2,
                    "전신 C",
                    "어깨와 엉덩이",
                    RoutineFocus.FULL_BODY,
                    "treadmill_walk",
                    "leg_extension",
                    "chest_supported_row",
                    "machine_shoulder_press",
                    "hip_thrust",
                    "dumbbell_curl",
                    "pallof_press",
                    secondaryFocuses = listOf(RoutineFocus.SHOULDERS, RoutineFocus.LOWER_BODY, RoutineFocus.CORE)
                )
            ),
            structure = RoutineStructure.FULL_BODY,
            recommendedExperience = TrainingExperience.BEGINNER,
            sessionMinutes = 45,
            focusSummary = listOf(RoutineFocus.FULL_BODY)
        ),
        PlanTemplate(
            id = "intermediate-balanced-4day",
            name = "균형 분할 4일 루틴",
            level = PlanLevel.INTERMEDIATE,
            daysPerWeek = 4,
            description = "상체/하체를 번갈아 나누어 빈도와 회복 균형을 유지하는 루틴입니다.",
            days = listOf(
                day(
                    0,
                    "상체 1",
                    "상체 균형",
                    RoutineFocus.UPPER_BODY,
                    "machine_chest_press",
                    "lat_pulldown",
                    "machine_shoulder_press",
                    "seated_cable_row",
                    "dumbbell_lateral_raise",
                    "triceps_pushdown",
                    secondaryFocuses = listOf(
                        RoutineFocus.BACK,
                        RoutineFocus.SHOULDERS,
                        RoutineFocus.ARMS,
                        RoutineFocus.TRICEPS
                    )
                ),
                day(
                    1,
                    "하체 1",
                    "프레스와 힙힌지",
                    RoutineFocus.LOWER_BODY,
                    "leg_press",
                    "romanian_deadlift",
                    "leg_extension",
                    "leg_curl",
                    "calf_raise",
                    "plank",
                    secondaryFocuses = listOf(RoutineFocus.CORE)
                ),
                day(
                    2,
                    "상체 2",
                    "상체 균형",
                    RoutineFocus.UPPER_BODY,
                    "incline_dumbbell_press",
                    "chest_supported_row",
                    "assisted_pullup",
                    "rear_delt_machine",
                    "cable_fly",
                    "cable_curl",
                    secondaryFocuses = listOf(
                        RoutineFocus.CHEST,
                        RoutineFocus.SHOULDERS,
                        RoutineFocus.ARMS,
                        RoutineFocus.BICEPS
                    )
                ),
                day(
                    3,
                    "하체 2",
                    "스쿼트와 둔근",
                    RoutineFocus.LOWER_BODY,
                    "goblet_squat",
                    "hip_thrust",
                    "dumbbell_split_squat",
                    "leg_curl",
                    "calf_raise",
                    "pallof_press",
                    secondaryFocuses = listOf(RoutineFocus.CORE)
                )
            ),
            structure = RoutineStructure.BALANCED_SPLIT,
            recommendedExperience = TrainingExperience.INTERMEDIATE,
            sessionMinutes = 45,
            focusSummary = listOf(
                RoutineFocus.UPPER_BODY,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.CHEST,
                RoutineFocus.BACK,
                RoutineFocus.SHOULDERS
            )
        ),
        PlanTemplate(
            id = "intermediate-body-part-4day-30",
            name = "부위 집중 4일 30분 루틴",
            level = PlanLevel.INTERMEDIATE,
            daysPerWeek = 4,
            description = "핵심 운동 4개로 등(당기기), 가슴(밀기), 하체, 어깨+팔 집중감을 짧게 이어가는 루틴입니다.",
            days = listOf(
                day(
                    0,
                    "등 집중",
                    "당기는 운동",
                    RoutineFocus.BACK,
                    "lat_pulldown",
                    "seated_cable_row",
                    "machine_row",
                    "straight_arm_pulldown",
                    secondaryFocuses = listOf(RoutineFocus.PULL),
                    minRecoveryHours = 36
                ),
                day(
                    1,
                    "가슴 집중",
                    "미는 운동",
                    RoutineFocus.CHEST,
                    "machine_chest_press",
                    "incline_machine_press",
                    "pec_deck_fly",
                    "cable_fly",
                    secondaryFocuses = listOf(RoutineFocus.PUSH),
                    minRecoveryHours = 36
                ),
                day(
                    2,
                    "하체 집중",
                    "스쿼트와 힙힌지",
                    RoutineFocus.LOWER_BODY,
                    "leg_press",
                    "hack_squat",
                    "leg_extension",
                    "leg_curl",
                    minRecoveryHours = 48
                ),
                day(
                    3,
                    "어깨+팔 집중",
                    "측면·후면 어깨와 이두·삼두",
                    RoutineFocus.SHOULDERS,
                    "machine_shoulder_press",
                    "dumbbell_lateral_raise",
                    "dumbbell_curl",
                    "triceps_pushdown",
                    secondaryFocuses = listOf(RoutineFocus.ARMS, RoutineFocus.BICEPS, RoutineFocus.TRICEPS),
                    minRecoveryHours = 36
                )
            ),
            structure = RoutineStructure.BODY_PART_SPLIT,
            recommendedExperience = TrainingExperience.INTERMEDIATE,
            sessionMinutes = 30,
            focusSummary = listOf(
                RoutineFocus.BACK,
                RoutineFocus.CHEST,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.SHOULDERS,
                RoutineFocus.ARMS,
                RoutineFocus.BICEPS,
                RoutineFocus.TRICEPS,
                RoutineFocus.PUSH,
                RoutineFocus.PULL
            )
        ),
        PlanTemplate(
            id = "intermediate-body-part-4day",
            name = "부위 집중 4일 루틴",
            level = PlanLevel.INTERMEDIATE,
            daysPerWeek = 4,
            description = "등(당기기), 가슴(밀기), 하체, 어깨+팔을 하루씩 집중하되 이두와 삼두를 명확히 노출하는 루틴입니다.",
            days = listOf(
                day(
                    0,
                    "등 집중",
                    "당기는 운동",
                    RoutineFocus.BACK,
                    "lat_pulldown",
                    "seated_cable_row",
                    "machine_row",
                    "chest_supported_row",
                    "straight_arm_pulldown",
                    secondaryFocuses = listOf(RoutineFocus.PULL),
                    minRecoveryHours = 36
                ),
                day(
                    1,
                    "가슴 집중",
                    "미는 운동",
                    RoutineFocus.CHEST,
                    "machine_chest_press",
                    "dumbbell_bench_press",
                    "incline_machine_press",
                    "pec_deck_fly",
                    "cable_fly",
                    secondaryFocuses = listOf(RoutineFocus.PUSH),
                    minRecoveryHours = 36
                ),
                day(
                    2,
                    "하체 집중",
                    "스쿼트와 힙힌지",
                    RoutineFocus.LOWER_BODY,
                    "leg_press",
                    "romanian_deadlift",
                    "hack_squat",
                    "leg_extension",
                    "leg_curl",
                    minRecoveryHours = 48
                ),
                day(
                    3,
                    "어깨+팔 집중",
                    "측면·후면 어깨와 이두·삼두",
                    RoutineFocus.SHOULDERS,
                    "machine_shoulder_press",
                    "dumbbell_lateral_raise",
                    "rear_delt_machine",
                    "dumbbell_curl",
                    "triceps_pushdown",
                    secondaryFocuses = listOf(RoutineFocus.ARMS, RoutineFocus.BICEPS, RoutineFocus.TRICEPS),
                    minRecoveryHours = 36
                )
            ),
            structure = RoutineStructure.BODY_PART_SPLIT,
            recommendedExperience = TrainingExperience.INTERMEDIATE,
            sessionMinutes = 45,
            focusSummary = listOf(
                RoutineFocus.BACK,
                RoutineFocus.CHEST,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.SHOULDERS,
                RoutineFocus.ARMS,
                RoutineFocus.BICEPS,
                RoutineFocus.TRICEPS,
                RoutineFocus.PUSH,
                RoutineFocus.PULL
            )
        ),
        PlanTemplate(
            id = "intermediate-body-part-4day-60",
            name = "부위 집중 4일 60분 루틴",
            level = PlanLevel.INTERMEDIATE,
            daysPerWeek = 4,
            description = "등(당기기)과 가슴(밀기)을 포함해 집중 부위를 5개 안팎의 핵심·보조 운동으로 채우는 60분 루틴입니다.",
            days = listOf(
                day(
                    0,
                    "등 집중",
                    "당기는 운동",
                    RoutineFocus.BACK,
                    "lat_pulldown",
                    "barbell_bent_over_row",
                    "seated_cable_row",
                    "machine_row",
                    "chest_supported_row",
                    "assisted_pullup",
                    "straight_arm_pulldown",
                    secondaryFocuses = listOf(RoutineFocus.PULL),
                    minRecoveryHours = 36
                ),
                day(
                    1,
                    "가슴 집중",
                    "미는 운동",
                    RoutineFocus.CHEST,
                    "barbell_bench_press",
                    "incline_machine_press",
                    "dumbbell_bench_press",
                    "pec_deck_fly",
                    "cable_fly",
                    "assisted_dip",
                    "pushup",
                    "triceps_pushdown",
                    secondaryFocuses = listOf(RoutineFocus.PUSH),
                    minRecoveryHours = 36
                ),
                day(
                    2,
                    "하체 집중",
                    "스쿼트와 힙힌지",
                    RoutineFocus.LOWER_BODY,
                    "barbell_back_squat",
                    "leg_press",
                    "romanian_deadlift",
                    "hack_squat",
                    "leg_extension",
                    "leg_curl",
                    "calf_raise",
                    minRecoveryHours = 48
                ),
                day(
                    3,
                    "어깨+팔 집중",
                    "측면·후면 어깨와 이두·삼두",
                    RoutineFocus.SHOULDERS,
                    "machine_shoulder_press",
                    "dumbbell_shoulder_press",
                    "dumbbell_lateral_raise",
                    "rear_delt_machine",
                    "face_pull",
                    "dumbbell_curl",
                    "hammer_curl",
                    "triceps_pushdown",
                    "rope_overhead_triceps",
                    secondaryFocuses = listOf(RoutineFocus.ARMS, RoutineFocus.BICEPS, RoutineFocus.TRICEPS),
                    minRecoveryHours = 36
                )
            ),
            structure = RoutineStructure.BODY_PART_SPLIT,
            recommendedExperience = TrainingExperience.INTERMEDIATE,
            sessionMinutes = 60,
            focusSummary = listOf(
                RoutineFocus.BACK,
                RoutineFocus.CHEST,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.SHOULDERS,
                RoutineFocus.ARMS,
                RoutineFocus.BICEPS,
                RoutineFocus.TRICEPS,
                RoutineFocus.PUSH,
                RoutineFocus.PULL
            )
        ),
        PlanTemplate(
            id = "intermediate-body-part-5day",
            name = "부위 집중 5일 루틴",
            level = PlanLevel.INTERMEDIATE,
            daysPerWeek = 5,
            description = "가슴(밀기), 등(당기기), 하체, 어깨, 팔+유산소를 분리하되 이두와 삼두를 명확히 나누는 루틴입니다.",
            days = listOf(
                day(
                    0,
                    "가슴 집중",
                    "프레스와 플라이",
                    RoutineFocus.CHEST,
                    "barbell_bench_press",
                    "incline_machine_press",
                    "dumbbell_bench_press",
                    "cable_fly",
                    "assisted_dip",
                    "pec_deck_fly",
                    "triceps_pushdown",
                    secondaryFocuses = listOf(RoutineFocus.PUSH),
                    minRecoveryHours = 36
                ),
                day(
                    1,
                    "등 집중",
                    "수직·수평 당기기",
                    RoutineFocus.BACK,
                    "lat_pulldown",
                    "barbell_bent_over_row",
                    "seated_cable_row",
                    "machine_row",
                    "chest_supported_row",
                    "assisted_pullup",
                    "straight_arm_pulldown",
                    secondaryFocuses = listOf(RoutineFocus.PULL),
                    minRecoveryHours = 36
                ),
                day(
                    2,
                    "하체 집중",
                    "스쿼트와 후면",
                    RoutineFocus.LOWER_BODY,
                    "barbell_back_squat",
                    "romanian_deadlift",
                    "leg_press",
                    "hack_squat",
                    "leg_extension",
                    "leg_curl",
                    "calf_raise",
                    minRecoveryHours = 48
                ),
                day(
                    3,
                    "어깨 집중",
                    "측면·후면 우선",
                    RoutineFocus.SHOULDERS,
                    "machine_shoulder_press",
                    "dumbbell_shoulder_press",
                    "arnold_press",
                    "dumbbell_lateral_raise",
                    "cable_lateral_raise",
                    "rear_delt_machine",
                    "face_pull",
                    minRecoveryHours = 36
                ),
                day(
                    4,
                    "팔+유산소",
                    "이두·삼두 보조와 컨디셔닝",
                    RoutineFocus.ARMS,
                    "dumbbell_curl",
                    "preacher_curl_machine",
                    "hammer_curl",
                    "cable_curl",
                    "reverse_curl",
                    "triceps_pushdown",
                    "rope_overhead_triceps",
                    "rowing_machine",
                    "stair_climber",
                    secondaryFocuses = listOf(
                        RoutineFocus.BICEPS,
                        RoutineFocus.TRICEPS,
                        RoutineFocus.CARDIO_CONDITIONING
                    ),
                    minRecoveryHours = 24
                )
            ),
            structure = RoutineStructure.BODY_PART_SPLIT,
            recommendedExperience = TrainingExperience.INTERMEDIATE,
            sessionMinutes = 60,
            focusSummary = listOf(
                RoutineFocus.CHEST,
                RoutineFocus.BACK,
                RoutineFocus.LOWER_BODY,
                RoutineFocus.SHOULDERS,
                RoutineFocus.ARMS,
                RoutineFocus.BICEPS,
                RoutineFocus.TRICEPS,
                RoutineFocus.PUSH,
                RoutineFocus.PULL,
                RoutineFocus.CARDIO_CONDITIONING
            )
        )
    )

    private val generatedCoverageTemplates: List<PlanTemplate> by lazy {
        TrainingExperience.entries.flatMap { experience ->
            SUPPORTED_DAYS_PER_WEEK.flatMap { daysPerWeek ->
                allowedSessionMinutes(
                    experience = experience,
                    daysPerWeek = daysPerWeek
                ).flatMap { sessionMinutes ->
                    listOf(
                        generatedTemplate(
                            experience = experience,
                            structure = RoutineStructure.FULL_BODY,
                            daysPerWeek = daysPerWeek,
                            sessionMinutes = sessionMinutes
                        ),
                        generatedTemplate(
                            experience = experience,
                            structure = RoutineStructure.BODY_PART_SPLIT,
                            daysPerWeek = daysPerWeek,
                            sessionMinutes = sessionMinutes
                        )
                    )
                }
            }
        }
    }

    private fun allowedSessionMinutes(
        experience: TrainingExperience,
        daysPerWeek: Int
    ): List<Int> = when (experience) {
        TrainingExperience.BEGINNER -> when (daysPerWeek) {
            2, 3 -> listOf(30, 45, 60)
            else -> listOf(30, 45)
        }
        TrainingExperience.INTERMEDIATE -> when (daysPerWeek) {
            2 -> listOf(45, 60)
            3, 4 -> listOf(30, 45, 60)
            else -> listOf(30, 45, 60)
        }
        TrainingExperience.ADVANCED -> when (daysPerWeek) {
            2 -> listOf(45, 60)
            else -> listOf(45, 60)
        }
    }

    private fun generatedTemplate(
        experience: TrainingExperience,
        structure: RoutineStructure,
        daysPerWeek: Int,
        sessionMinutes: Int
    ): PlanTemplate {
        val structureLabel = when (structure) {
            RoutineStructure.FULL_BODY -> "전신"
            RoutineStructure.BALANCED_SPLIT -> "균형"
            RoutineStructure.BODY_PART_SPLIT -> "부위 집중"
        }
        val id = listOf(
            experience.slug,
            structure.slug,
            "${daysPerWeek}day",
            "${sessionMinutes}m"
        ).joinToString("-")
        return PlanTemplate(
            id = id,
            name = "${experience.label} $structureLabel ${daysPerWeek}일 ${sessionMinutes}분 루틴",
            level = experience.planLevel,
            daysPerWeek = daysPerWeek,
            description = "${experience.label} 사용자가 주 ${daysPerWeek}회, 회당 ${sessionMinutes}분 안에서 수행하도록 구성한 $structureLabel 루틴입니다.",
            days = generatedDays(
                experience = experience,
                structure = structure,
                daysPerWeek = daysPerWeek,
                sessionMinutes = sessionMinutes
            ),
            structure = structure,
            recommendedExperience = experience,
            cycleLength = daysPerWeek,
            sessionMinutes = sessionMinutes,
            focusSummary = if (structure == RoutineStructure.FULL_BODY) {
                listOf(RoutineFocus.FULL_BODY)
            } else {
                BODY_PART_FOCUS_SUMMARY
            }
        )
    }

    private fun generatedDays(
        experience: TrainingExperience,
        structure: RoutineStructure,
        daysPerWeek: Int,
        sessionMinutes: Int
    ): List<PlanTemplateDay> {
        val seeds = when (structure) {
            RoutineStructure.FULL_BODY -> fullBodyDaySeeds(experience)
            RoutineStructure.BALANCED_SPLIT -> fullBodyDaySeeds(experience)
            RoutineStructure.BODY_PART_SPLIT -> bodyPartDaySeeds(experience, daysPerWeek)
        }
        return seeds.take(daysPerWeek).mapIndexed { index, seed ->
            timedDay(
                dayOffset = index,
                seed = seed,
                sessionMinutes = sessionMinutes
            )
        }
    }

    private fun timedDay(
        dayOffset: Int,
        seed: RoutineDaySeed,
        sessionMinutes: Int
    ): PlanTemplateDay = day(
        dayOffset = dayOffset,
        title = seed.title,
        focus = seed.focus,
        primaryFocus = seed.primaryFocus,
        exerciseIds = selectExerciseIdsForTarget(
            exerciseIds = seed.exerciseIds,
            sessionMinutes = sessionMinutes
        ).toTypedArray(),
        secondaryFocuses = seed.secondaryFocuses,
        minRecoveryHours = seed.minRecoveryHours
    )

    private fun selectExerciseIdsForTarget(
        exerciseIds: List<String>,
        sessionMinutes: Int
    ): List<String> {
        val selected = mutableListOf<String>()
        val targetSeconds = sessionMinutes * SECONDS_PER_MINUTE
        var totalSeconds = 0

        for (exerciseId in exerciseIds.distinct()) {
            val exerciseSeconds = exerciseEstimateSeconds(exerciseId)
            val candidateTotalSeconds = totalSeconds + exerciseSeconds
            if (candidateTotalSeconds <= targetSeconds) {
                selected += exerciseId
                totalSeconds = candidateTotalSeconds
                continue
            }

            val underTargetGap = targetSeconds - totalSeconds
            val overTargetGap = candidateTotalSeconds - targetSeconds
            if (
                selected.isEmpty() ||
                (overTargetGap < underTargetGap && overTargetGap <= SESSION_TARGET_TOLERANCE_SECONDS)
            ) {
                selected += exerciseId
                totalSeconds = candidateTotalSeconds
                break
            }
        }

        return selected.ifEmpty { exerciseIds.take(1) }
    }

    private fun exerciseEstimateSeconds(exerciseId: String): Int {
        val exercise = exerciseById(exerciseId)
        return estimateExerciseSeconds(
            sets = exercise.defaultSets,
            repRange = exercise.defaultRepRange,
            durationMinutes = exercise.defaultDurationMinutes,
            restSeconds = exercise.restSeconds,
            repDurationSeconds = exercise.defaultRepDurationSeconds
        )
    }

    private fun fullBodyDaySeeds(experience: TrainingExperience): List<RoutineDaySeed> = when (experience) {
        TrainingExperience.BEGINNER -> listOf(
            RoutineDaySeed(
                title = "전신 A",
                focus = "하체+밀기+당기기",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "treadmill_walk",
                    "goblet_squat",
                    "machine_chest_press",
                    "lat_pulldown",
                    "leg_curl",
                    "dumbbell_lateral_raise",
                    "plank",
                    "farmer_carry",
                    "triceps_pushdown"
                )
            ),
            RoutineDaySeed(
                title = "전신 B",
                focus = "후면과 자세",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "indoor_bike",
                    "leg_press",
                    "seated_cable_row",
                    "pushup",
                    "glute_bridge",
                    "machine_shoulder_press",
                    "dead_bug",
                    "dumbbell_curl",
                    "calf_raise"
                )
            ),
            RoutineDaySeed(
                title = "전신 C",
                focus = "스쿼트와 등",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "treadmill_walk",
                    "box_squat",
                    "chest_supported_row",
                    "dumbbell_floor_press",
                    "hip_abduction_machine",
                    "face_pull",
                    "pallof_press",
                    "kettlebell_farmer_carry"
                )
            ),
            RoutineDaySeed(
                title = "전신 D",
                focus = "기초 분할 적응",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "indoor_bike",
                    "kettlebell_deadlift",
                    "incline_machine_press",
                    "machine_row",
                    "leg_extension",
                    "pec_deck_fly",
                    "bird_dog",
                    "cable_curl"
                )
            ),
            RoutineDaySeed(
                title = "전신 E",
                focus = "가벼운 보강",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "treadmill_walk",
                    "bodyweight_squat",
                    "kettlebell_floor_press",
                    "straight_arm_pulldown",
                    "hip_adduction_machine",
                    "rear_delt_machine",
                    "side_plank",
                    "hammer_curl"
                )
            )
        )
        TrainingExperience.INTERMEDIATE -> listOf(
            RoutineDaySeed(
                title = "전신 A",
                focus = "복합 패턴",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "rowing_machine",
                    "leg_press",
                    "dumbbell_bench_press",
                    "lat_pulldown",
                    "romanian_deadlift",
                    "machine_shoulder_press",
                    "pallof_press",
                    "triceps_pushdown",
                    "cable_curl"
                )
            ),
            RoutineDaySeed(
                title = "전신 B",
                focus = "상하체 균형",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "treadmill_walk",
                    "hack_squat",
                    "seated_cable_row",
                    "incline_dumbbell_press",
                    "hip_thrust",
                    "rear_delt_machine",
                    "cable_crunch",
                    "farmer_carry"
                )
            ),
            RoutineDaySeed(
                title = "전신 C",
                focus = "당기기와 힙힌지",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "indoor_bike",
                    "goblet_squat",
                    "assisted_pullup",
                    "cable_chest_press",
                    "barbell_romanian_deadlift",
                    "dumbbell_lateral_raise",
                    "hanging_knee_raise",
                    "rope_overhead_triceps"
                )
            ),
            RoutineDaySeed(
                title = "전신 D",
                focus = "상체 보강",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "rowing_machine",
                    "smith_machine_squat",
                    "chest_supported_row",
                    "dip",
                    "leg_curl",
                    "face_pull",
                    "side_plank",
                    "reverse_curl"
                )
            ),
            RoutineDaySeed(
                title = "전신 E",
                focus = "컨디셔닝",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "stair_climber",
                    "kettlebell_goblet_squat",
                    "barbell_bent_over_row",
                    "pushup",
                    "calf_raise",
                    "landmine_press",
                    "cable_woodchop",
                    "medicine_ball_slam"
                )
            )
        )
        TrainingExperience.ADVANCED -> listOf(
            RoutineDaySeed(
                title = "전신 A",
                focus = "힘 우선",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "barbell_back_squat",
                    "machine_chest_press",
                    "lat_pulldown",
                    "plank",
                    "barbell_bench_press",
                    "barbell_bent_over_row",
                    "barbell_romanian_deadlift",
                    "barbell_overhead_press",
                    "hanging_knee_raise"
                )
            ),
            RoutineDaySeed(
                title = "전신 B",
                focus = "후면 사슬",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "conventional_deadlift",
                    "incline_machine_press",
                    "seated_cable_row",
                    "side_plank",
                    "hack_squat",
                    "assisted_pullup",
                    "dumbbell_shoulder_press",
                    "farmer_carry"
                )
            ),
            RoutineDaySeed(
                title = "전신 C",
                focus = "볼륨 균형",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "rowing_machine",
                    "smith_machine_squat",
                    "dumbbell_bench_press",
                    "t_bar_row",
                    "hip_thrust",
                    "landmine_press",
                    "cable_crunch",
                    "sled_push"
                )
            ),
            RoutineDaySeed(
                title = "전신 D",
                focus = "단측 안정",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "bulgarian_split_squat",
                    "kettlebell_floor_press",
                    "one_arm_dumbbell_row",
                    "pallof_press",
                    "barbell_romanian_deadlift",
                    "face_pull",
                    "rope_overhead_triceps",
                    "hammer_curl"
                )
            ),
            RoutineDaySeed(
                title = "전신 E",
                focus = "파워와 회복",
                primaryFocus = RoutineFocus.FULL_BODY,
                exerciseIds = listOf(
                    "two_hand_kettlebell_swing",
                    "goblet_squat",
                    "pullup",
                    "cable_chest_press",
                    "rear_delt_machine",
                    "mountain_climber",
                    "battle_rope",
                    "reverse_curl"
                )
            )
        )
    }

    private fun bodyPartDaySeeds(
        experience: TrainingExperience,
        daysPerWeek: Int
    ): List<RoutineDaySeed> {
        val library = bodyPartLibrary(experience)
        return when (daysPerWeek) {
            2 -> listOf(library.upper, library.lower)
            3 -> listOf(library.push, library.pull, library.lower)
            4 -> listOf(library.back, library.chest, library.lower, library.shouldersArms)
            else -> listOf(library.chest, library.back, library.lower, library.shoulders, library.arms)
        }
    }

    private fun bodyPartLibrary(experience: TrainingExperience): BodyPartDayLibrary = when (experience) {
        TrainingExperience.BEGINNER -> BodyPartDayLibrary(
            upper = RoutineDaySeed(
                title = "상체 집중",
                focus = "밀기와 당기기",
                primaryFocus = RoutineFocus.UPPER_BODY,
                exerciseIds = listOf(
                    "machine_chest_press",
                    "lat_pulldown",
                    "machine_shoulder_press",
                    "seated_cable_row",
                    "pushup",
                    "face_pull",
                    "dumbbell_curl",
                    "triceps_pushdown"
                ),
                minRecoveryHours = 36
            ),
            push = RoutineDaySeed(
                title = "가슴+삼두 집중",
                focus = "쉬운 밀기",
                primaryFocus = RoutineFocus.CHEST,
                exerciseIds = listOf(
                    "machine_chest_press",
                    "incline_machine_press",
                    "triceps_pushdown",
                    "pec_deck_fly",
                    "pushup",
                    "dumbbell_floor_press",
                    "close_grip_pushup",
                    "overhead_triceps_extension"
                ),
                secondaryFocuses = listOf(RoutineFocus.TRICEPS, RoutineFocus.PUSH),
                minRecoveryHours = 36
            ),
            pull = RoutineDaySeed(
                title = "등+이두 집중",
                focus = "쉬운 당기기",
                primaryFocus = RoutineFocus.BACK,
                exerciseIds = listOf(
                    "lat_pulldown",
                    "seated_cable_row",
                    "dumbbell_curl",
                    "machine_row",
                    "chest_supported_row",
                    "straight_arm_pulldown",
                    "hammer_curl",
                    "preacher_curl_machine"
                ),
                secondaryFocuses = listOf(RoutineFocus.BICEPS, RoutineFocus.PULL),
                minRecoveryHours = 36
            ),
            chest = RoutineDaySeed(
                title = "가슴 집중",
                focus = "프레스와 플라이",
                primaryFocus = RoutineFocus.CHEST,
                exerciseIds = listOf(
                    "machine_chest_press",
                    "incline_machine_press",
                    "pec_deck_fly",
                    "cable_fly",
                    "pushup",
                    "dumbbell_floor_press",
                    "cable_chest_press"
                ),
                secondaryFocuses = listOf(RoutineFocus.PUSH),
                minRecoveryHours = 36
            ),
            back = RoutineDaySeed(
                title = "등 집중",
                focus = "수직·수평 당기기",
                primaryFocus = RoutineFocus.BACK,
                exerciseIds = listOf(
                    "lat_pulldown",
                    "seated_cable_row",
                    "machine_row",
                    "chest_supported_row",
                    "straight_arm_pulldown",
                    "dumbbell_shrug",
                    "cable_pullover"
                ),
                secondaryFocuses = listOf(RoutineFocus.PULL),
                minRecoveryHours = 36
            ),
            lower = RoutineDaySeed(
                title = "하체+코어 집중",
                focus = "프레스와 안정성",
                primaryFocus = RoutineFocus.LOWER_BODY,
                exerciseIds = listOf(
                    "leg_press",
                    "goblet_squat",
                    "plank",
                    "leg_curl",
                    "leg_extension",
                    "glute_bridge",
                    "calf_raise",
                    "dead_bug",
                    "pallof_press"
                ),
                secondaryFocuses = listOf(RoutineFocus.CORE),
                minRecoveryHours = 48
            ),
            shoulders = RoutineDaySeed(
                title = "어깨 집중",
                focus = "프레스와 측후면",
                primaryFocus = RoutineFocus.SHOULDERS,
                exerciseIds = listOf(
                    "machine_shoulder_press",
                    "dumbbell_lateral_raise",
                    "rear_delt_machine",
                    "face_pull",
                    "front_raise",
                    "prone_y_raise",
                    "kettlebell_halo"
                ),
                minRecoveryHours = 36
            ),
            shouldersArms = RoutineDaySeed(
                title = "어깨+팔 집중",
                focus = "측후면과 팔",
                primaryFocus = RoutineFocus.SHOULDERS,
                exerciseIds = listOf(
                    "machine_shoulder_press",
                    "dumbbell_lateral_raise",
                    "dumbbell_curl",
                    "triceps_pushdown",
                    "front_raise",
                    "rear_delt_machine",
                    "face_pull",
                    "hammer_curl",
                    "overhead_triceps_extension"
                ),
                secondaryFocuses = listOf(RoutineFocus.BICEPS, RoutineFocus.TRICEPS),
                minRecoveryHours = 36
            ),
            arms = RoutineDaySeed(
                title = "팔+유산소 집중",
                focus = "팔 보조와 심폐",
                primaryFocus = RoutineFocus.ARMS,
                exerciseIds = listOf(
                    "dumbbell_curl",
                    "triceps_pushdown",
                    "rowing_machine",
                    "hammer_curl",
                    "preacher_curl_machine",
                    "overhead_triceps_extension",
                    "close_grip_pushup"
                ),
                secondaryFocuses = listOf(
                    RoutineFocus.BICEPS,
                    RoutineFocus.TRICEPS,
                    RoutineFocus.CARDIO_CONDITIONING
                ),
                minRecoveryHours = 24
            )
        )
        TrainingExperience.INTERMEDIATE -> BodyPartDayLibrary(
            upper = RoutineDaySeed(
                title = "상체 집중",
                focus = "프레스와 로우",
                primaryFocus = RoutineFocus.UPPER_BODY,
                exerciseIds = listOf(
                    "dumbbell_bench_press",
                    "lat_pulldown",
                    "machine_shoulder_press",
                    "seated_cable_row",
                    "incline_dumbbell_press",
                    "assisted_pullup",
                    "rear_delt_machine",
                    "triceps_pushdown",
                    "cable_curl"
                ),
                minRecoveryHours = 36
            ),
            push = RoutineDaySeed(
                title = "가슴+삼두 집중",
                focus = "프레스 중심",
                primaryFocus = RoutineFocus.CHEST,
                exerciseIds = listOf(
                    "machine_chest_press",
                    "dumbbell_bench_press",
                    "triceps_pushdown",
                    "incline_machine_press",
                    "incline_dumbbell_press",
                    "pec_deck_fly",
                    "cable_fly",
                    "rope_overhead_triceps"
                ),
                secondaryFocuses = listOf(RoutineFocus.TRICEPS, RoutineFocus.PUSH),
                minRecoveryHours = 36
            ),
            pull = RoutineDaySeed(
                title = "등+이두 집중",
                focus = "등 두께와 광배",
                primaryFocus = RoutineFocus.BACK,
                exerciseIds = listOf(
                    "lat_pulldown",
                    "seated_cable_row",
                    "cable_curl",
                    "machine_row",
                    "chest_supported_row",
                    "assisted_pullup",
                    "straight_arm_pulldown",
                    "hammer_curl"
                ),
                secondaryFocuses = listOf(RoutineFocus.BICEPS, RoutineFocus.PULL),
                minRecoveryHours = 36
            ),
            chest = RoutineDaySeed(
                title = "가슴 집중",
                focus = "프레스와 플라이",
                primaryFocus = RoutineFocus.CHEST,
                exerciseIds = listOf(
                    "machine_chest_press",
                    "dumbbell_bench_press",
                    "incline_machine_press",
                    "incline_dumbbell_press",
                    "pec_deck_fly",
                    "cable_fly",
                    "assisted_dip",
                    "triceps_pushdown"
                ),
                secondaryFocuses = listOf(RoutineFocus.PUSH),
                minRecoveryHours = 36
            ),
            back = RoutineDaySeed(
                title = "등 집중",
                focus = "수직·수평 당기기",
                primaryFocus = RoutineFocus.BACK,
                exerciseIds = listOf(
                    "lat_pulldown",
                    "barbell_bent_over_row",
                    "seated_cable_row",
                    "machine_row",
                    "chest_supported_row",
                    "assisted_pullup",
                    "straight_arm_pulldown",
                    "cable_curl"
                ),
                secondaryFocuses = listOf(RoutineFocus.PULL),
                minRecoveryHours = 36
            ),
            lower = RoutineDaySeed(
                title = "하체+코어 집중",
                focus = "스쿼트와 힙힌지",
                primaryFocus = RoutineFocus.LOWER_BODY,
                exerciseIds = listOf(
                    "leg_press",
                    "romanian_deadlift",
                    "hack_squat",
                    "pallof_press",
                    "leg_extension",
                    "leg_curl",
                    "calf_raise",
                    "hip_thrust",
                    "cable_crunch"
                ),
                secondaryFocuses = listOf(RoutineFocus.CORE),
                minRecoveryHours = 48
            ),
            shoulders = RoutineDaySeed(
                title = "어깨 집중",
                focus = "프레스와 측후면",
                primaryFocus = RoutineFocus.SHOULDERS,
                exerciseIds = listOf(
                    "machine_shoulder_press",
                    "dumbbell_shoulder_press",
                    "dumbbell_lateral_raise",
                    "cable_lateral_raise",
                    "rear_delt_machine",
                    "face_pull",
                    "arnold_press",
                    "half_kneeling_kettlebell_press"
                ),
                minRecoveryHours = 36
            ),
            shouldersArms = RoutineDaySeed(
                title = "어깨+팔 집중",
                focus = "어깨와 팔 보조",
                primaryFocus = RoutineFocus.SHOULDERS,
                exerciseIds = listOf(
                    "machine_shoulder_press",
                    "dumbbell_lateral_raise",
                    "dumbbell_curl",
                    "triceps_pushdown",
                    "dumbbell_shoulder_press",
                    "rear_delt_machine",
                    "face_pull",
                    "hammer_curl",
                    "rope_overhead_triceps"
                ),
                secondaryFocuses = listOf(RoutineFocus.BICEPS, RoutineFocus.TRICEPS),
                minRecoveryHours = 36
            ),
            arms = RoutineDaySeed(
                title = "팔+유산소 집중",
                focus = "이두·삼두와 심폐",
                primaryFocus = RoutineFocus.ARMS,
                exerciseIds = listOf(
                    "dumbbell_curl",
                    "triceps_pushdown",
                    "rowing_machine",
                    "preacher_curl_machine",
                    "hammer_curl",
                    "cable_curl",
                    "rope_overhead_triceps",
                    "close_grip_pushup",
                    "stair_climber"
                ),
                secondaryFocuses = listOf(
                    RoutineFocus.BICEPS,
                    RoutineFocus.TRICEPS,
                    RoutineFocus.CARDIO_CONDITIONING
                ),
                minRecoveryHours = 24
            )
        )
        TrainingExperience.ADVANCED -> BodyPartDayLibrary(
            upper = RoutineDaySeed(
                title = "상체 집중",
                focus = "고효율 상체",
                primaryFocus = RoutineFocus.UPPER_BODY,
                exerciseIds = listOf(
                    "barbell_bench_press",
                    "barbell_bent_over_row",
                    "barbell_overhead_press",
                    "assisted_pullup",
                    "incline_dumbbell_press",
                    "t_bar_row",
                    "rear_delt_machine",
                    "triceps_pushdown",
                    "cable_curl"
                ),
                minRecoveryHours = 36
            ),
            push = RoutineDaySeed(
                title = "가슴+삼두 집중",
                focus = "고볼륨 밀기",
                primaryFocus = RoutineFocus.CHEST,
                exerciseIds = listOf(
                    "barbell_bench_press",
                    "incline_dumbbell_press",
                    "triceps_pushdown",
                    "dumbbell_bench_press",
                    "assisted_dip",
                    "cable_fly",
                    "pec_deck_fly",
                    "rope_overhead_triceps",
                    "close_grip_pushup"
                ),
                secondaryFocuses = listOf(RoutineFocus.TRICEPS, RoutineFocus.PUSH),
                minRecoveryHours = 36
            ),
            pull = RoutineDaySeed(
                title = "등+이두 집중",
                focus = "고볼륨 당기기",
                primaryFocus = RoutineFocus.BACK,
                exerciseIds = listOf(
                    "barbell_bent_over_row",
                    "lat_pulldown",
                    "cable_curl",
                    "t_bar_row",
                    "assisted_pullup",
                    "seated_cable_row",
                    "straight_arm_pulldown",
                    "hammer_curl",
                    "reverse_curl"
                ),
                secondaryFocuses = listOf(RoutineFocus.BICEPS, RoutineFocus.PULL),
                minRecoveryHours = 36
            ),
            chest = RoutineDaySeed(
                title = "가슴+삼두 집중",
                focus = "프레스 강도",
                primaryFocus = RoutineFocus.CHEST,
                exerciseIds = listOf(
                    "barbell_bench_press",
                    "incline_dumbbell_press",
                    "triceps_pushdown",
                    "dumbbell_bench_press",
                    "assisted_dip",
                    "cable_fly",
                    "pec_deck_fly",
                    "pushup",
                    "rope_overhead_triceps"
                ),
                secondaryFocuses = listOf(RoutineFocus.TRICEPS, RoutineFocus.PUSH),
                minRecoveryHours = 36
            ),
            back = RoutineDaySeed(
                title = "등+이두 집중",
                focus = "수직·수평 고볼륨",
                primaryFocus = RoutineFocus.BACK,
                exerciseIds = listOf(
                    "barbell_bent_over_row",
                    "lat_pulldown",
                    "cable_curl",
                    "t_bar_row",
                    "assisted_pullup",
                    "seated_cable_row",
                    "machine_row",
                    "straight_arm_pulldown",
                    "reverse_curl"
                ),
                secondaryFocuses = listOf(RoutineFocus.BICEPS, RoutineFocus.PULL),
                minRecoveryHours = 36
            ),
            lower = RoutineDaySeed(
                title = "하체+코어 집중",
                focus = "스쿼트와 후면",
                primaryFocus = RoutineFocus.LOWER_BODY,
                exerciseIds = listOf(
                    "barbell_back_squat",
                    "barbell_romanian_deadlift",
                    "leg_press",
                    "hanging_knee_raise",
                    "hack_squat",
                    "hip_thrust",
                    "leg_extension",
                    "leg_curl",
                    "calf_raise"
                ),
                secondaryFocuses = listOf(RoutineFocus.CORE),
                minRecoveryHours = 48
            ),
            shoulders = RoutineDaySeed(
                title = "어깨 집중",
                focus = "프레스와 측후면",
                primaryFocus = RoutineFocus.SHOULDERS,
                exerciseIds = listOf(
                    "barbell_overhead_press",
                    "dumbbell_shoulder_press",
                    "arnold_press",
                    "dumbbell_lateral_raise",
                    "cable_lateral_raise",
                    "rear_delt_machine",
                    "face_pull",
                    "half_kneeling_kettlebell_press",
                    "landmine_press"
                ),
                minRecoveryHours = 36
            ),
            shouldersArms = RoutineDaySeed(
                title = "어깨+팔 집중",
                focus = "어깨와 팔 고볼륨",
                primaryFocus = RoutineFocus.SHOULDERS,
                exerciseIds = listOf(
                    "barbell_overhead_press",
                    "dumbbell_shoulder_press",
                    "dumbbell_lateral_raise",
                    "dumbbell_curl",
                    "triceps_pushdown",
                    "rear_delt_machine",
                    "face_pull",
                    "cable_curl",
                    "rope_overhead_triceps"
                ),
                secondaryFocuses = listOf(RoutineFocus.BICEPS, RoutineFocus.TRICEPS),
                minRecoveryHours = 36
            ),
            arms = RoutineDaySeed(
                title = "팔+유산소 집중",
                focus = "팔 볼륨과 컨디셔닝",
                primaryFocus = RoutineFocus.ARMS,
                exerciseIds = listOf(
                    "cable_curl",
                    "triceps_pushdown",
                    "rowing_machine",
                    "preacher_curl_machine",
                    "hammer_curl",
                    "reverse_curl",
                    "rope_overhead_triceps",
                    "close_grip_pushup",
                    "stair_climber"
                ),
                secondaryFocuses = listOf(
                    RoutineFocus.BICEPS,
                    RoutineFocus.TRICEPS,
                    RoutineFocus.CARDIO_CONDITIONING
                ),
                minRecoveryHours = 24
            )
        )
    }

    private data class RoutineDaySeed(
        val title: String,
        val focus: String,
        val primaryFocus: RoutineFocus,
        val exerciseIds: List<String>,
        val secondaryFocuses: List<RoutineFocus> = emptyList(),
        val minRecoveryHours: Int = 24
    )

    private data class BodyPartDayLibrary(
        val upper: RoutineDaySeed,
        val push: RoutineDaySeed,
        val pull: RoutineDaySeed,
        val chest: RoutineDaySeed,
        val back: RoutineDaySeed,
        val lower: RoutineDaySeed,
        val shoulders: RoutineDaySeed,
        val shouldersArms: RoutineDaySeed,
        val arms: RoutineDaySeed
    )

    private val TrainingExperience.slug: String
        get() = when (this) {
            TrainingExperience.BEGINNER -> "beginner"
            TrainingExperience.INTERMEDIATE -> "intermediate"
            TrainingExperience.ADVANCED -> "advanced"
        }

    private val TrainingExperience.label: String
        get() = when (this) {
            TrainingExperience.BEGINNER -> "초보"
            TrainingExperience.INTERMEDIATE -> "중급"
            TrainingExperience.ADVANCED -> "고급"
        }

    private val TrainingExperience.planLevel: PlanLevel
        get() = when (this) {
            TrainingExperience.BEGINNER -> PlanLevel.BEGINNER
            TrainingExperience.INTERMEDIATE -> PlanLevel.INTERMEDIATE
            TrainingExperience.ADVANCED -> PlanLevel.ADVANCED
        }

    private val RoutineStructure.slug: String
        get() = when (this) {
            RoutineStructure.FULL_BODY -> "full-body"
            RoutineStructure.BALANCED_SPLIT -> "balanced"
            RoutineStructure.BODY_PART_SPLIT -> "body-part"
        }

    private val BODY_PART_FOCUS_SUMMARY = listOf(
        RoutineFocus.BACK,
        RoutineFocus.CHEST,
        RoutineFocus.LOWER_BODY,
        RoutineFocus.SHOULDERS,
        RoutineFocus.ARMS,
        RoutineFocus.BICEPS,
        RoutineFocus.TRICEPS,
        RoutineFocus.PUSH,
        RoutineFocus.PULL
    )

    private val SUPPORTED_DAYS_PER_WEEK = 2..5
    private const val SECONDS_PER_MINUTE = 60
    private const val SESSION_TARGET_TOLERANCE_SECONDS = 10 * SECONDS_PER_MINUTE

    private fun exercise(
        id: String,
        name: String,
        muscleGroup: MuscleGroup,
        equipment: EquipmentType,
        difficulty: DifficultyLevel,
        imageKey: String,
        summary: String,
        instructions: List<String>,
        safetyCues: List<String>,
        defaultSets: Int,
        defaultRepRange: IntRange?,
        defaultDurationMinutes: Int?,
        restSeconds: Int
    ): Exercise {
        val normalizedSets = when {
            defaultRepRange != null -> DEFAULT_REP_SETS
            muscleGroup == MuscleGroup.CARDIO -> defaultSets
            else -> defaultSets.coerceAtLeast(DEFAULT_TIME_BASED_SETS)
        }
        return Exercise(
            id = ExerciseId(id),
            name = name,
            muscleGroup = muscleGroup,
            muscleGroups = (listOf(muscleGroup) + secondaryMuscleGroupsFor(id, muscleGroup)).distinct(),
            equipment = equipment,
            difficulty = difficulty,
            imageKey = imageKey,
            summary = summary,
            instructions = instructions,
            safetyCues = safetyCues,
            defaultSets = normalizedSets,
            defaultRepRange = defaultRepRange?.let { DEFAULT_REP_RANGE },
            defaultDurationMinutes = defaultDurationMinutes,
            restSeconds = restSeconds,
            defaultRepDurationSeconds = defaultRepDurationSeconds(
                id = id,
                muscleGroup = muscleGroup,
                equipment = equipment,
                difficulty = difficulty
            )
        )
    }

    private fun secondaryMuscleGroupsFor(id: String, primary: MuscleGroup): List<MuscleGroup> = when (id) {
        "bodyweight_squat",
        "goblet_squat",
        "box_squat",
        "kettlebell_goblet_squat",
        "kettlebell_box_squat" -> SQUAT_PATTERN_SECONDARY_GROUPS

        "dumbbell_split_squat",
        "bulgarian_split_squat",
        "walking_lunge",
        "kettlebell_reverse_lunge",
        "kettlebell_split_squat",
        "kettlebell_step_up" -> UNILATERAL_LOWER_SECONDARY_GROUPS

        "romanian_deadlift",
        "barbell_romanian_deadlift",
        "kettlebell_romanian_deadlift",
        "kettlebell_sumo_deadlift" -> HINGE_SECONDARY_GROUPS

        "dumbbell_deadlift",
        "kettlebell_deadlift" -> listOf(MuscleGroup.LOWER_BODY, MuscleGroup.BACK, MuscleGroup.CORE)

        "lat_pulldown",
        "assisted_pullup",
        "pullup" -> PULL_SECONDARY_GROUPS + MuscleGroup.CORE

        "seated_cable_row",
        "chest_supported_row",
        "one_arm_dumbbell_row",
        "kettlebell_bent_over_row",
        "one_arm_kettlebell_row" -> ROW_SECONDARY_GROUPS

        "face_pull",
        "incline_prone_y_raise" -> listOf(MuscleGroup.BACK)

        "machine_chest_press",
        "dumbbell_bench_press",
        "incline_dumbbell_press",
        "dumbbell_floor_press",
        "kettlebell_floor_press" -> PRESS_SECONDARY_GROUPS

        "pushup",
        "close_grip_pushup",
        "dip",
        "assisted_dip" -> PRESS_SECONDARY_GROUPS + MuscleGroup.CORE

        "machine_shoulder_press",
        "kettlebell_shoulder_press",
        "half_kneeling_kettlebell_press",
        "landmine_press" -> OVERHEAD_PRESS_SECONDARY_GROUPS

        "pallof_press",
        "mountain_climber",
        "plank",
        "side_plank" -> listOf(MuscleGroup.SHOULDERS)

        "farmer_carry",
        "kettlebell_farmer_carry" -> listOf(
            MuscleGroup.LOWER_BODY,
            MuscleGroup.BACK,
            MuscleGroup.CORE,
            MuscleGroup.FOREARMS
        )

        "kettlebell_suitcase_carry",
        "kettlebell_rack_carry" -> listOf(MuscleGroup.LOWER_BODY, MuscleGroup.BACK, MuscleGroup.FOREARMS)

        "two_hand_kettlebell_swing" -> listOf(
            MuscleGroup.LOWER_BODY,
            MuscleGroup.BACK,
            MuscleGroup.CORE,
            MuscleGroup.CARDIO
        )

        "medicine_ball_slam" -> listOf(
            MuscleGroup.LOWER_BODY,
            MuscleGroup.BACK,
            MuscleGroup.SHOULDERS,
            MuscleGroup.CORE,
            MuscleGroup.CARDIO
        )

        "battle_rope" -> listOf(MuscleGroup.SHOULDERS, MuscleGroup.ARMS, MuscleGroup.CORE, MuscleGroup.CARDIO)
        "sled_push" -> listOf(MuscleGroup.LOWER_BODY, MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.CORE)
        "rowing_machine" -> listOf(MuscleGroup.LOWER_BODY, MuscleGroup.BACK, MuscleGroup.ARMS, MuscleGroup.CORE)
        "treadmill_walk",
        "indoor_bike",
        "elliptical",
        "stair_climber" -> listOf(MuscleGroup.LOWER_BODY)

        else -> emptyList()
    }.filterNot { it == primary }

    private fun exerciseById(exerciseId: String): Exercise =
        exercisesById[exerciseId] ?: throw NoSuchElementException("Exercise $exerciseId not found")

    private fun defaultRepDurationSeconds(
        id: String,
        muscleGroup: MuscleGroup,
        equipment: EquipmentType,
        difficulty: DifficultyLevel
    ): Int = when {
        id in FAST_CONDITIONING_EXERCISE_IDS -> 3
        id.contains("lunge") ||
            id.contains("split_squat") ||
            id.contains("step_up") ||
            id.contains("one_arm") ||
            id.contains("single") ||
            id.contains("half_kneeling") -> 8
        muscleGroup == MuscleGroup.CORE -> 4
        equipment == EquipmentType.MACHINE || equipment == EquipmentType.CABLE -> 5
        difficulty == DifficultyLevel.INTERMEDIATE -> 5
        else -> 4
    }

    private fun day(
        dayOffset: Int,
        title: String,
        focus: String,
        primaryFocus: RoutineFocus,
        vararg exerciseIds: String,
        secondaryFocuses: List<RoutineFocus> = emptyList(),
        minRecoveryHours: Int = 24
    ) = PlanTemplateDay(
        dayOffset = dayOffset,
        title = title,
        focus = focus,
        exercises = exerciseIds.mapIndexed { index, id ->
            val exercise = exerciseById(id)
            TemplateExercise(
                exerciseId = exercise.id,
                sets = if (index == 0 && exercise.muscleGroup == MuscleGroup.CARDIO) 1 else exercise.defaultSets,
                repRange = exercise.defaultRepRange,
                durationMinutes = exercise.defaultDurationMinutes,
                restSeconds = exercise.restSeconds,
                note = if (exercise.difficulty == DifficultyLevel.BEGINNER) {
                    "자세가 안정되면 다음 주에 1-2회만 늘려보세요."
                } else {
                    "RPE 7-8 안에서 자세가 무너지지 않는 선까지만 진행하세요."
                },
                repDurationSeconds = exercise.defaultRepDurationSeconds
            )
        },
        dayNumber = dayOffset + 1,
        primaryFocus = primaryFocus,
        secondaryFocuses = secondaryFocuses,
        minRecoveryHours = minRecoveryHours
    )

}
