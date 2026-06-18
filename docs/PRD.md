# PRD.md

# Personal Reservation Link App MVP

# 1. 프로젝트 개요

## 1.1 서비스 한 줄 설명

개인이 자신의 가능한 시간을 공개하고, 다른 사용자가 링크를 통해 예약을 신청할 수 있는 개인 예약 링크 앱.

## 1.2 서비스 컨셉

네이버 예약이 매장과 사업자 중심이라면, 이 앱은 일반 개인이 자신의 시간을 예약받는 서비스다.

예시:

- 커피챗 예약
- 식사 약속 신청
- 스터디 일정 신청
- 멘토링 신청
- 상담 신청

## 1.3 핵심 가치

- 내 가능한 시간만 공개한다.
- 상대방은 링크를 통해 예약을 신청한다.
- Host는 예약을 승인하거나 거절할 수 있다.
- 승인된 예약은 일정으로 관리할 수 있다.

---

# 2. MVP 목표

## 2.1 목표

MVP 출시 후 실제 사용자 10명 이상에게 사용해보게 하고, 카카오톡으로 약속 잡는 것보다 편한 순간이 있는지 검증한다.

## 2.2 검증 질문

- 사용자가 자신의 예약 링크를 만들고 공유하는가?
- 링크를 받은 사용자가 예약 신청까지 완료하는가?
- Host가 예약 승인/거절 기능을 실제로 사용하는가?
- 이 앱이 카카오톡 약속 조율보다 편한 상황이 있는가?

---

# 3. 사용자 유형

## 3.1 Host

예약을 받는 사람.

### 권한

- 로그인
- 프로필 생성
- 예약 가능 시간 생성
- 예약 가능 시간 삭제 또는 비활성화
- 예약 신청 조회
- 예약 승인
- 예약 거절
- 거절 사유 입력
- 예약 링크 공유

## 3.2 Guest

예약을 신청하는 사람.

### 권한

- 로그인
- Host 프로필 조회
- Host 예약 가능 시간 조회
- 예약 목적 선택
- 예약 메시지 입력
- 예약 신청
- 본인이 신청한 예약 조회
- 본인이 신청한 예약 취소

---

# 4. 인증 정책

## 4.1 MVP 인증 방식

Google 로그인만 사용한다.

## 4.2 비회원 예약

MVP에서는 지원하지 않는다.

### 제외 이유

- 이메일 발송 API가 필요하다.
- 예약 조회가 복잡해진다.
- 악성 예약 신청 대응이 어렵다.
- 개인정보 처리 범위가 넓어진다.
- Firebase Auth 기반 구조가 더 단순하다.

---

# 5. 핵심 기능

# 5.1 Google 로그인

## 설명

사용자는 Google 계정으로 로그인한다.

## 요구사항

- Firebase Auth 사용
- 로그인 성공 시 UID를 획득한다.
- 최초 로그인 시 users 문서를 생성한다.
- FCM 토큰은 user_private 문서에 저장한다.

---

# 5.2 프로필

## 설명

Host가 예약 링크에 노출할 프로필 정보를 설정한다.

## 필드

- 닉네임
- 한 줄 소개
- 프로필 이미지
- 예약 링크 ID

## 요구사항

- 닉네임은 필수다.
- 한 줄 소개는 선택이다.
- 프로필 이미지는 선택이다.
- 예약 링크 ID는 공유 링크에 사용된다.
- 예약 링크 ID는 중복될 수 없다.

---

# 5.3 예약 가능 시간

## 설명

Host가 예약 가능한 시간 슬롯을 생성한다.

## 요구사항

- 날짜를 선택할 수 있다.
- 시작 시간을 선택할 수 있다.
- 30분 또는 60분 단위로 생성할 수 있다.
- 과거 시간은 생성할 수 없다.
- 같은 Host의 같은 시간대 중복 생성은 불가능하다.
- Host는 슬롯을 비활성화할 수 있다.

## 슬롯 상태

- AVAILABLE
- RESERVED
- DISABLED

---

# 5.4 예약 신청

## 설명

Guest가 Host의 예약 링크로 진입하여 예약을 신청한다.

## 요구사항

- Guest는 로그인 상태여야 한다.
- Host 프로필을 볼 수 있다.
- AVAILABLE 상태의 슬롯만 볼 수 있다.
- 예약 목적을 선택한다.
- 메시지를 입력한다.
- 예약 신청 시 Reservation 상태는 PENDING이다.

## 예약 목적

- COFFEE_CHAT
- MEAL
- STUDY
- CONSULTING
- ETC

---

# 5.5 예약 승인

## 설명

Host가 PENDING 상태의 예약을 승인한다.

## 요구사항

- Host 본인만 승인할 수 있다.
- PENDING 상태만 승인할 수 있다.
- 승인 시 Reservation 상태는 APPROVED가 된다.
- 승인 시 TimeSlot 상태는 RESERVED가 된다.
- Reservation 업데이트와 TimeSlot 업데이트는 Transaction으로 처리한다.

---

# 5.6 예약 거절

## 설명

Host가 PENDING 상태의 예약을 거절한다.

## 요구사항

- Host 본인만 거절할 수 있다.
- PENDING 상태만 거절할 수 있다.
- 거절 사유는 필수다.
- 거절 시 Reservation 상태는 REJECTED가 된다.
- TimeSlot 상태는 변경하지 않는다.

---

# 5.7 예약 취소

## 설명

