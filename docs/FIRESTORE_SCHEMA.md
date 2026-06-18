# FIRESTORE_SCHEMA.md

# 1. 개요

이 문서는 개인 예약 링크 앱의 Firestore 데이터 구조를 정의한다.

MVP에서는 아래 컬렉션만 사용한다.

```text
users
user_private
time_slots
reservations
```

---

# 2. 공통 규칙

## 2.1 시간 저장 규칙

모든 시간은 `Long` 타입의 Unix Timestamp Milliseconds로 저장한다.

```kotlin
val now = System.currentTimeMillis()
```

예시:

```json
{
  "createdAt": 1781600000000,
  "updatedAt": 1781600000000
}
```

## 2.2 문서 ID 규칙

### users

Firebase Auth UID를 문서 ID로 사용한다.

```text
users/{uid}
```

### user_private

Firebase Auth UID를 문서 ID로 사용한다.

```text
user_private/{uid}
```

### time_slots

Firestore Auto ID를 사용한다.

```text
time_slots/{slotId}
```

### reservations

Firestore Auto ID를 사용한다.

```text
reservations/{reservationId}
```

---

# 3. users 컬렉션

## 3.1 경로

```text
users/{uid}
```

## 3.2 목적

공개 가능한 사용자 프로필 정보를 저장한다.

예약 링크를 통해 다른 사용자가 조회할 수 있는 정보만 저장한다.

## 3.3 필드

| 필드명 | 타입 | 필수 | 설명 |
|---|---|---:|---|
| uid | String | O | Firebase Auth UID |
| nickname | String | O | 사용자 닉네임 |
| bio | String? | X | 한 줄 소개 |
| profileImageUrl | String? | X | 프로필 이미지 URL |
| reservationLinkId | String | O | 공유 링크용 ID |
| createdAt | Long | O | 생성 시간 |
| updatedAt | Long | O | 수정 시간 |

## 3.4 예시

```json
{
  "uid": "firebase_uid_123",
  "nickname": "김프로",
  "bio": "커피챗과 안드로이드 상담을 받습니다.",
  "profileImageUrl": "https://example.com/profile.png",
  "reservationLinkId": "kimpro",
  "createdAt": 1781600000000,
  "updatedAt": 1781600000000
}
```

---

# 4. user_private 컬렉션

## 4.1 경로

```text
user_private/{uid}
```

## 4.2 목적

공개되면 안 되는 사용자 개인 정보를 저장한다.

MVP에서는 이메일과 FCM 토큰 저장 용도로 사용한다.

## 4.3 필드

| 필드명 | 타입 | 필수 | 설명 |
|---|---|---:|---|
| uid | String | O | Firebase Auth UID |
| email | String? | X | Firebase Auth 이메일 |
| fcmToken | String? | X | 현재 디바이스 FCM 토큰 |
| updatedAt | Long | O | 수정 시간 |

## 4.4 예시

```json
{
  "uid": "firebase_uid_123",
  "email": "user@gmail.com",
  "fcmToken": "fcm_token_value",
  "updatedAt": 1781600000000
}
```

---

# 5. time_slots 컬렉션

## 5.1 경로

```text
time_slots/{slotId}
```

## 5.2 목적

Host가 예약 가능한 시간대를 저장한다.

## 5.3 필드

| 필드명 | 타입 | 필수 | 설명 |
|---|---|---:|---|
| id | String | O | slotId |
| hostId | String | O | 예약을 받는 사람 UID |
| startAt | Long | O | 시작 시간 |
| endAt | Long | O | 종료 시간 |
| durationMinutes | Int | O | 30 또는 60 |
| status | String | O | AVAILABLE / RESERVED / DISABLED |
| createdAt | Long | O | 생성 시간 |
| updatedAt | Long | O | 수정 시간 |

## 5.4 status

| 값 | 설명 |
|---|---|
| AVAILABLE | 예약 신청 가능 |
| RESERVED | 승인된 예약 존재 |
| DISABLED | Host가 비활성화한 슬롯 |

## 5.5 예시

```json
{
  "id": "slot_123",
  "hostId": "host_uid_123",
  "startAt": 1781600000000,
  "endAt": 1781603600000,
  "durationMinutes": 60,
  "status": "AVAILABLE",
  "createdAt": 1781500000000,
  "updatedAt": 1781500000000
}
```

---

# 6. reservations 컬렉션

## 6.1 경로

```text
reservations/{reservationId}
```

## 6.2 목적

예약 신청 정보를 저장한다.

## 6.3 필드

