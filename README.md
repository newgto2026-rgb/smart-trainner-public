# Smart Trainner

Smart Trainner는 운동 루틴 선택, 운동 기록, 운동 카탈로그, 사이클 분석을 하나의 Android 앱 안에서 다루는 트레이닝 앱이다.

이 프로젝트는 단순히 운동 기록 앱을 만드는 데서 끝나지 않는다. 사용자가 실제 운동을 수행하는 방식, 즉 루틴을 날짜 단위로 진행하고 필요할 때 운동을 추가하거나 대체하며, 기록을 나중에 몰아서 입력할 수도 있는 현실을 앱 정책과 데이터 구조에 반영하는 것을 목표로 한다.

## 프로젝트 목적

이 저장소가 던지는 핵심 질문은 다음과 같다.

> 운동 루틴, 수행 날짜, 세트 기록, 분석 지표가 서로 꼬이지 않도록 하려면 Android 앱의 제품 정책과 모듈 구조를 어떻게 설계해야 하는가?

Smart Trainner는 운동이라는 비교적 복잡한 도메인을 다룬다. 같은 운동이라도 루틴의 어느 사이클, 어느 일차, 어느 날짜에 수행됐는지가 중요하고, 사용자는 루틴을 그대로 따라가기도 하지만 운동을 추가하거나 대체하기도 한다. 이 저장소는 이런 정책을 명확한 domain/use case와 테스트 가능한 data 경계로 나누는 데 집중한다.

특히 다음에 집중한다.

- 루틴 일차와 실제 수행 날짜를 분리해 기록 혼동을 줄이는 방식
- 운동 카탈로그, 루틴, 기록, 분석이 같은 domain 모델을 공유하는 방식
- Android 멀티모듈 경계 안에서 feature와 core 책임을 나누는 방식
- PRD/TRD, QA 문서, 리뷰 재작업 기록으로 긴 AI-assisted 작업을 이어받는 방식
- Gradle test, lint, UI test, module boundary check로 변경을 검증하는 방식

## 앱 개요

| 영역 | 제공 기능 |
|---|---|
| Home | 오늘 수행할 루틴 일차, 다음 운동, 최근 기록 진입 |
| Routine | 시스템 루틴 선택, 추천 루틴, 커스텀 루틴 생성, 일차 날짜 지정/수정, 완료/취소 |
| Exercises | 운동 검색, 부위/장비/난이도 정보, 단계별 이미지와 안전 큐 |
| Workout Record | 세트별 반복/중량/휴식/시간 기록, 이전 기록 기반 프리필 |
| Analysis | 최근 기록, 사이클 요약, 완료율, 볼륨, 운동 시간, 근육 균형 |
| Profile/Auth | 로컬/Google 세션, 닉네임, 신체 정보, 운동 수준, 테마 톤 |

앱의 핵심은 "운동을 언제 입력했는지"보다 "이 운동이 어느 루틴 일차의 어느 날짜에 수행됐는지"를 안정적으로 보존하는 것이다. 루틴 일차는 `routineDayInstanceId`와 지정 날짜로 식별되고, 운동 기록은 이 정보를 통해 분석과 완료 상태에 반영된다.

## 아키텍처

Smart Trainner는 Kotlin 기반 Android 멀티모듈 프로젝트다. 주요 기술은 Jetpack Compose, Hilt, Room, DataStore, Retrofit, Kotlinx Serialization, WorkManager, Navigation Compose다.

```text
app
  - 앱 셸, splash, login/profile, bottom navigation
  - feature entry composition, Hilt binding, training flow coordination

feature:*:api
  - app-facing route와 feature 공개 계약

feature:*:domain
  - feature 전용 use case와 repository contract

feature:*:data
  - feature 전용 repository 구현, Room/DataStore/Network 조합

feature:*:impl
  - Compose UI, ViewModel, UiState, dialog/sheet state

core:model
  - Exercise, PlanTemplate, RoutineProgress, WorkoutLog 등 순수 모델

core:domain
  - repository contract, 공통 use case, seed content

core:data
  - 공통 repository 구현

core:database / core:datastore / core:network
  - Room, Preferences DataStore, Retrofit API/DTO 경계

core:designsystem / core:ui / core:exercise-media / core:testing
  - 브랜드/테마, 공용 UI, 운동 이미지, 테스트 유틸리티
```

의존 방향은 root Gradle의 `checkModuleBoundaries`가 검사한다.

