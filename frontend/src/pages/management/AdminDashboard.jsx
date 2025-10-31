import React, { useEffect, useState } from 'react';
import AdminLayout from '../../components/AdminLayout';
import DashboardCard from '../../components/DashboardCard';
import LoadingSpinner from '../../components/LoadingSpinner';
import EmptyState from '../../components/EmptyState';
import { inventoryApi, salesApi } from '../../services/management/api';
import { formatCurrency, formatDateTime, getToday } from '../../utils/formatters';
import '../../styles/management/Admin.css';

const AdminDashboard = () => {
  const [loading, setLoading] = useState(true);
  const [todaySales, setTodaySales] = useState(0);
  const [todayQuantity, setTodayQuantity] = useState(0);
  const [lowStockCount, setLowStockCount] = useState(0);
  const [totalStock, setTotalStock] = useState(0);
  const [lowStockList, setLowStockList] = useState([]);
  const [recentSales, setRecentSales] = useState([]);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      const today = getToday();
      
      // 오늘 시작과 끝 시간 (ISO DateTime 형식)
      const todayStart = `${today}T00:00:00`;
      const todayEnd = `${today}T23:59:59`;

      const [inventoryRes, dailySalesRes, salesListRes, lowStockRes] = await Promise.all([
        inventoryApi.getAll(),
        salesApi.getDaily(today),
        salesApi.getByPeriod(todayStart, todayEnd),
        inventoryApi.getLowStock(),
      ]);

      const inventory = inventoryRes.data;
      const dailySales = dailySalesRes.data;
      const salesList = salesListRes.data;
      const lowStock = lowStockRes.data;

      // dailySales는 이제 단일 객체 (DailySalesResponse)
      const totalSalesAmount = dailySales.totalRevenue || 0;
      const totalQuantityAmount = dailySales.totalQuantity || 0;
      const totalStockAmount = inventory.reduce((sum, item) => sum + (item.quantity || 0), 0);

      setTodaySales(totalSalesAmount);
      setTodayQuantity(totalQuantityAmount);
      setLowStockCount(lowStock.length);
      setTotalStock(totalStockAmount);
      setLowStockList(lowStock);
      
      // 최근 5개 판매 내역 표시
      setRecentSales(salesList.slice(0, 5));
    } catch (error) {
      console.error('대시보드 데이터 로드 실패:', error);
      alert('대시보드 데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <AdminLayout>
        <LoadingSpinner />
      </AdminLayout>
    );
  }

  return (
    <AdminLayout>
      <div className="fade-in">
        <div className="row mb-4">
          <div className="col-12">
            <h2>
              <i className="bi bi-speedometer2"></i> 대시보드
            </h2>
            <p className="text-muted">빵집 운영 현황을 한눈에 확인하세요</p>
          </div>
        </div>

        <div className="row g-3 mb-4">
          <DashboardCard
            title="오늘 매출"
            value={formatCurrency(todaySales)}
            icon="bi-cash-stack"
            bgColor="primary"
          />
          <DashboardCard
            title="오늘 판매량"
            value={`${todayQuantity}개`}
            icon="bi-bag-check"
            bgColor="success"
          />
          <DashboardCard
            title="저재고 품목"
            value={`${lowStockCount}개`}
            icon="bi-exclamation-triangle"
            bgColor="warning"
          />
          <DashboardCard
            title="총 재고"
            value={`${totalStock}개`}
            icon="bi-boxes"
            bgColor="info"
          />
        </div>

        <div className="row">
          <div className="col-md-6">
            <div className="card">
              <div className="card-header bg-danger text-white">
                <h5 className="mb-0">
                  <i className="bi bi-exclamation-circle"></i> 저재고 알림
                </h5>
              </div>
              <div className="card-body">
                {lowStockList.length === 0 ? (
                  <EmptyState
                    icon="bi-check-circle text-success"
                    message="모든 재고가 충분합니다!"
                  />
                ) : (
                  <div className="list-group">
                    {lowStockList.map((item) => (
                      <div
                        key={item.id}
                        className="list-group-item d-flex justify-content-between align-items-center"
                      >
                        <div>
                          <h6 className="mb-1">{item.breadName}</h6>
                          <small className="text-danger">
                            <i className="bi bi-exclamation-triangle"></i> 현재: {item.quantity}개 /
                            최소: {item.minStockLevel}개
                          </small>
                        </div>
                        <span className="badge bg-danger">{item.quantity}개</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card">
              <div className="card-header bg-primary text-white">
                <h5 className="mb-0">
                  <i className="bi bi-clock-history"></i> 최근 매출
                </h5>
              </div>
              <div className="card-body">
                {recentSales.length === 0 ? (
                  <EmptyState icon="bi-cart-x" message="오늘 판매된 상품이 없습니다." />
                ) : (
                  <div className="list-group">
                    {recentSales.map((sale) => (
                      <div
                        key={sale.id}
                        className="list-group-item d-flex justify-content-between align-items-center"
                      >
                        <div>
                          <h6 className="mb-1">{sale.breadName}</h6>
                          <small className="text-muted">
                            <i className="bi bi-clock"></i> {formatDateTime(sale.saleDate)}
                          </small>
                        </div>
                        <div className="text-end">
                          <div>
                            <strong>{formatCurrency(sale.totalPrice)}</strong>
                          </div>
                          <small className="text-muted">{sale.quantity}개</small>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </AdminLayout>
  );
};

export default AdminDashboard;

