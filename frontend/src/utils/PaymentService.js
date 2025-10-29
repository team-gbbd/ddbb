/**
 * 포트원 결제 처리 함수
 * 
 * @param {Array} cartItems - 장바구니 아이템 목록
 * @param {Object} cartItems[].breadId - 빵 ID
 * @param {string} cartItems[].breadName - 빵 이름
 * @param {number} cartItems[].quantity - 수량
 * @param {number} cartItems[].price - 개당 가격
 * @param {Object} customer - 고객 정보 (선택)
 * @returns {Promise<Object>} 결제 결과
 */
export const handlePayment = async (cartItems, customer = null) => {
    try {
        // 1. 입력 검증
        if (!cartItems || cartItems.length === 0) {
            alert('장바구니가 비어있습니다.');
            return { success: false, error: '장바구니가 비어있습니다.' };
        }

        // 2. 총액 계산
        const totalAmount = cartItems.reduce((sum, item) => {
            return sum + (item.price * item.quantity);
        }, 0);

        // 3. 주문명 생성 (첫 번째 상품명 + 외 N건)
        const orderName = cartItems.length === 1
            ? cartItems[0].breadName
            : `${cartItems[0].breadName} 외 ${cartItems.length - 1}건`;

        // 4. 포트원 결제창 호출
        const paymentId = `payment-${Date.now()}`;
        const response = await window.PortOne.requestPayment({
            storeId: 'store-6b2d26e1-a195-4610-baeb-6f635652cdd4',
            channelKey: 'channel-key-16598416-3361-454d-b6ac-442fc41ee995',
            paymentId: paymentId,
            orderName: orderName,
            totalAmount: totalAmount,
            currency: 'CURRENCY_KRW',
            payMethod: 'EASY_PAY',
            // 🆕 장바구니 데이터를 customData로 전달
            // 김준기님의 PaymentService에서 이 데이터로 재고 차감 + 매출 기록
            customData: {
                items: cartItems.map(item => ({
                    breadId: item.breadId,
                    quantity: item.quantity,
                    price: item.price,
                })),
            },
            customer: customer || {
                fullName: 'testuser',
                phoneNumber: '010-1234-1234',
                email: 'test@test.co.kr',
            },
        });

        // 5. 결제 실패 처리
        if (response.code != null) {
            alert(`결제 실패: ${response.message}`);
            return { success: false, error: response.message };
        }

        console.log('결제 성공:', response);

        // 6. 김준기님의 백엔드로 결제 검증 요청
        // customData에 이미 장바구니 정보가 포함되어 있으므로
        // 김준기님의 PaymentService에서 자동으로 재고 차감 + 매출 기록이 실행됩니다!
        const verifyResponse = await fetch('http://localhost:8080/api/payment/complete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                paymentId: response.paymentId,
            }),
        });

        if (!verifyResponse.ok) {
            throw new Error('결제 검증에 실패했습니다.');
        }

        const paymentData = await verifyResponse.json();
        console.log('백엔드 처리 결과:', paymentData);

        // 7. 성공 메시지
        alert(`결제가 완료되었습니다!\n총 ${cartItems.length}개 상품 / ${totalAmount.toLocaleString()}원`);
        
        return { success: true, data: paymentData };

    } catch (error) {
        console.error('결제 오류:', error);
        alert(`결제 중 오류가 발생했습니다: ${error.message}`);
        return { success: false, error: error.message };
    }
};

