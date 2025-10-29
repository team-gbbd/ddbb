import React from "react";
import Header from "../assets/Header";
import "../styles/Payment.css";
import {handlePayment} from "../utils/PaymentService"

const dummyBreads = [
  { name: "통밀빵", price: 4300 },
  { name: "호두파이", price: 4700 },
  { name: "크라상", price: 3200 },
  { name: "츄러스꽈배기", price: 3500 },
  { name: "에그마요소금빵", price: 4500 },
  { name: "초코청크머핀", price: 4500 },
  { name: "다크초코피넛버터쿠키", price: 4200 },
  { name: "소금빵", price: 2800 },
  { name: "소시지페스트리", price: 4500 },
];

const Payment = () => {
  // 더미 데이터를 실제 장바구니 형식으로 변환
  // TODO: 실제로는 AI 스캔 결과나 장바구니에서 가져와야 함
  const cartItems = dummyBreads.map((bread, index) => ({
    breadId: index + 1, // 실제로는 빵의 실제 ID를 사용해야 함
    breadName: bread.name,
    quantity: 1,
    price: bread.price,
  }));

  const total = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  const onPaymentClick = () => {
    // 장바구니 데이터를 handlePayment에 전달
    handlePayment(cartItems);
  };

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
          <ul className="bread-list">
            {cartItems.map((item, idx) => (
              <li key={idx}>
                <span className="bread-name">
                  {item.breadName} x {item.quantity}
                </span>
                <span className="bread-price">
                  {(item.price * item.quantity).toLocaleString()}원
                </span>
              </li>
            ))}
          </ul>
          <div className="total-price">
            총 합계: <span>{total.toLocaleString()}원</span>
          </div>
          <button className="pay-btn" onClick={onPaymentClick}>결제하기</button>
        </div>
      </div>
    </>
  );
};

export default Payment;

