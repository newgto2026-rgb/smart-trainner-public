# 루틴 첫 진입 성능 개선 TRD

## 1. 문서 목적
루틴 화면 첫 진입이 무겁게 느껴지는 원인을 데이터, 렌더링, 상태 생명주기 관점에서 분석하고, 화면을 로컬 DB 기반으로 빠르게 그리는 구조를 명확히 한다.

이 문서는 다음 질문에 답한다.

- 앱은 기본적으로 DB를 보고 화면을 그리는가?
- 그렇다면 서버 동기화가 왜 첫 진입 체감 속도에 영향을 줄 수 있는가?
- 루틴 화면에서 첫 진입 비용을 키우는 구체 코드 경로는 무엇인가?
- 이번 개선에서 바로 줄일 수 있는 비용과 후속 개선으로 남겨야 할 비용은 무엇인가?

## 2. 결론 요약
원칙적으로 화면은 서버 응답을 직접 기다려 그리는 구조가 아니라, 로컬 DB와 로컬 상태를 Flow로 구독해 그리는 구조가 맞다.

하지만 현재 구현에는 다음 이유로 "DB 기반 화면"이어도 첫 진입이 느려질 수 있다.

1. 커스텀 루틴 템플릿 관찰 경로에서 Room Flow 첫 emission 전에 서버 동기화를 먼저 실행하고 있었다.
   - `DefaultRoutinePlanRepository.observeCustomRoutines()`
   - 이 경우 UI는 DB 기반이지만, 첫 DB 결과가 네트워크 뒤로 밀릴 수 있다.

2. 루틴 ViewModel이 탭별 navigation owner에 묶이면 홈, 루틴, 운동 탭에서 같은 루틴 데이터 파이프라인을 새로 만들 수 있다.
   - `RoutineFeatureEntryImpl.rememberRouteState()`
   - 홈에서 이미 계산한 루틴 상태가 루틴 탭 첫 진입 때 재사용되지 않을 수 있다.

3. 루틴 ViewModel이 실제 UI에 쓰지 않는 주간 로그 Flow까지 구독하고 있었다.
   - `RoutineViewModel.logState`
   - 전체 로그와 최신 로그가 이미 UI 입력으로 쓰이는데, 주간 로그 쿼리가 추가로 실행됐다.

4. 최신 운동 로그 계산이 전체 로그를 DB에서 모두 읽은 뒤 앱 메모리에서 `distinctBy` 하는 방식이었다.
   - `DefaultWorkoutLogRepository.observeLatestWorkoutLogs()`
   - 기록이 누적될수록 루틴 첫 상태 계산과 diff 비용이 증가한다.

5. 루틴 목록 row가 운동 이미지를 즉시 로드한다.
   - `RoutinePlanContent`
   - 첫 화면 visible row 수만큼 큰 `drawable-nodpi` PNG decode가 발생할 수 있다.

이번 1차 개선은 1~4번을 코드로 반영한다. 5번은 이미지 자산/썸네일 전략이 필요하므로 후속 단계로 둔다.

## 3. 목표
### 기능 목표
- 루틴 첫 진입 시 로컬 DB와 seed 데이터만으로 즉시 초기 화면을 그린다.
- 서버 동기화는 화면 첫 emission을 막지 않고, 완료 후 DB/DataStore 갱신을 통해 자연스럽게 UI에 반영한다.
- 홈, 루틴, 운동 탭에서 동일한 루틴 ViewModel 인스턴스를 공유해 중복 구독과 중복 초기 계산을 줄인다.
- 루틴 화면에서 실제로 쓰지 않는 데이터 구독을 제거한다.
- 최신 운동 로그는 전체 로그를 읽지 않고 DB가 운동별 최신 행만 반환하도록 한다.

### 성능 목표
정량 측정은 후속 Macrobenchmark/trace에서 확정한다. 1차 목표치는 다음과 같다.

