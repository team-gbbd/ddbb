import React from "react";
import "../components/MainPage.css";
import Header from "../assets/Header";
import {handlePayment} from "../utils/PaymentService"

const MainPage = () => {


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
                <span>빵집 사진</span>
              </div>
            </div>
            <div className="banner-text">
              <h1>빵집 스캔</h1>
              <p className="subtitle">맛있는 빵과 달콤한 추억을 찾아보세요</p>
              <p className="description">
                우리의 빵집 스캔 서비스는 최고의 빵집 정보를 제공하며, 편리한
                결제와 관리 시스템으로 운영됩니다. 신선한 빵과 함께 특별한
                하루를 시작하세요.
              </p>
            </div>
          </div>
        </section>

        {/* 액션 버튼 섹션 */}
        <section className="action-section">
          <div className="action-container">
            <button
              className="action-button payment"
              onClick={handlePayment}
            >
              <div className="button-icon">💳</div>
              <div className="button-content">
                <h3>결제 시작</h3>
                <p>빠르고 편리한 결제를 시작하세요</p>
              </div>
            </button>

            <button className="action-button admin" onClick={handleAdminPage}>
              <div className="button-icon">⚙️</div>
              <div className="button-content">
                <h3>관리자 페이지</h3>
                <p>상품 및 주문을 관리하세요</p>
              </div>
            </button>
          </div>
        </section>
      </div>
    </>
  );
};

export default MainPage;
