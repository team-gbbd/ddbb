import React from "react";
import Header from "../components/Header";
import "../styles/GuidePage.css";

const GuidePage = () => {
  return (
    <>
      <Header />
      <div className="guide-page">
        <div className="guide-container">
          <h1>이용 가이드</h1>
          <div className="guide-content">
            <section className="guide-section">
              <h2>🍞 서비스 소개</h2>
              <p>
                딤딤빵빵(DDBB)은 빵집을 위한 스마트 관리 시스템입니다.
                재고 관리부터 매출 분석까지 한 곳에서 관리하세요.
              </p>
            </section>

            <section className="guide-section">
              <h2>💳 결제 시작</h2>
              <ol>
                <li>메인 페이지에서 "결제 시작" 버튼을 클릭합니다</li>
                <li>원하는 빵을 선택합니다</li>
                <li>결제 정보를 입력합니다</li>
                <li>결제를 완료합니다</li>
              </ol>
            </section>

            <section className="guide-section">
              <h2>⚙️ 관리자 페이지</h2>
              <p>관리자 페이지에서는 다음 기능을 사용할 수 있습니다:</p>
              <ul>
                <li><strong>대시보드:</strong> 오늘의 매출 및 재고 현황을 한눈에 확인</li>
                <li><strong>재고 관리:</strong> 빵별 재고 조회 및 업데이트</li>
                <li><strong>매출 관리:</strong> 매출 등록 및 일별 조회</li>
                <li><strong>통계:</strong> 기간별 매출 통계 및 분석</li>
              </ul>
            </section>

            <section className="guide-section">
              <h2>📞 고객 지원</h2>
              <p>
                문의사항이 있으시면 언제든지 연락주세요.<br />
                이메일: support@ddbb.com<br />
                전화: 02-1234-5678
              </p>
            </section>
          </div>
        </div>
      </div>
    </>
  );
};

export default GuidePage;

