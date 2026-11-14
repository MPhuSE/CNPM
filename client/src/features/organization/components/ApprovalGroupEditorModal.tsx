import React, { useState, useEffect } from 'react';
import { Modal } from '@/shared/ui/Modal';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { Alert } from '@/shared/ui/Alert';
import {
  ApprovalGroupItem,
  CreateApprovalGroupRequest,
  UpdateApprovalGroupRequest,
} from '../types/organization.types';
import { organizationApi } from '../services/organizationApi';
import { ApiError } from '@/shared/api/client';

interface ApprovalGroupEditorModalProps {
  group?: ApprovalGroupItem | null;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const ApprovalGroupEditorModal: React.FC<ApprovalGroupEditorModalProps> = ({
  group,
  isOpen,
  onClose,
  onSuccess,
}) => {
  const isEditing = !!group;

  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [isActive, setIsActive] = useState(true);

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (group) {
      setCode(group.code || '');
      setName(group.name || '');
      setIsActive(group.isActive ?? true);
    } else {
      setCode('');
      setName('');
      setIsActive(true);
    }
    setErrors({});
    setServerError(null);
  }, [group, isOpen]);

  const validate = (): boolean => {
    const errs: Record<string, string> = {};

    if (!code.trim()) {
      errs.code = 'Mã nhóm không được để trống.';
    } else if (!/^[A-Z0-9_-]{2,50}$/i.test(code.trim())) {
      errs.code = 'Mã nhóm từ 2–50 ký tự (chữ hoa, số, gạch dưới/ngang). Ví dụ: FINANCE, ACCOUNTING, IT_OPS.';
    }

    if (!name.trim()) {
      errs.name = 'Tên nhóm phê duyệt không được để trống.';
    }

    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setServerError(null);

    if (!validate()) return;

    setIsLoading(true);

    try {
      if (isEditing && group) {
        const updatePayload: UpdateApprovalGroupRequest = {
          code: code.trim().toUpperCase(),
          name: name.trim(),
          isActive,
          // RÀNG BUỘC: Không gửi version giả
          lockVersion: group.lockVersion,
        };
        await organizationApi.updateApprovalGroup(group.id, updatePayload);
      } else {
        const createPayload: CreateApprovalGroupRequest = {
          code: code.trim().toUpperCase(),
          name: name.trim(),
          isActive,
        };
        await organizationApi.createApprovalGroup(createPayload);
      }

      onSuccess();
      onClose();
    } catch (err: any) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          setServerError('Xung đột dữ liệu hoặc mã nhóm này đã tồn tại trong hệ thống.');
        } else {
          setServerError(err.message || 'Thao tác lưu nhóm phê duyệt thất bại.');
        }
      } else {
        setServerError('Đã xảy ra lỗi không xác định. Vui lòng thử lại.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? 'Chỉnh sửa nhóm phê duyệt' : 'Tạo mới nhóm phê duyệt'}
      description="Nhóm phê duyệt độc lập với vai trò tài khoản (Finance, Accounting, IT...)"
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {serverError && (
          <Alert variant="error" title="Lỗi thao tác">
            {serverError}
          </Alert>
        )}

        <Input
          label="Mã nhóm phê duyệt"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          required
          disabled={isEditing}
          error={errors.code}
          placeholder="FINANCE, ACCOUNTING, IT..."
          helperText={isEditing ? 'Mã nhóm không thể thay đổi sau khi tạo' : 'Mã định danh duy nhất (in hoa)'}
        />

        <Input
          label="Tên hiển thị nhóm phê duyệt"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          error={errors.name}
          placeholder="Ban Tài chính / Phòng Kế toán / IT Support"
        />

        <div className="flex items-center gap-2 p-3 bg-gray-50 rounded-lg border border-gray-100">
          <input
            type="checkbox"
            id="group-is-active"
            checked={isActive}
            onChange={(e) => setIsActive(e.target.checked)}
            className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded cursor-pointer"
          />
          <label htmlFor="group-is-active" className="text-sm font-medium text-gray-800 cursor-pointer">
            Nhóm đang hoạt động (Active)
          </label>
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
          <Button variant="outline" type="button" onClick={onClose} disabled={isLoading}>
            Hủy bỏ
          </Button>
          <Button variant="primary" type="submit" isLoading={isLoading}>
            {isEditing ? 'Lưu thay đổi' : 'Tạo nhóm phê duyệt'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
