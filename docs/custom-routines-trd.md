# 커스텀 루틴 TRD

## 1. 기술 목표
커스텀 루틴을 Android local-first 기능으로 구현하고, 서버에는 같은 개념을 저장/조회/선택할 API를 추가한다. Android는 기존 `PlanTemplate`, `WeeklyPlan`, `RoutineProgress` 흐름을 재사용하되 `RoutineSource`와 Room 저장소를 추가해 기본 루틴과 내 루틴을 분리한다.

## 2. 영향 모듈
- `:core:model`: `RoutineSource`, custom routine input model.
- `:core:domain`: custom routine validation/save/delete use cases, repository contract.
- `:core:database`: custom routine Room tables, DAO, v4 migration.
- `:core:data`: seed templates + custom templates merge, weekly plan id strategy.
- `:feature:training:impl`: builder state, ViewModel events, custom routine editor UI, resources.
- `:app`: connected UI regression test.
- Server: `src/server/types.ts`, `store.ts`, `validation.ts`, `customRoutines.ts`, `app/api/routines/**`, tests.

## 3. Android 모델 결정
- `PlanTemplate.source: RoutineSource = SYSTEM`을 추가한다.
- 커스텀 루틴은 Room에서 읽어 `PlanTemplate(source = CUSTOM)`로 매핑한다.
- 추천 알고리즘은 `RoutineSource.SYSTEM`만 후보로 사용한다.
- `CustomRoutineInput`, `CustomRoutineDayInput`, `CustomRoutineExerciseInput`은 domain save use case와 ViewModel form 사이의 계약으로 사용한다.
- 커스텀 일차의 `primaryFocus`는 nullable로 둔다. 저장 값이 없거나 legacy implicit `FULL_BODY`이면 `null`로 매핑해 앱이 임의 주요 부위를 표시하지 않는다.

## 4. 저장소 결정
Android:
- Room v4에 `custom_routines`, `custom_routine_days`, `custom_routine_exercises`를 추가한다.
- active routine progress는 기존 DataStore 키를 유지한다.
- active custom routine이 삭제되면 repository는 기본 템플릿으로 fallback한다.

Server:
- 기존 JSON DB에 `customRoutines`, `customRoutineSelections`를 추가한다.
- route handler는 얇게 두고 로직은 `src/server/customRoutines.ts`에 둔다.
- response envelope은 기존 규칙대로 single `{ data }`, list `{ data, count }`를 유지한다.

## 5. ID 전략
기본 루틴은 기존 `date_exerciseId`를 유지한다.

커스텀 루틴은 같은 날짜에 같은 운동이 여러 번 있어도 충돌하지 않도록:

```text
${date}_${templateId}_day${dayIndex + 1}_slot${slotIndex + 1}_${exerciseId}
```

이 id는 workout log의 `plannedExerciseId` unique index와 충돌하지 않는다.

## 6. Domain Validation
`ValidateCustomRoutineUseCase` 규칙:
- 루틴명 1~60자.
- 1~7일차.
- 각 일차 최소 1개 운동.
- 운동 id는 catalog에 존재해야 함.
- 세트 1~12.
- 반복 범위 1~50, 시작 <= 끝.
- 시간 1~240분.
- 휴식 0~600초.
- reps 또는 duration 중 하나는 있어야 함.

ViewModel은 오류를 UI 상태로 매핑하지만 정책 자체는 domain use case가 소유한다.

