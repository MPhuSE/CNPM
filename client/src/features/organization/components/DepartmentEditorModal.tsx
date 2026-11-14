import React, { useState, useEffect } from 'react';
import { Modal } from '@/shared/ui/Modal';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { Alert } from '@/shared/ui/Alert';
import {
  DepartmentItem,
  UserItem,
  CreateDepartmentRequest,
  UpdateDepartmentRequest,
} from '../types/organization.types';
import { organizationApi } from '../services/organizationApi';
import {
  wouldCreateDepartmentCycle,
  getDescendantDepartmentIds,
} from '../utils/departmentUtils';
import { ApiError } from '@/shared/api/client';
import { AlertCircle, Building2, UserCheck } from 'lucide-react';

interface DepartmentEditorModalProps {
  department?: DepartmentItem | null; // null for Create, DepartmentItem for Edit
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  allDepartments: DepartmentItem[];
  availableUsers: UserItem[];
}

export const DepartmentEditorModal: React.FC<DepartmentEditorModalProps> = ({
  department,
  isOpen,
  onClose,
  onSuccess,
  allDepartments,
  availableUsers,
}) => {
  const isEditing = !!department;

  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [parentId, setParentId] = useState<string>('');
  const [managerId, setManagerId] = useState<string>('');
  const [isActive, setIsActive] = useState(true);

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (department) {
      setCode(department.code || '');
      setName(department.name || '');
      setParentId(department.parentId || '');
      setManagerId(department.managerId || '');
      setIsActive(department.isActive ?? true);
    } else {
      setCode('');
      setName('');
      setParentId('');
      setManagerId('');
      setIsActive(true);
    }
    setErrors({});
    setServerError(null);
  }, [department, isOpen]);

  // Ngăn chặn chu trình trên UI: tính toán danh sách ID bị cấm chọn làm parent
  const forbiddenParentIds = isEditing && department
    ? new Set([department.id, ...Array.from(getDescendantDepartmentIds(department.id, allDepartments))])
    : new Set<string>();

  const validate = (): boolean => {
    const errs: Record<string, string> = {};

    if (!code.trim()) {
      errs.code = 'Mã phòng ban không được để trống.';
    } else if (!/^[A-Z0-9_-]{2,30}$/i.test(code.trim())) {
      errs.code = 'Mã phòng ban từ 2–30 ký tự (chữ hoa/thường, số, gạch nối hoặc gạch dưới).';
    }

    if (!name.trim()) {
      errs.name = 'Tên phòng ban không được để trống.';
    }

    // Kiểm tra chu trình trên client trước khi gửi request
    if (isEditing && department && parentId) {
      if (wouldCreateDepartmentCycle(department.id, parentId, allDepartments)) {
        errs.parentId =
          'Phát hiện chu trình vòng lặp! Không thể chọn chính phòng ban này hoặc một phòng ban con cháu của nó làm phòng ban cấp trên.';
      }
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
      if (isEditing && department) {
        // TODO(DEV02): Đồng bộ giao dịch và khóa policy coarse-grained khi thay đổi cơ cấu phòng ban
        const updatePayload: UpdateDepartmentRequest = {
          code: code.trim().toUpperCase(),
          name: name.trim(),
          parentId: parentId ? parentId : null,
          managerId: managerId ? managerId : null,
          isActive,
          // RÀNG BUỘC: Không gửi version giả - chỉ truyền lockVersion nếu entity trả về có
          lockVersion: department.lockVersion,
        };

        await organizationApi.updateDepartment(department.id, updatePayload);
      } else {
        const createPayload: CreateDepartmentRequest = {
          code: code.trim().toUpperCase(),
          name: name.trim(),
          parentId: parentId ? parentId : null,
          managerId: managerId ? managerId : null,
          isActive,
        };

        await organizationApi.createDepartment(createPayload);
      }

      onSuccess();
      onClose();
    } catch (err: any) {
      if (err instanceof ApiError) {
        if (
          err.code === 'DEPARTMENT_CYCLE_DETECTED' ||
          err.code === 'CYCLE_DETECTED' ||
          err.message?.toLowerCase().includes('cycle') ||
          err.message?.toLowerCase().includes('chu trình')
        ) {
          setServerError(
            'Lỗi chu trình: Cấu trúc phòng ban cấp trên tạo thành vòng lặp. Vui lòng chọn một phòng ban cấp trên hợp lệ.'
          );
        } else if (
          err.code === 'INVALID_MANAGER' ||
          err.code === 'ASSIGNEE_NOT_ELIGIBLE' ||
          err.message?.toLowerCase().includes('manager')
        ) {
          setServerError(
            'Trưởng bộ phận không hợp lệ: Người dùng được chọn đã bị vô hiệu hóa hoặc không đủ điều kiện giữ vị trí quản lý.'
          );
        } else if (err.status === 409) {
          setServerError(
            'Xung đột phiên bản (409): Dữ liệu phòng ban đã bị thay đổi bởi phiên làm việc khác. Vui lòng tải lại dữ liệu mới nhất.'
          );
        } else if (
          err.code === 'DUPLICATE_CODE' ||
          (err.status === 409 && err.message?.toLowerCase().includes('code'))
        ) {
          setErrors((prev) => ({ ...prev, code: 'Mã phòng ban này đã tồn tại trong hệ thống.' }));
          setServerError('Mã phòng ban đã tồn tại.');
        } else {
          setServerError(err.message || 'Thao tác cập nhật phòng ban thất bại.');
        }
      } else {
        setServerError('Đã xảy ra lỗi không xác định. Vui lòng thử lại sau.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  // Tạo danh sách dropdown phòng ban cha với ghi chú các phòng ban bị vô hiệu hóa do chu trình
  const parentOptions = [
    { label: '-- Là phòng ban gốc (Không có cấp trên) --', value: '' },
    ...allDepartments.map((d) => {
      const isForbidden = forbiddenParentIds.has(d.id);
      return {
        label: isForbidden ? `${d.name} (${d.code}) [Không khả dụng - Tránh chu trình]` : `${d.name} (${d.code})`,
        value: d.id,
      };
    }),
  ];

  // Chỉ cho phép chọn Manager là tài khoản ĐANG HOẠT ĐỘNG
  const managerOptions = [
    { label: '-- Chưa bổ nhiệm trưởng phòng --', value: '' },
    ...availableUsers
      .filter((u) => u.isActive)
      .map((u) => ({
        label: `${u.fullName} (@${u.username}) ${u.roles.includes('ROLE_APPROVER') ? '✓ [Approver]' : ''}`,
        value: u.id,
      })),
  ];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? 'Chỉnh sửa thông tin phòng ban' : 'Tạo mới phòng ban'}
      description="Quản lý cơ cấu phòng ban và phân công Trưởng bộ phận phụ trách"
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {serverError && (
          <Alert variant="error" title="Lỗi thao tác">
            {serverError}
          </Alert>
        )}

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Mã phòng ban"
            value={code}
            onChange={(e) => setCode(e.target.value.toUpperCase())}
            required
            error={errors.code}
            placeholder="IT, HR, FIN..."
            helperText="Mã duy nhất viết hoa"
          />

          <Input
            label="Tên phòng ban"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            error={errors.name}
            placeholder="Phòng Công nghệ Thông tin"
          />
        </div>

        <div>
          <Select
            label="Phòng ban cấp trên (Parent Department)"
            options={parentOptions}
            value={parentId}
            onChange={(e) => {
              const val = e.target.value;
              if (forbiddenParentIds.has(val)) {
                setErrors((prev) => ({
                  ...prev,
                  parentId: 'Không thể chọn phòng ban này vì sẽ tạo thành chu trình vòng lặp!',
                }));
              } else {
                setErrors((prev) => {
                  const { parentId: _, ...rest } = prev;
                  return rest;
                });
              }
              setParentId(val);
            }}
            error={errors.parentId}
          />
          <p className="text-[11px] text-gray-500 mt-1">
            Hệ thống tự động kiểm tra và khóa các nhánh phòng ban con cháu để ngăn chặn chu trình (A trực thuộc B, B trực thuộc A).
          </p>
        </div>

        <div>
          <Select
            label="Trưởng bộ phận / Quản lý (Manager)"
            options={managerOptions}
            value={managerId}
            onChange={(e) => setManagerId(e.target.value)}
            error={errors.managerId}
          />
          <p className="text-[11px] text-gray-500 mt-1">
            Chỉ người dùng đang hoạt động mới được bổ nhiệm. Trưởng phòng là căn cứ để định tuyến bước duyệt MANAGER_APPROVAL.
          </p>
        </div>

        <div className="flex items-center gap-2 p-3 bg-gray-50 rounded-lg border border-gray-100">
          <input
            type="checkbox"
            id="dept-is-active"
            checked={isActive}
            onChange={(e) => setIsActive(e.target.checked)}
            className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
          />
          <label htmlFor="dept-is-active" className="text-sm font-medium text-gray-800 cursor-pointer">
            Phòng ban đang hoạt động (Active)
          </label>
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
          <Button variant="outline" type="button" onClick={onClose} disabled={isLoading}>
            Hủy bỏ
          </Button>
          <Button variant="primary" type="submit" isLoading={isLoading}>
            {isEditing ? 'Lưu thay đổi' : 'Tạo phòng ban'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
