# 루틴 사이클 동기화 PRD

## 1. 제품 정의
Smart Trainner의 루틴 진행은 주차가 아니라 `사이클 + n일차`를 기준으로 관리한다. 사용자는 루틴을 한 바퀴 도는 단위를 사이클로 이해하고, 앱은 서버를 최종 기준으로 여러 디바이스의 기록과 진행도를 같은 루틴 슬롯 단위로 정리한다.

핵심 포지셔닝:

> Smart Trainner는 사용자가 요일이나 주차에 묶이지 않고 루틴을 사이클 단위로 이어가도록 돕고, 여러 디바이스에서 기록이 달라져도 서버 기준으로 안전하게 동기화하는 운동 기록 앱이다.

## 2. 배경과 문제 정의
기존 `1일차~N일차` 진행은 요일 고정 루틴보다 유연하지만, 계정 로그인과 서버 동기화가 들어오면 다음 문제가 생긴다.

- 사용자는 3일 루틴을 꼭 1주 안에 끝내지 않는다.
- 어떤 사이클은 3일로 끝나고, 다음 사이클은 4일 구성이 될 수 있다.
- 두 디바이스에서 같은 루틴 1일차를 각각 기록하면 운동을 두 번 한 것처럼 부풀 수 있다.
- 두 디바이스의 진행도가 다를 때 앱이 임의로 현재 일차를 밀거나 되돌리면 루틴이 꼬인다.
- 현재 서버 백업은 단건 workout log 중심이고, 안정적인 사이클/일차/슬롯 충돌 정책이 없다.
- 오프라인 기록 후 로그인하거나 네트워크가 복구되면 사용자는 기록이 사라졌는지, 교체됐는지, 서버와 맞춰졌는지 알아야 한다.

## 3. 에이전트 논의 요약
이 PRD는 제품/UX, Android 앱, 서버 관점의 병렬 리뷰를 반영한다.

### 제품/UX 관점
- `사이클`은 루틴을 한 바퀴 수행하는 단위로, `주차`보다 실제 운동 리듬을 잘 담는다.
- 충돌을 사용자에게 너무 자주 보여주면 피로해지므로 대부분은 서버 기준으로 자동 정리한다.
- 단, 기록이 교체되거나 사용자가 손실로 느낄 수 있는 경우에는 `동기화 검토` 알림과 변경 요약을 제공한다.
- 오늘 루틴은 강제 체크리스트가 아니라 기본 계획이어야 한다.

### Android 관점
- 앱 로컬 DB는 최종 원본이 아니라 캐시와 오프라인 작업 큐여야 한다.
- 현재 DataStore 중심 진행 상태만으로는 서버 동기화에 약하다.
- `RoutineCycle`, `WorkoutLog`, `SyncMutation`, `SyncState`는 Room 모델로 관리하는 편이 안전하다.
- WorkManager 기반 동기화가 필요하며, 로그인, 앱 실행, 네트워크 복구, 기록 저장, 일차 완료 시 예약되어야 한다.
- `plannedExerciseId`는 날짜 기반이 아니라 루틴 스냅샷 안에서 안정적인 슬롯 id여야 한다.

### 서버 관점
- 서버가 SSOT가 되려면 `RoutineCycle`을 1급 엔티티로 올려야 한다.
- 현재 `sessionId + plannedExerciseId` 업서트는 사이클이 반복될 때 다른 사이클 기록까지 덮을 수 있다.
- 서버의 canonical key는 `userId + routineCycleId + dayIndex + routineExerciseSlotId`가 되어야 한다.
- 단건 백업 API만으로는 부족하고, batch push, delta pull, revision, cursor, item-level sync result가 필요하다.
- `x-smart-trainner-session-id`만 신뢰하는 방식은 장기적으로 약하므로 인증된 계정 소유권과 서버 세션을 묶어야 한다.