| 필드명 | 타입 | 필수 | 설명 |
|---|---|---:|---|
| id | String | O | reservationId |
| hostId | String | O | 예약 받는 사람 UID |
| guestId | String | O | 예약 신청자 UID |
| slotId | String | O | 선택한 TimeSlot ID |
| startAt | Long | O | 예약 시작 시간 복사본 |
| endAt | Long | O | 예약 종료 시간 복사본 |
| purpose | String | O | COFFEE_CHAT / MEAL / STUDY / CONSULTING / ETC |
| message | String | O | 신청 메시지 |
| status | String | O | PENDING / APPROVED / REJECTED / CANCELLED |
| rejectReason | String? | X | 거절 사유 |
| createdAt | Long | O | 신청 생성 시간 |
| updatedAt | Long | O | 수정 시간 |

## 6.4 status

| 값 | 설명 |
|---|---|
| PENDING | 승인 대기 |
| APPROVED | 승인 완료 |
| REJECTED | 거절 |
| CANCELLED | 취소 |

## 6.5 purpose

| 값 | 설명 |
|---|---|
| COFFEE_CHAT | 커피챗 |
| MEAL | 식사 |
| STUDY | 스터디 |
| CONSULTING | 상담 |
| ETC | 기타 |

## 6.6 예시

```json
{
  "id": "reservation_123",
  "hostId": "host_uid_123",
  "guestId": "guest_uid_456",
  "slotId": "slot_123",
  "startAt": 1781600000000,
  "endAt": 1781603600000,
  "purpose": "COFFEE_CHAT",
  "message": "안드로이드 커리어 관련해서 이야기 나누고 싶습니다.",
  "status": "PENDING",
  "rejectReason": null,
  "createdAt": 1781500000000,
  "updatedAt": 1781500000000
}
```

---

# 7. 예약 상태 변경 규칙

## 7.1 예약 신청

조건:

- guestId는 로그인한 사용자 UID여야 한다.
- time_slots/{slotId}.status가 AVAILABLE이어야 한다.
- 과거 시간 슬롯은 신청할 수 없다.
- 동일 slotId에 PENDING 또는 APPROVED 예약이 있으면 신청할 수 없다.

처리:

```text
reservations 생성
status = PENDING
```

time_slots 상태는 변경하지 않는다.

---

## 7.2 예약 승인

조건:

- 로그인한 사용자가 hostId와 같아야 한다.
- reservation.status가 PENDING이어야 한다.
- time_slots.status가 AVAILABLE이어야 한다.

처리:

```text
reservation.status = APPROVED
time_slots.status = RESERVED
```

주의:

예약 승인과 슬롯 상태 변경은 반드시 Transaction으로 처리한다.

---

## 7.3 예약 거절

조건:

- 로그인한 사용자가 hostId와 같아야 한다.
- reservation.status가 PENDING이어야 한다.
- rejectReason은 필수다.

처리:

```text
reservation.status = REJECTED
reservation.rejectReason = 입력한 사유
```

time_slots 상태는 변경하지 않는다.

---

## 7.4 예약 취소

조건:

- hostId 또는 guestId가 로그인한 사용자 UID와 같아야 한다.
- reservation.status가 PENDING 또는 APPROVED여야 한다.

처리:

```text
reservation.status = CANCELLED
```

추가 처리:

APPROVED 상태였던 예약을 취소하면

```text
time_slots.status = AVAILABLE
```

로 되돌린다.

이 작업은 Transaction으로 처리한다.

---

# 8. 중복 예약 방지 규칙

MVP에서는 승인 전까지 같은 슬롯에 여러 PENDING 예약을 허용하지 않는다.

즉, 하나의 slotId에는 PENDING 또는 APPROVED 예약이 하나만 존재해야 한다.

예약 신청 시 아래 조건을 검사한다.

```text
reservations
where slotId == 선택한 slotId
where status in [PENDING, APPROVED]
```

결과가 있으면 예약 신청을 막는다.

---

# 9. 권장 Query

## 9.1 Host의 시간 슬롯 조회

```text
time_slots
where hostId == uid
where startAt >= startOfDay
orderBy startAt asc
```

## 9.2 특정 Host의 예약 가능한 슬롯 조회

```text
time_slots
where hostId == hostId
where status == AVAILABLE
where startAt >= now
orderBy startAt asc
```

## 9.3 Host가 받은 예약 신청 조회

```text
reservations
where hostId == uid
orderBy createdAt desc
```

## 9.4 Guest가 신청한 예약 조회

```text
reservations
where guestId == uid
orderBy createdAt desc
```

## 9.5 승인 대기 예약 조회

```text
reservations
where hostId == uid
where status == PENDING
orderBy createdAt desc
```

---

# 10. 필요한 Firestore Index

아래 쿼리는 복합 인덱스가 필요할 수 있다.

```text
time_slots
hostId ASC
startAt ASC
```

```text
time_slots
hostId ASC
status ASC
startAt ASC
```

```text
reservations
hostId ASC
createdAt DESC
```

```text
reservations
guestId ASC
createdAt DESC
```

```text
reservations
hostId ASC
status ASC
createdAt DESC
```

```text
reservations
slotId ASC
status ASC
```

---

# 11. Kotlin Enum 권장

