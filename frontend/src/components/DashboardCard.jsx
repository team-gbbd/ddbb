import React from 'react';

const DashboardCard = ({ title, value, icon, bgColor }) => {
  return (
    <div className="col-md-3">
      <div className={`card text-white bg-${bgColor}`}>
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <h6 className="card-subtitle mb-2">{title}</h6>
              <h3 className="card-title mb-0">{value}</h3>
            </div>
            <i className={`bi ${icon} fs-1`}></i>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardCard;

