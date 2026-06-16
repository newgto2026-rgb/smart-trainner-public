# Smart Trainner 품질 게이트

이 문서는 Smart Trainner에서 변경 종류별로 어떤 검증을 실행할지 정리한다. 루트 `AGENTS.md`의 검증 명령을 더 실행 중심으로 풀어 쓴 문서다.

## 1. 기본 원칙

- 새 작업은 `scripts/new-smart-task <task-name>`으로 최신 `origin/main` 기반 worktree에서 시작한다.
- `main`/`master` 직접 commit과 push는 금지한다.
- Android `core:network` DTO나 API path가 바뀌면 companion server repo도 같은 작업에서 확인한다.
- Compose UI는 `UiState`를 렌더링하고 user event를 ViewModel/use case로 보낸다.
- 사용자 노출 문자열은 `values`와 `values-ko` resource에 둔다.
- 사용자에게 보이는 기능 플로우를 새로 만들거나 바꾸면, 사소한 플로우라도 기본적으로 UI 테스트를 함께 작성한다.
- 기능 설명, PR 설명, 화면에 드러난 플로우는 모두 UI 테스트 커버리지 대상이다. 단위 테스트는 UI에서 경험되는 흐름의 대체물이 아니다.
- 문서만 바뀐 경우에는 Gradle 전체 검증보다 markdown sanity와 변경 범위 확인을 우선한다.

## 2. 변경별 추천 검증

| 변경 범위 | 추천 검증 |
|---|---|
| 문서만 변경 | `git diff --check` |
| root Gradle/settings/module boundary 변경 | `./gradlew checkModuleBoundaries` + `./gradlew lint` |
| app shell/navigation/profile 변경 | `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` |
| core domain/model 변경 | `./gradlew :core:domain:test` + 영향 feature tests |
| core database 변경 | `./gradlew :core:database:testDebugUnitTest` + schema/migration 확인 |
| core datastore 변경 | `./gradlew :core:datastore:testDebugUnitTest` |
| core network DTO/API 변경 | `./gradlew :core:network:testDebugUnitTest` + companion server 확인 |
| routine domain 변경 | `./gradlew :feature:routine:domain:test` |
| routine data 변경 | `./gradlew :feature:routine:data:testDebugUnitTest :feature:routine:data:lintDebug` |
| routine UI/ViewModel 변경 | `./gradlew :feature:routine:impl:testDebugUnitTest :feature:routine:impl:lintDebug` |
| workout data/UI 변경 | `./gradlew :feature:workout:data:testDebugUnitTest :feature:workout:impl:testDebugUnitTest` |
| exercise catalog/detail 변경 | `./gradlew :feature:exercise:impl:testDebugUnitTest :feature:exercise:impl:lintDebug` |
| analysis 변경 | `./gradlew :feature:analysis:domain:test :feature:analysis:impl:testDebugUnitTest` |
| 신규/변경 사용자 플로우 | 해당 플로우 UI 테스트 추가 + `./gradlew connectedDebugAndroidTest` |
| core user flow 또는 UI test harness 변경 | `./gradlew connectedDebugAndroidTest` |

## 3. UI 테스트 필수 기준

기능을 만들거나 바꿀 때는 구현 전에 사용자가 실제로 밟는 플로우를 먼저 나열하고, 그 플로우를 UI 테스트로 검증한다. 작은 버튼, 상태 전환, 필터, 저장/취소, 완료 처리처럼 사소해 보이는 흐름도 빠뜨리지 않는다.

기본 커버리지 대상:

- 정상 플로우와 주요 분기
- 화면 진입, 뒤로가기, 탭/라우트 이동
- 입력 검증, 비활성/활성 상태, 저장과 취소
- loading, empty, error 상태
- 완료, 삭제, 선택, 필터링, 정렬처럼 화면에서 결과가 바뀌는 동작
- 여러 화면을 넘나드는 상태 유지와 갱신

UI 테스트가 빠져도 되는 예외는 "기술적으로 지금 자동화가 불가능한 경우"뿐이다. 이때도 테스트 하네스를 같은 작업에서 보강하는 것을 우선하고, 그래도 막히면 PR 설명에 빠진 플로우, 이유, 후속 작업을 명시한다.

## 4. 공통 게이트

PR 전에 변경 폭이 크거나 여러 모듈에 걸치면 다음 Android gate를 실행한다.

```sh
./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

전체 Android unit/JVM/lint가 필요한 경우:

```sh
./gradlew testDebugUnitTest
./gradlew test
./gradlew lint
```

UI regression이 중요한 경우:

```sh
./gradlew connectedDebugAndroidTest
```

## 5. CI 구성

GitHub Actions는 두 workflow를 사용한다.

| Workflow | Trigger | 실행 내용 |
|---|---|---|
| Android CI | 모든 PR | `testDebugUnitTest`, `test`, `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, `lint` |
| Android Instrumented Tests | app/feature/core main 또는 androidTest 등 UI 영향 PR | emulator에서 `:app:connectedDebugAndroidTest` |

## 6. 로컬 Hook

| Hook | 역할 |
|---|---|
| `.husky/pre-commit` | `main`/`master` 직접 commit 차단 |
| `.husky/pre-push` | `main`/`master` 직접 push 차단, 변경 파일 기준 lint 실행 |

pre-push는 변경 파일을 보고 module lint를 고른다. Gradle 설정, workflow, hook, 일부 core module 변경은 전체 `lint`로 폴백한다.

## 7. 리뷰/CI 재작업 기록

리뷰 피드백이나 CI 실패로 후속 수정이 생기면 `docs/ai-rework/branches/<branch>.md`에 기록한다.

기록할 최소 항목:

- 피드백 또는 실패 원인
- 수정한 파일과 정책
- follow-up commit
- 실행한 검증 명령
- 남은 리스크

현재 Smart Trainner에는 YourTodo처럼 `scripts/quality/rework-metrics-check.sh`가 없으므로, 재작업 문서는 수동으로 정합성을 확인한다.

## 8. PR 설명 체크리스트

PR에는 다음 내용을 남긴다.

- 문제와 제품 gap
- 변경 범위와 out of scope
- 모듈 경계/서버 계약 영향
- 주요 파일과 변경 이유
- 동작 전/후
- 신규/변경 사용자 플로우와 해당 UI 테스트
- 자동/수동 검증 명령과 결과
- migration 또는 release note 필요 여부
- rollback plan

이 체크리스트는 AI와 사람이 같은 품질 기준으로 PR을 읽게 해준다.
