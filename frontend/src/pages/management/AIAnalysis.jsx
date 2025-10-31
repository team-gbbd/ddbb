import React, { useState } from 'react';
import AdminLayout from '../../components/AdminLayout';
import LoadingSpinner from '../../components/LoadingSpinner';
import { aiAnalysisApi } from '../../services/management/api';
import { getToday, getFirstDayOfMonth } from '../../utils/formatters';
import {
  SalesPredictionChart,
  RevenuePredictionChart,
  BreadComparisonChart,
  GrowthRateChart,
} from '../../components/SalesChart';
import '../../styles/management/Admin.css';

const AIAnalysis = () => {
  const [loading, setLoading] = useState(false);
  const [startDate, setStartDate] = useState(getFirstDayOfMonth());
  const [endDate, setEndDate] = useState(getToday());
  const [analysisType, setAnalysisType] = useState('COMPREHENSIVE');
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleAnalyze = async () => {
    if (!startDate || !endDate) {
      alert('시작일과 종료일을 선택해주세요.');
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await aiAnalysisApi.analyze({
        startDate,
        endDate,
        analysisType,
      });
      setResult(response.data);
    } catch (err) {
      console.error('AI 분석 실패:', err);
      setError('AI 분석에 실패했습니다. OpenAI API 키를 확인해주세요.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickAnalysis = async () => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await aiAnalysisApi.quickAnalysis();
      setResult(response.data);
    } catch (err) {
      console.error('빠른 분석 실패:', err);
      setError('AI 분석에 실패했습니다. OpenAI API 키를 확인해주세요.');
    } finally {
      setLoading(false);
    }
  };

  const getAnalysisTypeLabel = (type) => {
    const labels = {
      COMPREHENSIVE: '종합 분석',
      SALES_PREDICTION: '판매량 예측',
      INVENTORY_RECOMMENDATION: '재고 수준 추천',
      OVERSTOCK_WARNING: '과잉 재고 경고',
      SHORTAGE_WARNING: '재고 부족 경고',
    };
    return labels[type] || type;
  };

  return (
    <AdminLayout>
      <div className="fade-in">
        <div className="row mb-4">
          <div className="col-12">
            <h2>
              <i className="bi bi-robot"></i> AI 분석
            </h2>
            <p className="text-muted">
              OpenAI를 활용한 지능형 재고 관리 및 판매 예측
            </p>
          </div>
        </div>

        {/* 분석 설정 카드 */}
        <div className="card mb-4">
          <div className="card-header bg-primary text-white">
            <h5 className="mb-0">
              <i className="bi bi-gear"></i> 분석 설정
            </h5>
          </div>
          <div className="card-body">
            <div className="row">
              <div className="col-md-3">
                <label htmlFor="startDate" className="form-label">
                  시작일
                </label>
                <input
                  type="date"
                  className="form-control"
                  id="startDate"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  disabled={loading}
                />
              </div>
              <div className="col-md-3">
                <label htmlFor="endDate" className="form-label">
                  종료일
                </label>
                <input
                  type="date"
                  className="form-control"
                  id="endDate"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  disabled={loading}
                />
              </div>
              <div className="col-md-3">
                <label htmlFor="analysisType" className="form-label">
                  분석 타입
                </label>
                <select
                  className="form-select"
                  id="analysisType"
                  value={analysisType}
                  onChange={(e) => setAnalysisType(e.target.value)}
                  disabled={loading}
                >
                  <option value="COMPREHENSIVE">종합 분석</option>
                  <option value="SALES_PREDICTION">판매량 예측</option>
                  <option value="INVENTORY_RECOMMENDATION">재고 수준 추천</option>
                  <option value="OVERSTOCK_WARNING">과잉 재고 경고</option>
                  <option value="SHORTAGE_WARNING">재고 부족 경고</option>
                </select>
              </div>
              <div className="col-md-3">
                <label className="form-label">&nbsp;</label>
                <button
                  className="btn btn-primary w-100"
                  onClick={handleAnalyze}
                  disabled={loading}
                >
                  <i className="bi bi-cpu"></i> AI 분석 실행
                </button>
              </div>
            </div>
            <div className="row mt-3">
              <div className="col-md-3 ms-auto">
                <button
                  className="btn btn-success w-100"
                  onClick={handleQuickAnalysis}
                  disabled={loading}
                >
                  <i className="bi bi-lightning"></i> 빠른 분석 (최근 30일)
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 로딩 상태 */}
        {loading && (
          <div className="card">
            <div className="card-body text-center py-5">
              <LoadingSpinner />
              <p className="mt-3 text-muted">
                AI가 데이터를 분석하고 있습니다... (30초~1분 소요)
              </p>
            </div>
          </div>
        )}

        {/* 에러 메시지 */}
        {error && (
          <div className="alert alert-danger">
            <i className="bi bi-exclamation-triangle"></i> {error}
          </div>
        )}

        {/* 분석 결과 */}
        {result && !loading && (
          <>

            {/* 그래프 섹션 */}
            {result.chartData && (
              <>
                <div className="row mb-4">
                  <div className="col-12">
                    <h3 className="mb-3">
                      <i className="bi bi-bar-chart-line"></i> 데이터 시각화
                    </h3>
                  </div>
                </div>

                {/* 판매량 예측 그래프 */}
                <div className="card mb-4">
                  <div className="card-header bg-primary text-white">
                    <h5 className="mb-0">
                      <i className="bi bi-graph-up-arrow"></i> 판매량 예측
                    </h5>
                  </div>
                  <div className="card-body">
                    <SalesPredictionChart chartData={result.chartData} />
                  </div>
                </div>

                {/* 수익 예측 그래프 */}
                <div className="card mb-4">
                  <div className="card-header bg-success text-white">
                    <h5 className="mb-0">
                      <i className="bi bi-currency-dollar"></i> 수익 예측
                    </h5>
                  </div>
                  <div className="card-body">
                    <RevenuePredictionChart chartData={result.chartData} />
                  </div>
                </div>

                <div className="row">
                  {/* 빵별 비교 그래프 */}
                  <div className="col-lg-6">
                    <div className="card mb-4">
                      <div className="card-header bg-warning text-white">
                        <h5 className="mb-0">
                          <i className="bi bi-basket"></i> 빵별 판매 비교
                        </h5>
                      </div>
                      <div className="card-body">
                        <BreadComparisonChart chartData={result.chartData} />
                      </div>
                    </div>
                  </div>

                  {/* 성장률 그래프 */}
                  <div className="col-lg-6">
                    <div className="card mb-4">
                      <div className="card-header bg-info text-white">
                        <h5 className="mb-0">
                          <i className="bi bi-arrow-up-right"></i> 성장세 분석
                        </h5>
                      </div>
                      <div className="card-body">
                        <GrowthRateChart chartData={result.chartData} />
                      </div>
                    </div>
                  </div>
                </div>
              </>
            )}

            {/* 예측 데이터 (JSON - 옵션)
            {result.predictionData && result.predictionData !== '{}' && (
              <div className="card">
                <div className="card-header bg-secondary text-white">
                  <h5 className="mb-0">
                    <i className="bi bi-code-square"></i> 원시 예측 데이터 (JSON)
                  </h5>
                </div>
                <div className="card-body">
                  <pre className="bg-light p-3 rounded" style={{ fontSize: '0.85rem' }}>
                    {JSON.stringify(JSON.parse(result.predictionData), null, 2)}
                  </pre>
                </div>
              </div>
            )} */}
          </>
        )}

        {/* 초기 상태 */}
        {!result && !loading && !error && (
          <div className="card">
            <div className="card-body text-center py-5">
              <i className="bi bi-robot" style={{ fontSize: '4rem', opacity: 0.3 }}></i>
              <p className="mt-3 text-muted">
                분석 옵션을 선택하고 AI 분석을 실행해보세요.
              </p>
              <p className="text-muted">
                AI가 판매 데이터와 재고 현황을 분석하여<br />
                최적의 재고 관리 전략을 제안합니다.
              </p>
            </div>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default AIAnalysis;

