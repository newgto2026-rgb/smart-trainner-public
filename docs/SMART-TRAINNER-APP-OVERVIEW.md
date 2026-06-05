# Smart Trainner 앱 구조 소개서

## 1. 한 줄 소개

Smart Trainner는 루틴 선택, 운동 수행, 세트 기록, 운동 카탈로그, 사이클 분석을 하나의 흐름으로 연결하는 Android 트레이닝 앱이다.

핵심은 "오늘 어떤 운동을 해야 하는가"와 "이미 수행한 운동이 어느 루틴 일차/사이클/날짜에 속하는가"를 사용자가 헷갈리지 않게 유지하는 것이다. 사용자는 추천 루틴이나 커스텀 루틴을 고르고, 각 루틴 일차에 수행 날짜를 지정한 뒤, 운동별 세트 기록을 남기고, 사이클 단위 분석에서 누적 결과를 확인한다.

## 2. 제품 경험

| 영역 | 사용자 가치 | 대표 기능 |
|---|---|---|
| Home | 바로 다음 운동 흐름 파악 | 오늘 루틴 카드, 다음 일차, 운동 시작, 최근 기록 진입 |
| Routine | 내 상황에 맞는 루틴 진행 | 추천 루틴, 시스템 루틴 라이브러리, 커스텀 루틴, 일차 날짜 지정/수정, 완료/취소 |
| Workout Record | 실제 수행 기록 보존 | 세트별 반복/중량/휴식/시간, 이전 기록 프리필, 루틴 세션 액션 |
| Exercises | 운동 방법 탐색 | 검색, 부위/장비/난이도, 단계별 이미지, 안전 큐, 기록 진입 |
| Analysis | 운동 결과 이해 | 최근 기록, 사이클 번호, 완료율, 볼륨, 운동 시간, 근육 균형 |
| Profile/Auth | 사용자 맥락 관리 | 로컬/Google 로그인, 닉네임, 성별/키/몸무게, 운동 수준, 테마 톤 |

앱은 운동 계획과 운동 기록을 분리하지 않고, 같은 루틴 컨텍스트 안에서 연결한다. 예를 들어 루틴에서 운동을 기록하면 해당 운동은 `routineDayInstanceId`를 가지고 저장되고, 분석과 완료 상태는 이 식별자를 기준으로 같은 루틴/사이클의 기록만 사용한다.

## 3. 루틴 날짜 정책

Smart Trainner에서 중요한 정책은 "기록 입력 시간"과 "운동 수행 날짜"를 분리하는 것이다.

```text
루틴 시작 또는 일차 기록
  -> 해당 루틴 일차의 수행 날짜 선택
  -> PlannedExercise.routineDayDate에 반영
  -> 운동 기록 performedAt은 지정 날짜 정오 기준으로 저장
  -> 같은 routineDayInstanceId의 기존 기록 날짜도 함께 보정
```

이 정책은 몰아서 입력하는 사용자를 보호한다. 사용자가 실제로 2026-06-03에 한 운동을 2026-06-05에 입력하더라도, 분석은 2026-06-03 수행 기록으로 해석해야 한다.

일차 간 제약도 domain 정책으로 다룬다.

| 정책 | 의미 |
|---|---|
| 같은 루틴 일차의 운동 | 모두 같은 수행 날짜를 공유 |
| 다음 루틴 일차 | 이전 일차와 같은 날짜 또는 이전 날짜로 지정할 수 없음 |
| 일차 날짜 수정 | 이미 저장된 같은 routine day 기록의 날짜도 함께 보정 |
| 다른 사이클/템플릿 기록 | 현재 사이클 완료 계산과 분석에 섞이지 않음 |

## 4. 전체 구조

```text
app
  - SmartTrainnerApp, splash, login/profile setup, bottom navigation
  - TrainingRoute로 feature dialog와 shared TrainingViewModel 조정
  - Hilt module에서 feature entry와 repository 구현 binding

feature:routine:api
  - RoutineFeatureEntry, RoutineRouteState, routine-facing route contract

feature:routine:domain
  - 루틴 추천, 준비도, 완료 계산, 루틴 일차 날짜, 커스텀 루틴 command/read use case

feature:routine:data
  - RoutineProgressRepository, RoutinePlan repository 구현
  - DataStore, Room custom routine, routine progress network sync

feature:routine:impl
  - 루틴 화면, 라이브러리/추천/설정/dialog, 커스텀 루틴 builder

feature:workout:api/domain/data/impl
  - 운동 기록 dialog 계약, 저장 use case, Room/Network repository, record form UI

feature:exercise:api/domain/impl
  - 운동 카탈로그와 상세 화면, 검색, 단계별 이미지

feature:analysis:api/domain/data/impl
  - 분석 route, weekly/cycle summary, 최근 운동 기록 UI

core:model
  - Exercise, PlanTemplate, WorkoutLog, RoutineProgress 등 순수 domain model

core:domain
  - 공통 repository contract와 seed training content

core:data
  - 세션, 공통 운동/주간 플랜/운동 로그 repository 구현

core:database / core:datastore / core:network
  - Room, Preferences DataStore, Retrofit DTO/API

core:exercise-media
  - 운동 단계 이미지와 thumbnail resource mapping

core:designsystem / core:ui / core:testing
  - 브랜드 테마, 공통 UI, 테스트 유틸리티
```

