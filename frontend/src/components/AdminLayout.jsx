import React from 'react';
import AdminNavigation from './AdminNavigation';

const AdminLayout = ({ children }) => {
  return (
    <div className="app-container">
      <AdminNavigation />
      <div className="container-fluid mt-4">
        {children}
      </div>
    </div>
  );
};

export default AdminLayout;

