# 친구 연결 TRD

## 기술 목표

친구 연결은 Android와 서버가 동시에 바뀌는 데이터 연동 기능이다. 서버가 친구 관계와 요청 상태의 source of truth가 되고, Android는 Room 캐시를 통해 화면을 렌더링한 뒤 서버 refresh로 동기화한다. FCM push는 특정 feature가 아니라 app 단에 둔다.

## 영향 범위

Android:

- `:app`
- `:core:network`
- `:core:database`
- `:core:domain`
- `:core:data`
- `:feature:friend:api`
- `:feature:friend:domain`
- `:feature:friend:data`
- `:feature:friend:impl`

Server:

- `/Users/kimtaenyun/server/smart-trainner/src/server/db.ts`
- `/Users/kimtaenyun/server/smart-trainner/src/server/types.ts`
- `/Users/kimtaenyun/server/smart-trainner/src/server/validation.ts`
- `/Users/kimtaenyun/server/smart-trainner/src/server/friends.ts`
- `/Users/kimtaenyun/server/smart-trainner/src/server/notifications.ts`
- `/Users/kimtaenyun/server/smart-trainner/src/server/pushTokens.ts`
- `/Users/kimtaenyun/server/smart-trainner/src/server/fcm.ts`
- `/Users/kimtaenyun/server/smart-trainner/app/api/friends/**`
- `/Users/kimtaenyun/server/smart-trainner/app/api/friend-requests/**`
- `/Users/kimtaenyun/server/smart-trainner/app/api/notifications/**`
- `/Users/kimtaenyun/server/smart-trainner/app/api/push-tokens/route.ts`

## Android 모듈 설계

새 feature는 기존 feature split을 따른다.

- `feature:friend:api`: `FriendFeatureEntry`
- `feature:friend:domain`: friend models, repository contract, use cases
- `feature:friend:data`: Room cache, network DTO mapping, repository implementation
- `feature:friend:impl`: Compose route, ViewModel, UI state/action, strings

Push token registration은 친구 feature에 두지 않는다.

- `core:domain`: `PushTokenRepository`, `RegisterPushTokenUseCase`
- `core:data`: `DefaultPushTokenRepository`
- `app`: `PushTokenRegistrar`, `FirebasePushTokenRegistrar`, `SmartTrainnerFirebaseMessagingService`, notification permission prompt, notification tap navigation and refresh request to Friends

## Android 데이터 모델

Domain:

- `SocialUser`
- `FriendConnection`
- `FriendRequest`
- `FriendRequestStatus`
- `FriendRequestDirection`
- `FriendConnectionId`
- `FriendRequestId`

Room cache:

- `friend_connections`
  - ownerSessionId
  - id
  - friendSessionId
  - friendNickname
  - friendDisplayName
  - friendAvatarUrl
  - createdAt
  - updatedAt
- `friend_requests`
  - ownerSessionId
  - id
  - requester public fields
  - receiver public fields
  - direction
  - status
  - createdAt
  - updatedAt
  - respondedAt

Room version changes from `8` to `9`.

## Android Sync Policy

- UI observes Room via `FriendRepository.observeFriends()` and `observeIncomingRequests()`.
- `FriendViewModel` calls `RefreshFriendsUseCase` on route init and on retry.
- `send`, `accept`, `decline`, and `remove` are network commands.
- After each command, repository refreshes friends, incoming requests, and outgoing requests from the server.
- The app does not invent accepted friendships offline.
- Pending friend commands are not queued in MVP. If the network fails, UI shows an inline failure state.

## Android Network Contract

