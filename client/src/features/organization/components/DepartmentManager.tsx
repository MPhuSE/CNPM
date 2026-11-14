import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { organizationApi } from '../services/organizationApi';
import { DepartmentItem, UserItem } from '../types/organization.types';
import { buildDepartmentTree, DepartmentTreeNode } from '../utils/departmentUtils';
import { Table, TableHead, TableBody, TableRow, TableHeaderCell, TableCell } from '@/shared/ui/Table';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Alert } from '@/shared/ui/Alert';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { DepartmentEditorModal } from './DepartmentEditorModal';
import {
  Building2,
  Plus,
  RefreshCw,
  Edit2,
  FolderTree,
  List,
  ChevronRight,
  User,
} from 'lucide-react';

interface DepartmentManagerProps {
  availableUsers: UserItem[];
}

export const DepartmentManager: React.FC<DepartmentManagerProps> = ({ availableUsers }) => {
  const [viewMode, setViewMode] = useState<'table' | 'tree'>('table');
  const [isEditorOpen, setIsEditorOpen] = useState(false);
  const [selectedDept, setSelectedDept] = useState<DepartmentItem | null>(null);

  const {
    data,
    isLoading,
    isError,
    error,
    refetch,
    isFetching,
  } = useQuery({
    queryKey: ['departments-list'],
    queryFn: () => organizationApi.getDepartments({ page: 0, size: 100 }),
  });

  const departments = data?.items || [];
  const treeRoots = buildDepartmentTree(departments);

  const handleOpenCreate = () => {
    setSelectedDept(null);
    setIsEditorOpen(true);
  };

  const handleOpenEdit = (dept: DepartmentItem) => {
    setSelectedDept(dept);
    setIsEditorOpen(true);
  };

  // Render tree node recursively
  const renderTreeNode = (node: DepartmentTreeNode) => {
    return (
      <div key={node.id} className="space-y-1">
        <div
          className={`flex items-center justify-between p-3 rounded-xl border transition-all ${
            node.level === 0
              ? 'bg-white border-gray-200 shadow-sm'
              : 'bg-slate-50/70 border-slate-200 ml-6'
          }`}
        >
          <div className="flex items-center gap-3">
            <div
              className={`w-8 h-8 rounded-lg flex items-center justify-center font-bold text-xs ${
                node.level === 0
                  ? 'bg-indigo-600 text-white'
                  : 'bg-indigo-100 text-indigo-700'
              }`}
            >
              <Building2 className="w-4 h-4" />
            </div>

            <div>
              <div className="flex items-center gap-2">
                <span className="font-semibold text-gray-900 text-sm">{node.name}</span>
                <span className="font-mono text-xs text-gray-500 bg-gray-100 px-1.5 py-0.5 rounded">
                  {node.code}
                </span>
                <Badge size="sm" color={node.isActive ? 'emerald' : 'gray'}>
                  {node.isActive ? 'Hoạt động' : 'Tạm ngưng'}
                </Badge>
              </div>

              <div className="flex items-center gap-3 text-xs text-gray-500 mt-1">
                {node.parentName && (
                  <span>
                    Trực thuộc: <strong className="text-gray-700">{node.parentName}</strong>
                  </span>
                )}
                <span className="flex items-center gap-1">
                  <User className="w-3.5 h-3.5 text-gray-400" />
                  Trưởng bộ phận:{' '}
                  {node.managerName ? (
                    <strong className="text-gray-800">{node.managerName}</strong>
                  ) : (
                    <span className="italic text-gray-400">Chưa bổ nhiệm</span>
                  )}
                </span>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-1">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => handleOpenEdit(node)}
              className="p-1.5 text-gray-500 hover:text-indigo-600"
              title="Chỉnh sửa phòng ban"
            >
              <Edit2 className="w-4 h-4" />
            </Button>
          </div>
        </div>

        {/* Child nodes */}
        {node.children && node.children.length > 0 && (
          <div className="pl-4 border-l-2 border-indigo-100 space-y-1 my-1">
            {node.children.map((child) => renderTreeNode(child))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="space-y-4">
      {/* Top action bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <div className="flex items-center gap-2">
          <div className="flex bg-gray-100 p-1 rounded-lg border border-gray-200">
            <button
              onClick={() => setViewMode('table')}
              className={`flex items-center gap-1.5 px-3 py-1 text-xs font-semibold rounded-md transition-colors ${
                viewMode === 'table'
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              <List className="w-3.5 h-3.5" />
              Dạng bảng
            </button>
            <button
              onClick={() => setViewMode('tree')}
              className={`flex items-center gap-1.5 px-3 py-1 text-xs font-semibold rounded-md transition-colors ${
                viewMode === 'tree'
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              <FolderTree className="w-3.5 h-3.5" />
              Cây phân cấp
            </button>
          </div>
          <span className="text-xs text-gray-400">
            Tổng cộng: <strong className="text-gray-700">{departments.length}</strong> phòng ban
          </span>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            disabled={isFetching}
            className="flex items-center gap-1.5"
            title="Tải lại danh sách phòng ban"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin' : ''}`} />
            <span>Làm mới</span>
          </Button>

          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenCreate}
            className="flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" />
            <span>Thêm phòng ban</span>
          </Button>
        </div>
      </div>

      {/* 3 UI STATES */}
      {isLoading ? (
        <div className="py-12 bg-white rounded-xl border border-gray-200">
          <LoadingSpinner size="lg" label="Đang tải danh sách phòng ban..." />
        </div>
      ) : isError ? (
        <Alert variant="error" title="Không thể tải danh sách phòng ban">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mt-1">
            <span>
              {(error as any)?.message ||
                'Đã xảy ra sự cố khi tải dữ liệu cơ cấu tổ chức. Vui lòng thử lại.'}
            </span>
            <Button variant="outline" size="sm" onClick={() => refetch()}>
              Thử lại
            </Button>
          </div>
        </Alert>
      ) : departments.length === 0 ? (
        <div className="py-16 text-center bg-white rounded-xl border border-gray-200 p-6">
          <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mx-auto text-gray-400 mb-3">
            <Building2 className="w-6 h-6" />
          </div>
          <h3 className="text-base font-semibold text-gray-900">Chưa có phòng ban nào</h3>
          <p className="text-xs text-gray-500 mt-1 max-w-sm mx-auto">
            Hệ thống chưa có dữ liệu phòng ban. Hãy tạo phòng ban đầu tiên để thiết lập cấu trúc tổ chức.
          </p>
          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenCreate}
            className="mt-4"
          >
            Thêm phòng ban mới
          </Button>
        </div>
      ) : viewMode === 'tree' ? (
        <div className="bg-white p-6 rounded-xl border border-gray-200 space-y-3 shadow-sm">
          <div className="flex items-center gap-2 pb-3 border-b border-gray-100 text-xs text-gray-500">
            <FolderTree className="w-4 h-4 text-indigo-600" />
            <span>Sơ đồ cây phân cấp tổ chức (Hỗ trợ phát hiện và ngăn ngừa chu trình đệ quy)</span>
          </div>
          <div className="space-y-3">
            {treeRoots.map((root) => renderTreeNode(root))}
          </div>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>Mã phòng ban</TableHeaderCell>
                <TableHeaderCell>Tên phòng ban</TableHeaderCell>
                <TableHeaderCell>Phòng ban cấp trên</TableHeaderCell>
                <TableHeaderCell>Trưởng bộ phận (Manager)</TableHeaderCell>
                <TableHeaderCell>Trạng thái</TableHeaderCell>
                <TableHeaderCell className="text-right">Thao tác</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {departments.map((dept) => (
                <TableRow key={dept.id}>
                  <TableCell className="font-mono text-xs font-semibold text-gray-900">
                    {dept.code}
                  </TableCell>

                  <TableCell className="font-medium text-gray-900">
                    <div className="flex items-center gap-2">
                      <Building2 className="w-4 h-4 text-indigo-500" />
                      <span>{dept.name}</span>
                    </div>
                  </TableCell>

                  <TableCell className="text-xs text-gray-700">
                    {dept.parentName ? (
                      <span className="inline-flex items-center gap-1 font-medium bg-gray-50 px-2 py-0.5 rounded border border-gray-200">
                        {dept.parentName}
                      </span>
                    ) : (
                      <span className="text-gray-400 italic">Phòng ban cấp cao nhất</span>
                    )}
                  </TableCell>

                  <TableCell className="text-xs text-gray-700">
                    {dept.managerName ? (
                      <span className="font-semibold text-gray-900">
                        {dept.managerName}
                      </span>
                    ) : (
                      <span className="text-gray-400 italic">Chưa bổ nhiệm</span>
                    )}
                  </TableCell>

                  <TableCell>
                    <Badge color={dept.isActive ? 'emerald' : 'gray'}>
                      {dept.isActive ? 'Hoạt động' : 'Tạm ngưng'}
                    </Badge>
                  </TableCell>

                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleOpenEdit(dept)}
                      className="p-1.5 text-gray-500 hover:text-indigo-600"
                      title="Chỉnh sửa phòng ban"
                    >
                      <Edit2 className="w-4 h-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Editor Modal */}
      <DepartmentEditorModal
        isOpen={isEditorOpen}
        onClose={() => setIsEditorOpen(false)}
        onSuccess={() => refetch()}
        department={selectedDept}
        allDepartments={departments}
        availableUsers={availableUsers}
      />
    </div>
  );
};
