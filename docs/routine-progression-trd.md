# 전신/분할 루틴 진행 가이드 TRD

## 1. 기술 목표
PRD에서 확정한 `1일차~N일차` 루틴 진행 모델을 현재 Android 모듈 구조에 맞게 구현한다.

핵심 기술 목표:

- 날짜 중심 주간 플랜 표시에서 `현재 루틴 일차` 중심 진행으로 이동한다.
- 전신, 균형 분할, 부위 집중 분할을 모델로 표현한다.
- 사용자가 루틴을 완료하면 다음 일차로 이동하고 마지막 일차 뒤에는 1일차로 순환한다.
- 부위 집중 루틴은 프리셋 템플릿으로 제공한다.
- 기존 운동 기록, 운동 상세, 요약 흐름은 유지한다.

## 2. 현재 구조
현재 구조는 다음 경로를 중심으로 동작한다.

- `core:model`: `PlanTemplate`, `PlanTemplateDay`, `WeeklyPlan`, `WorkoutDayPlan`, `PlannedExercise`.
- `core:data`: `SeedTrainingContent`와 `DefaultTrainingRepository`가 템플릿 기반 주간 플랜을 만든다.
- `core:datastore`: `TrainingPreferencesDataSource`가 선택된 템플릿 id를 저장한다.
- `core:domain`: repository contract와 use case wrapper를 제공한다.
- `feature:training:impl`: `TrainingViewModel`, `TrainingUiState`, `TrainingRoute`가 화면 상태와 UI를 구성한다.
- `app`: instrumentation UI 테스트가 core flow를 검증한다.

## 3. 영향 모듈
- `:core:model`
- `:core:domain`
- `:core:data`
- `:core:datastore`
- `:feature:training:impl`
- `:app` instrumentation tests

모듈 경계:

- 루틴 추천/진행 정책은 `core:domain`에 둔다.
- seed 템플릿과 exercise 참조 검증은 `core:data`에 둔다.
- DataStore는 raw preference만 저장한다.
- ViewModel은 UI 상태와 이벤트 조정만 담당한다.
- Composable은 state 렌더링과 event emit만 한다.

## 4. 모델 설계
### 신규 enum
`core:model`에 추가한다.

```kotlin
enum class RoutineStructure {
    FULL_BODY,
    BALANCED_SPLIT,
    BODY_PART_SPLIT
}

enum class RoutineFocus {
    FULL_BODY,
    CHEST,
    BACK,
    LOWER_BODY,
    SHOULDERS,
    ARMS,
    CARDIO_CONDITIONING,
    CORE
}

enum class TrainingExperience {
    BEGINNER,
    INTERMEDIATE
}

enum class RoutineFeeling {
    BALANCED_FULL_BODY,
    FOCUSED_BODY_PART,
    APP_RECOMMENDED
}
```

### `PlanTemplate` 확장
기존 필드에 기본값을 가진 메타데이터를 추가해 constructor churn을 줄인다.

```kotlin
data class PlanTemplate(
    val id: String,
    val name: String,
    val level: PlanLevel,
    val daysPerWeek: Int,
    val description: String,
    val days: List<PlanTemplateDay>,
    val structure: RoutineStructure = RoutineStructure.FULL_BODY,
    val recommendedExperience: TrainingExperience = TrainingExperience.BEGINNER,
    val cycleLength: Int = days.size,
    val sessionMinutes: Int = 45,
    val focusSummary: List<RoutineFocus> = listOf(RoutineFocus.FULL_BODY)
)
```

구현 시 Kotlin 문법에 맞춰 `focusSummary: List<RoutineFocus>`로 작성한다.

### `PlanTemplateDay` 확장
```kotlin
data class PlanTemplateDay(
    val dayOffset: Int,
    val title: String,
    val focus: String,
    val exercises: List<TemplateExercise>,
    val dayNumber: Int = dayOffset + 1,
    val primaryFocus: RoutineFocus = RoutineFocus.FULL_BODY,
    val secondaryFocuses: List<RoutineFocus> = emptyList(),
    val minRecoveryHours: Int = 24
)
```

주의:

- `dayOffset`는 기존 weekly plan 호환을 위해 1차 구현에서 유지한다.
- UI에서는 `dayNumber`와 `primaryFocus`를 우선 사용한다.

### `WeeklyPlan` / `WorkoutDayPlan`
1차 구현에서는 기존 `WeeklyPlan`을 유지하고 메타데이터를 전달한다.

장기적으로는 날짜 없는 `RoutineCyclePlan` 모델을 분리할 수 있다.

1차 권장:

- `WeeklyPlan`에 `structure`, `cycleLength`, `currentDayNumber`를 추가하지 않고 ViewModel에서 active progress와 plan days를 조합한다.
- `WorkoutDayPlan`에는 `dayNumber`, `primaryFocus`, `secondaryFocuses`, `minRecoveryHours`를 전달한다.