`core:network/FriendNetworkApi.kt`

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/api/friends` | session/device headers | `{ data: FriendDto[], count }` |
| GET | `/api/friend-requests?box=incoming` | session/device headers | `{ data: FriendRequestDto[], count }` |
| GET | `/api/friend-requests?box=outgoing` | session/device headers | `{ data: FriendRequestDto[], count }` |
| POST | `/api/friend-requests` | `{ nickname }` | `{ data: FriendRequestDto }` |
| POST | `/api/friend-requests/{id}/accept` | empty | `{ data: FriendDto }` |
| POST | `/api/friend-requests/{id}/decline` | empty | `{ data: FriendRequestDto }` |
| DELETE | `/api/friends/{friendSessionId}` | empty | `204`; server/client contract prepared, not surfaced in MVP UI |

`core:network/PushTokenNetworkApi.kt`

| Method | Path | Request | Response |
|---|---|---|---|
| POST | `/api/push-tokens` | `{ token, platform, appVersion? }` | `{ data: PushTokenRegistrationDto }` |

All protected calls use:

- `x-smart-trainner-session-id`
- `x-smart-trainner-device-id`

Friend, notification, and push-token endpoints require an account-linked session. `local-default` or any `provider=local` session returns `403 FRIEND_ACCOUNT_REQUIRED`.

DTO contract:

- `SocialUserDto`: `{ sessionId, displayName, nickname, avatarUrl? }`
- `FriendDto`: `{ id, friend, createdAt, updatedAt }`. `id` is the friendship id. `friend.sessionId` is the other user's session id.
- `FriendRequestDto`: `{ id, requester, receiver, status, createdAt, updatedAt, respondedAt? }`
- `DELETE /api/friends/{friendSessionId}` uses the other user's `friend.sessionId`, not `FriendDto.id`.
- `PushTokenRegistrationDto`: `{ sessionId, deviceId, platform, updatedAt }`

Error contract:

All friend/push endpoints use `{ error, code, details? }`. Required codes include `FRIEND_ACCOUNT_REQUIRED`, `FRIEND_NOT_FOUND`, `FRIEND_REQUEST_SELF`, `ALREADY_FRIENDS`, `FRIEND_REQUEST_PENDING`, `FRIEND_REQUEST_RESOLVED`, `DEVICE_REQUIRED`, and `DEVICE_SESSION_REPLACED`.

## Server Schema

`friend_requests`

- `id`
- `requester_session_id`
- `receiver_session_id`
- `pair_key`
- `status`: `PENDING`, `ACCEPTED`, `DECLINED`
- `created_at`
- `updated_at`
- `responded_at`
- unique pending direction index
- unique pending pair index

`friendships`

- `id`
- `session_a_id`
- `session_b_id`
- `pair_key`
- `status`: `ACTIVE`, `REMOVED`
- `created_at`
- `updated_at`
- `removed_at`
- `removed_by_session_id`

`notification_events`

- `id`
- `recipient_session_id`
- `actor_session_id`
- `type`: `FRIEND_REQUEST_RECEIVED`, `FRIEND_REQUEST_ACCEPTED`
- `friend_request_id`
- `friendship_id`
- `title`
- `body`
- `payload_json`
- `read_at`
- `push_status`: `PENDING`, `SENT`, `PARTIAL`, `NO_TOKENS`, `DISABLED`, `FAILED`
- `pushed_at`
- `push_error`
- `created_at`

`push_tokens`

- `session_id`
- `device_id`
- `token`
- `platform`
- `app_version`
- `created_at`
- `updated_at`
- `invalidated_at`

## Server Rules

- Route handlers stay thin.
- Business logic lives in `src/server/friends.ts`.
- Notification record and friend mutation happen in the same DB transaction.
- FCM dispatch happens after transaction commit.
- Unknown nickname returns `404 FRIEND_NOT_FOUND`.
- Self request returns `400 FRIEND_REQUEST_SELF`.
- Existing active friendship returns `409 ALREADY_FRIENDS`.
- Existing pending relationship returns `409 FRIEND_REQUEST_PENDING` or the same pending request for same-direction idempotency.
- Reverse pending requests do not auto-accept in MVP. The receiver must explicitly accept the existing incoming request.
- Only the request receiver can accept or decline.
- Friendship pair keys are canonical sorted session ids.
- Push token registration is idempotent per active session/device. When the same device id or FCM token is registered for a new session, previous active rows for that device/token are invalidated to avoid cross-account notification leakage.

## FCM Design

Android:

- `app` applies `google-services` only when `app/google-services.json` exists.
- Firebase Android app is registered in project `weefit-48cd6` with package `com.smarttrainner`; `app/google-services.json` is included for client-side Firebase configuration.
- `FirebasePushTokenRegistrar` no-ops when `FirebaseApp` is not configured.
- `SmartTrainnerAppViewModel` registers current token after Google session validation and data sync.
- `SmartTrainnerFirebaseMessagingService` registers refreshed tokens from `onNewToken`.
- `SmartTrainnerFirebaseMessagingService` displays foreground/background data notification payloads in the app-owned friend notification channel.
- FCM notification taps send an app-owned navigation request to `SmartTrainnerDestination.Friends`.
- `SmartTrainnerNavigation` passes the monotonically increasing request id to `FriendFeatureEntry.Route(refreshRequest)`.
- `feature:friend:impl` treats non-zero refresh requests as server refresh commands so an already-alive Friends screen updates after a notification tap.

Server:

- `firebase-admin` is initialized lazily.
- Supported credential sources:
  - `FIREBASE_SERVICE_ACCOUNT_JSON`
  - `FIREBASE_PROJECT_ID`, `FIREBASE_CLIENT_EMAIL`, `FIREBASE_PRIVATE_KEY`
  - `GOOGLE_APPLICATION_CREDENTIALS`
- Missing credentials do not fail friend requests. The notification row is retained with push status `DISABLED`.
- Malformed credentials, FCM initialization errors, and send errors do not fail committed friend mutations. The notification row is retained with push status `FAILED`.
- Partial FCM delivery records `PARTIAL` with a push error summary.

FCM data payload:

- `notificationEventId`
- `notificationId`
- `type`: `FRIEND_REQUEST_RECEIVED` or `FRIEND_REQUEST_ACCEPTED`
- `actorSessionId`
- `actorNickname`
- `friendRequestId`
- `friendshipId`
- `deepLink`: `smarttrainner://friends` or `smarttrainner://friends/requests`

