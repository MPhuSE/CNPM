import React, { useState, useEffect } from 'react';
import { Modal } from '@/shared/ui/Modal';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { Alert } from '@/shared/ui/Alert';
import { Badge } from '@/shared/ui/Badge';
import {
  UserItem,
  DepartmentItem,
  SystemRole,
  SYSTEM_ROLE_OPTIONS,
  CreateUserRequest,
  UpdateUserRequest,
} from '../types/organization.types';
import { organizationApi } from '../services/organizationApi';
import { ApiError } from '@/shared/api/client';
import { Shield, Info } from 'lucide-react';

interface UserEditorModalProps {
  user?: UserItem | null; // null for Create, UserItem for Edit
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  departments: DepartmentItem[];
  availableManagers: UserItem[];
}

export const UserEditorModal: React.FC<UserEditorModalProps> = ({
  user,
  isOpen,
  onClose,
  onSuccess,
  departments,
  availableManagers,
}) => {
  const isEditing = !!user;

  // Form states
  const [username, setUsername] = useState('');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [departmentId, setDepartmentId] = useState<string>('');
  const [managerId, setManagerId] = useState<string>('');
  const [roles, setRoles] = useState<SystemRole[]>(['ROLE_EMPLOYEE']);

  // Validation & Error states
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // Sync state when modal opens or user prop changes
  useEffect(() => {
    if (user) {
      setUsername(user.username || '');
      setFullName(user.fullName || '');
      setEmail(user.email || '');
      setPassword('');
      setDepartmentId(user.departmentId || '');
      setManagerId(user.managerId || '');
      setRoles(user.roles && user.roles.length > 0 ? user.roles : ['ROLE_EMPLOYEE']);
    } else {
      setUsername('');
      setFullName('');
      setEmail('');
      setPassword('');
      setDepartmentId('');
      setManagerId('');
      setRoles(['ROLE_EMPLOYEE']);
    }
    setErrors({});
    setServerError(null);
  }, [user, isOpen]);

  // Handle role checkbox toggling
  const handleToggleRole = (roleCode: SystemRole) => {
    setRoles((prev) => {
      if (prev.includes(roleCode)) {
        // Prevent unchecking if it's the only selected role
        if (prev.length === 1) {
          setErrors((e) => ({ ...e, roles: 'Người dùng bắt buộc phải có ít nhất 1 vai trò hệ thống.' }));
          return prev;
        }
        return prev.filter((r) => r !== roleCode);
      } else {
        setErrors((e) => {
          const { roles: _, ...rest } = e;
          return rest;
        });
        return [...prev, roleCode];
      }
    });
  };

  const validate = (): boolean => {
    const errs: Record<string, string> = {};

    if (!isEditing) {
      if (!username.trim()) {
        errs.username = 'Tên đăng nhập không được để trống.';
      } else if (!/^[a-zA-Z0-9_.-]{3,30}$/.test(username.trim())) {
        errs.username = 'Tên đăng nhập từ 3–30 ký tự, chỉ gồm chữ, số, dấu gạch nối, gạch dưới hoặc chấm.';
      }

      if (!password.trim()) {
        errs.password = 'Mật khẩu khởi tạo không được để trống.';
      } else if (password.length < 6) {
        errs.password = 'Mật khẩu phải có độ dài tối thiểu 6 ký tự.';
      }
    }

    if (!fullName.trim()) {
      errs.fullName = 'Họ và tên không được để trống.';
    }

    if (!email.trim()) {
      errs.email = 'Email không được để trống.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errs.email = 'Địa chỉ email không đúng định dạng (ví dụ: user@company.com).';
    }

    if (roles.length === 0) {
      errs.roles = 'Bắt buộc phải chọn ít nhất 1 vai trò hệ thống.';
    }

    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setServerError(null);

    if (!validate()) {
      return;
    }

    setIsLoading(true);

    try {
      if (isEditing && user) {
        const updatePayload: UpdateUserRequest = {
          fullName: fullName.trim(),
          email: email.trim(),
          departmentId: departmentId ? departmentId : null,
          managerId: managerId ? managerId : null,
          roles,
          lockVersion: user.lockVersion,
        };

        await organizationApi.updateUser(user.id, updatePayload);
      } else {
        const createPayload: CreateUserRequest = {
          username: username.trim().toLowerCase(),
          fullName: fullName.trim(),
          email: email.trim().toLowerCase(),
          password: password.trim(),
          departmentId: departmentId ? departmentId : null,
          managerId: managerId ? managerId : null,
          roles,
        };

        await organizationApi.createUser(createPayload);
      }

      onSuccess();
      onClose();
    } catch (err: any) {
      if (err instanceof ApiError) {
        // Xử lý các mã lỗi nghiệp vụ cụ thể
        if (
          err.code === 'DUPLICATE_EMAIL' ||
          err.code === 'EMAIL_ALREADY_EXISTS' ||
          (err.status === 409 && err.message?.toLowerCase().includes('email'))
        ) {
          setErrors((prev) => ({
            ...prev,
            email: 'Email này đã tồn tại trong hệ thống. Vui lòng sử dụng địa chỉ email khác.',
          }));
          setServerError('Email này đã được sử dụng bởi một tài khoản khác trong hệ thống.');
        } else if (
          err.code === 'LAST_ADMIN_REQUIRED' ||
          err.message?.toLowerCase().includes('last admin')
        ) {
          setServerError(
            'Không thể gỡ quyền Quản trị viên (ROLE_ADMIN) của quản trị viên cuối cùng trong hệ thống. Hệ thống bắt buộc phải có ít nhất một Quản trị viên hoạt động.'
          );
        } else if (err.status === 409) {
          setServerError(
            'Xung đột phiên bản: Dữ liệu tài khoản đã bị thay đổi bởi một phiên khác. Vui lòng tải lại dữ liệu mới nhất.'
          );
        } else if (err.code === 'INVALID_MANAGER' || err.code === 'ASSIGNEE_NOT_ELIGIBLE') {
          setServerError('Người quản lý được chọn không hợp lệ hoặc đã bị vô hiệu hóa.');
        } else {
          setServerError(err.message || 'Thao tác lưu thông tin người dùng thất bại.');
        }
      } else {
        setServerError('Đã xảy ra lỗi không xác định. Vui lòng kiểm tra lại kết nối mạng.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const departmentOptions = [
    { label: '-- Không thuộc phòng ban --', value: '' },
    ...departments.map((d) => ({
      label: `${d.name} (${d.code})`,
      value: d.id,
    })),
  ];

  const managerOptions = [
    { label: '-- Không có quản lý trực tiếp --', value: '' },
    ...availableManagers
      .filter((m) => (!isEditing || m.id !== user?.id) && m.isActive)
      .map((m) => ({
        label: `${m.fullName} (@${m.username})`,
        value: m.id,
      })),
  ];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? 'Chỉnh sửa tài khoản người dùng' : 'Tạo mới tài khoản người dùng'}
      description="Quản trị thông tin nhân sự và gán vai trò hệ thống theo đúng quy chuẩn FlowOps 2.0"
      maxWidth="lg"
    >
      <form onSubmit={handleSubmit} className="space-y-5">
        {serverError && (
          <Alert variant="error" title="Lỗi thao tác">
            {serverError}
          </Alert>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Input
            label="Tên đăng nhập (Username)"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={isEditing}
            required={!isEditing}
            error={errors.username}
            helperText={isEditing ? 'Tên đăng nhập không thể thay đổi sau khi tạo' : 'Dùng để đăng nhập hệ thống'}
          />

          <Input
            label="Họ và tên"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
            error={errors.fullName}
            placeholder="Nguyễn Văn A"
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Input
            label="Địa chỉ Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            error={errors.email}
            placeholder="name@company.com"
          />

          {!isEditing ? (
            <Input
              label="Mật khẩu khởi tạo"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              error={errors.password}
              placeholder="Tối thiểu 6 ký tự"
            />
          ) : (
            <div className="flex flex-col justify-center text-xs text-gray-500 bg-gray-50 p-3 rounded-lg border border-gray-100">
              <span className="font-semibold text-gray-700">Mật khẩu tài khoản:</span>
              <span>Mật khẩu được người dùng tự đổi qua tính năng Đổi mật khẩu.</span>
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Select
            label="Phòng ban"
            options={departmentOptions}
            value={departmentId}
            onChange={(e) => setDepartmentId(e.target.value)}
          />

          <Select
            label="Quản lý trực tiếp"
            options={managerOptions}
            value={managerId}
            onChange={(e) => setManagerId(e.target.value)}
          />
        </div>

        {/* Gán 3 System Roles chuẩn */}
        <div className="border-t border-gray-100 pt-4">
          <label className="block text-sm font-semibold text-gray-800 mb-1">
            Gán vai trò hệ thống (System Roles) <span className="text-rose-500">*</span>
          </label>
          <p className="text-xs text-gray-500 mb-3 flex items-center gap-1">
            <Info className="w-3.5 h-3.5 text-gray-400" />
            Một tài khoản có thể có nhiều vai trò. Nhóm Tài chính, Kế toán, IT được quản lý riêng tại tab Nhóm phê duyệt.
          </p>

          {errors.roles && <p className="text-xs text-rose-600 mb-2 font-medium">{errors.roles}</p>}

          <div className="space-y-2">
            {SYSTEM_ROLE_OPTIONS.map((role) => {
              const isChecked = roles.includes(role.code);
              return (
                <div
                  key={role.code}
                  onClick={() => handleToggleRole(role.code)}
                  className={`flex items-start gap-3 p-3 rounded-xl border cursor-pointer transition-all ${
                    isChecked
                      ? 'bg-indigo-50/60 border-indigo-200'
                      : 'bg-white border-gray-200 hover:bg-gray-50'
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={isChecked}
                    onChange={() => {}} // Controlled via container click
                    className="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded cursor-pointer"
                  />
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-semibold text-gray-900">{role.name}</span>
                      <Badge size="sm" color={role.badgeColor}>
                        {role.code}
                      </Badge>
                    </div>
                    <p className="text-xs text-gray-500 mt-0.5">{role.description}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
          <Button variant="outline" type="button" onClick={onClose} disabled={isLoading}>
            Hủy bỏ
          </Button>
          <Button variant="primary" type="submit" isLoading={isLoading}>
            {isEditing ? 'Lưu thay đổi' : 'Tạo tài khoản'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
