import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import ddbblogo from '../assets/ddbblogo.png';

const AdminNavigation = () => {
  const location = useLocation();

  const isActive = (path) => {
    return location.pathname === path ? 'active' : '';
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary">
      <div className="container-fluid">
        <Link className="navbar-brand d-flex align-items-center" to="/">
          <img src={ddbblogo} alt="DDBB 로고" style={{ height: '40px', marginRight: '10px' }} />

        </Link>
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav">
            <li className="nav-item">
              <Link className={`nav-link ${isActive('/admin')}`} to="/admin">
                <i className="bi bi-speedometer2"></i> 대시보드
              </Link>
            </li>
            <li className="nav-item">
              <Link className={`nav-link ${isActive('/admin/inventory')}`} to="/admin/inventory">
                <i className="bi bi-box-seam"></i> 재고 관리
              </Link>
            </li>
            <li className="nav-item">
              <Link className={`nav-link ${isActive('/admin/statistics')}`} to="/admin/statistics">
                <i className="bi bi-graph-up"></i> 통계
              </Link>
            </li>
            <li className="nav-item">
              <Link className={`nav-link ${isActive('/admin/ai-analysis')}`} to="/admin/ai-analysis">
                <i className="bi bi-robot"></i> AI 분석
              </Link>
            </li>
            <li className="nav-item">
              <Link className={`nav-link ${isActive('/dashboard')}`} to="/dashboard">
                <i className="bi bi-bar-chart-line"></i> AI 대시보드
              </Link>
            </li>
          </ul>
          <ul className="navbar-nav ms-auto">
            <li className="nav-item">
              <Link className="nav-link" to="/">
                <i className="bi bi-house-door"></i> 메인으로
              </Link>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default AdminNavigation;

