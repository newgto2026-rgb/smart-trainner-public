# Smart Trainner AI 기반 개발 사례 소개서

## 1. 한 줄 소개

Smart Trainner는 AI를 단순 코드 생성 도구로 쓰는 대신, 제품 정책 정리, Android 멀티모듈 구현, 테스트, 리뷰 대응, 문서화를 함께 수행하는 AI-assisted development 방식으로 발전한 Android 앱이다.

이 프로젝트에서 AI가 특히 도움을 준 지점은 운동 도메인의 애매한 정책을 코드로 옮기기 전에 문서와 테스트로 고정하는 일이다. 예를 들어 루틴 일차의 수행 날짜, 사이클 완료 조건, 운동 이미지 품질, 커스텀 루틴 구조는 대화와 PRD/TRD를 통해 먼저 정리되고, 이후 feature/domain/data/UI 변경으로 이어졌다.

## 2. AI 개발에서 중요한 프로젝트 특성

Smart Trainner는 AI가 안정적으로 작업하기 좋은 구조를 갖추고 있다.

| 구조 | AI 개발에서의 장점 |
|---|---|
| 멀티모듈 Gradle | 변경 범위를 module 단위로 좁히고 검증 명령을 고르기 쉬움 |
| `core:model` 중심 domain model | 운동/루틴/기록/분석 모델을 여러 feature가 공유 |
| feature `api/domain/data/impl` 분리 | route 계약, 정책, 저장소 구현, UI를 분리해 영향 범위가 선명 |
| use case 중심 정책 | 루틴 추천/완료/날짜 제약 같은 판단을 화면 밖에서 테스트 가능 |
| Room/DataStore/Network 분리 | 로컬 저장, preference, 서버 계약을 따로 읽고 수정 가능 |
| Compose ViewModel 테스트 | UI 상태 변화를 화면 렌더링과 분리해 검증 가능 |
| PRD/TRD/QA 문서 | 긴 작업의 제품 의도와 기술 판단을 다음 세션이 이어받기 쉬움 |
| `AGENTS.md` | 작업 시작, 서버 계약 확인, 모듈 경계, 검증 명령을 명시 |

AI는 경계가 흐릿한 코드보다, 어디를 읽고 어디를 바꿔야 하는지 명확한 코드에서 훨씬 안정적으로 움직인다. Smart Trainner의 모듈 구조와 문서들은 그 작업 레일 역할을 한다.

## 3. AI가 맡은 역할

### 3.1 제품 정책 정리

운동 앱은 표면적으로는 "운동을 기록한다"는 단순한 문제처럼 보이지만 실제 정책은 더 복잡하다.

| 정책 질문 | Smart Trainner에서의 처리 |
|---|---|
| 운동 입력 시각과 수행 날짜가 다르면 무엇을 믿을 것인가? | 루틴 일차 지정 날짜를 수행 날짜로 사용 |
| 같은 날 1일차와 2일차를 모두 수행해도 되는가? | 루틴 일차는 다른 날짜여야 하며 필요하면 같은 일차에 운동을 추가 |
| 이전 일차보다 빠른 날짜로 다음 일차를 지정할 수 있는가? | 날짜 제약으로 차단 |
| 다른 루틴/사이클 기록이 현재 완료 계산에 섞일 수 있는가? | current template/cycle routine day prefix로 제한 |
| 운동을 추가/대체하면 완료 계산은 어떻게 되는가? | planned exercise와 routine-added id를 분리해 처리 |

이런 정책은 대화로 먼저 명확히 한 뒤 `RoutinePolicyUseCases`, `RoutineViewModel`, `DefaultRoutineProgressRepository`, `WeeklySummaryCalculator` 같은 코드에 반영됐다.

### 3.2 코드베이스 탐색자

AI 작업은 보통 다음 흐름으로 진행된다.

```text
요구사항 확인
  -> AGENTS.md와 settings.gradle.kts 확인
  -> 영향 feature/core 모듈 식별
  -> 기존 use case, repository, ViewModel, test 패턴 확인
  -> 필요한 경우 companion server repo 확인
  -> 변경 범위와 검증 명령 결정
```