Host 또는 Guest가 예약을 취소한다.

## 요구사항

- Host 또는 해당 Guest만 취소할 수 있다.
- PENDING 또는 APPROVED 상태만 취소할 수 있다.
- 취소 시 Reservation 상태는 CANCELLED가 된다.
- APPROVED 예약 취소 시 TimeSlot 상태는 AVAILABLE로 되돌린다.
- APPROVED 예약 취소는 Transaction으로 처리한다.

---

# 5.8 푸시 알림

## 설명

예약 상태 변화에 따라 FCM 알림을 보낸다.

## 알림 종류

### 예약 신청

Host에게 발송.

문구:

```text
새로운 예약 신청이 도착했습니다.
```

### 예약 승인

Guest에게 발송.

문구:

```text
예약이 승인되었습니다.
```

### 예약 거절

Guest에게 발송.

문구:

```text
예약이 거절되었습니다.
```

---

# 5.9 공유 링크

## 설명

Host는 자신의 예약 링크를 공유할 수 있다.

## 링크 형태

1차 MVP에서는 아래 형태 중 하나를 사용한다.

```text
https://app-domain.com/host/{reservationLinkId}
```

또는

```text
https://app-domain.com/u/{reservationLinkId}
```

## 요구사항

- 앱 설치 시 Android App Links로 앱을 실행한다.
- 앱 미설치 시 Firebase Hosting 랜딩 페이지를 보여준다.
- Firebase Dynamic Links는 사용하지 않는다.

---

# 6. 화면 구성

## 6.1 SplashScreen

### 기능

- 로그인 상태 확인
- 로그인 상태면 Home으로 이동
- 비로그인 상태면 Login으로 이동

---

## 6.2 LoginScreen

### 기능

- Google 로그인 버튼
- 로그인 실패 메시지 표시

---

## 6.3 HomeScreen

### 기능

- 내 프로필 요약
- 내 예약 링크 표시
- 예약 링크 공유
- 다가오는 예약 요약
- 받은 예약 신청 요약

---

## 6.4 ProfileScreen

### 기능

- 닉네임 수정
- 한 줄 소개 수정
- 프로필 이미지 수정
- 예약 링크 ID 수정

---

## 6.5 TimeSlotManagementScreen

### 기능

- 날짜 선택
- 시간 선택
- 30분/60분 선택
- 시간 슬롯 생성
- 시간 슬롯 목록 조회
- 시간 슬롯 비활성화

---

## 6.6 HostReservationPage

### 기능

- 공유 링크로 접근하는 화면
- Host 프로필 표시
- 예약 가능한 시간 표시
- 예약 목적 선택
- 메시지 입력
- 예약 신청

---

## 6.7 ReservationRequestsScreen

### 기능

- Host가 받은 예약 신청 목록 조회
- PENDING / APPROVED / REJECTED / CANCELLED 구분 표시

---

## 6.8 MyReservationsScreen

### 기능

- Guest가 신청한 예약 목록 조회
- 예약 상태 표시
- 예약 취소

---

## 6.9 ReservationDetailScreen

### 기능

- 예약 상세 정보 표시
- 예약 목적 표시
- 메시지 표시
- 승인
- 거절
- 취소

---

# 7. MVP 포함 기능

- Google 로그인
- 프로필
- 예약 가능 시간
- 예약 신청
- 승인
- 거절
- 취소
- FCM 알림
- 공유 링크
- Android App Links
- Firebase Hosting 랜딩

---

# 8. MVP 제외 기능

아래 기능은 MVP에서 구현하지 않는다.

- 비회원 예약
- 이메일 발송
- 채팅
- 결제
- 구독
- 그룹 예약
- 카카오 로그인
- 네이버 로그인
- 팔로우
- 댓글
- 후기
- 평점
- 소셜 피드
- 네이버 캘린더 연동
- 양방향 캘린더 동기화

---

# 9. 기술 스택

## Android

- Kotlin
- Jetpack Compose
- Navigation Compose
- Hilt
- Coil

## Firebase

- Firebase Auth
- Firestore
- Firebase Cloud Messaging
- Firebase Hosting

## Architecture

- MVVM
- Repository Pattern
- UiState
- SharedFlow 또는 Channel 기반 단발 이벤트

---

# 10. 개발 순서

## STEP 1. 프로젝트 세팅

- Compose
- Hilt
- Navigation
- Firebase 연동
- 기본 패키지 구조 생성

## STEP 2. Google 로그인

- Firebase Auth 연동
- 로그인 상태 관리
- 로그아웃

## STEP 3. 프로필

- users 생성
- 프로필 조회
- 프로필 수정

## STEP 4. 시간 슬롯

- 시간 슬롯 생성
- 시간 슬롯 조회
- 시간 슬롯 비활성화
- 중복 방지

## STEP 5. 예약 신청

- Host 예약 페이지
- 가능한 슬롯 조회
- 목적 선택
- 메시지 입력
- 예약 신청

## STEP 6. 예약 목록

- 받은 예약 목록
- 내가 신청한 예약 목록
- 상세 화면

## STEP 7. 승인 / 거절 / 취소

- 승인 Transaction
- 거절 사유 저장
- 취소 Transaction

## STEP 8. FCM

- 토큰 저장
- 알림 발송 구조 구현

## STEP 9. 공유 링크

- Firebase Hosting
- Android App Links
- Deep Link 처리

## STEP 10. QA

- 예외 처리
- 보안 규칙 점검
- 내부 테스트