## 5. 모듈 경계

루트 Gradle의 `checkModuleBoundaries`는 다음 규칙을 검사한다.

| 규칙 | 이유 |
|---|---|
| `core:*`는 `feature:*`에 의존하지 않음 | 공통 계층이 feature 구현에 끌려가지 않게 함 |
| feature UI는 data 구현체 대신 domain/use case를 봄 | 화면에서 저장소 구현 세부사항을 숨김 |
| feature data는 자기 feature domain contract만 봄 | 다른 feature의 private policy와 얽히지 않음 |
| app의 feature impl/data 참조는 DI composition allowlist 안에서만 허용 | app 셸이 feature 구현 세부사항을 통제된 지점에서만 조립 |
| Navigation 참조는 app navigation 파일과 허용된 feature entry에 제한 | 화면 구현에 app routing 책임이 흩어지는 것을 줄임 |

이 경계 덕분에 루틴 정책 변경, 운동 기록 저장 변경, 분석 계산 변경을 각 모듈 안에서 좁혀 검증할 수 있다.

## 6. 데이터 구조

### 6.1 Room

Room은 운동 기록과 커스텀 루틴을 저장한다.

| Entity | 역할 |
|---|---|
| `WorkoutLogEntity` | 세션별 운동 기록, 수행 날짜/시간, planned exercise, routine day instance, sync pending |
| `WorkoutSetLogEntity` | 운동 기록의 세트별 reps/weight/duration/rest |
| `CustomRoutineEntity` | 사용자 커스텀 루틴 메타데이터와 sync state |
| `CustomRoutineDayEntity` | 커스텀 루틴 일차, focus, recovery policy |
| `CustomRoutineExerciseEntity` | 커스텀 루틴 일차 안의 운동 슬롯 |

`SmartTrainnerDatabase`는 schema version 8이며 schema export가 켜져 있다.

### 6.2 DataStore

DataStore는 앱 상태와 사용자별 preference를 저장한다.

| 항목 | 역할 |
|---|---|
| active session | 현재 로그인/로컬 세션 |
| profile/body measurement | 성별, 키, 몸무게, 닉네임 |
| selected theme tone | 앱 테마 톤 |
| training experience | BEGINNER/INTERMEDIATE/ADVANCED |
| selected template | 선택 루틴 템플릿 |
| routine progress | day index, cycle number, startedAt, cycleStartedAt |
| routine day dates | `routineDayInstanceId -> LocalDate` mapping |
| installation device id | Google 세션 device validation |

### 6.3 Network

Network 계층은 Retrofit API와 Kotlinx Serialization DTO를 가진다.

| API | 역할 |
|---|---|
| `SessionNetworkApi` | 세션 생성, Google 로그인, 닉네임 확인, device validation, profile update |
| `ExerciseCatalogNetworkApi` | 서버 운동 카탈로그 조회 |
| `PlanGenerationNetworkApi` | 서버 기반 플랜 생성 |
| `RoutineNetworkApi` | 커스텀 루틴 CRUD와 선택 |
| `RoutineProgressNetworkApi` | 루틴 진행 시작/전환/완료/취소/sync |
| `WorkoutLogNetworkApi` | 운동 기록 조회/생성 |
| `ProgressSummaryNetworkApi` | 서버 진행 요약 |

Android `core:network` 계약이 바뀌면 companion server repo도 같은 작업에서 확인해야 한다.

## 7. 운동 기록 흐름

```text
사용자가 운동 기록 dialog를 연다
  -> WorkoutRecordingViewModel이 planned exercise와 이전 기록을 기준으로 form prefill
  -> 사용자가 세트별 reps/weight/rest/duration 입력
  -> SaveWorkoutLogUseCase
  -> DefaultWorkoutRecordingRepository
  -> Room upsert + set logs 저장
  -> 서버 createWorkoutLog 시도
  -> 성공하면 syncPending false
```

루틴 일차에서 온 planned exercise는 `routineDayInstanceId`와 `routineDayDate`를 가진다. 기록 저장 시 `performedAt`은 `routineDayDate.atTime(12, 0)`로 생성되어, 입력 시각이 아니라 수행 날짜 중심으로 분석된다.

## 8. 루틴 진행 흐름

```text
템플릿 선택
  -> selectedTemplateId / activeRoutineProgress 저장
  -> WeeklyPlan 생성
  -> RoutineViewModel이 현재 dayIndex와 cycleNumber 계산
  -> 일차 날짜가 없으면 date picker
  -> 운동 기록 저장
  -> current day planned exercise 완료 여부 계산
  -> CompleteRoutineDayUseCase
  -> 다음 dayIndex 또는 다음 cycleNumber로 이동
```

루틴 완료 계산은 current template/cycle prefix를 가진 routine day log만 routine log로 인정한다. routine day id가 없는 ad-hoc 기록은 cycle start timestamp 이후인지로만 fallback 판단한다.