이 탐색 단계는 Android 멀티모듈 프로젝트에서 특히 중요하다. feature UI가 data implementation에 직접 의존하거나, core가 feature를 참조하는 식의 빠른 수정은 나중에 구조 회귀가 된다. Smart Trainner는 `checkModuleBoundaries`가 이 위험을 실행 가능한 검증으로 막는다.

### 3.3 설계-구현-검증 루프

Smart Trainner의 AI-assisted 작업은 다음 루프를 따른다.

```text
Plan
  -> 제품 정책과 비목표 정리
Explore
  -> 관련 모듈과 테스트 확인
Implement
  -> 모듈 경계 안에서 코드/문서 변경
Verify
  -> 영향 모듈 test/lint/build/UI test 실행
Review
  -> PR 설명, 리뷰 대응, rework 기록
```

이 루프는 "AI가 코드를 많이 쓴다"보다 "AI가 변경을 끝까지 검증 가능한 단위로 닫는다"에 가깝다.

## 4. 대표 AI 협업 사례

### 4.1 커스텀 루틴

커스텀 루틴은 제품 요구사항과 기술 요구사항이 함께 필요했던 기능이다.

```text
CustomRoutineInput
  -> CustomRoutineEntity/Day/Exercise
  -> Room DAO
  -> RoutineNetworkApi
  -> Routine repository
  -> Routine builder UI
```

AI는 PRD/TRD로 기능 범위와 데이터 구조를 정리하고, 이후 domain model, Room entity, mapper, repository, builder UI, 테스트를 연결하는 흐름을 도왔다.

### 4.2 루틴 진행과 일차 날짜

루틴 진행은 Smart Trainner에서 가장 중요한 정책 영역이다.

```text
RoutineProgress
  + cycleNumber
  + dayIndex
  + cycleStartedAt
  + routineDayDates
  + routineDayInstanceId
```

AI는 사용자의 실제 운동 입력 방식, 즉 "운동을 몰아서 입력할 수 있다"는 문제를 정책으로 바꾸는 데 참여했다. 결과적으로 운동 기록은 루틴 일차 날짜를 따르고, 분석과 완료 계산은 같은 template/cycle의 routine day log만 사용한다.

### 4.3 운동 이미지 품질 관리

운동 카탈로그는 텍스트만으로 충분하지 않다. Smart Trainner는 운동별 단계 이미지와 thumbnail을 포함하고, 이미지 품질 문서와 audit script를 통해 일관성을 유지한다.

| 산출물 | 역할 |
|---|---|
| `docs/exercise-image-main-standard.md` | 이미지 기준 |
| `docs/exercise-art-quality-gate.md` | 품질 게이트 |
| `docs/exercise-image-full-audit-20260521.md` | 전수 감사 기록 |
| `scripts/audit_exercise_images.*` | 이미지 누락/일관성 검사 |
| `core:exercise-media` | 앱에서 사용할 이미지 mapping |

AI는 이미지 생성/교체/검수 과정에서도 "보이는 결과"와 "코드 리소스 연결"이 함께 맞는지 확인하는 역할을 했다.

### 4.4 리뷰 재작업 관측성

Smart Trainner에는 `docs/ai-rework/branches`가 있다. 리뷰나 CI에서 후속 작업이 생기면 어떤 피드백이 있었고, 어떤 커밋에서 처리됐고, 어떤 검증을 돌렸는지를 브랜치 단위로 남긴다.

이 기록은 AI-assisted development에서 중요하다. AI가 빠르게 수정하더라도, 같은 종류의 리뷰가 반복되는지, 어떤 검증이 재작업을 줄였는지, PR 설명과 실제 후속 커밋이 맞는지 추적할 수 있기 때문이다.

## 5. 저장소 하네스

Smart Trainner가 가진 실행 가능한 하네스는 다음과 같다.