## 4. 핵심 용어
- `사이클`: 루틴을 한 바퀴 수행하는 단위. 예: `3사이클`.
- `n일차`: 사이클 안의 루틴 구성 단위. 예: `3사이클 2일차`.
- `루틴 템플릿`: 사용자가 선택하거나 만든 루틴 원본.
- `루틴 스냅샷`: 특정 사이클을 시작할 때 고정된 루틴 구성. 이후 템플릿이 바뀌어도 과거 사이클은 이 스냅샷으로 해석한다.
- `일차 슬롯`: 사이클 안의 특정 일차. 예: 2일차.
- `운동 슬롯`: 일차 안에 계획된 특정 운동 자리.
- `계획 운동`: 루틴 스냅샷에 포함된 운동.
- `대체 운동`: 계획 운동 슬롯을 다른 운동으로 수행한 기록.
- `생략`: 계획 운동을 의도적으로 하지 않은 상태.
- `추가 운동`: 루틴 계획에는 없지만 사용자가 추가로 수행한 운동.
- `운동 세션`: 사용자가 특정 사이클/일차에서 실제 수행한 기록 묶음.
- `동기화 검토`: 서버와 디바이스 상태가 달라 사용자에게 변경 요약이나 확인이 필요한 상태.
- `SyncMutation`: 로컬에서 먼저 저장된 사용자의 변경 요청. 사용자에게는 `동기화 대기 중` 또는 `동기화 중` 상태로만 노출한다.

## 5. 핵심 원칙
- 서버가 SSOT다.
- 앱 로컬 데이터는 캐시와 오프라인 작업 큐다.
- 앱은 현재 사이클과 일차를 임의로 확정하지 않고 서버 canonical state를 따른다.
- 운동 기록은 무조건 append하지 않는다.
- 루틴 슬롯에 매핑된 기록은 같은 슬롯이면 서버에서 교체 또는 갱신한다.
- 추가 운동은 계획 슬롯과 분리해 append한다.
- 생략, 대체, 미완료는 서로 다른 상태로 저장한다.
- 충돌이 생겼을 때 서버 기준으로 자동 정리하되, 사용자가 기록 손실처럼 느낄 수 있는 경우에는 알림과 변경 내역을 제공한다.
- 사이클을 판정할 때 단순 `cycleIndex`만 믿지 않고 `routineCycleId`, `templateSnapshot`, `deviceId`, `baseRevision`을 함께 고려한다.

사용자 기준 판단:
- 사용자가 같은 사이클/일차를 다른 기기에서 다시 입력한 경우, 기본 해석은 `한 운동을 두 번 수행함`이 아니라 `같은 루틴 슬롯을 다른 기기에서 수정함`이다.
- 사용자가 계획에 없던 운동을 명시적으로 추가한 경우에만 별도 운동으로 보존한다.
- 사용자가 생략을 선택한 운동은 기록 누락이 아니라 의도적 결정으로 보존한다.
- 서버 기준으로 기록이 교체되더라도 사용자가 `내 기록이 사라졌다`고 느끼지 않도록 변경 요약을 제공한다.
- 동기화 알림은 운동 흐름을 막지 않아야 하며, 사용자가 다음 행동을 결정해야 하는 경우에만 검토 화면으로 보낸다.

## 6. 목표
- 사용자에게 `N사이클 M일차` 중심 진행 모델을 제공한다.
- 두 디바이스에서 같은 루틴 슬롯을 기록해도 운동량이 두 배로 부풀지 않게 한다.
- 오프라인 기록 후 연결되면 서버 기준으로 자연스럽게 동기화한다.
- 오늘 루틴에서 계획 운동을 완료, 생략, 대체, 추가할 수 있게 한다.
- 서버가 사이클 진행도와 운동 슬롯의 canonical 결과를 계산한다.
- 충돌 시 사용자에게 간결한 동기화 알림과 변경 요약을 제공한다.

## 7. 비목표
- MVP에서 모든 필드별 수동 merge 화면을 제공하지 않는다.
- MVP에서 복잡한 버전 복원 UI를 제공하지 않는다.
- MVP에서 고급 주기화, 디로드, AI 자동 루틴 재처방을 포함하지 않는다.
- MVP에서 주차 기반 캘린더 스케줄러를 기본 진행 단위로 삼지 않는다.
- MVP에서 모든 과거 충돌을 사용자가 직접 해결하는 conflict inbox를 만들지 않는다.