## 9. 운동 카탈로그와 이미지

운동 콘텐츠는 `SeedTrainingContent`와 `core:exercise-media`가 함께 제공한다.

| 구성 | 역할 |
|---|---|
| `Exercise` model | 이름, 부위, 장비, 난이도, 설명, 단계, 안전 큐 |
| movement pattern / popularity rank | 운동 목록 정렬과 탐색 안정성 |
| primary/secondary muscle role | 루틴 builder에서 관련 운동을 더 넓게 찾음 |
| generated step images | 운동 상세 단계별 이미지와 확대 viewer |
| thumbnail images | 카탈로그에서 빠르게 운동을 식별 |

운동 검색은 이름, ID, 주 부위, 장비명을 token 단위로 정규화해 AND 조건으로 필터링한다.

## 10. 분석 구조

Analysis는 운동 로그와 현재 루틴 진행 상태를 함께 본다.

```text
observeAllWorkoutLogs()
  + observeRoutineProgress()
  + observeCycleSummary(weekStart, progress, zone)
  + observeExercises()
  -> AnalysisUiState
```

분석은 최근 기록 목록과 현재 cycle number를 보여주고, summary는 완료 운동 수, 세트 수, 총 볼륨, 운동 시간, streak, 근육 균형을 계산한다. 루틴 사이클 요약은 현재 template/cycle에 해당하는 routine day log만 포함하도록 제한한다.

## 11. Android 런타임 통합

| 런타임 요소 | 앱에서의 역할 |
|---|---|
| Jetpack Compose | 모든 화면과 dialog/sheet UI |
| Hilt | repository/use case/ViewModel/feature entry DI |
| Room | 운동 기록, 세트, 커스텀 루틴 저장 |
| DataStore | 세션, 프로필, 루틴 진행, preference 저장 |
| Retrofit/OkHttp | companion server API 호출 |
| Kotlinx Serialization | network DTO JSON 변환 |
| Navigation Compose | Home/Routine/Exercises/Analysis 탭 라우팅 |
| WorkManager | 현재 dependency로 포함, 향후 background sync/reminder 확장 지점 |
| Android Credentials + Google ID | Google 로그인 credential flow |
| Coil | 운동 이미지 표시 |

## 12. 테스트와 품질 기반

| 테스트 영역 | 예시 |
|---|---|
| App ViewModel | session/profile/login/sync state |
| Training flow | 기록 dialog, 연속 루틴, skip/add/substitute |
| Routine domain | 추천, readiness, cycle completion, day date policy |
| Routine data | progress preference mapping, server/local sync policy |
| Workout data | workout log network mapping, pending sync |
| Exercise feature | search, detail, step text |
| Analysis domain | weekly/cycle summary 계산 |
| Database | workout DAO, custom routine DAO |
| Instrumented UI | login 이후 탭, 운동 상세 이미지, 기록 저장, 루틴 builder |

자동화 하네스:

| 하네스 | 역할 |
|---|---|
| `checkModuleBoundaries` | Gradle 의존 방향과 금지 import 검사 |
| `.husky/pre-commit` | `main`/`master` 직접 commit 차단 |
| `.husky/pre-push` | `main`/`master` 직접 push 차단, 변경 영향 lint 실행 |
| Android CI | unit test, JVM test, debug build, androidTest APK, strict lint |
| Android Instrumented Tests | core app/feature 변경 시 emulator UI test 실행 |
| PR template | 변경 범위, 설계, 테스트, 리스크, 마이그레이션 기록 |
| `docs/ai-rework/branches` | 리뷰/CI 후속 작업 관측성 기록 |

## 13. 앱 구성의 강점

| 강점 | 설명 |
|---|---|
| 루틴 날짜 중심 정책 | 입력 시각보다 수행 날짜를 보존해 몰아서 입력하는 사용자를 지원 |
| 사이클 오염 방지 | 다른 template/cycle routine log가 현재 완료/분석에 섞이지 않도록 제한 |
| 운동 콘텐츠 밀도 | 다양한 부위/장비/난이도와 단계별 이미지 제공 |
| 커스텀 루틴 확장성 | Room + network sync state로 사용자 루틴을 관리 |
| 명확한 모듈 경계 | feature/domain/data/impl과 core 계층 책임이 분리 |
| 테스트 가능한 domain 정책 | 루틴 추천, 완료 계산, 분석 요약을 화면 밖에서 검증 |
| Android UI 회귀 기반 | 주요 사용자 흐름을 connected UI test로 확인 |

## 14. 앞으로 보강하기 좋은 문서

- 사용자 매뉴얼 PDF: 로그인, 프로필 설정, 루틴 선택, 일차 날짜 지정, 운동 기록, 분석 확인을 화면 캡처와 함께 설명
- 데이터 아키텍처 리포트: Room/DataStore/Network sync 관계를 더 깊게 정리
- 루틴 날짜 정책 ADR: 수행 날짜, cycle start, 일차 완료, 기록 수정의 불변조건을 별도 decision record로 고정
- 서버 계약 문서: Android `core:network` DTO와 companion server route를 한 문서에서 대조
