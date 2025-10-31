# DDBB 프론트엔드

딤딤빵빵(DDBB) 빵집 관리 시스템의 프론트엔드 애플리케이션입니다.

## 기술 스택

- **Vite** - 빠른 빌드 도구
- **React 19** - UI 라이브러리
- **React Router** - 라우팅
- **Axios** - HTTP 클라이언트
- **Bootstrap 5** - UI 프레임워크
- **Bootstrap Icons** - 아이콘

## 페이지 구조

### 메인 페이지
- `/` - 메인 랜딩 페이지
- `/guide` - 이용 가이드

### 관리자 페이지
- `/admin` - 대시보드
- `/admin/inventory` - 재고 관리
- `/admin/sales` - 매출 관리
- `/admin/statistics` - 통계

## 설치 및 실행

### 개발 서버 실행

```bash
npm install
npm run dev
```

브라우저에서 http://localhost:3000 으로 접속합니다.

### 프로덕션 빌드

```bash
npm run build
npm run preview
```

## 백엔드 연동

프록시 설정이 `vite.config.js`에 구성되어 있어, `/api` 경로로의 요청은 자동으로 `http://localhost:8080`로 전달됩니다.

백엔드 서버가 실행 중이어야 모든 기능이 정상 작동합니다.

## 프로젝트 구조

```
src/
├── components/          # 재사용 가능한 컴포넌트
│   ├── Header.jsx
│   ├── AdminLayout.jsx
│   ├── AdminNavigation.jsx
│   ├── DashboardCard.jsx
│   ├── LoadingSpinner.jsx
│   └── EmptyState.jsx
├── pages/              # 페이지 컴포넌트
│   ├── MainPage.jsx
│   ├── GuidePage.jsx
│   └── admin/
│       ├── AdminDashboard.jsx
│       ├── Inventory.jsx
│       ├── Sales.jsx
│       └── Statistics.jsx
├── services/           # API 서비스
│   └── api.js
├── styles/             # 스타일 파일
│   ├── MainPage.css
│   ├── GuidePage.css
│   └── Admin.css
├── utils/              # 유틸리티 함수
│   └── formatters.js
├── App.jsx             # 메인 앱 컴포넌트
├── main.jsx            # 엔트리 포인트
└── index.css           # 전역 스타일
```

## 주요 기능

### 메인 페이지
- 빵집 소개
- 결제 시작 버튼 (준비 중)
- 관리자 페이지 진입

### 관리자 대시보드
- 오늘의 매출 및 판매량
- 저재고 알림
- 최근 매출 내역

### 재고 관리
- 전체 재고 조회
- 재고 검색
- 재고 수량 업데이트

### 매출 관리
- 매출 등록
- 일별 매출 조회

### 통계
- 기간별 매출 통계
- 빵별 판매 현황
- 판매 비율 시각화

## 라이선스

MIT