## 8. 사용자 흐름
### 8.1 정상 온라인 흐름
1. 사용자가 홈에서 `3사이클 2일차`를 본다.
2. `오늘 루틴 시작`을 누른다.
3. 계획 운동 목록이 표시된다.
4. 각 계획 운동은 `기록`, `대체`, `생략` 중 하나의 상태를 가질 수 있다.
5. 사용자는 `+ 추가 운동`으로 계획 외 운동을 추가할 수 있다.
6. `2일차 완료`를 누르면 서버가 일차 완료와 현재 진행도를 확정한다.
7. 앱은 서버가 내려준 다음 상태를 표시한다. 예: `3사이클 3일차`.

### 8.2 미완료 운동이 있는 완료 흐름
1. 사용자가 계획 운동 일부를 기록하지 않은 상태에서 `n일차 완료`를 누른다.
2. 앱은 완료 전 선택지를 보여준다.

```text
아직 완료하지 않은 운동이 있어요.

[진행 중으로 저장]
[남은 운동 건너뛰고 n일차 완료]
```

3. `진행 중으로 저장`은 진행도를 전진시키지 않는다.
4. `남은 운동 건너뛰고 완료`는 미기록 계획 운동을 `skipped`로 남기고 서버에 일차 완료를 요청한다.

### 8.3 같은 사이클 충돌 흐름
예:
- 서버에는 `3사이클 2일차 벤치프레스` 기록이 있다.
- 디바이스 A가 오프라인에서 같은 `3사이클 2일차 벤치프레스`를 다시 기록한다.

정책:
- 앱은 `SyncMutation`을 서버에 보낸다.
- 서버는 `routineCycleId + dayIndex + routineExerciseSlotId`가 같음을 확인한다.
- 서버는 기존 슬롯 결과를 새 canonical 결과로 교체 또는 갱신한다.
- 응답은 `replaced` 또는 `updated`를 포함한다.
- 앱은 배너로 요약한다.

```text
다른 기기 기록과 맞췄어요.
3사이클 2일차 벤치프레스 기록이 서버 기준으로 업데이트됐어요.
```

### 8.4 다른 사이클 충돌 흐름
예:
- 서버는 `4사이클 1일차`가 최신 상태다.
- 디바이스 B는 오프라인에서 `3사이클 3일차`를 완료했다.

정책:
- 서버의 현재 사이클을 되돌리지 않는다.
- 로컬 기록은 과거 사이클 보강으로 적용 가능한지 판단한다.
- 적용 가능하면 과거 기록으로 반영하고 현재 진행도는 서버의 `4사이클 1일차`를 유지한다.
- 적용 불가하면 `requires_review`로 응답하고 앱은 동기화 검토 상태를 표시한다.

```text
서버의 최신 진행도는 4사이클이에요.
이 기기의 3사이클 기록을 서버 기록에 맞춰 정리했어요.
```

### 8.5 로컬 전용 데이터로 로그인하는 흐름
1. 사용자가 local-default 상태에서 운동 기록을 쌓는다.
2. Google 로그인을 한다.
3. 앱은 로컬 기록을 바로 서버 계정에 덮어쓰지 않고 마이그레이션 후보 `SyncMutation`으로 저장한다.
4. 서버는 현재 계정의 active cycle과 로컬 기록의 template snapshot을 비교한다.
5. 같은 사이클로 매칭되면 슬롯 단위로 교체 또는 보강한다.
6. 매칭되지 않으면 서버 canonical state를 우선 표시하고 로컬 기록은 동기화 검토 상태로 보관한다.

## 9. 데이터 모델 요구사항
### 9.1 RoutineTemplate
- `id`
- `name`
- `version`
- `source`: `system`, `custom`
- `days`
- `updatedAt`

