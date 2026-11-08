import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { WorkflowSummary } from '@/shared/types/workflow';
import { Table, TableHead, TableBody, TableRow, TableHeaderCell, TableCell } from '@/shared/ui/Table';
import { Button } from '@/shared/ui/Button';
import { Badge } from '@/shared/ui/Badge';
import { Card } from '@/shared/ui/Card';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { PlusCircle, Edit } from 'lucide-react';

export const WorkflowListPage: React.FC = () => {
  const { data: workflows, isLoading } = useQuery({
    queryKey: ['workflows'],
    queryFn: () => apiClient<any>(ENDPOINTS.WORKFLOWS),
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Cấu hình quy trình</h1>
          <p className="text-sm text-gray-500 mt-1">
            Quản lý định nghĩa biểu mẫu, bước phê duyệt và rẽ nhánh điều kiện
          </p>
        </div>
      </div>

      <Card>
        {isLoading ? (
          <LoadingSpinner label="Đang tải danh sách quy trình..." />
        ) : !workflows || workflows.items?.length === 0 ? (
          <div className="text-center py-12 text-gray-500">Chưa có quy trình nào được tạo.</div>
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>Mã quy trình</TableHeaderCell>
                <TableHeaderCell>Tên quy trình</TableHeaderCell>
                <TableHeaderCell>Phiên bản kích hoạt</TableHeaderCell>
                <TableHeaderCell>Trạng thái</TableHeaderCell>
                <TableHeaderCell>Thao tác</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {workflows.items.map((wf: WorkflowSummary) => (
                <TableRow key={wf.id}>
                  <TableCell className="font-mono font-medium text-xs text-indigo-700">
                    {wf.code}
                  </TableCell>
                  <TableCell>
                    <div className="font-medium text-gray-900">{wf.name}</div>
                    <div className="text-xs text-gray-400">{wf.description}</div>
                  </TableCell>
                  <TableCell>
                    {wf.activeVersionNo ? (
                      <span className="font-semibold text-xs text-gray-700 bg-gray-100 px-2 py-0.5 rounded">
                        v{wf.activeVersionNo}
                      </span>
                    ) : (
                      <span className="text-xs text-amber-600 font-medium italic">Chưa publish</span>
                    )}
                  </TableCell>
                  <TableCell>
                    <Badge color={wf.isActive ? 'emerald' : 'gray'}>
                      {wf.isActive ? 'Hoạt động' : 'Tạm khóa'}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Link to={`/workflows/${wf.id}`}>
                      <Button variant="outline" size="sm" className="flex items-center gap-1.5">
                        <Edit className="w-3.5 h-3.5" />
                        Chỉnh sửa / Xem
                      </Button>
                    </Link>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>
    </div>
  );
};