## 7. UI 구조
- Plan 탭 상단 `현재 루틴` 카드에 source badge를 표시한다.
- 현재 선택된 custom routine의 현재 루틴 카드에는 `수정` 액션을 표시하고, 해당 template id로 builder를 다시 연다.
- Plan 탭 본문은 현재 루틴 카드, `루틴 바꾸기`, `내 루틴 만들기`, 루틴 일정만 렌더링한다.
- `내 루틴` 목록, `기본 루틴` 목록, 기본 루틴 복사 액션은 `RoutineLibraryDialog` 안으로 이동한다.
- `내 루틴` 카드에는 선택과 별도로 `수정` 액션을 제공한다. 수정은 기존 `PlanTemplate(source = CUSTOM)`를 builder state로 역매핑하고 같은 routine id로 저장한다.
- 추천 기반 루틴 찾기는 `RoutineLibraryDialog`에서 `추천으로 찾기`를 누르면 기존 settings/recommendations dialog 흐름으로 이어진다.
- 커스텀 편집기는 Plan 탭에서 여는 full-height editor surface로 구현한다.
- 편집기는 루틴명, day chips, 현재 day 주요 부위 선택, 현재 day 운동 목록, 운동 추가, 순서 이동, 일차 삭제, 저장 액션을 가진다.
- 현재 day 주요 부위 선택은 항상 모든 옵션을 펼치는 chip grid가 아니라 한 줄짜리 선택 surface와 full-width dropdown menu로 구현한다.
- 새로 만드는 custom day는 기본 운동 없이 빈 목록과 `주요 부위: 설정 안 함`으로 시작한다.
- `저장하고 시작`은 빌더에서 제공하지 않는다. 저장 성공 시 빌더는 닫히며, 시작은 `RoutineLibraryDialog`에서 저장된 내 루틴 카드를 선택하는 기존 start routine 흐름을 사용한다.
- 커스텀 루틴에서 주요 부위가 없는 day는 홈의 주요 부위 섹션과 `오늘은 전신 루틴`류 제목을 렌더링하지 않고 day title만 보여준다.
- 커스텀 루틴은 session duration을 추정해 표시하지 않는다. 카드 meta는 일차 구성만 보여주고, 홈 day subtitle에는 `약 N분`을 넣지 않는다.
- 커스텀 루틴 focus flow는 설정된 주요 부위가 있는 day만 포함하며, 아무 day에도 주요 부위가 없으면 flow row를 숨긴다.
- 커스텀 루틴 focus flow는 한 줄 문자열 말줄임이 아니라 일차별 칩 행으로 표시한다. 4일 이상 구성도 현재 루틴 카드와 내 루틴 카드에서 모든 설정 일차가 보이도록 한다.
- 시스템 루틴 focus flow는 기존처럼 compact summary text를 유지한다.
- 운동 추가 후보는 현재 선택된 day에 없는 운동만 보여준다.
- 현재 day에 주요 부위가 설정되어 있으면 후보 운동과 add 이벤트는 `RoutineFocus -> MuscleGroup` 매핑에 맞는 운동만 허용한다. `설정 안 함`은 전체 카탈로그, `상체`는 등/가슴/어깨/팔, `유산소/컨디셔닝`은 유산소로 매핑한다.
- 현재 day의 주요 부위가 변경되면 이미 추가된 운동도 같은 매핑으로 필터링한다. 새 주요 부위에 속한 운동은 유지하고, 벗어나는 운동만 제거한다. `설정 안 함`은 전체 그룹을 허용하므로 남아 있는 운동을 추가로 제거하지 않는다.
- 운동 추가 후보는 `MuscleGroup` 순서대로 그룹화하고 모든 그룹은 기본 접힘 상태다.
- 그룹 펼침 상태는 ViewModel의 `CustomRoutineBuilderState.expandedExerciseGroups`가 소유하며 day 전환/추가/삭제/주요 부위 변경 시 접힌 상태로 초기화한다.
- 운동 후보 행은 행 전체 탭으로 추가를 유지하고, 별도 `운동 방법` 버튼은 `showExerciseMethod(exerciseId)`로 기존 운동 상세 팝업을 연다.
- 커스텀 편집 중 운동 상세가 열리면 builder surface를 유지하고, `plannedExercise = null`로 전달해 기록 시작 액션을 노출하지 않는다.
- 운동 상세 다이얼로그는 커스텀 빌더보다 나중에 compose해 빌더 위에 오버레이로 띄운다.
- `ExerciseDetailContent`는 대표 이미지와 단계 이미지 탭 이벤트를 처리해 확대 이미지 다이얼로그를 띄운다.
- 같은 일차에 이미 들어간 운동을 다시 추가하려는 이벤트는 ViewModel에서 무시한다.
- 기본 루틴은 직접 편집하지 않고 복사해서 custom routine으로 저장한다.
- 홈 다음 운동 카드의 `NextRoutineDayUiModel.previewExercises`는 더 이상 3개 샘플이 아니라 해당 day의 전체 planned exercises를 담는다. UI 문구는 `오늘 운동 계획`으로 표시한다.