### 9.2 RoutineCycle
- `id`: 서버 id. 오프라인 생성 시 임시 local id 가능.
- `userId`
- `routineTemplateId`
- `templateVersion`
- `templateSnapshot`
- `templateSnapshotSignature`
- `cycleIndex`
- `status`: `active`, `completed`, `archived`, `requires_review`
- `startedAt`
- `completedAt`
- `serverRevision`

### 9.3 RoutineDaySlot
- `id`
- `routineCycleId`
- `dayIndex`
- `title`
- `focus`
- `plannedExerciseSlots`

### 9.4 RoutineExerciseSlot
- `id`: 루틴 스냅샷 안에서 안정적인 slot id.
- `routineCycleId`
- `dayIndex`
- `order`
- `plannedExerciseId`
- `defaultPrescription`

### 9.5 WorkoutLog
- `localId`
- `serverId`
- `userId`
- `routineCycleId`
- `cycleIndex`
- `dayIndex`
- `routineExerciseSlotId`
- `performedExerciseId`
- `entryKind`: `planned`, `substitute`, `additional`
- `slotStatus`: `completed`, `skipped`, `not_recorded`, `pending`
- `sets`: 세트별 상세 기록 리스트
  - `setIndex`
  - `reps`
  - `weightKg`
  - `durationMinutes`
  - `restSeconds`
  - `completed`
- `memo`
- `performedAt`
- `clientUpdatedAt`
- `serverUpdatedAt`
- `clientMutationId`
- `deviceId`
- `baseRevision`
- `serverRevision`
- `syncStatus`: `synced`, `pending_upload`, `syncing`, `conflict`, `requires_review`

### 9.6 SyncMutation
- `clientMutationId`
- `deviceId`
- `userId`
- `mutationType`
- `entityType`
- `entityLocalId`
- `baseRevision`
- `payload`
- `createdAt`
- `attemptCount`
- `lastAttemptAt`
- `status`

### 9.7 SyncState
- `userId`
- `deviceId`
- `serverCursor`
- `lastSuccessfulSyncAt`
- `activeRoutineCycleId`
- `activeCycleIndex`
- `activeDayIndex`
- `serverRevision`

## 10. 서버 충돌 정책
| 상황 | 서버 정책 | 앱 UX |
|---|---|---|
| 같은 사이클, 같은 일차, 같은 운동 슬롯 | 기존 canonical 슬롯 결과를 교체 또는 갱신 | 조용히 반영하고 필요 시 변경 요약 |
| 같은 사이클, 같은 슬롯, 더 오래된 baseRevision | 서버가 `ignored_stale` 또는 `replaced` 결정 | 서버 기록으로 맞췄다는 배너 |
| 같은 사이클, 대체 운동 | 같은 슬롯의 `substitute` 결과로 반영 | 원래 운동 아래 `대체됨` 표시 |
| 같은 사이클, 추가 운동 | `additional`로 append | 오늘 루틴의 추가 운동 섹션에 표시 |
| 같은 사이클, 같은 추가 운동 중복 의심 | 서버가 `merge_candidate` 반환 가능 | 기존 기록에 세트 추가 또는 별도 운동 선택 |
| 미완료 계획 운동 | 자동 삭제 금지. `not_recorded` 또는 사용자가 선택한 `skipped` 유지 | 완료 전 선택지 제공 |
| 클라이언트가 과거 사이클 기록 전송 | 현재 서버 진행도는 되돌리지 않고 과거 기록 보강 또는 `stale_cycle` | 서버 현재 사이클로 화면 유지 |
| 클라이언트가 미래 사이클 기록 전송 | 선행 사이클/일차 근거가 없으면 `requires_review` | 동기화 검토 알림 |
| 클라이언트에 서버 cycleId 없음 | active cycle과 template snapshot signature로 매칭 시도 | 매칭 실패 시 검토 상태 |
| 같은 `clientMutationId` 재전송 | idempotent하게 같은 결과 반환 | 중복 알림 없음 |

## 11. 동기화 프로토콜
동기화는 다음 순서를 기본으로 한다.

1. `pull server state`
   - 서버의 active cycle, active day, revision, cursor를 가져온다.
