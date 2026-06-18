# AGENTS.md

# AI 개발 규칙

이 프로젝트는 개인 예약 링크 앱 MVP 개발 프로젝트다.

Codex는 작업을 시작하기 전에 반드시 아래 문서를 먼저 읽는다.

1. `docs/PRD.md`
2. `docs/FIRESTORE_SCHEMA.md`

---

# 1. 핵심 원칙

- MVP 검증이 목적이다.
- 문서에 없는 기능은 구현하지 않는다.
- 한 번에 전체 기능을 구현하지 않는다.
- 반드시 STEP 단위로 개발한다.
- 사용자 승인 없이 다음 STEP으로 넘어가지 않는다.
- 빌드 실패 상태에서는 새 기능을 추가하지 않는다.
- 기능 추가보다 빌드 성공을 우선한다.
- 임의로 데이터 모델, 필드명, enum 값을 변경하지 않는다.

---

# 2. 개발 진행 방식

각 STEP을 진행할 때 반드시 아래 순서를 따른다.

1. 작업 범위 요약
2. 변경 예정 파일 목록 설명
3. 코드 구현
4. 빌드 또는 컴파일 확인
5. 완료 내용 요약
6. 다음 STEP 진행 여부 확인

---

# 3. 아키텍처 규칙

- Android Native Kotlin으로 개발한다.
- UI는 Jetpack Compose를 사용한다.
- MVVM 구조를 따른다.
- Repository Pattern을 사용한다.
- ViewModel에서 Firebase를 직접 호출하지 않는다.
- Firebase 접근은 Repository에서 처리한다.
- Compose UI는 가능한 Stateless Composable로 작성한다.
- 화면 상태는 ViewModel의 UiState로 관리한다.
- 단발 이벤트는 SharedFlow 또는 Channel을 사용한다.

---

# 4. Firebase 규칙

- Firebase Auth는 Google 로그인만 사용한다.
- Firestore 컬렉션/필드 구조는 `docs/FIRESTORE_SCHEMA.md`를 따른다.
- 예약 승인/취소처럼 여러 문서를 동시에 수정하는 기능은 Transaction으로 처리한다.
- 비회원 예약은 MVP에서 구현하지 않는다.
- 이메일 발송 API는 MVP에서 구현하지 않는다.

---

# 5. MVP 제외 기능

아래 기능은 절대 구현하지 않는다.

- 채팅
- 결제
- 구독
- 그룹 예약
- 카카오 로그인
- 네이버 로그인
- 비회원 예약
- 이메일 발송
- 후기
- 평점
- 팔로우
- 댓글
- 소셜 피드
- 네이버 캘린더 연동
- 양방향 캘린더 동기화

---

# 6. 개발 STEP

아래 순서대로만 개발한다.

## STEP 1. 프로젝트 기본 세팅

- Compose
- Hilt
- Navigation Compose
- Firebase Auth
- Firestore
- FCM
- Coil

## STEP 2. Google 로그인

- Firebase Auth 연동
- 로그인 상태 확인
- 로그아웃

## STEP 3. 프로필

- users 문서 생성
- 프로필 조회
- 프로필 수정

## STEP 4. 시간 슬롯

- 시간 슬롯 생성
- 시간 슬롯 조회
- 시간 슬롯 비활성화
- 중복 시간 생성 방지

## STEP 5. 예약 신청

- Host 예약 링크 진입
- 가능한 슬롯 조회
- 예약 목적 선택
- 메시지 입력
- 예약 신청 생성

## STEP 6. 예약 목록

- Host가 받은 예약 목록
- Guest가 신청한 예약 목록
- 예약 상세

## STEP 7. 승인 / 거절 / 취소

- 예약 승인
- 예약 거절
- 거절 사유 저장
- 예약 취소
- Transaction 처리

## STEP 8. FCM 알림

- 예약 신청 알림
- 승인 알림
- 거절 알림

## STEP 9. 공유 링크 / App Links

- Firebase Hosting 기반 링크
- Android App Links
- 앱 미설치 시 랜딩 페이지

## STEP 10. QA / 배포 준비

- 버그 수정
- 예외 처리
- Firestore Rule 재검토
- Play Store 내부 테스트 준비
