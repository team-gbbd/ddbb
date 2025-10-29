import React from 'react';

const EmptyState = ({ icon, message }) => {
  return (
    <div className="empty-state">
      <i className={`bi ${icon}`}></i>
      <p>{message}</p>
    </div>
  );
};

export default EmptyState;