2. `compare local SyncMutation queue`
   - 로컬 pending mutation이 서버 state와 같은 사이클인지, 과거/미래 사이클인지 분류한다.
3. `push mutation batch`
   - `SyncMutation`을 순서대로 batch 전송한다.
4. `receive item-level results`
   - 각 mutation에 대해 `created`, `updated`, `replaced`, `ignored_stale`, `merge_candidate`, `requires_review`, `conflict`를 받는다.
5. `pull delta`
   - 서버 cursor 이후 변경분을 가져온다.
6. `apply canonical state`
   - Room 캐시, DataStore 화면 상태, SyncMutation 상태를 서버 결과에 맞춘다.

사용자에게 보이는 결과:
- `created`, `updated`: 별도 방해 없이 최신 기록으로 반영한다.
- `replaced`: 홈 배너나 동기화 상태에서 변경 요약을 제공한다.
- `ignored_stale`: 서버 기록이 최신임을 조용히 반영하되, 사용자가 방금 입력한 내용이면 변경 요약을 제공한다.
- `merge_candidate`: 사용자가 같은 추가 운동을 한 번 더 한 것인지, 기존 기록에 세트를 더한 것인지 선택할 수 있게 한다.
- `requires_review`, `conflict`: 운동 중에는 방해하지 않고 홈/프로필에서 `동기화 검토 필요` 상태로 노출한다.

## 12. API 요구사항
### 12.1 Sync State
```text
GET /api/sync/state
```

반환:
- active routine cycle
- current day index
- server revision
- sync cursor
- device sync status

### 12.2 Push Mutations
```text
POST /api/sync/push
```

요구사항:
- batch mutation을 받는다.
- item-level result를 반환한다.
- 동일 `clientMutationId`는 idempotent하게 처리한다.
- 서버 canonical cycle과 맞지 않는 mutation은 진행도를 임의로 변경하지 않는다.

### 12.3 Pull Delta
```text
GET /api/sync/pull?cursor=...
```

반환:
- routine cycles 변경분
- workout logs 변경분
- day completion 변경분
- deleted/archived markers
- next cursor

### 12.4 Routine Cycle
```text
POST /api/routine-cycles
POST /api/routine-cycles/{id}/days/{dayIndex}/complete
```

요구사항:
- 서버가 cycle 생성과 완료를 canonical하게 결정한다.
- day completion은 같은 cycle/day에서 중복 전진하지 않는다.
- 마지막 day 완료 시 다음 cycle 생성 또는 다음 active state를 서버가 결정한다.

## 13. Android 요구사항
- 로컬 Room에 routine cycle, workout log, sync mutation, sync state를 저장한다.
- DataStore는 화면용 선택 상태나 lightweight preference에만 사용하고 서버 동기화의 원본으로 쓰지 않는다.
- WorkManager 기반 `RoutineSyncWorker`를 추가한다.
- 동기화 트리거:
  - 앱 실행
  - Google 로그인 성공
  - 네트워크 복구
  - 운동 기록 저장
  - 일차 완료
  - 사용자가 수동 새로고침
- offline first 저장은 항상 로컬에 먼저 성공해야 한다.
- 서버와 연결되면 `SyncMutation`을 push하고 canonical delta를 pull한다.
- 서버 응답으로 로컬 기록이 교체되면 Room DB를 트랜잭션으로 갱신하고, UI는 Room의 Flow 또는 UiState를 관찰해 반응형으로 반영한다.
- 사용자는 동기화 적용 중 중간 상태가 흔들리는 화면을 보지 않아야 한다.
- 충돌 또는 검토 상태는 홈 배너와 프로필/동기화 상태에서 접근할 수 있어야 한다.