Dialog state:
- `showRoutineLibraryDialog`: 내 루틴/기본 루틴 목록 선택 팝업.
- `showRoutineSettingsDialog`: 추천 조건 입력 팝업.
- `showRoutineRecommendationsDialog`: 추천 결과/미리보기 팝업.
- 하나의 선택 흐름에서 다음 팝업으로 이동할 때 이전 팝업은 닫아 겹쳐 보이지 않게 한다.

## 8. Server API
- `GET /api/routines`
- `POST /api/routines`
- `GET /api/routines/selected`
- `GET /api/routines/:id`
- `PATCH /api/routines/:id`
- `DELETE /api/routines/:id`
- `POST /api/routines/:id/select`

Session resolution:
- `x-smart-trainner-session-id` 우선.
- 없으면 `sessionId` query.
- 없으면 `local-default`.

Validation:
- unknown exerciseId는 400.
- 다른 세션의 routine은 404.
- duplicate day, empty exercises, invalid set/reps/duration은 400.

## 9. 테스트 계획
Android:
- `:core:domain:test`: custom routine validation, recommendation excludes custom routines.
- `:core:database:testDebugUnitTest`: DAO CRUD, migration/schema.
- `:core:data:testDebugUnitTest`: seed+custom merge, custom planned id stability, delete fallback, nullable custom focus/legacy `FULL_BODY` hiding.
- `:feature:training:impl:testDebugUnitTest`: builder save/start separation, save-success builder dismissal, empty custom day default, optional day focus selection/clearing, focus-to-muscle candidate filtering, existing exercise pruning on focus changes, edit existing custom routine, source badges, custom next day, routine library dialog visibility/dismiss behavior, duplicate exercise prevention, exercise reorder, exercise group expansion state, next routine day includes every planned exercise.
- `connectedDebugAndroidTest`: Plan 탭에 전체 루틴 목록이 직접 노출되지 않는지 확인, routine library dialog에서 추천 찾기 진입, create 4-day custom routine, compact focus dropdown/no full-body focus option, empty default day/no save-start button, optional focus selection, focused candidate filtering, focus-change pruning of already-added out-of-focus exercises, edit saved custom routine from my routines and current routine card, collapsed exercise group expansion, 커스텀 후보 운동 방법 팝업(기록 액션 없음), 운동 이미지 확대 뷰어, already-added candidate hiding, reorder arrows, save closes builder, select saved custom routine, custom routine flow shows all 4 configured days, home shows 내 루틴 without estimated duration, `오늘 운동 계획` shows all exercises for the day, complete day moves to 2일차 with configured focus.
- `connectedDebugAndroidTest`는 Hilt에서 app-owned production repository binding module을 제거하고 인메모리 repository fake를 사용한다. 테스트 초기화는 fake 상태만 reset하며 실제 앱 Room/DataStore를 clear하지 않는다.
- Gradle connected test execution must leave installed APKs in place after the run (`android.injected.androidTest.leaveApksInstalledAfterRun=true`) because the default UTP teardown can uninstall the target app and wipe developer/emulator Room/DataStore data.

Server:
- `npm test`: custom routine service and route behavior.
- `npm run lint`.
- `npm run build`.

## 10. Rollback
- Android rollback: hide custom routine CTA and keep system routines. Existing DataStore progress keys remain compatible.
- Server rollback: remove `/api/routines/**` routes. Existing sessions/workout logs/plans contracts are unaffected.
