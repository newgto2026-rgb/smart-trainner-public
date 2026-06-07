# 친구 연결 PRD

## 배경

Smart Trainner는 개인의 운동 계획, 수행 기록, 루틴 진행 상황을 앱 안에 안정적으로 쌓는 단계까지 왔다. 다음 단계의 소셜 기능은 친구끼리 운동 상황, 루틴 진행, 기록 요약을 공유하는 것이지만, 공유 기능의 전제는 신뢰 가능한 양방향 친구 관계다.

이번 범위는 운동 데이터 공유가 아니라 친구 연결까지다. 친구 관계는 앞으로 대부분의 운동 데이터와 새 기능의 접근 제어 기준이 되므로, 첫 구현부터 관계 상태와 알림을 서버 권위 데이터로 관리한다.

## 목표

- 하단 친구 탭을 추가한다.
- 사용자는 친구의 닉네임을 입력해 친구 요청을 보낼 수 있다.
- 요청을 받은 사용자는 친구 탭에서 수락하거나 거절할 수 있다.
- 수락된 친구는 친구 목록에 표시된다.
- 친구 요청과 수락 이벤트는 서버 notification record로 남고 FCM push 발송 대상이 된다.
- FCM 수신/토큰 등록은 특정 feature가 아니라 app 단에서 소유한다.
- 향후 운동 데이터 공유를 위해 관계, 알림, push token 데이터 구조를 확장 가능하게 둔다.

## 비목표

- 운동 기록, 루틴, 분석 데이터 공유의 실제 노출.
- 친구 피드, 랭킹, 채팅, 추천 친구.
- 공개 프로필 검색 화면.
- 친구별 세부 공유 권한 UI.
- 친구 삭제 UI.
- 정확한 시간 기반 local alarm/reminder.

## 사용자 흐름

1. 사용자가 하단 친구 탭을 연다.
2. 친구 닉네임을 입력하고 요청을 보낸다.
3. 서버는 닉네임을 정규화해 대상 세션을 찾고 자기 자신, 이미 친구, 중복 대기 요청을 막는다.
4. 대상 사용자에게 친구 요청 notification record가 생성되고 FCM push가 시도된다.
5. 대상 사용자는 친구 탭에서 받은 요청을 수락하거나 거절한다.
6. 수락하면 양방향 친구 관계가 활성화되고 요청자에게 수락 notification record와 FCM push가 생성된다.
7. 양쪽 사용자의 친구 목록은 서버 상태로 갱신된다.

## 정책

- 친구 관계는 양방향 동의가 있어야 활성화된다.
- 친구 추가는 MVP에서 닉네임 exact match 기반이다.
- 닉네임 비교는 서버의 기존 nickname key 정책처럼 공백 정규화 및 case-insensitive 비교를 따른다.
- 자기 자신에게 요청할 수 없다.
- 이미 활성 친구인 사용자에게 다시 요청할 수 없다.
- 같은 방향의 pending 요청은 중복 생성하지 않고 기존 요청 상태를 유지한다.
- 반대 방향 pending 요청은 충돌로 처리하고, 사용자는 받은 요청을 수락해야 한다.
- 거절된 요청은 친구 관계를 만들지 않는다.
- 친구 API와 push token 등록은 Google 등 계정 연결이 완료된 세션만 사용할 수 있다. `local-default` 세션은 `FRIEND_ACCOUNT_REQUIRED`로 거절한다.
- 친구 끊기 API가 호출될 경우 활성 관계만 `REMOVED` 상태로 바꾸며, 과거 friend request와 notification record는 보존한다. 단, MVP UI에는 친구 삭제를 노출하지 않는다.

## 개인정보와 공유 경계

MVP 친구 DTO는 다음 공개 정보만 포함한다.

- sessionId
- displayName
- nickname
- avatarUrl, 존재할 경우

다음 정보는 친구 목록이나 요청 응답으로 노출하지 않는다.

- email
- device id/name
- body measurements
- workout logs
- routine progress
- analysis summaries

운동 데이터 공유가 추가될 때도 서버는 친구 관계만으로 raw workout data를 열지 않는다. 별도의 share scope와 owner consent가 필요하다.

## FCM/알림 정책

- push token 등록과 FCM 수신 서비스는 `app` 단에서 관리한다.
- 친구 feature는 받은 요청과 친구 목록을 표시하는 소비자 역할만 한다.
- 서버는 notification record를 먼저 저장하고, 등록된 push token이 있으면 FCM 발송을 시도한다.
- FCM credentials가 없거나 token이 없으면 서버는 기능 실패가 아니라 push 상태를 `DISABLED` 또는 `NO_TOKENS`로 기록한다.
- FCM 초기화/발송 실패는 친구 요청/수락 API 성공을 되돌리지 않고 notification push 상태를 `FAILED` 또는 `PARTIAL`로 기록한다.
- 알림 탭은 앱을 열고 친구 탭으로 이동시킨다. Android는 push를 state source로 쓰지 않고 친구 탭 진입 후 서버/Room 동기화로 상태를 갱신한다.
- Android 13 이상에서는 알림 권한이 허용되어야 시스템 알림이 표시된다.

실제 push 검증에 필요한 외부 입력:

- `app/google-services.json`
- Firebase project의 Cloud Messaging 활성화
- 서버 실행 환경의 Firebase Admin credential
  - `GOOGLE_APPLICATION_CREDENTIALS` 또는
  - `FIREBASE_SERVICE_ACCOUNT_JSON` 또는
  - `FIREBASE_PROJECT_ID`, `FIREBASE_CLIENT_EMAIL`, `FIREBASE_PRIVATE_KEY`

## 성공 기준

- 친구 탭이 앱 하단 탭에 보인다.
- 사용자는 닉네임으로 친구 요청을 보낼 수 있다.
- 받은 요청은 수락/거절할 수 있다.
- 수락 후 양쪽 친구 목록에 서로가 표시된다.
- 요청/수락 이벤트는 서버 notification record로 남는다.
- Firebase 설정이 없어도 앱 빌드와 서버 테스트가 통과한다.
- Firebase 설정이 제공되면 app 단 FCM token registration과 server push dispatch가 동작할 수 있다.

## 향후 확장

- 친구별 공유 scope: `PROFILE_BASIC`, `WORKOUT_SUMMARY`, `ROUTINE_PROGRESS`, `WORKOUT_LOGS`.
- 친구 활동 요약 카드.
- 운동 완료 push.
- 친구별 알림 mute.
- 친구 삭제, 차단, 신고.
- trainer/client 관계 모델.