## 14. 서버 요구사항
- `RoutineCycle`을 1급 엔티티로 저장한다.
- workout log는 cycle/day/slot 기준 canonical result를 표현해야 한다.
- `plannedExerciseId` 단독 업서트를 제거하고 `routineCycleId + dayIndex + routineExerciseSlotId`를 충돌 키로 사용한다.
- 추가 운동은 slot id 없이 별도 append 정책을 적용한다.
- 모든 sync mutation은 인증된 사용자 소유권을 검증한다.
- `x-smart-trainner-session-id`만으로 cloud account 데이터를 변경하지 않는다.
- JSON file store는 MVP 실험까지만 허용하고, 동기화 기능이 본격화되면 DB 전환을 검토한다.
- 모든 push 결과는 item-level로 반환한다.

## 15. UX 요구사항
### 홈
홈은 서버 canonical state를 기준으로 현재 위치를 보여준다.

```text
3사이클 2일차
오늘은 등 집중

오늘 운동 계획
랫풀다운 · 시티드 로우 · 페이스풀

[오늘 루틴 시작]
```

### 운동 진행 화면
계획 운동 행은 다음 상태를 지원한다.

- `기록`
- `완료`
- `대체됨`
- `건너뜀`
- `미완료`

추가 운동은 계획 운동 목록과 분리하되 같은 일차 세션에 묶는다.

```text
계획 운동
벤치프레스        완료
숄더프레스        대체됨: 덤벨 숄더프레스
레그레이즈        건너뜀

추가 운동
이두 컬           완료
```

### 동기화 알림
기본은 blocking modal이 아니라 홈 배너 또는 상태 표시다.

예시:

```text
다른 기기 기록과 맞췄어요.
3사이클 2일차 기록 2개가 서버 기준으로 업데이트됐어요.
[변경 내역 보기]
```

```text
동기화 검토가 필요해요.
이 기기의 4사이클 기록을 서버 진행도와 비교해야 해요.
[검토하기]
```

## 16. MVP 범위
- `사이클 + n일차` 용어와 화면 표시.
- 계획 운동의 완료, 생략, 대체, 추가 상태.
- 서버 SSOT 원칙과 sync state/push/pull API 계약 정의.
- Room `SyncMutation` 큐와 WorkManager 동기화 설계.
- 같은 사이클/일차/슬롯의 서버 교체 정책.
- 다른 사이클 충돌의 서버 canonical rebase 정책.
- 동기화 배너와 변경 요약 UX.

## 17. MVP 제외 범위
- 필드 단위 수동 merge UI.
- 과거 모든 revision 복원.
- 고급 conflict inbox.
- 사이클별 고급 분석 리포트.
- 여러 루틴을 동시에 active로 두는 기능.
- 팀/소셜 피드 연동.

## 18. 수용 기준
- 사용자는 홈에서 현재 위치를 `N사이클 M일차`로 이해할 수 있다.
- 사용자는 오늘 계획 운동을 완료, 생략, 대체할 수 있다.
- 사용자는 오늘 계획 외 추가 운동을 기록할 수 있다.
- 미완료 운동이 있는 상태에서 일차 완료 시 `진행 중 저장` 또는 `남은 운동 건너뛰고 완료`를 선택할 수 있다.
- 서버는 같은 cycle/day/slot 기록을 두 개로 만들지 않는다.
- 서버는 다른 cycle의 같은 day/slot 기록을 서로 다른 기록으로 보존한다.
- 서버는 과거 사이클 기록이 들어와도 현재 진행도를 뒤로 돌리지 않는다.
- 서버는 미래 사이클 기록이 선행 근거 없이 들어오면 자동 전진하지 않는다.
- 앱은 오프라인 기록을 `SyncMutation`으로 저장하고 네트워크 복구 시 동기화를 예약한다.
- 동일 `clientMutationId` 재전송은 중복 기록을 만들지 않는다.
- 서버 canonical state 적용 후 앱의 홈, 기록 목록, 동기화 상태가 일관된다.
- 동기화로 로컬 기록이 교체되면 사용자는 변경 요약을 볼 수 있다.

