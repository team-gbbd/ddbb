import React from "react";
import "../styles/MainPage.css";
import Header from "../assets/Header";
import MainLogo from "../assets/MainLogo.png";

const MainPage = () => {
  const handlePaymentStart = () => {
    // 결제 시작 로직
    window.location.href = "/Payment";
  };

  const handleAdminPage = () => {
    // 관리자 페이지 로직

    window.location.href = "/admin";
  };

  return (
    <>
      <Header />
      <div className="main-page">
        {/* 배너 섹션 */}
        <section className="banner-section">
          <div className="banner-content">
            <div className="banner-image">
              <div className="placeholder-image">
                <img src={MainLogo} alt="빵집 사진" />
              </div>
            </div>
            <div className="banner-text">
              <h1>딥딥빵빵</h1>
              <p className="subtitle">편리한 결제와 관리 시스템을 경험하세요</p>
              <p className="description">
                딥딥빵빵은 AI를 통해 빠르고 편리한 빵 스캔 및 빵 추천, 결제
                시스템을 제공합니다. 어쩌고저쩌고
              </p>
            </div>
          </div>
        </section>

        {/* 액션 버튼 섹션 */}
        <section className="action-section">
          <div className="action-container">
            <button
              className="action-button payment"
              onClick={handlePaymentStart}
            >
              <div className="button-icon">💳</div>
              <div className="button-content">
                <h3>결제 시작</h3>
                <p>빠르고 편리한 결제 시작</p>
              </div>
            </button>

            <button className="action-button admin" onClick={handleAdminPage}>
              <div className="button-icon">⚙️</div>
              <div className="button-content">
                <h3>관리자 대시보드</h3>
                <p>재고 및 매출 관리</p>
              </div>
            </button>
          </div>
        </section>
      </div>
    </>
  );
};

export default MainPage;

