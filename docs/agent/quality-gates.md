# Smart Trainner 품질 게이트

이 문서는 Smart Trainner에서 변경 종류별로 어떤 검증을 실행할지 정리한다. 루트 `AGENTS.md`의 검증 명령을 더 실행 중심으로 풀어 쓴 문서다.

## 1. 기본 원칙

- 새 작업은 `scripts/new-smart-task <task-name>`으로 최신 `origin/main` 기반 worktree에서 시작한다.
- `main`/`master` 직접 commit과 push는 금지한다.
- Android `core:network` DTO나 API path가 바뀌면 companion server repo도 같은 작업에서 확인한다.
- Compose UI는 `UiState`를 렌더링하고 user event를 ViewModel/use case로 보낸다.
- 사용자 노출 문자열은 `values`와 `values-ko` resource에 둔다.
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
| core user flow 또는 UI test harness 변경 | `./gradlew connectedDebugAndroidTest` |

## 3. 공통 게이트

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

## 4. CI 구성

GitHub Actions는 두 workflow를 사용한다.

| Workflow | Trigger | 실행 내용 |
|---|---|---|
| Android CI | 모든 PR | `testDebugUnitTest`, `test`, `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, `lint` |
| Android Instrumented Tests | app/feature/core main 또는 androidTest 등 UI 영향 PR | emulator에서 `:app:connectedDebugAndroidTest` |

## 5. 로컬 Hook

| Hook | 역할 |
|---|---|
| `.husky/pre-commit` | `main`/`master` 직접 commit 차단 |
| `.husky/pre-push` | `main`/`master` 직접 push 차단, 변경 파일 기준 lint 실행 |

pre-push는 변경 파일을 보고 module lint를 고른다. Gradle 설정, workflow, hook, 일부 core module 변경은 전체 `lint`로 폴백한다.

## 6. 리뷰/CI 재작업 기록

리뷰 피드백이나 CI 실패로 후속 수정이 생기면 `docs/ai-rework/branches/<branch>.md`에 기록한다.

기록할 최소 항목:

- 피드백 또는 실패 원인
- 수정한 파일과 정책
- follow-up commit
- 실행한 검증 명령
- 남은 리스크

현재 Smart Trainner에는 YourTodo처럼 `scripts/quality/rework-metrics-check.sh`가 없으므로, 재작업 문서는 수동으로 정합성을 확인한다.

## 7. PR 설명 체크리스트

PR에는 다음 내용을 남긴다.

- 문제와 제품 gap
- 변경 범위와 out of scope
- 모듈 경계/서버 계약 영향
- 주요 파일과 변경 이유
- 동작 전/후
- 자동/수동 검증 명령과 결과
- migration 또는 release note 필요 여부
- rollback plan

이 체크리스트는 AI와 사람이 같은 품질 기준으로 PR을 읽게 해준다.