## 5. DataStore 설계
기존 `selected_template_id_$sessionId`는 유지한다.

추가 preference key:

- `active_routine_template_id_$sessionId`
- `active_routine_day_index_$sessionId`
- `active_routine_started_at_$sessionId`
- `last_completed_routine_day_$sessionId`
- `last_completed_at_$sessionId`

DataStore API:

```kotlin
fun activeRoutineProgress(sessionId: String): Flow<RoutineProgressPreference>
suspend fun setActiveRoutineTemplate(sessionId: String, templateId: String)
suspend fun setActiveRoutineDayIndex(sessionId: String, dayIndex: Int)
suspend fun markRoutineDayCompleted(sessionId: String, dayIndex: Int, completedAt: String)
```

DataStore에는 정책을 넣지 않는다. day index 유효성, 순환 계산은 domain/use case에서 처리한다.

## 6. Domain 설계
### 신규 모델
`core:domain` 또는 `core:model` 중 공유 필요성을 기준으로 배치한다. UI와 data가 함께 쓰면 `core:model`에 둔다.

```kotlin
data class RoutineRecommendationInput(
    val daysPerWeek: Int,
    val sessionMinutes: Int,
    val experience: TrainingExperience,
    val feeling: RoutineFeeling
)

data class RoutineRecommendation(
    val primaryTemplateId: String,
    val alternativeTemplateIds: List<String>,
    val reasonCode: String
)

data class RoutineProgress(
    val templateId: String,
    val dayIndex: Int,
    val lastCompletedDayIndex: Int?,
    val lastCompletedAt: LocalDateTime?
)
```

### 신규 use case
- `RecommendRoutineUseCase`
  - 입력: `RoutineRecommendationInput`, templates.
  - 출력: `RoutineRecommendation`.
  - 정책: 초보자 주 2~3회는 전신 우선, 중급자 주 4회 이상은 부위 집중 후보 허용.

- `ObserveRoutineProgressUseCase`
  - 현재 active routine progress observe.

- `StartRoutineUseCase`
  - template 선택 + active progress 초기화.

- `AdvanceRoutineDayUseCase`
  - 완료된 일차 기준 다음 day index 계산.
  - 마지막 일차 완료 시 0으로 순환.

- `EvaluateRoutineReadinessUseCase`
  - 최소 휴식 시간 전 다음 Day 시작 시 warning 상태 반환.
  - MVP에서는 hard block보다 warning을 권장.

## 7. Repository contract 변경
`TrainingRepository`에 추가 후보:

```kotlin
fun observeRoutineProgress(): Flow<RoutineProgress>
suspend fun startRoutine(templateId: String): Result<Unit>
suspend fun completeRoutineDay(dayIndex: Int, completedAt: LocalDateTime): Result<Unit>
```

기존 `selectPlanTemplate`은 호환 유지한다. 새 UI는 `startRoutine`을 사용하고, 내부적으로 selected template도 함께 갱신한다.

## 8. Seed Data 설계
기존 seed 템플릿을 메타데이터화하고 MVP 템플릿을 보강한다.

필수 템플릿:

1. `beginner-full-body-2day`
   - 전신 A/B.
   - 초보자 주 2회.

2. `beginner-full-body-3day`
   - 전신 A/B/C.
   - 초보자 주 3회.

3. `intermediate-balanced-4day`
   - 상체/하체/상체/하체.
   - 중급자 주 4회.

4. `intermediate-body-part-4day`
   - 1일차 등 집중.
   - 2일차 가슴 집중.
   - 3일차 하체 집중.
   - 4일차 어깨+팔 집중.

2차 후보:

5. `intermediate-body-part-5day`
   - 가슴/등/하체/어깨/팔+유산소.

Seed 검증 규칙:

- 모든 exercise id는 catalog에 존재해야 한다.
- 모든 템플릿의 `cycleLength == days.size`.
- 부위 집중 템플릿은 각 day에 `primaryFocus != FULL_BODY`.
- 부위 집중 4일 템플릿은 `BACK`, `CHEST`, `LOWER_BODY`, `SHOULDERS` 또는 `ARMS`를 포함해야 한다.
- 초보자용 5일 body-part split은 없어야 한다.

## 9. ViewModel 설계
`TrainingUiState`에 추가:

```kotlin
val activeRoutineProgress: RoutineProgressUiModel?
val nextRoutineDay: WorkoutDayPlan?
val nextRoutineTitle: String
val nextRoutineSubtitle: String
val routineRecommendationInput: RoutineRecommendationFormState
val recommendedTemplateId: String?
val alternativeTemplateIds: List<String>
```

ViewModel 책임:

- active progress와 selected plan을 조합해 다음 day를 계산한다.
- 홈 화면에 보여줄 `오늘은 등 집중`, `2일차 · 당기는 운동 · 약 50분` 형태의 UI 모델을 만든다.
- 루틴 완료 시 `completeRoutineDay`를 호출하고 다음 day로 이동한다.
- 루틴 선택 시 `startRoutine`을 호출한다.

Composable 책임:

- ViewModel에서 받은 UI 모델을 렌더링한다.
- 클릭 이벤트만 전달한다.

## 10. UI 설계
### 홈 화면
기존 `TodayProgressLine`과 `FeaturedExerciseCard` 중심에서 `NextRoutineDayCard` 중심으로 이동한다.

카드 구성:

- 헤드라인: `오늘은 등 집중`
- 서브라인: `2일차 · 당기는 운동 · 약 50분`
- 주요 부위 chips: `광배`, `등 중앙`, `후면 어깨`, `이두`
- 운동 예시 3개.
- CTA: `운동 시작`
- 다음 예고: `다음 운동: 가슴 집중`

### 플랜 화면
- 추천 루틴 카드 1개를 크게 표시.
- 대안 루틴 1~2개 표시.
- 구조 라벨:
  - `매번 전신을 골고루`
  - `상체/하체로 나눠 하기`
  - `부위별로 집중하기`
- 각 루틴 카드에 일차 흐름 표시:
  - `등 → 가슴 → 하체 → 어깨+팔`

### 기록 흐름
기존 운동별 기록 dialog는 유지한다.

MVP에서는 “Day 전체 완료”와 “개별 운동 기록”의 관계를 정해야 한다.

권장 1차:

- 모든 planned exercise가 완료되면 day complete로 간주.
- 또는 `오늘 운동 완료` 버튼을 별도 제공.
- 자동 day advance는 명확한 사용자 액션 후 수행한다.

## 11. Localization
추가 문자열은 `feature/training/impl/src/main/res/values/strings.xml`와 `values-ko/strings.xml`에 모두 추가한다.

예:

- `training_today_focus_title`
- `training_routine_day_subtitle`
- `training_next_routine_day`
- `training_routine_structure_full_body`
- `training_routine_structure_balanced_split`
- `training_routine_structure_body_part_split`
- `training_start_routine`
- `training_complete_routine_day`

## 12. 테스트 전략
### `:core:model:test`
- enum/model 생성 compile 검증.

### `:core:domain:test`
- `RecommendRoutineUseCase`
  - 초보자 주 2회는 전신 추천.
  - 초보자 주 5회는 body-part split 기본 추천 금지.
  - 중급자 주 4회 + focused feeling은 body-part split 추천.
- `AdvanceRoutineDayUseCase`
  - 마지막 일차 뒤 1일차 순환.
- `EvaluateRoutineReadinessUseCase`
  - minRecoveryHours 전이면 warning.

### `:core:data:testDebugUnitTest`
- seed template id uniqueness.
- exercise reference integrity.
- body-part split 4day focus coverage.
- no beginner high-frequency body-part split.

### `:feature:training:impl:testDebugUnitTest`
- ViewModel combines progress and plan into next day UI model.
- complete day event advances progress.

### `connectedDebugAndroidTest`
- 홈에서 `오늘은 등 집중` 카드 노출.
- 부위 집중 루틴 선택.
- 운동 기록 후 다음 day 표시.

## 13. 구현 순서
1. `core:model` 루틴 구조/포커스/경험 모델 추가.
2. `core:domain` 추천/진행/회복 use case 추가 및 테스트.
3. `core:datastore` active routine progress preference 추가.
4. `core:data` repository contract 구현 및 seed metadata 보강.
5. `feature:training:impl` UiState/ViewModel에 next routine day 모델 추가.
6. 홈 화면을 next routine day 중심으로 개편.
7. 플랜 화면에 추천 루틴/대안 루틴 구조 반영.
8. UI/resource/localization 추가.
9. unit/lint/build/instrumentation 검증.

## 14. 호환성
- 기존 selected template id는 유지한다.
- 새 active routine progress가 없으면 selected template의 첫 day를 next day로 간주한다.
- 기존 workout logs와 Room schema는 변경하지 않는다.
- DataStore key는 추가만 하고 rename/remove하지 않는다.

## 15. 리스크
- Day 전체 완료와 exercise별 완료의 관계가 애매해질 수 있다.
- 날짜 기반 summary와 day-index 기반 progress가 동시에 존재한다.
- 부위 집중 루틴이 과한 처방처럼 보일 수 있다.
- UI가 루틴 추천, 진행, 기록을 한 화면에 과하게 담을 수 있다.

완화:

- MVP는 day progression만 추가하고 주간 summary는 기존 방식 유지.
- 루틴은 검증된 seed 프리셋만 제공.
- 안전/회복 문구는 안내 수준으로 유지.
- 자유형 생성/약점 분석은 제외한다.
