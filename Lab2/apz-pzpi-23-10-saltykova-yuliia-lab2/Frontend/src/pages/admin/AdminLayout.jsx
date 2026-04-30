import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from '../../components/layout/Sidebar';
import '../../styles/admin.css';

export default function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="admin-layout">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="admin-content">
        <Outlet />
      </div>
      <button className="sidebar-toggle" onClick={() => setSidebarOpen(!sidebarOpen)}>
        <span className="material-symbols-outlined">{sidebarOpen ? 'close' : 'menu'}</span>
      </button>
    </div>
  );
}
