import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

export type UserRole = 'Employee' | 'Approver' | 'Admin';

interface ProtectedRouteProps {
  allowedRoles?: UserRole[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ allowedRoles }) => {
  const isAuthenticated = true; // Tạm để true để test giao diện
  const currentUserRole: UserRole = 'Admin'; // Có thể đổi thành 'Employee' hoặc 'Approver' để kiểm tra phân quyền

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(currentUserRole)) {
    return <Navigate to="/my-requests" replace />;
  }

  return <Outlet />;
};