- 루틴 템플릿 Flow 첫 로컬 emission이 네트워크 RTT에 의존하지 않는다.
- 루틴 탭 첫 진입 시 새 `RoutineViewModel` 생성이 반복되지 않는다.
- 최신 로그 계산에서 반환 row 수를 전체 로그 수가 아니라 운동 종류 수 수준으로 제한한다.
- 초기 루틴 UI 구성에서 불필요한 Room Flow 최소 1개를 제거한다.

### 비목표
- 서버 API contract 변경은 하지 않는다.
- 루틴/운동 도메인 모델의 의미 변경은 하지 않는다.
- 이번 단계에서 이미지 자산 리사이징, CDN, WebP 변환은 하지 않는다.
- DataStore schema나 Room schema version 변경은 하지 않는다.

## 4. 영향 모듈
- `:feature:routine:impl`
  - 루틴 ViewModel scope 공유.
  - 불필요한 주간 로그 Flow 제거.

- `:feature:routine:data`
  - 커스텀 루틴 관찰을 local-first로 변경.

- `:core:data`
  - 최신 운동 로그 repository 쿼리 경로 개선.

- `:core:database`
  - 운동별 최신 로그 관찰 DAO 쿼리 추가.
  - schema 변경 없음.

- `:core:domain`
  - 운동 기록, 운동 상세, 루틴 완료 흐름을 app이 조율할 때 쓰는 `RoutineSessionCoordinator` contract 추가.
  - routine UI route state가 cross-feature 세션 조율 메서드를 직접 노출하지 않도록 분리.

- `docs`
  - 본 TRD 추가.

서버 API contract 변경은 없으므로 companion server repo 변경은 필요 없다.

## 5. 현재 데이터 흐름
### 앱 전체 동기화 흐름
앱 시작 또는 로그인/session 변경 후 `SmartTrainnerAppViewModel`은 `SyncPendingTrainingDataUseCase`를 통해 여러 `TrainingDataSyncer`를 실행한다.

대표 syncer:

- `DefaultSessionRepository`
- `DefaultRoutinePlanRepository`
- `DefaultRoutineProgressRepository`
- `DefaultWorkoutRecordingRepository`

이 흐름은 서버 데이터를 로컬 DB/DataStore에 반영하는 책임을 가진다. 따라서 화면 관찰 Flow가 서버 동기화를 선행 조건으로 삼을 필요는 없다.

### 루틴 템플릿 흐름
현재 루틴 템플릿은 다음 데이터의 합이다.

- seed 템플릿: `TrainingSeedStore.templates`
- 커스텀 템플릿: `CustomRoutineDao.observeForSession(sessionId)`

기존 흐름:

```text
observePlanTemplates()
  -> observeCustomRoutines()
    -> observeSessionId()
    -> customRoutineDao.observeForSession(sessionId)
       onStart { syncCustomRoutinesFromServer(sessionId) }
    -> map DB rows to PlanTemplate
  -> seed templates + custom templates
```

문제:

- `onStart`는 downstream에 첫 값을 내보내기 전에 실행된다.
- `syncCustomRoutinesFromServer()`가 네트워크 요청을 포함하므로 Room 첫 emission이 네트워크 뒤에 놓일 수 있다.
- 네트워크 실패는 `runCatching`으로 삼켜지지만, 실패까지 걸리는 시간은 여전히 첫 emission을 지연시킬 수 있다.

개선 흐름:

```text
observePlanTemplates()
  -> observeCustomRoutines()
    -> observeSessionId()
    -> customRoutineDao.observeForSession(sessionId)
    -> map DB rows to PlanTemplate
  -> seed templates + custom templates

background sync
  -> SyncPendingTrainingDataUseCase
  -> DefaultRoutinePlanRepository.syncPendingTrainingData()
  -> syncCustomRoutinesFromServer(sessionId)
  -> DB update
  -> Room emits updated custom templates
```