## 11.1 TimeSlotStatus

```kotlin
enum class TimeSlotStatus {
    AVAILABLE,
    RESERVED,
    DISABLED
}
```

## 11.2 ReservationStatus

```kotlin
enum class ReservationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
```

## 11.3 ReservationPurpose

```kotlin
enum class ReservationPurpose {
    COFFEE_CHAT,
    MEAL,
    STUDY,
    CONSULTING,
    ETC
}
```

---

# 12. Kotlin Data Class 권장

## 12.1 User

```kotlin
data class User(
    val uid: String = "",
    val nickname: String = "",
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val reservationLinkId: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
```

## 12.2 UserPrivate

```kotlin
data class UserPrivate(
    val uid: String = "",
    val email: String? = null,
    val fcmToken: String? = null,
    val updatedAt: Long = 0L
)
```

## 12.3 TimeSlot

```kotlin
data class TimeSlot(
    val id: String = "",
    val hostId: String = "",
    val startAt: Long = 0L,
    val endAt: Long = 0L,
    val durationMinutes: Int = 30,
    val status: String = TimeSlotStatus.AVAILABLE.name,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
```

## 12.4 Reservation

```kotlin
data class Reservation(
    val id: String = "",
    val hostId: String = "",
    val guestId: String = "",
    val slotId: String = "",
    val startAt: Long = 0L,
    val endAt: Long = 0L,
    val purpose: String = ReservationPurpose.ETC.name,
    val message: String = "",
    val status: String = ReservationStatus.PENDING.name,
    val rejectReason: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
```

---

# 13. Repository 함수 설계

## 13.1 AuthRepository

```kotlin
interface AuthRepository {
    fun getCurrentUserId(): String?
    suspend fun signInWithGoogle(): String
    suspend fun signOut()
}
```

## 13.2 UserRepository

```kotlin
interface UserRepository {
    suspend fun createOrUpdateUser(user: User)
    suspend fun getUser(uid: String): User?
    suspend fun updateProfile(
        nickname: String,
        bio: String?,
        profileImageUrl: String?
    )
}
```

## 13.3 TimeSlotRepository

```kotlin
interface TimeSlotRepository {
    suspend fun createTimeSlot(
        hostId: String,
        startAt: Long,
        endAt: Long,
        durationMinutes: Int
    )

    suspend fun getHostTimeSlots(hostId: String): List<TimeSlot>

    suspend fun getAvailableTimeSlots(hostId: String): List<TimeSlot>

    suspend fun disableTimeSlot(slotId: String)
}
```

## 13.4 ReservationRepository

```kotlin
interface ReservationRepository {
    suspend fun requestReservation(
        hostId: String,
        guestId: String,
        slotId: String,
        purpose: ReservationPurpose,
        message: String
    )

    suspend fun getReceivedReservations(hostId: String): List<Reservation>

    suspend fun getMyReservations(guestId: String): List<Reservation>

    suspend fun approveReservation(reservationId: String)

    suspend fun rejectReservation(
        reservationId: String,
        reason: String
    )

    suspend fun cancelReservation(reservationId: String)
}
```

---

# 14. 보안 규칙 초안

MVP 개발 단계용 초안이다.

실제 배포 전 반드시 재검토한다.

```js
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    function isSignedIn() {
      return request.auth != null;
    }

    function isOwner(uid) {
      return isSignedIn() && request.auth.uid == uid;
    }

    match /users/{uid} {
      allow read: if true;
      allow create, update: if isOwner(uid);
      allow delete: if false;
    }

    match /user_private/{uid} {
      allow read, create, update: if isOwner(uid);
      allow delete: if false;
    }

    match /time_slots/{slotId} {
      allow read: if true;

      allow create: if isSignedIn()
        && request.resource.data.hostId == request.auth.uid;

      allow update: if isSignedIn()
        && resource.data.hostId == request.auth.uid;

      allow delete: if false;
    }

    match /reservations/{reservationId} {
      allow read: if isSignedIn()
        && (
          resource.data.hostId == request.auth.uid ||
          resource.data.guestId == request.auth.uid
        );

      allow create: if isSignedIn()
        && request.resource.data.guestId == request.auth.uid;

      allow update: if isSignedIn()
        && (
          resource.data.hostId == request.auth.uid ||
          resource.data.guestId == request.auth.uid
        );

      allow delete: if false;
    }
  }
}
```

---

# 15. 주의사항

- Firestore 보안 규칙만으로 모든 상태 전이를 완벽하게 막기 어렵다.
- 승인, 취소, 중복 예약 방지는 앱 코드 Transaction에서 처리한다.
- 추후 Cloud Functions를 도입하면 상태 변경 로직을 서버로 이동할 수 있다.
- MVP에서는 Firebase-only 구조를 우선한다.
- 이메일 기반 비회원 예약은 구현하지 않는다.
- 메일 발송 API는 MVP에서 사용하지 않는다.
