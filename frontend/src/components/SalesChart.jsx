import React from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Line, Bar } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

// 판매량 예측 그래프 (과거 + 예측)
export const SalesPredictionChart = ({ chartData }) => {
  if (!chartData) return null;

  const historicalDates = Object.keys(chartData.historicalSales || {});
  const predictedDates = Object.keys(chartData.predictedSales || {});
  const allDates = [...historicalDates, ...predictedDates];

  const data = {
    labels: allDates.map(date => {
      const d = new Date(date);
      return `${d.getMonth() + 1}/${d.getDate()}`;
    }),
    datasets: [
      {
        label: '과거 판매량',
        data: [
          ...Object.values(chartData.historicalSales || {}),
          ...new Array(predictedDates.length).fill(null),
        ],
        borderColor: '#8b6f47',
        backgroundColor: 'rgba(139, 111, 71, 0.1)',
        borderWidth: 3,
        fill: true,
        tension: 0.4,
      },
      {
        label: '예측 판매량',
        data: [
          ...new Array(historicalDates.length - 1).fill(null),
          historicalDates.length > 0
            ? Object.values(chartData.historicalSales || {})[historicalDates.length - 1]
            : null,
          ...Object.values(chartData.predictedSales || {}),
        ],
        borderColor: '#e6b88a',
        backgroundColor: 'rgba(230, 184, 138, 0.1)',
        borderWidth: 3,
        borderDash: [5, 5],
        fill: true,
        tension: 0.4,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          font: { size: 14, weight: 'bold' },
          color: '#5c4a30',
        },
      },
      title: {
        display: true,
        text: '판매량 예측 (7일)',
        font: { size: 18, weight: 'bold' },
        color: '#5c4a30',
      },
      tooltip: {
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        titleColor: '#5c4a30',
        bodyColor: '#5c4a30',
        borderColor: '#8b6f47',
        borderWidth: 2,
        padding: 12,
        displayColors: true,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          color: '#5c4a30',
          font: { size: 12 },
        },
        grid: {
          color: 'rgba(139, 111, 71, 0.1)',
        },
      },
      x: {
        ticks: {
          color: '#5c4a30',
          font: { size: 12 },
        },
        grid: {
          display: false,
        },
      },
    },
  };

  return <Line data={data} options={options} height={300} />;
};

// 수익 예측 그래프
export const RevenuePredictionChart = ({ chartData }) => {
  if (!chartData) return null;

  const historicalDates = Object.keys(chartData.historicalRevenue || {});
  const predictedDates = Object.keys(chartData.predictedRevenue || {});
  const allDates = [...historicalDates, ...predictedDates];

  const data = {
    labels: allDates.map(date => {
      const d = new Date(date);
      return `${d.getMonth() + 1}/${d.getDate()}`;
    }),
    datasets: [
      {
        label: '과거 수익',
        data: [
          ...Object.values(chartData.historicalRevenue || {}),
          ...new Array(predictedDates.length).fill(null),
        ],
        backgroundColor: 'rgba(122, 155, 111, 0.8)',
        borderColor: '#7a9b6f',
        borderWidth: 2,
      },
      {
        label: '예측 수익',
        data: [
          ...new Array(historicalDates.length).fill(null),
          ...Object.values(chartData.predictedRevenue || {}),
        ],
        backgroundColor: 'rgba(212, 165, 116, 0.8)',
        borderColor: '#d4a574',
        borderWidth: 2,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          font: { size: 14, weight: 'bold' },
          color: '#5c4a30',
        },
      },
      title: {
        display: true,
        text: '수익 예측 (만원)',
        font: { size: 18, weight: 'bold' },
        color: '#5c4a30',
      },
      tooltip: {
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        titleColor: '#5c4a30',
        bodyColor: '#5c4a30',
        borderColor: '#8b6f47',
        borderWidth: 2,
        padding: 12,
        callbacks: {
          label: (context) => {
            const value = context.parsed.y;
            return `${context.dataset.label}: ₩${(value / 10000).toLocaleString()}만원`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          color: '#5c4a30',
          font: { size: 12 },
          callback: (value) => `₩${(value / 10000).toFixed(1)}만`,
        },
        grid: {
          color: 'rgba(139, 111, 71, 0.1)',
        },
      },
      x: {
        ticks: {
          color: '#5c4a30',
          font: { size: 12 },
        },
        grid: {
          display: false,
        },
      },
    },
  };

  return <Bar data={data} options={options} height={300} />;
};

