import React from 'react';

const LoadingSpinner = () => {
  return (
    <div className="text-center py-5">
      <div className="spinner-border text-primary" role="status">
        <span className="visually-hidden">로딩 중...</span>
      </div>
      <p className="mt-3 text-muted">데이터를 불러오는 중입니다...</p>
    </div>
  );
};

export default LoadingSpinner;

