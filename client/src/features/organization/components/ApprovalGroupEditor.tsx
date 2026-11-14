import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { organizationApi } from '../services/organizationApi';
import {
  ApprovalGroupItem,
  UserItem,
} from '../types/organization.types';
import { Table, TableHead, TableBody, TableRow, TableHeaderCell, TableCell } from '@/shared/ui/Table';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Alert } from '@/shared/ui/Alert';
import { Modal } from '@/shared/ui/Modal';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { ApprovalGroupEditorModal } from './ApprovalGroupEditorModal';
import { ApiError } from '@/shared/api/client';
import {
  Shield,
  Plus,
  RefreshCw,
  Edit2,
  UserPlus,
  UserMinus,
  Users,
  CheckCircle2,
  AlertCircle,
  AlertTriangle,
  Info,
} from 'lucide-react';

interface ApprovalGroupEditorProps {
  availableUsers: UserItem[];
}

export const ApprovalGroupEditor: React.FC<ApprovalGroupEditorProps> = ({ availableUsers }) => {
  const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null);

  // Group modal state (Create / Edit Group)
  const [isGroupModalOpen, setIsGroupModalOpen] = useState(false);
  const [editingGroup, setEditingGroup] = useState<ApprovalGroupItem | null>(null);

  // Member modal state (Add Member)
  const [isAddMemberModalOpen, setIsAddMemberModalOpen] = useState(false);
  const [selectedUserIdToAdd, setSelectedUserIdToAdd] = useState<string>('');
  const [memberActionError, setMemberActionError] = useState<string | null>(null);
  const [isMemberActionLoading, setIsMemberActionLoading] = useState(false);

  // Fetch approval groups dynamically from API
  // RÀNG BUỘC TUYỆT ĐỐI - KHÔNG HARDCODE: Dữ liệu nhóm lấy hoàn toàn từ API
  const {
    data,
    isLoading,
    isError,
    error,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ['approval-groups-list'],
    queryFn: () => organizationApi.getApprovalGroups({ page: 0, size: 50 }),
  });

  const groups = data?.items || [];

  // Automatically select the first group if none selected
  const activeGroup =
    groups.find((g) => g.id === selectedGroupId) || (groups.length > 0 ? groups[0] : null);

  const handleOpenCreateGroup = () => {
    setEditingGroup(null);
    setIsGroupModalOpen(true);
  };

  const handleOpenEditGroup = (group: ApprovalGroupItem, e: React.MouseEvent) => {
    e.stopPropagation();
    setEditingGroup(group);
    setIsGroupModalOpen(true);
  };

  // Lọc danh sách nhân sự ứng viên hợp lệ để thêm vào nhóm:
  // Theo quy tắc SRS 2.0: Thành viên nhóm phê duyệt bắt buộc phải:
  // 1. Đang hoạt động (isActive = true).
  // 2. Có vai trò người phê duyệt (ROLE_APPROVER).
  // 3. Chưa có trong nhóm hiện tại.
  const currentMemberIds = activeGroup?.members?.map((m) => m.id) || [];

  const eligibleCandidateUsers = availableUsers.filter((u) => {
    return u.isActive && !currentMemberIds.includes(u.id);
  });

  const handleAddMember = async () => {
    if (!activeGroup || !selectedUserIdToAdd) return;

    setMemberActionError(null);
    setIsMemberActionLoading(true);

    const userToAdd = availableUsers.find((u) => u.id === selectedUserIdToAdd);

    // Client-side rule check
    if (!userToAdd?.isActive) {
      setMemberActionError('Không thể thêm người dùng đã bị vô hiệu hóa vào nhóm phê duyệt.');
      setIsMemberActionLoading(false);
      return;
    }

    if (!userToAdd.roles.includes('ROLE_APPROVER')) {
      setMemberActionError(
        `Người dùng "${userToAdd.fullName}" chưa được gán vai trò Người phê duyệt (ROLE_APPROVER). Theo quy định SRS 2.0, chỉ nhân sự có quyền ROLE_APPROVER mới được tham gia nhóm phê duyệt.`
      );
      setIsMemberActionLoading(false);
      return;
    }

    try {
      await organizationApi.addGroupMember(
        activeGroup.id,
        selectedUserIdToAdd,
        currentMemberIds,
        activeGroup.lockVersion // Không gửi version giả - chỉ gửi nếu có
      );

      setIsAddMemberModalOpen(false);
      setSelectedUserIdToAdd('');
      refetch();
    } catch (err: any) {
      if (err instanceof ApiError) {
        if (
          err.code === 'ASSIGNEE_NOT_ELIGIBLE' ||
          err.code === 'INVALID_MEMBER' ||
          err.message?.toLowerCase().includes('approver')
        ) {
          setMemberActionError(
            'Thành viên không hợp lệ: Người dùng đã bị vô hiệu hóa hoặc không đủ điều kiện (yêu cầu vai trò ROLE_APPROVER).'
          );
        } else if (err.status === 409) {
          setMemberActionError(
            'Xung đột phiên bản (409): Dữ liệu nhóm đã bị cập nhật bởi phiên làm việc khác. Vui lòng tải lại dữ liệu mới nhất.'
          );
        } else {
          setMemberActionError(err.message || 'Thêm thành viên thất bại.');
        }
      } else {
        setMemberActionError('Đã xảy ra lỗi không xác định.');
      }
    } finally {
      setIsMemberActionLoading(false);
    }
  };

  const handleRemoveMember = async (userId: string, memberName: string) => {
    if (!activeGroup) return;

    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn xóa "${memberName}" khỏi nhóm phê duyệt "${activeGroup.name}"?`
    );
    if (!confirmed) return;

    try {
      await organizationApi.removeGroupMember(
        activeGroup.id,
        userId,
        currentMemberIds,
        activeGroup.lockVersion // Không gửi version giả
      );
      refetch();
    } catch (err: any) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          alert('Xung đột phiên bản (409): Dữ liệu nhóm đã bị sửa đổi. Vui lòng tải lại.');
        } else {
          alert(err.message || 'Xóa thành viên thất bại.');
        }
      } else {
        alert('Lỗi kết nối khi xóa thành viên.');
      }
    }
  };

  return (
    <div className="space-y-4">
      {/* Top action bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <div>
          <h2 className="text-base font-semibold text-gray-900">
            Nhóm phê duyệt độc lập (Approval Groups)
          </h2>
          <p className="text-xs text-gray-500">
            Quản lý thành viên cho các nhóm phê duyệt (Finance, Accounting, IT...). Dữ liệu nhóm được tải động từ API.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            disabled={isFetching}
            className="flex items-center gap-1.5"
            title="Tải lại danh sách nhóm"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin' : ''}`} />
            <span>Làm mới</span>
          </Button>

          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenCreateGroup}
            className="flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" />
            <span>Tạo nhóm mới</span>
          </Button>
        </div>
      </div>

      {/* 3 UI STATES */}
      {isLoading ? (
        <div className="py-12 bg-white rounded-xl border border-gray-200">
          <LoadingSpinner size="lg" label="Đang tải danh sách nhóm phê duyệt..." />
        </div>
      ) : isError ? (
        <Alert variant="error" title="Không thể tải nhóm phê duyệt">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mt-1">
            <span>
              {(error as any)?.message ||
                'Đã xảy ra sự cố khi kết nối tới máy chủ. Vui lòng thử lại.'}
            </span>
            <Button variant="outline" size="sm" onClick={() => refetch()}>
              Thử lại
            </Button>
          </div>
        </Alert>
      ) : groups.length === 0 ? (
        <div className="py-16 text-center bg-white rounded-xl border border-gray-200 p-6">
          <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mx-auto text-gray-400 mb-3">
            <Shield className="w-6 h-6" />
          </div>
          <h3 className="text-base font-semibold text-gray-900">Chưa có nhóm phê duyệt nào</h3>
          <p className="text-xs text-gray-500 mt-1 max-w-sm mx-auto">
            Hệ thống chưa có nhóm phê duyệt độc lập. Nhấn nút bên dưới để tạo nhóm (ví dụ: FINANCE, ACCOUNTING, IT).
          </p>
          <Button variant="primary" size="sm" onClick={handleOpenCreateGroup} className="mt-4">
            Tạo nhóm đầu tiên
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Left column: List of groups */}
          <div className="lg:col-span-4 bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
            <div className="p-3 bg-gray-50 border-b border-gray-100 flex items-center justify-between">
              <span className="text-xs font-semibold text-gray-700 uppercase tracking-wider">
                Danh sách nhóm ({groups.length})
              </span>
              <span className="text-[11px] text-gray-400">Chọn nhóm để quản lý</span>
            </div>

            <div className="divide-y divide-gray-100">
              {groups.map((group) => {
                const isSelected = activeGroup?.id === group.id;
                return (
                  <div
                    key={group.id}
                    onClick={() => setSelectedGroupId(group.id)}
                    className={`p-3.5 cursor-pointer transition-colors flex items-center justify-between ${
                      isSelected
                        ? 'bg-indigo-50/70 border-l-4 border-indigo-600'
                        : 'hover:bg-gray-50'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className={`w-9 h-9 rounded-lg flex items-center justify-center text-xs font-bold ${
                          isSelected
                            ? 'bg-indigo-600 text-white'
                            : 'bg-gray-100 text-gray-600'
                        }`}
                      >
                        <Shield className="w-4 h-4" />
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-semibold text-gray-900">
                            {group.name}
                          </span>
                        </div>
                        <div className="flex items-center gap-2 mt-0.5">
                          <span className="font-mono text-xs text-gray-500">
                            {group.code}
                          </span>
                          <span className="text-gray-300">•</span>
                          <span className="text-xs text-gray-500">
                            {group.members?.length ?? group.memberCount ?? 0} thành viên
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-1">
                      <Badge size="sm" color={group.isActive ? 'emerald' : 'gray'}>
                        {group.isActive ? 'Active' : 'Off'}
                      </Badge>
                      <button
                        onClick={(e) => handleOpenEditGroup(group, e)}
                        className="p-1 text-gray-400 hover:text-gray-600 rounded transition-colors ml-1"
                        title="Sửa thông tin nhóm"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Right column: Selected Group Details & Members */}
          <div className="lg:col-span-8 space-y-4">
            {activeGroup ? (
              <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
                {/* Header of selected group */}
                <div className="p-4 bg-slate-50 border-b border-gray-200 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
                  <div>
                    <div className="flex items-center gap-2">
                      <Shield className="w-5 h-5 text-indigo-600" />
                      <h3 className="text-base font-bold text-gray-900">{activeGroup.name}</h3>
                      <span className="font-mono text-xs bg-indigo-100 text-indigo-800 px-2 py-0.5 rounded font-semibold">
                        {activeGroup.code}
                      </span>
                      <Badge size="sm" color={activeGroup.isActive ? 'emerald' : 'gray'}>
                        {activeGroup.isActive ? 'Đang hoạt động' : 'Tạm ngưng'}
                      </Badge>
                    </div>
                    <p className="text-xs text-gray-500 mt-1">
                      Thành viên trong nhóm này có thẩm quyền nhận việc (claim) và ra quyết định trên các bước duyệt của nhóm.
                    </p>
                  </div>

                  <Button
                    variant="primary"
                    size="sm"
                    onClick={() => {
                      setSelectedUserIdToAdd('');
                      setMemberActionError(null);
                      setIsAddMemberModalOpen(true);
                    }}
                    className="flex items-center gap-1.5 shrink-0"
                  >
                    <UserPlus className="w-4 h-4" />
                    <span>Thêm thành viên</span>
                  </Button>
                </div>

                {/* Member list table */}
                {(!activeGroup.members || activeGroup.members.length === 0) ? (
                  <div className="p-12 text-center">
                    <Users className="w-8 h-8 text-gray-300 mx-auto mb-2" />
                    <p className="text-sm font-medium text-gray-700">Chưa có thành viên nào trong nhóm này</p>
                    <p className="text-xs text-gray-400 mt-1 max-w-sm mx-auto">
                      Quy trình yêu cầu nhóm này duyệt sẽ chuyển sang trạng thái <strong>Cần phân công (NEEDS_ASSIGNMENT)</strong> nếu nhóm rỗng.
                    </p>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        setSelectedUserIdToAdd('');
                        setMemberActionError(null);
                        setIsAddMemberModalOpen(true);
                      }}
                      className="mt-3"
                    >
                      Bổ sung thành viên ngay
                    </Button>
                  </div>
                ) : (
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableHeaderCell>Thành viên</TableHeaderCell>
                        <TableHeaderCell>Họ và tên</TableHeaderCell>
                        <TableHeaderCell>Email</TableHeaderCell>
                        <TableHeaderCell>Vai trò phê duyệt</TableHeaderCell>
                        <TableHeaderCell className="text-right">Thao tác</TableHeaderCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {activeGroup.members.map((member) => (
                        <TableRow key={member.id}>
                          <TableCell className="font-mono text-xs font-semibold text-gray-900">
                            @{member.username}
                          </TableCell>
                          <TableCell className="font-medium text-gray-900 text-sm">
                            {member.fullName}
                          </TableCell>
                          <TableCell className="text-xs text-gray-600 font-mono">
                            {member.email || '—'}
                          </TableCell>
                          <TableCell>
                            <Badge size="sm" color="indigo">
                              APPROVER
                            </Badge>
                          </TableCell>
                          <TableCell className="text-right">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleRemoveMember(member.id, member.fullName)}
                              className="p-1 text-gray-400 hover:text-rose-600 hover:bg-rose-50"
                              title="Xóa khỏi nhóm"
                            >
                              <UserMinus className="w-4 h-4" />
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </div>
            ) : null}
          </div>
        </div>
      )}

      {/* Create / Edit Group Modal */}
      <ApprovalGroupEditorModal
        group={editingGroup}
        isOpen={isGroupModalOpen}
        onClose={() => setIsGroupModalOpen(false)}
        onSuccess={() => refetch()}
      />

      {/* Add Member Modal */}
      <Modal
        isOpen={isAddMemberModalOpen}
        onClose={() => setIsAddMemberModalOpen(false)}
        title={`Thêm thành viên vào nhóm "${activeGroup?.name || ''}"`}
        description="Chỉ nhân sự đang hoạt động và có vai trò Người phê duyệt (ROLE_APPROVER) mới hợp lệ để thêm vào nhóm"
        maxWidth="md"
      >
        <div className="space-y-4">
          {memberActionError && (
            <Alert variant="error" title="Không thể thêm thành viên">
              {memberActionError}
            </Alert>
          )}

          <div className="bg-blue-50 p-3 rounded-lg border border-blue-100 text-xs text-blue-800 flex items-start gap-2">
            <Info className="w-4 h-4 text-blue-500 shrink-0 mt-0.5" />
            <div>
              Theo đặc tả nghiệp vụ <strong>SRS 2.0 (FR02–03)</strong>: Thành viên nhóm phê duyệt được kiểm tra điều kiện <code>ROLE_APPROVER</code> và trạng thái <code>Active</code> tại thời điểm thêm vào nhóm, nhận việc (claim) và ra quyết định.
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Chọn nhân sự từ hệ thống <span className="text-rose-500">*</span>
            </label>
            <select
              value={selectedUserIdToAdd}
              onChange={(e) => setSelectedUserIdToAdd(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="">-- Chọn nhân sự hợp lệ --</option>
              {eligibleCandidateUsers.map((user) => {
                const hasApprover = user.roles.includes('ROLE_APPROVER');
                return (
                  <option key={user.id} value={user.id}>
                    {user.fullName} (@{user.username}) {hasApprover ? '✓ [Đủ điều kiện APPROVER]' : '⚠️ [Thiếu ROLE_APPROVER]'}
                  </option>
                );
              })}
            </select>
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <Button
              variant="outline"
              onClick={() => setIsAddMemberModalOpen(false)}
              disabled={isMemberActionLoading}
            >
              Hủy
            </Button>
            <Button
              variant="primary"
              onClick={handleAddMember}
              disabled={!selectedUserIdToAdd}
              isLoading={isMemberActionLoading}
            >
              Thêm vào nhóm
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
