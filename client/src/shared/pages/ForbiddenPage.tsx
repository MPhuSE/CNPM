import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldX, ArrowLeft, Home } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { useAuth } from '@/app/providers/AuthProvider';
import { Badge } from '@/shared/ui/Badge';

interface ForbiddenPageProps {
  requiredRole?: string;
}

export const ForbiddenPage: React.FC<ForbiddenPageProps> = ({
  requiredRole = 'ROLE_ADMIN',
}) => {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <div className="min-h-[70vh] flex items-center justify-center p-6">
      <div className="max-w-md w-full text-center bg-white p-8 rounded-2xl border border-gray-200 shadow-sm">
        <div className="w-16 h-16 rounded-2xl bg-rose-50 border border-rose-100 flex items-center justify-center mx-auto mb-4 text-rose-600">
          <ShieldX className="w-9 h-9" />
        </div>

        <span className="inline-block text-xs font-bold uppercase tracking-wider text-rose-600 bg-rose-50 px-2.5 py-1 rounded-full mb-3">
          Mã lỗi 403: Truy cập bị từ chối
        </span>

        <h1 className="text-xl font-bold text-gray-900 mb-2">
          Khu vực hạn chế quyền truy cập
        </h1>

        <p className="text-sm text-gray-600 mb-6 leading-relaxed">
          Tài khoản của bạn không có đủ thẩm quyền để truy cập trang quản trị này.
          Khu vực này yêu cầu vai trò quyền hạn{' '}
          <span className="font-semibold text-gray-800">{requiredRole}</span>.
        </p>

        {user && (
          <div className="bg-gray-50 rounded-xl p-3 mb-6 text-left border border-gray-100 text-xs text-gray-600">
            <div className="font-medium text-gray-700 mb-1">Thông tin người dùng hiện tại:</div>
            <div>Họ tên: <span className="font-semibold text-gray-900">{user.fullName}</span></div>
            <div>Tên đăng nhập: <span className="font-mono text-gray-900">@{user.username}</span></div>
            <div className="mt-1 flex items-center gap-1 flex-wrap">
              <span>Vai trò hiện có:</span>
              {user.roles?.map((r) => (
                <Badge key={r} size="sm" color="gray">
                  {r.replace('ROLE_', '')}
                </Badge>
              ))}
            </div>
          </div>
        )}

        <div className="flex flex-col sm:flex-row gap-3 justify-center">
          <Button
            variant="outline"
            className="flex items-center gap-2"
            onClick={() => navigate(-1)}
          >
            <ArrowLeft className="w-4 h-4" />
            Quay lại
          </Button>

          <Button
            variant="primary"
            className="flex items-center gap-2"
            onClick={() => navigate('/my-requests')}
          >
            <Home className="w-4 h-4" />
            Về trang chủ của tôi
          </Button>
        </div>
      </div>
    </div>
  );
};