```text
app -> feature:*:api, feature:*:impl, approved feature data/domain, core:*
feature:*:impl -> feature:*:api, feature domain, core:*
feature:*:data -> own feature domain, approved core infrastructure
core:* does not depend on feature:*
```

이 구조는 화면이 data 구현체를 직접 알지 않게 하고, 공통 core 계층이 feature 구현 세부사항에 끌려가지 않게 한다.

## 데이터와 동기화

Smart Trainner는 로컬 우선 경험을 기본으로 한다.

| 데이터 | 로컬 저장소 | 서버/API |
|---|---|---|
| 세션/프로필/운동 수준 | DataStore | `SessionNetworkApi` |
| 루틴 진행 상태 | DataStore | `RoutineProgressNetworkApi` |
| 루틴 일차 날짜 | DataStore + workout log date update | routine progress sync |
| 운동 기록/세트 | Room | `WorkoutLogNetworkApi` |
| 커스텀 루틴 | Room | `RoutineNetworkApi` |
| 운동 카탈로그/플랜 | seed content + network DTO | `ExerciseCatalogNetworkApi`, `PlanGenerationNetworkApi` |
| 분석 요약 | domain calculator + server summary API | `ProgressSummaryNetworkApi` |

운동 기록은 먼저 Room에 저장되고, 서버 저장이 성공하면 sync pending 상태가 정리된다. 네트워크가 실패해도 로컬 화면은 기록을 유지하고, `TrainingDataSyncer` 구현체가 이후 pending 데이터를 서버와 맞춘다.

## 품질 게이트

주요 검증 명령어:

```sh
./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
./gradlew testDebugUnitTest
./gradlew test
./gradlew lint
./gradlew connectedDebugAndroidTest
./gradlew checkModuleBoundaries
```

GitHub Actions는 PR마다 Android unit test, JVM unit test, debug APK, androidTest APK, strict lint를 실행한다. 앱/feature/core main 코드나 UI test harness가 바뀌면 별도 instrumented workflow가 emulator에서 `:app:connectedDebugAndroidTest`를 실행한다.

로컬 hook은 `main`/`master` 직접 commit과 push를 막고, push 시 변경 영향에 맞는 lint 또는 전체 lint를 실행한다.

## 문서

처음 읽을 대표 문서는 아래 네 개다.

- [앱 구조 소개서](docs/SMART-TRAINNER-APP-OVERVIEW.md)
- [AI 기반 개발 사례 소개서](docs/SMART-TRAINNER-AI-DEVELOPMENT-STORY.md)
- [문서 인덱스](docs/README.md)
- [품질 게이트](docs/agent/quality-gates.md)

기존 PRD/TRD, QA, 운동 이미지, 브랜드, AI 재작업 기록은 [문서 인덱스](docs/README.md)에서 목적별로 찾는다. 이 문서들은 외부 소개용 완성 문서라기보다 기능 설계와 작업 기록을 보존하는 내부 문서에 가깝다.

## 작업 정책

새 작업은 최신 public `main`에서 전용 worktree와 `codex/<task-name>` 브랜치를 만들어 진행한다.

```sh
scripts/new-smart-task <task-name>
```

Android `core:network` DTO나 API 경로가 바뀌면 companion server repo인 `/Users/kimtaenyun/server/smart-trainner`도 같은 작업에서 확인한다.

기본 작업 흐름:

```text
AGENTS.md 확인
  -> 최신 main 기반 작업 브랜치 생성
  -> 영향 모듈과 서버 계약 영향 확인
  -> 기존 use case/repository/ViewModel 패턴 확인
  -> 문서 또는 테스트로 정책 고정
  -> 모듈 경계 안에서 구현
  -> 영향 모듈 test/lint/build 실행
  -> PR에 검증 결과와 리스크 기록
```

## 이 프로젝트가 남기는 것

Smart Trainner의 제품적 핵심은 루틴과 기록의 의미를 안정적으로 보존하는 것이다. 사용자가 운동을 실시간으로 기록하든 나중에 몰아서 입력하든, 앱은 루틴 일차, 수행 날짜, 운동 세트, 사이클 분석을 같은 기준으로 해석해야 한다.

개발 시스템의 핵심은 그 정책을 코드와 문서 양쪽에 남기는 것이다. PRD/TRD는 제품 판단을 보존하고, module boundary check와 CI는 구조적 회귀를 막으며, 테스트와 리뷰 기록은 다음 작업자가 같은 맥락에서 이어갈 수 있게 한다.