Android treats this payload as a navigation/refresh hint only. It does not trust payload data as the source of truth; the Friends feature refreshes from the server after notification navigation.

## Future Workout Sharing

Friendship is a relationship primitive, not a sharing permission by itself. Future data sharing should add explicit permission rows, for example:

- `friend_share_permissions`
  - ownerSessionId
  - friendSessionId
  - scope
  - status
  - grantedAt
  - revokedAt

Initial scopes:

- `PROFILE_BASIC`
- `WORKOUT_SUMMARY`
- `ROUTINE_PROGRESS`
- `WORKOUT_LOGS`

Every future shared workout endpoint must check:

1. active friendship,
2. owner-granted scope,
3. resource ownership,
4. revocation state.

## Verification

Server:

- `npm test`
- `npm run lint`
- `npm run build`

Android:

- `./gradlew :feature:friend:domain:test`
- `./gradlew :feature:friend:impl:testDebugUnitTest`
- `./gradlew :core:database:testDebugUnitTest`
- `./gradlew :app:assembleDebug`
- `./gradlew :app:lintDebug`
- `./gradlew :app:testDebugUnitTest`

Final gate:

- `./gradlew :core:domain:test :core:data:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`

## Rollout Plan

1. Deploy server schema and social/push endpoints first.
2. Configure Firebase Admin credentials in server runtime. Without credentials, friend APIs still work and notification rows record `DISABLED`.
3. Use the registered Firebase Android app (`weefit-48cd6` / `com.smarttrainner`) and included `app/google-services.json` for Firebase-enabled Android builds. Without the file, app builds still pass and FCM token registration no-ops.
4. Smoke test nickname request, incoming list, accept, decline, friend list, friend removal, push-token registration, and notification tap to Friends.
5. Keep Room migration `8 -> 9` non-destructive for existing installs.

## Rollback

- Hide or remove `SmartTrainnerDestination.Friends`.
- Keep server tables inert. Existing workout/routine contracts are unaffected.
- Disable FCM by removing Firebase credentials from server runtime.
- Android builds continue without `google-services.json`.
