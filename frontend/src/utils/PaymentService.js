// 포트원 결제 처리 함수
export const handlePayment = async () => {
    try {
        // 1. 포트원 결제창 호출
        const response = await window.PortOne.requestPayment({
            storeId: 'store-6b2d26e1-a195-4610-baeb-6f635652cdd4',
            channelKey: 'channel-key-16598416-3361-454d-b6ac-442fc41ee995',
            paymentId: `payment-${Date.now()}`,
            orderName: '빵',
            totalAmount: 1000,
            currency: 'CURRENCY_KRW',
            payMethod: 'EASY_PAY',
            customer: {
                fullName: 'testuser',
                phoneNumber: '010-1234-1234',
                email: 'test@test.co.kr',
            },
        });

        // 2. 결제 실패 처리
        if (response.code != null) {
            alert(`결제 실패: ${response.message}`);
            return { success: false, error: response.message };
        }

        console.log('결제 성공:', response);

        // 3. 백엔드로 결제 검증 요청
        const verifyResponse = await fetch('http://localhost:8080/api/payment/complete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                paymentId: response.paymentId,
            }),
        });

        const paymentData = await verifyResponse.json();
        console.log('백엔드 검증 결과:', paymentData);

        alert('결제가 완료되었습니다!');
        return { success: true, data: paymentData };

    } catch (error) {
        console.error('결제 오류:', error);
        alert('결제 중 오류가 발생했습니다.');
        return { success: false, error: error.message };
    }
};
