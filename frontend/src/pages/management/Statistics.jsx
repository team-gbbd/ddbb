import React, { useState, useEffect } from 'react';
import AdminLayout from '../../components/AdminLayout';
import { salesApi } from '../../services/management/api';
import { formatCurrency, getToday, getFirstDayOfMonth } from '../../utils/formatters';
import '../../styles/management/Admin.css';

const Statistics = () => {
  const [startDate, setStartDate] = useState(getFirstDayOfMonth());
  const [endDate, setEndDate] = useState(getToday());
  const [summary, setSummary] = useState(null);
  const [showResults, setShowResults] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // 컴포넌트 마운트 시 자동으로 이번 달 통계 조회
    loadStatistics();
  }, []);

  const loadStatistics = async () => {
    if (!startDate || !endDate) {
      alert('시작일과 종료일을 선택해주세요.');
      return;
    }

    try {
      setLoading(true);
      const response = await salesApi.getSummary(startDate, endDate);
      setSummary(response.data);
      setShowResults(true);
    } catch (error) {
      console.error('통계 조회 실패:', error);
      alert('통계 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const getPercentage = (quantity) => {
    if (!summary || summary.totalQuantity === 0) return 0;
    return (quantity / summary.totalQuantity) * 100;
  };

  return (
    <AdminLayout>
      <div className="fade-in">
        <div className="row mb-4">
          <div className="col-12">
            <h2><i className="bi bi-graph-up"></i> 매출 통계</h2>
            <p className="text-muted">기간별 매출 통계를 확인하세요</p>
          </div>
        </div>

        <div className="card mb-4">
          <div className="card-body">
            <div className="row">
              <div className="col-md-4">
                <label htmlFor="startDate" className="form-label">시작일</label>
                <input
                  type="date"
                  className="form-control"
                  id="startDate"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                />
              </div>
              <div className="col-md-4">
                <label htmlFor="endDate" className="form-label">종료일</label>
                <input
                  type="date"
                  className="form-control"
                  id="endDate"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">&nbsp;</label>
                <button 
                  className="btn btn-primary w-100" 
                  onClick={loadStatistics}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                      조회 중...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-bar-chart"></i> 통계 조회
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>

        {loading && (
          <div className="text-center py-5">
            <div className="spinner-border text-primary" role="status">
              <span className="visually-hidden">로딩 중...</span>
            </div>
            <p className="mt-3 text-muted">통계 데이터를 불러오는 중입니다...</p>
          </div>
        )}

        {!loading && showResults && (
          <>
            {!summary || summary.totalSales === 0 ? (
              <div className="alert alert-warning">
                <i className="bi bi-info-circle"></i> 해당 기간에 매출 기록이 없습니다.
              </div>
            ) : (
              <>
                <div className="row mb-4">
                  <div className="col-md-4">
                    <div className="card text-white bg-primary">
                      <div className="card-body">
                        <h6 className="card-subtitle mb-2">총 매출액</h6>
                        <h3 className="card-title">{formatCurrency(summary.totalSales)}</h3>
                        <small>{startDate} ~ {endDate}</small>
                      </div>
                    </div>
                  </div>
                  <div className="col-md-4">
                    <div className="card text-white bg-success">
                      <div className="card-body">
                        <h6 className="card-subtitle mb-2">총 판매량</h6>
                        <h3 className="card-title">{summary.totalQuantity}개</h3>
                        <small>{startDate} ~ {endDate}</small>
                      </div>
                    </div>
                  </div>
                  <div className="col-md-4">
                    <div className="card text-white bg-info">
                      <div className="card-body">
                        <h6 className="card-subtitle mb-2">평균 단가</h6>
                        <h3 className="card-title">{formatCurrency(summary.averagePrice)}</h3>
                        <small>판매된 상품 평균</small>
                      </div>
                    </div>
                  </div>
                </div>

                {summary.breadSales && summary.breadSales.length > 0 && (
                  <div className="card">
                    <div className="card-header bg-primary text-white">
                      <h5 className="mb-0"><i className="bi bi-bar-chart"></i> 빵별 판매 현황</h5>
                    </div>
                    <div className="card-body">
                      <div className="table-responsive">
                        <table className="table table-hover">
                          <thead className="table-light">
                            <tr>
                              <th>순위</th>
                              <th>빵 이름</th>
                              <th>판매량</th>
                              <th>매출액</th>
                              <th>비율</th>
                            </tr>
                          </thead>
                          <tbody>
                            {summary.breadSales.map((item, index) => {
                              const percentage = getPercentage(item.totalQuantity);
                              return (
                                <tr key={item.breadName}>
                                  <td><span className="badge bg-primary">{index + 1}</span></td>
                                  <td><strong>{item.breadName}</strong></td>
                                  <td>{item.totalQuantity}개</td>
                                  <td>{formatCurrency(item.totalSales)}</td>
                                  <td>
                                    <div className="progress" style={{ height: '20px' }}>
                                      <div
                                        className="progress-bar"
                                        role="progressbar"
                                        style={{ width: `${percentage}%` }}
                                        aria-valuenow={percentage}
                                        aria-valuemin={0}
                                        aria-valuemax={100}
                                      >
                                        {percentage.toFixed(1)}%
                                      </div>
                                    </div>
                                  </td>
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </div>
                )}
              </>
            )}
          </>
        )}

        {!loading && !showResults && (
          <div className="alert alert-info">
            <i className="bi bi-info-circle"></i> 기간을 선택하고 통계를 조회하세요
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default Statistics;

