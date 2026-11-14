import React, { useState } from 'react';
import { Modal } from '@/shared/ui/Modal';
import { Button } from '@/shared/ui/Button';
import { Alert } from '@/shared/ui/Alert';
import { UserItem } from '../types/organization.types';
import { organizationApi } from '../services/organizationApi';
import { AlertTriangle, ShieldAlert } from 'lucide-react';
import { ApiError } from '@/shared/api/client';

interface UserStatusConfirmModalProps {
  user: UserItem | null;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const UserStatusConfirmModal: React.FC<UserStatusConfirmModalProps> = ({
  user,
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!user) return null;

  const isDeactivating = user.isActive;
  const isAdmin = user.roles.includes('ROLE_ADMIN');

  const handleConfirm = async () => {
    setIsLoading(true);
    setErrorMessage(null);

    try {
      await organizationApi.toggleUserStatus(user.id, !user.isActive, user.lockVersion);
      onSuccess();
      onClose();
    } catch (err: any) {
      if (err instanceof ApiError) {
        if (err.code === 'LAST_ADMIN_REQUIRED' || err.message?.toLowerCase().includes('last admin')) {
          setErrorMessage(
            'Không thể vô hiệu hóa Quản trị viên cuối cùng của hệ thống. Hệ thống bắt buộc phải có ít nhất một tài khoản Quản trị viên hoạt động.'
          );
        } else if (err.status === 409) {
          setErrorMessage(
            'Xung đột phiên bản: Dữ liệu người dùng đã được cập nhật bởi một phiên làm việc khác. Vui lòng tải lại trang.'
          );
        } else {
          setErrorMessage(err.message || 'Thao tác cập nhật trạng thái thất bại.');
        }
      } else {
        setErrorMessage('Đã xảy ra lỗi không xác định. Vui lòng thử lại sau.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isDeactivating ? 'Xác nhận vô hiệu hóa tài khoản' : 'Xác nhận kích hoạt tài khoản'}
      maxWidth="md"
    >
      <div className="space-y-4">
        {errorMessage && (
          <Alert variant="error" title="Không thể thay đổi trạng thái">
            {errorMessage}
          </Alert>
        )}

        <div className="flex items-start gap-3 p-3 bg-slate-50 rounded-xl border border-slate-100 text-sm">
          {isDeactivating ? (
            <AlertTriangle className="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />
          ) : (
            <ShieldAlert className="w-5 h-5 text-indigo-500 shrink-0 mt-0.5" />
          )}
          <div>
            <span className="font-semibold text-gray-900">{user.fullName}</span> (
            <span className="font-mono text-xs text-gray-600">@{user.username}</span>)
            <div className="text-xs text-gray-500 mt-1">Email: {user.email}</div>
          </div>
        </div>

        {isDeactivating ? (
          <div className="space-y-2 text-sm text-gray-600">
            <p>
              Bạn có chắc chắn muốn <span className="font-semibold text-rose-600">vô hiệu hóa</span> tài khoản này?
            </p>
            <ul className="list-disc pl-5 text-xs text-gray-500 space-y-1">
              <li>Tài khoản sẽ không thể đăng nhập vào hệ thống.</li>
              <li>Mọi phiên đăng nhập hiện tại sẽ bị thu hồi ngay lập tức (auth_version được tăng).</li>
              <li>Các tác vụ đang chờ duyệt cá nhân sẽ chuyển sang trạng thái <strong>Cần phân công (NEEDS_ASSIGNMENT)</strong>.</li>
            </ul>

            {isAdmin && (
              <Alert variant="warning" className="mt-3">
                <strong>Lưu ý:</strong> Tài khoản này có vai trò <strong>Quản trị viên (ROLE_ADMIN)</strong>.
                Nếu đây là tài khoản Quản trị viên duy nhất còn lại, hành động này sẽ bị hệ thống từ chối để bảo vệ an toàn hệ thống.
              </Alert>
            )}
          </div>
        ) : (
          <div className="text-sm text-gray-600">
            <p>
              Bạn có chắc chắn muốn <span className="font-semibold text-emerald-600">kích hoạt lại</span> tài khoản này?
            </p>
            <p className="text-xs text-gray-500 mt-2">
              Người dùng sẽ có thể đăng nhập bình thường và tiếp tục thực hiện các quyền theo vai trò được gán.
            </p>
          </div>
        )}

        <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
          <Button variant="outline" onClick={onClose} disabled={isLoading}>
            Hủy bỏ
          </Button>

          <Button
            variant={isDeactivating ? 'danger' : 'primary'}
            onClick={handleConfirm}
            isLoading={isLoading}
          >
            {isDeactivating ? 'Xác nhận vô hiệu hóa' : 'Xác nhận kích hoạt'}
          </Button>
        </div>
      </div>
    </Modal>
  );
};