| 하네스 | 실제 구성 | 품질 효과 |
|---|---|---|
| 새 작업 bootstrap | `scripts/new-smart-task <task-name>` | 최신 `origin/main` 기반 branch/worktree 생성 |
| main 보호 | `.husky/pre-commit`, `.husky/pre-push` | main/master 직접 commit/push 차단 |
| 영향 lint | `scripts/git-hooks/pre-push.sh` | 변경 파일에 맞는 module lint 또는 전체 lint 실행 |
| 모듈 경계 검사 | `./gradlew checkModuleBoundaries` | Gradle 의존 방향과 금지 import 검사 |
| Android CI | `.github/workflows/android-ci.yml` | unit test, JVM test, build, strict lint |
| Instrumented CI | `.github/workflows/android-instrumented.yml` | emulator 기반 `:app:connectedDebugAndroidTest` |
| PR template | `.github/pull_request_template.md` | 문제/범위/설계/테스트/리스크 기록 |
| 재작업 기록 | `docs/ai-rework/branches` | review/CI follow-up 추적 |

YourTodo처럼 product harness, coverage gate, TDD guard가 완전한 형태로 있는 것은 아니지만, Smart Trainner도 core gate는 이미 갖고 있다. 앞으로는 rework metrics check script, coverage verification, module-local AGENTS.md를 보강하면 AI 작업 안정성이 더 좋아진다.

## 6. AI 개발 방식의 장점

| 장점 | 설명 |
|---|---|
| 정책을 먼저 말로 고정 | 날짜/사이클/완료 같은 애매한 도메인을 구현 전에 명확히 함 |
| 변경 범위가 좁아짐 | feature/domain/data/impl 구조 덕분에 수정 지점이 선명 |
| 테스트가 정책을 보존 | routine completion, analysis summary, repository mapping 테스트가 회귀를 막음 |
| 문서가 다음 세션을 돕음 | PRD/TRD/QA/rework 기록이 AI와 사람이 같은 맥락에서 이어가게 함 |
| 리뷰 대응이 추적 가능 | 피드백, 후속 커밋, 검증 명령을 PR과 문서에 남길 수 있음 |
| 사람 판단이 제품 정책으로 올라감 | 사용자는 구현 세부보다 "운동 기록은 어떤 날짜로 볼 것인가" 같은 판단에 집중 |

## 7. 남은 하네스 보강 기회

Smart Trainner가 YourTodo 수준의 개발 하네스로 더 가까워지려면 다음이 효과적이다.

| 보강 항목 | 기대 효과 |
|---|---|
| module-local `AGENTS.md` | feature/core별 변경 규칙과 검증 명령을 더 빠르게 찾음 |
| `scripts/quality/product-harness-check.sh` | 모듈 가이드, 의존 방향, 문서 인덱스 누락을 자동 검사 |
| `scripts/quality/rework-metrics-check.sh` | PR review thread, PR body, rework docs 불일치 자동 검사 |
| coverage verification | domain/data/ViewModel non-view logic의 테스트 기준을 수치화 |
| 사용자 매뉴얼 PDF | 앱 기능을 외부 공유 가능한 산출물로 정리 |
| 데이터 아키텍처 리포트 | Room/DataStore/Network sync 관계를 신규 작업자가 빠르게 이해 |

## 8. 프로젝트 인사이트

Smart Trainner의 AI-assisted development 인사이트는 단순하다.

> AI에게 더 많은 구현을 맡기려면, 제품 정책과 검증 루프가 더 명확해야 한다.

운동 앱에서 가장 위험한 회귀는 UI가 조금 어긋나는 것만이 아니다. 루틴 일차, 수행 날짜, 사이클, 기록이 서로 다른 기준으로 해석되는 것이 더 큰 문제다. Smart Trainner는 이런 정책을 domain use case, repository, Room/DataStore, 분석 계산, 테스트, 문서로 나누어 보존한다.

AI는 이 구조 안에서 빠르게 탐색하고 수정하고 검증할 수 있다. 사람은 최종적으로 운동 경험의 의미, 예외 정책, 우선순위를 판단한다. 이 역할 분담이 Smart Trainner 개발 방식의 핵심이다.