// 빵별 판매 비교 (과거 vs 예측)
export const BreadComparisonChart = ({ chartData }) => {
  if (!chartData) return null;

  const breadNames = Object.keys(chartData.breadHistoricalSales || {});

  const data = {
    labels: breadNames,
    datasets: [
      {
        label: '과거 판매량',
        data: Object.values(chartData.breadHistoricalSales || {}),
        backgroundColor: 'rgba(139, 111, 71, 0.8)',
        borderColor: '#8b6f47',
        borderWidth: 2,
      },
      {
        label: '다음 주 예측',
        data: breadNames.map(name => (chartData.breadPredictedSales || {})[name] || 0),
        backgroundColor: 'rgba(230, 184, 138, 0.8)',
        borderColor: '#e6b88a',
        borderWidth: 2,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          font: { size: 14, weight: 'bold' },
          color: '#5c4a30',
        },
      },
      title: {
        display: true,
        text: '빵별 판매량 비교 (기간 총합 vs 다음 주 예측)',
        font: { size: 18, weight: 'bold' },
        color: '#5c4a30',
      },
      tooltip: {
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        titleColor: '#5c4a30',
        bodyColor: '#5c4a30',
        borderColor: '#8b6f47',
        borderWidth: 2,
        padding: 12,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          color: '#5c4a30',
          font: { size: 12 },
        },
        grid: {
          color: 'rgba(139, 111, 71, 0.1)',
        },
      },
      x: {
        ticks: {
          color: '#5c4a30',
          font: { size: 12 },
        },
        grid: {
          display: false,
        },
      },
    },
  };

  return <Bar data={data} options={options} height={300} />;
};

// 성장률 그래프 (과거 + 예측)
export const GrowthRateChart = ({ chartData }) => {
  if (!chartData || !chartData.growthRates) {
    console.log('GrowthRateChart: No chartData or growthRates');
    return null;
  }

  // 과거 성장률과 예측 성장률 분리
  const historicalGrowth = chartData.growthRates || [];
  const predictedGrowth = chartData.predictedGrowthRates || [];
  
  console.log('GrowthRateChart - Historical Growth:', historicalGrowth);
  console.log('GrowthRateChart - Predicted Growth:', predictedGrowth);
  
  // 모든 날짜 레이블 생성
  const allGrowthData = [...historicalGrowth, ...predictedGrowth];
  
  if (allGrowthData.length === 0) {
    console.log('GrowthRateChart: No growth data available');
    return (
      <div className="text-center text-muted py-5">
        <p>성장률 데이터가 없습니다.</p>
        <small>최소 2일 이상의 판매 데이터가 필요합니다.</small>
      </div>
    );
  }
  
  const labels = allGrowthData.map(item => {
    const d = new Date(item.date);
    return `${d.getMonth() + 1}/${d.getDate()}`;
  });

  // 과거 데이터 (예측 부분은 null)
  const historicalData = [
    ...historicalGrowth.map(item => item.growthRate),
    ...new Array(predictedGrowth.length).fill(null),
  ];

  // 예측 데이터 (과거 부분은 null, 연결점만 마지막 과거 데이터)
  const predictedData = [
    ...new Array(historicalGrowth.length - 1).fill(null),
    historicalGrowth.length > 0 
      ? historicalGrowth[historicalGrowth.length - 1].growthRate 
      : null,
    ...predictedGrowth.map(item => item.growthRate),
  ];

  const data = {
    labels,
    datasets: [
      {
        label: '과거 성장률',
        data: historicalData,
        borderColor: '#7a9b6f',
        backgroundColor: (context) => {
          const value = context.parsed?.y;
          return value >= 0
            ? 'rgba(122, 155, 111, 0.2)'
            : 'rgba(201, 146, 122, 0.2)';
        },
        borderWidth: 3,
        fill: true,
        tension: 0.4,
        segment: {
          borderColor: (context) => {
            const value = context.p1.parsed.y;
            return value >= 0 ? '#7a9b6f' : '#c9927a';
          },
        },
      },
      {
        label: '예측 성장률',
        data: predictedData,
        borderColor: '#d4a574',
        backgroundColor: (context) => {
          const value = context.parsed?.y;
          return value >= 0
            ? 'rgba(212, 165, 116, 0.2)'
            : 'rgba(230, 184, 138, 0.2)';
        },
        borderWidth: 3,
        borderDash: [5, 5],
        fill: true,
        tension: 0.4,
        segment: {
          borderColor: (context) => {
            const value = context.p1.parsed.y;
            return value >= 0 ? '#d4a574' : '#e6b88a';
          },
        },
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          font: { size: 14, weight: 'bold' },
          color: '#5c4a30',
        },
      },
      title: {
        display: true,
        text: '성장률 분석 및 예측 (%)',
        font: { size: 18, weight: 'bold' },
        color: '#5c4a30',
      },
      tooltip: {
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        titleColor: '#5c4a30',
        bodyColor: '#5c4a30',
        borderColor: '#8b6f47',
        borderWidth: 2,
        padding: 12,
        callbacks: {
          label: (context) => {
            const value = context.parsed.y;
            return `${context.dataset.label}: ${value >= 0 ? '+' : ''}${value.toFixed(1)}%`;
          },
        },
      },
    },
    scales: {
      y: {
        ticks: {
          color: '#5c4a30',
          font: { size: 12 },
          callback: (value) => `${value >= 0 ? '+' : ''}${value}%`,
        },
        grid: {
          color: (context) => {
            return context.tick.value === 0
              ? '#8b6f47'
              : 'rgba(139, 111, 71, 0.1)';
          },
          lineWidth: (context) => (context.tick.value === 0 ? 2 : 1),
        },
      },
      x: {
        ticks: {
          color: '#5c4a30',
          font: { size: 12 },
        },
        grid: {
          display: false,
        },
      },
    },
  };

  return <Line data={data} options={options} height={300} />;
};

