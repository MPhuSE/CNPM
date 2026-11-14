import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { organizationApi } from '../services/organizationApi';
import { UserItem, SystemRole, DepartmentItem } from '../types/organization.types';
import { Table, TableHead, TableBody, TableRow, TableHeaderCell, TableCell } from '@/shared/ui/Table';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { Alert } from '@/shared/ui/Alert';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { formatDateTime } from '@/shared/utils/formatters';
import { UserEditorModal } from './UserEditorModal';
import { UserStatusConfirmModal } from './UserStatusConfirmModal';
import {
  Search,
  UserPlus,
  RefreshCw,
  UserX,
  UserCheck,
  Edit2,
  Users,
  Filter,
  Shield,
} from 'lucide-react';

interface UserListProps {
  departments: DepartmentItem[];
}

export const UserList: React.FC<UserListProps> = ({ departments }) => {
  // Query / Filter states
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState<SystemRole | 'ALL'>('ALL');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'true' | 'false'>('ALL');

  // Modal states
  const [isEditorOpen, setIsEditorOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserItem | null>(null);
  const [statusTargetUser, setStatusTargetUser] = useState<UserItem | null>(null);
  const [isStatusConfirmOpen, setIsStatusConfirmOpen] = useState(false);

  // Debounce search input
  const handleSearchChange = (val: string) => {
    setSearch(val);
    const timeout = setTimeout(() => {
      setDebouncedSearch(val);
      setPage(0); // Reset to first page
    }, 400);
    return () => clearTimeout(timeout);
  };

  // Convert status string to boolean or 'ALL'
  const computedIsActive =
    statusFilter === 'ALL' ? 'ALL' : statusFilter === 'true' ? true : false;

  // Fetch users via TanStack Query
  const {
    data,
    isLoading,
    isError,
    error,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ['users-list', page, size, debouncedSearch, roleFilter, statusFilter],
    queryFn: () =>
      organizationApi.getUsers({
        page,
        size,
        search: debouncedSearch,
        role: roleFilter,
        isActive: computedIsActive,
      }),
  });

  const users = data?.items || [];
  const total = data?.total || 0;
  const totalPages = Math.ceil(total / size) || 1;

  const handleOpenCreate = () => {
    setSelectedUser(null);
    setIsEditorOpen(true);
  };

  const handleOpenEdit = (user: UserItem) => {
    setSelectedUser(user);
    setIsEditorOpen(true);
  };

  const handleOpenStatusToggle = (user: UserItem) => {
    setStatusTargetUser(user);
    setIsStatusConfirmOpen(true);
  };

  const handleResetFilters = () => {
    setSearch('');
    setDebouncedSearch('');
    setRoleFilter('ALL');
    setStatusFilter('ALL');
    setPage(0);
  };

  return (
    <div className="space-y-4">
      {/* Top action bar & filters */}
      <div className="flex flex-col md:flex-row gap-3 justify-between items-start md:items-center">
        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto">
          {/* Search input */}
          <div className="relative w-full sm:w-64">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Tìm kiếm theo tên, email..."
              value={search}
              onChange={(e) => handleSearchChange(e.target.value)}
              className="w-full pl-9 pr-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>

          {/* Role filter */}
          <div className="w-full sm:w-48">
            <select
              value={roleFilter}
              onChange={(e) => {
                setRoleFilter(e.target.value as any);
                setPage(0);
              }}
              className="w-full py-1.5 px-3 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="ALL">Tất cả vai trò</option>
              <option value="ROLE_ADMIN">Quản trị viên (Admin)</option>
              <option value="ROLE_APPROVER">Người phê duyệt (Approver)</option>
              <option value="ROLE_EMPLOYEE">Nhân viên (Employee)</option>
            </select>
          </div>

          {/* Status filter */}
          <div className="w-full sm:w-40">
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value as any);
                setPage(0);
              }}
              className="w-full py-1.5 px-3 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="true">Đang hoạt động</option>
              <option value="false">Đã vô hiệu hóa</option>
            </select>
          </div>

          {(debouncedSearch || roleFilter !== 'ALL' || statusFilter !== 'ALL') && (
            <button
              onClick={handleResetFilters}
              className="text-xs text-indigo-600 hover:text-indigo-800 font-medium py-1 px-2 rounded hover:bg-indigo-50"
            >
              Đặt lại bộ lọc
            </button>
          )}
        </div>

        <div className="flex items-center gap-2 self-end md:self-auto">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            disabled={isFetching}
            className="flex items-center gap-1.5"
            title="Tải lại dữ liệu"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin' : ''}`} />
            <span className="hidden sm:inline">Làm mới</span>
          </Button>

          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenCreate}
            className="flex items-center gap-1.5"
          >
            <UserPlus className="w-4 h-4" />
            <span>Thêm người dùng</span>
          </Button>
        </div>
      </div>

      {/* 3 UI STATES: LOADING / ERROR / EMPTY / TABLE */}
      {isLoading ? (
        <div className="py-12 bg-white rounded-xl border border-gray-200">
          <LoadingSpinner size="lg" label="Đang tải danh sách người dùng..." />
        </div>
      ) : isError ? (
        <Alert variant="error" title="Không thể tải danh sách người dùng">
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
      ) : users.length === 0 ? (
        <div className="py-16 text-center bg-white rounded-xl border border-gray-200 p-6">
          <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mx-auto text-gray-400 mb-3">
            <Users className="w-6 h-6" />
          </div>
          <h3 className="text-base font-semibold text-gray-900">Không tìm thấy người dùng</h3>
          <p className="text-xs text-gray-500 mt-1 max-w-sm mx-auto">
            {debouncedSearch || roleFilter !== 'ALL' || statusFilter !== 'ALL'
              ? 'Không có tài khoản nào phù hợp với điều kiện tìm kiếm hoặc bộ lọc hiện tại.'
              : 'Hệ thống chưa có tài khoản người dùng nào. Hãy thêm tài khoản đầu tiên.'}
          </p>
          {(debouncedSearch || roleFilter !== 'ALL' || statusFilter !== 'ALL') && (
            <Button
              variant="outline"
              size="sm"
              onClick={handleResetFilters}
              className="mt-4"
            >
              Xóa bộ lọc tìm kiếm
            </Button>
          )}
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>Tài khoản</TableHeaderCell>
                <TableHeaderCell>Họ và tên</TableHeaderCell>
                <TableHeaderCell>Email</TableHeaderCell>
                <TableHeaderCell>Phòng ban</TableHeaderCell>
                <TableHeaderCell>Vai trò (Roles)</TableHeaderCell>
                <TableHeaderCell>Trạng thái</TableHeaderCell>
                <TableHeaderCell className="text-right">Thao tác</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map((u) => {
                const isAdmin = u.roles.includes('ROLE_ADMIN');
                return (
                  <TableRow key={u.id}>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-full bg-slate-100 flex items-center justify-center text-xs font-bold text-slate-700">
                          {u.fullName.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <div className="font-mono text-xs font-semibold text-gray-900">
                            @{u.username}
                          </div>
                          {u.managerName && (
                            <div className="text-[11px] text-gray-400">
                              QL: {u.managerName}
                            </div>
                          )}
                        </div>
                      </div>
                    </TableCell>

                    <TableCell className="font-medium text-gray-900">
                      {u.fullName}
                    </TableCell>

                    <TableCell className="text-xs text-gray-600 font-mono">
                      {u.email}
                    </TableCell>

                    <TableCell className="text-xs text-gray-700">
                      {u.departmentName ? (
                        <span className="font-medium">{u.departmentName}</span>
                      ) : (
                        <span className="text-gray-400 italic">Chưa gán</span>
                      )}
                    </TableCell>

                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {u.roles?.map((r) => (
                          <Badge
                            key={r}
                            size="sm"
                            color={
                              r === 'ROLE_ADMIN'
                                ? 'rose'
                                : r === 'ROLE_APPROVER'
                                ? 'indigo'
                                : 'blue'
                            }
                          >
                            {r.replace('ROLE_', '')}
                          </Badge>
                        ))}
                      </div>
                    </TableCell>

                    <TableCell>
                      <Badge color={u.isActive ? 'emerald' : 'gray'}>
                        {u.isActive ? 'Hoạt động' : 'Vô hiệu hóa'}
                      </Badge>
                    </TableCell>

                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => handleOpenEdit(u)}
                          className="p-1.5 text-gray-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                          title="Chỉnh sửa tài khoản"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>

                        <button
                          onClick={() => handleOpenStatusToggle(u)}
                          className={`p-1.5 rounded-lg transition-colors ${
                            u.isActive
                              ? 'text-gray-500 hover:text-rose-600 hover:bg-rose-50'
                              : 'text-gray-500 hover:text-emerald-600 hover:bg-emerald-50'
                          }`}
                          title={u.isActive ? 'Vô hiệu hóa tài khoản' : 'Kích hoạt lại tài khoản'}
                        >
                          {u.isActive ? (
                            <UserX className="w-4 h-4" />
                          ) : (
                            <UserCheck className="w-4 h-4" />
                          )}
                        </button>
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>

          {/* Pagination bar */}
          <div className="px-6 py-3 border-t border-gray-100 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-gray-500">
            <div>
              Hiển thị{' '}
              <span className="font-semibold text-gray-700">
                {total === 0 ? 0 : page * size + 1}
              </span>{' '}
              đến{' '}
              <span className="font-semibold text-gray-700">
                {Math.min((page + 1) * size, total)}
              </span>{' '}
              trong tổng số <span className="font-semibold text-gray-900">{total}</span> người dùng
            </div>

            <div className="flex items-center gap-2">
              <span className="text-gray-400">Số bản ghi:</span>
              <select
                value={size}
                onChange={(e) => {
                  setSize(Number(e.target.value));
                  setPage(0);
                }}
                className="border border-gray-200 rounded px-2 py-1 text-xs bg-white focus:outline-none"
              >
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
              </select>

              <div className="flex items-center gap-1 ml-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-2.5 py-1 text-xs"
                >
                  Trước
                </Button>

                <span className="px-2 font-medium text-gray-700">
                  {page + 1} / {totalPages}
                </span>

                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                  className="px-2.5 py-1 text-xs"
                >
                  Sau
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* MODALS */}
      <UserEditorModal
        isOpen={isEditorOpen}
        onClose={() => setIsEditorOpen(false)}
        onSuccess={() => refetch()}
        user={selectedUser}
        departments={departments}
        availableManagers={users}
      />

      <UserStatusConfirmModal
        isOpen={isStatusConfirmOpen}
        onClose={() => setIsStatusConfirmOpen(false)}
        onSuccess={() => refetch()}
        user={statusTargetUser}
      />
    </div>
  );
};