## 19. 성공 지표
- 같은 루틴 일차를 두 디바이스에서 기록해도 중복 운동량이 생성되지 않는 비율.
- 오프라인 기록 후 24시간 내 성공 동기화율.
- 동기화 충돌 후 사용자가 기록 손실을 신고하는 비율.
- `N사이클 M일차` 화면의 현재 위치 이해도.
- 미완료 운동이 있는 일차 완료 시 생략/진행 중 저장 선택 완료율.
- 동기화 배너에서 변경 내역 확인 후 이탈하지 않는 비율.

## 20. 위험과 오픈 퀘스천
- 오프라인 상태에서 서버 `routineCycleId`가 없는 기록을 어떤 기준으로 같은 사이클로 매칭할 것인가.
- 사용자가 실제로 같은 운동을 두 번 한 경우와 단말 중복 입력을 어떻게 구분할 것인가.
- 루틴 템플릿 변경은 현재 active cycle에 즉시 반영할지, 다음 cycle부터 반영할지.
- replacement 기준은 `clientUpdatedAt`, 서버 도착 순서, 명시 revision 중 무엇을 우선할 것인가.
- `skipped`와 `not_recorded`를 분석/진행도에서 어떻게 다르게 사용할 것인가.
- JSON file store로 sync 모델을 어디까지 검증하고 언제 DB로 전환할 것인가.
- 사용자에게 변경 내역과 되돌리기 후보를 어느 깊이까지 제공할 것인가.

## 21. 구현 분할 제안
### Phase 1: 모델과 계약
- PRD 확정.
- 앱/서버 공통 용어와 API contract 초안 작성.
- stable slot id 생성 정책 정의.
- server revision/cursor 정책 정의.

### Phase 2: 서버 SSOT 기반
- RoutineCycle 저장 모델 추가.
- sync state/push/pull API 추가.
- cycle/day/slot canonical key 적용.
- item-level sync result 테스트 추가.

### Phase 3: Android 로컬 SyncMutation
- Room sync 모델 추가.
- WorkManager 동기화 worker 추가.
- 로그인/네트워크 복구/기록 저장 트리거 연결.
- canonical delta 적용 로직 추가.

### Phase 4: UX
- 홈의 `N사이클 M일차` 표시.
- 운동 진행 화면의 완료/생략/대체/추가 상태.
- 일차 완료 전 미완료 운동 선택지.
- 동기화 배너와 변경 내역 화면.

## 22. 테스트 계획
### 서버
- 같은 cycle/day/slot mutation 2개가 1개 canonical workout log로 교체되는지 검증한다.
- 다른 cycle의 같은 day/slot은 서로 다른 기록으로 보존되는지 검증한다.
- 같은 `clientMutationId`를 재전송해도 같은 result를 반환하는지 검증한다.
- 과거 cycle mutation이 current cycle을 되돌리지 않는지 검증한다.
- 미래 cycle mutation이 선행 completion 없이 current cycle을 전진시키지 않는지 검증한다.
- skipped, substitute, additional 상태가 day completion과 progress projection에 올바르게 반영되는지 검증한다.

### Android
- 오프라인 저장이 Room과 `SyncMutation` 큐에 먼저 성공하는지 검증한다.
- WorkManager가 네트워크 복구 후 pending mutation을 처리하는지 검증한다.
- 서버 conflict response 수신 시 Room, DataStore, UI state가 한 번에 canonical state로 맞춰지는지 검증한다.
- 미완료 운동이 있는 일차 완료 UX가 `진행 중 저장`과 `건너뛰고 완료`를 구분하는지 검증한다.
- 대체 운동과 추가 운동이 서로 다른 sync payload로 전송되는지 검증한다.
- 주 경계나 날짜 변경이 stable slot id를 바꾸지 않는지 검증한다.

### 통합
- 디바이스 A와 B가 같은 사이클 같은 슬롯을 각각 오프라인 기록한 뒤 동기화해도 기록이 두 배로 늘지 않는지 검증한다.
- 디바이스 A는 서버보다 과거 사이클, 디바이스 B는 최신 사이클일 때 서버 current state가 유지되는지 검증한다.
- local-default 기록을 가진 사용자가 Google 로그인 후 서버 계정에 안전하게 병합 또는 검토 상태로 전환되는지 검증한다.
