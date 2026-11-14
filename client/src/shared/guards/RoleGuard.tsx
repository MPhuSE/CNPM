import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/app/providers/AuthProvider';
import { RoleCode } from '@/shared/types/auth';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { ForbiddenPage } from '@/shared/pages/ForbiddenPage';

interface RoleGuardProps {
  children: React.ReactNode;
  allowedRoles?: RoleCode[];
}

/**
 * Route Guard kiểm tra quyền hạn trước khi render trang.
 * - Nếu chưa đăng nhập: Điều hướng về /login với state `from` để quay lại sau đăng nhập.
 * - Nếu đã đăng nhập nhưng không có vai trò phù hợp: Hiển thị trang 403 Forbidden.
 */
export const RoleGuard: React.FC<RoleGuardProps> = ({
  children,
  allowedRoles = ['ROLE_ADMIN'],
}) => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-[50vh] flex items-center justify-center">
        <LoadingSpinner size="lg" label="Đang kiểm tra quyền hạn truy cập..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const hasPermission = allowedRoles.some((role) => user?.roles?.includes(role));

  if (!hasPermission) {
    return <ForbiddenPage requiredRole={allowedRoles.join(', ')} />;
  }

  return <>{children}</>;
};
