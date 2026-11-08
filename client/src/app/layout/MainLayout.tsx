import React from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { useAuth } from '../providers/AuthProvider';
import { Navbar } from './Navbar';
import { Sidebar } from './Sidebar';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';

export const MainLayout: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <LoadingSpinner size="lg" label="Đang kiểm tra phiên làm việc..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <Navbar />
      <div className="flex flex-1">
        <Sidebar />
        <main className="flex-1 p-8 overflow-y-auto max-w-7xl">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