## 6. 현재 렌더링/상태 흐름
### Navigation과 ViewModel scope
`TrainingCoordinatorRoutes`는 홈, 루틴, 운동 탭에서 `routineFeatureEntry.rememberRouteState(callbacks)`를 호출한다.

기존 `RoutineFeatureEntryImpl`은 다음처럼 현재 navigation destination owner 기준으로 Hilt ViewModel을 얻었다.

```kotlin
val viewModel: RoutineViewModel = hiltViewModel()
```

이 구조에서는 탭 destination이 다르면 `RoutineViewModel` 인스턴스가 각각 생길 수 있다. 루틴 ViewModel은 다음을 한 번에 구독한다.

- 템플릿 목록
- 현재 주간 플랜
- 루틴 진행 상태
- 전체 운동 로그
- 최신 운동 로그
- 운동 seed 목록
- 사용자 운동 경험
- 여러 dialog/form local state

홈에서 이미 루틴 요약을 위해 상태를 만들었더라도, 루틴 탭 첫 진입 시 같은 계산을 다시 시작할 수 있다.

개선 후에는 `TrainingViewModel`과 같은 방식으로 Activity의 `ViewModelStoreOwner`를 찾아 shared ViewModel을 사용한다.

```text
Home tab
  -> shared RoutineViewModel

Routine tab
  -> same shared RoutineViewModel

Exercises tab
  -> same shared RoutineViewModel
```

### RoutineViewModel 입력 Flow
기존 `RoutineViewModel.logState`는 아래 세 로그 Flow를 동시에 구독했다.

- `observeWorkoutLogs(weekStart)`
- `observeAllWorkoutLogs()`
- `observeLatestWorkoutLogs()`

하지만 최종 `RoutineUiState`는 `allLogs`와 `latestLogs`만 사용했다.

```kotlin
RoutineDataState(
    logs = logState.allLogs,
    latestLogs = logState.latestLogs,
)
```

`weeklyLogs`는 `RoutineLogState`에만 보관되고 UI 계산에는 사용되지 않았다. 따라서 주간 로그 Flow는 첫 진입 비용만 늘리고 결과에는 기여하지 않았다.

개선 후 `logState`는 다음 두 Flow만 구독한다.

- `observeAllWorkoutLogs()`
- `observeLatestWorkoutLogs()`

## 7. 최신 로그 쿼리 문제
기존 최신 로그 계산:

```text
WorkoutLogDao.observeAll(sessionId)
  -> all workout logs ordered by performedAt
  -> map every row to domain model
  -> distinctBy { exerciseId }
```

문제:

- 전체 로그 수가 증가할수록 DB 반환 row, relation set row, domain mapping 비용이 모두 증가한다.
- 루틴 화면에서 필요한 것은 "각 운동별 가장 최근 로그"이므로 전체 로그를 한 번 더 읽을 필요가 없다.
- `observeAllWorkoutLogs()`도 별도로 구독하고 있어, 최신 로그 용도까지 전체 로그를 중복 로드한다.

개선 후 최신 로그 계산:

```text
WorkoutLogDao.observeLatestByExerciseForSession(sessionId)
  -> each exerciseId's latest workout log only
  -> map returned rows to domain model
```

추가 DAO 쿼리는 schema 변경 없이 `workout_logs`에서 session별 distinct exerciseId를 잡고, 각 exerciseId의 최신 row만 반환한다.

정렬 기준:

- `performedAt DESC`
- tie-breaker: `id DESC`

## 8. 렌더링 병목 후보
### 운동 이미지 decode
루틴 화면의 row는 `ExerciseMediaRenderer.Image`를 통해 운동 이미지를 렌더링한다. 실제 asset은 `core/exercise-media/src/main/res/drawable-nodpi` 아래에 있으며, 다수의 PNG가 큰 해상도로 존재한다.

가능한 증상:

- 첫 visible row 이미지 decode가 main thread와 가까운 타이밍에 몰린다.
- LazyColumn이 화면 밖 row는 늦게 compose하더라도, 첫 화면에 보이는 row 수만큼 이미지는 즉시 요청된다.
- 탭 첫 진입과 ViewModel/Flow 초기 계산이 동시에 발생하면 이미지 decode가 체감 지연을 키운다.

후속 개선 후보:

- 리스트용 thumbnail asset 분리.
- `AsyncImage`에 `size` 제한 또는 preview/list 전용 request 적용.
- 첫 화면에서는 placeholder 우선 표시 후 이미지 decode를 늦추는 전략 검토.
- 자산을 WebP로 변환하거나 해상도별 리소스로 분리.

## 9. 구현 상세
### 9.1 루틴 ViewModel 공유
변경 파일:

- `feature/routine/impl/src/main/java/com/smarttrainner/feature/routine/impl/RoutineFeatureEntryImpl.kt`
- `feature/routine/api/src/main/java/com/smarttrainner/feature/routine/api/RoutineModels.kt`
- `core/domain/src/main/java/com/smarttrainner/core/domain/RoutineSessionCoordinator.kt`
- `app/src/main/java/com/smarttrainner/app/training/TrainingRoute.kt`
- `app/src/main/java/com/smarttrainner/app/training/TrainingCoordinatorRoutes.kt`

변경:

- 기존 `hiltViewModel()` 직접 호출을 `sharedRoutineViewModel()`로 대체.
- `LocalContext`에서 Activity `ViewModelStoreOwner`를 찾아 shared owner로 Hilt ViewModel을 생성.
- owner를 찾지 못하면 기존처럼 destination owner의 `hiltViewModel()` fallback 사용.
- `RoutineViewModel` 자체는 `feature:routine:impl`에 유지.
- app의 운동 기록/스킵/대체/추가 조율은 `RoutineRouteState`가 아니라 `core:domain`의 `RoutineSessionCoordinator`를 통해 수행.
- `RoutineRouteState`는 루틴 feature route UI state와 현재 루틴명, 그리고 core contract 구현체를 연결하는 feature API로 축소.

기대 효과:

- 홈에서 루틴 요약을 위해 이미 생성된 ViewModel을 루틴 탭 첫 진입 때 재사용한다.
- Flow 구독과 `uiState` combine 초기화 반복을 줄인다.
- dialog/form state도 탭 간 일관되게 유지된다.
- feature 간 직접 참조 없이 app coordinator가 core contract를 기준으로 루틴과 운동 feature를 조립한다.

리스크:

- Activity scope로 넓어지므로 루틴 state lifetime이 destination보다 길어진다.
- 기존 `TrainingViewModel`도 같은 패턴이므로 앱 내 일관성은 높다.
- 루틴 feature entry가 Activity 외 context에서 preview/test로 호출될 경우 fallback이 동작한다.
- `RoutineSessionCoordinator`에는 일부 UI intent 성격의 메서드가 포함되어 있다. 장기적으로는 app-level coordinator contract와 순수 domain policy를 더 나눌 수 있다.

### 9.2 커스텀 루틴 관찰 local-first화
변경 파일:

- `feature/routine/data/src/main/java/com/smarttrainner/feature/routine/data/DefaultRoutinePlanRepository.kt`

변경:

- `observeCustomRoutines()`에서 `onStart { syncCustomRoutinesFromServer(sessionId) }` 제거.
- 서버 동기화는 기존 `syncPendingTrainingData()` 경로에 맡긴다.

기대 효과:

- 루틴 템플릿 첫 emission이 네트워크에 의존하지 않는다.
- offline/느린 네트워크 상황에서도 seed + 로컬 커스텀 루틴을 먼저 보여준다.
- 서버 결과는 DB 갱신 후 Room emission으로 반영된다.

