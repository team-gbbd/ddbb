import React, { useState, useEffect } from "react";
import Header from "../components/Header";
import "../styles/Payment.css";
import {handlePayment} from "../utils/PaymentService"

const Payment = () => {
  const [breads, setBreads] = useState([]);
  const [loading, setLoading] = useState(false);

  // AI Scanner에서 감지된 빵 데이터를 가져오는 함수
  // 실제로는 BreadScanner에서 navigate로 데이터를 전달받거나
  // 세션 스토리지를 사용할 수 있습니다
  useEffect(() => {
    // 세션 스토리지에서 스캔된 빵 데이터 가져오기
    const scannedData = sessionStorage.getItem('scannedBreads');
    if (scannedData) {
      const parsedData = JSON.parse(scannedData);
      setBreads(parsedData);
    }
  }, []);

  const total = breads.reduce((sum, bread) => sum + (bread.price * bread.count), 0);

  return (
    <>
      <Header />
      <div className="payment-container">
        <div className="scan-box">
          <div className="scan-placeholder">
            빵 스캔 영역
            <br />
            (AI 연동)
          </div>
        </div>
        <div className="pos-box">
          <h2>스캔 내역</h2>
          {breads.length === 0 ? (
            <div className="empty-cart">
              <p>스캔된 빵이 없습니다.</p>
              <p>AI 스캐너에서 빵을 스캔해주세요.</p>
            </div>
          ) : (
            <ul className="bread-list">
              {breads.map((bread, idx) => (
                <li key={idx}>
                  <span className="bread-name">
                    {bread.korean_name || bread.name} x {bread.count}
                  </span>
                  <span className="bread-price">
                    {(bread.price * bread.count).toLocaleString()}원
                  </span>
                </li>
              ))}
            </ul>
          )}
          <div className="total-price">
            총 합계: <span>{total.toLocaleString()}원</span>
          </div>
          <button className="pay-btn" onClick={handlePayment}>결제하기</button>
        </div>
      </div>
    </>
  );
};

export default Payment;