리스크:

- 앱 진입 직후 sync가 아직 끝나지 않았다면 서버의 최신 커스텀 루틴이 몇 초 늦게 보일 수 있다.
- 그러나 이것은 local-first 앱의 정상 동작이며, "화면 첫 렌더링"과 "원격 최신성"을 분리하는 의도된 trade-off다.

### 9.3 불필요한 주간 로그 Flow 제거
변경 파일:

- `feature/routine/impl/src/main/java/com/smarttrainner/feature/routine/impl/RoutineViewModel.kt`
- `feature/routine/impl/src/test/java/com/smarttrainner/feature/routine/impl/RoutineViewModelTest.kt`

변경:

- `RoutineViewModel` 생성자에서 `ObserveWorkoutLogsUseCase` 제거.
- `logState`에서 `observeWorkoutLogs(weekStart)` 제거.
- `RoutineLogState.weeklyLogs` 제거.
- 테스트 factory에서 제거된 의존성 삭제.

기대 효과:

- 루틴 첫 구독 시 Room 주간 로그 쿼리 1개를 제거한다.
- week boundary refresh가 실제로 필요한 plan/progress 쪽에만 영향을 준다.
- UI 입력과 실제 사용 데이터가 맞아진다.

리스크:

- 특정 UI가 주간 로그만 의도했는데 전체 로그를 쓰고 있을 가능성은 남아 있다.
- 현재 코드상 `RoutineUiState.logs`는 전체 로그를 기반으로 루틴 완료 여부와 표시 로그를 계산하고 있었으므로 동작 의미는 유지된다.

### 9.4 최신 로그 DAO 쿼리 추가
변경 파일:

- `core/database/src/main/java/com/smarttrainner/core/database/WorkoutLogDao.kt`
- `core/data/src/main/java/com/smarttrainner/core/data/DefaultWorkoutLogRepository.kt`
- `core/database/src/test/java/com/smarttrainner/core/database/WorkoutLogDaoTest.kt`

변경:

- `WorkoutLogDao.observeLatestByExerciseForSession(sessionId)` 추가.
- `DefaultWorkoutLogRepository.observeLatestWorkoutLogs()`가 전체 로그를 읽은 뒤 `distinctBy` 하지 않고 새 DAO 쿼리를 사용.
- DAO 테스트로 session별 isolation, 운동별 최신 row 선택, relation set log 포함을 검증.

기대 효과:

- 최신 로그 구독의 반환 row 수를 전체 로그 수에서 운동 종류 수로 줄인다.
- domain model mapping 비용을 줄인다.
- 전체 로그 Flow와 최신 로그 Flow가 동일한 전체 데이터셋을 중복 로드하던 문제를 완화한다.

리스크:

- SQL tie-breaker가 기존 `distinctBy`의 ordering과 달라질 수 있다.
- 기존 `observeAll()`은 `performedAt DESC`만 정렬했다. 새 쿼리는 같은 `performedAt`일 때 `id DESC`를 추가해 더 결정적으로 만든다.
- schema 변경이 없으므로 migration 리스크는 없다.

## 10. 후속 개선 계획
### Phase 2: 루틴 UI state 분리
현재 `RoutineUiState`는 화면 전체가 하나의 큰 상태로 combine된다. 작은 데이터 변화가 전체 state recomposition을 유발할 수 있다.

개선 후보:

- `RoutineHomeSummaryState`
- `RoutinePlanListState`
- `RoutineLibraryState`
- `RoutineDialogState`

각 route가 필요한 state만 collect하도록 분리한다.

### Phase 3: 로그 쿼리 목적별 분리
현재 루틴 완료 계산은 전체 로그를 많이 참조한다. 로그가 누적되면 전체 로그 Flow 자체가 부담이 된다.

개선 후보:

- 현재 루틴 cycle/day에 필요한 로그만 가져오는 DAO 추가.
- `routineDayInstanceId` 기반 완료 로그 query 추가.
- 루틴 completion 계산에 필요한 projected row만 반환하는 lightweight query 도입.

### Phase 4: 이미지 최적화
개선 후보:

- list thumbnail 리소스 생성.
- `drawable-nodpi` 원본 이미지를 상세 화면용으로 제한.
- 리스트에서는 76x84에 맞는 asset/request 사용.
- first composition에서는 placeholder 우선, 이미지 decode는 lazy하게 진행.

### Phase 5: 측정 자동화
개선 후보:

- Macrobenchmark로 루틴 탭 첫 진입 time-to-first-frame 측정.
- `Trace.beginSection`으로 다음 구간 분리:
  - `routine.observe.templates`
  - `routine.observe.progress`
  - `routine.observe.logs.all`
  - `routine.observe.logs.latest`
  - `routine.compose.plan`
  - `routine.media.first_visible`
- Room query count 또는 query duration logging을 debug build에서만 수집.

## 11. 검증 계획
### 단위 테스트
- `:feature:routine:impl:testDebugUnitTest`
  - `RoutineViewModel` 생성자 변경과 상태 계산 유지 검증.

- `:core:database:testDebugUnitTest`
  - 새 DAO 쿼리가 운동별 최신 로그만 반환하는지 검증.

- `:core:data:testDebugUnitTest`
  - repository 변경 compile 및 기존 repository 테스트 확인.

### Lint
- `:feature:routine:impl:lintDebug`
- `:feature:routine:data:lintDebug`
- `:core:data:lintDebug`
- `:core:database:lintDebug`

### 빌드
- `:app:assembleDebug`

### 수동 확인
- 앱 cold start 후 홈 진입.
- 루틴 탭 첫 진입.
- 커스텀 루틴이 로컬에 있을 때 즉시 표시되는지 확인.
- 네트워크가 느리거나 실패해도 seed 루틴과 로컬 커스텀 루틴이 표시되는지 확인.
- 운동 기록이 여러 개 쌓인 상태에서 최신 기록 표시가 기존과 동일한지 확인.

## 12. 수용 기준
- 루틴 템플릿 관찰 Flow가 서버 동기화를 첫 emission 전에 실행하지 않는다.
- 홈/루틴/운동 탭이 같은 Activity scope `RoutineViewModel`을 사용한다.
- app의 workout/exercise 조율 코드는 `feature:routine:api.RoutineRouteState`의 세션 메서드가 아니라 `core:domain.RoutineSessionCoordinator`를 사용한다.
- `RoutineViewModel`이 UI에서 사용하지 않는 주간 로그 Flow를 구독하지 않는다.
- 최신 로그 Flow가 전체 로그를 로드한 뒤 앱 메모리에서 `distinctBy` 하지 않는다.
- 서버 API contract 변경이 없다.
- 관련 단위 테스트가 통과한다.

## 13. 사용자 질문에 대한 기술 답변
"앱에서는 DB를 가지고 화면을 그리는 것이 아닌가?"라는 질문에 대한 답은 "맞다"다.

다만 local-first 구조에서 중요한 것은 단순히 최종 데이터 출처가 DB인지가 아니라, 첫 화면을 그리는 Flow가 어떤 선행 작업을 기다리는지다.

좋은 local-first 흐름:

```text
UI subscribes
  -> DB emits local data immediately
  -> UI renders
  -> background sync updates DB
  -> DB emits updated data
  -> UI updates
```

문제가 되는 흐름:

```text
UI subscribes
  -> Flow onStart runs network sync
  -> DB emits local data after network completes/fails
  -> UI renders
```

두 흐름 모두 "화면은 DB를 보고 그린다"고 말할 수 있지만, 체감 성능은 크게 다르다. 이번 개선은 루틴 첫 진입을 첫 번째 흐름으로 되돌리는 데 초점을 둔다.
