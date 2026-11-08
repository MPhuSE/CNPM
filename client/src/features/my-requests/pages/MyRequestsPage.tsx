import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { requestApi } from '../api/requestApi';
import { Table, TableHead, TableBody, TableRow, TableHeaderCell, TableCell } from '@/shared/ui/Table';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { formatDateTime, getRequestStatusMeta } from '@/shared/utils/formatters';
import { PlusCircle, Search, Filter } from 'lucide-react';

export const MyRequestsPage: React.FC = () => {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<string>('');

  const { data, isLoading } = useQuery({
    queryKey: ['my-requests', page, statusFilter],
    queryFn: () => requestApi.getMyRequests({ page, size: 20, status: statusFilter || undefined }),
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Hồ sơ của tôi</h1>
          <p className="text-sm text-gray-500 mt-1">Danh sách các yêu cầu và quy trình bạn đã tạo</p>
        </div>
        <Link to="/my-requests/new">
          <Button className="flex items-center gap-2">
            <PlusCircle className="w-4 h-4" />
            Tạo hồ sơ mới
          </Button>
        </Link>
      </div>

      <Card>
        {/* Filters */}
        <div className="flex items-center gap-4 mb-4">
          <div className="w-64">
            <select
              className="w-full text-sm border border-gray-300 rounded-lg px-3 py-2 bg-white"
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(0);
              }}
            >
              <option value="">-- Tất cả trạng thái --</option>
              <option value="DRAFT">Bản nháp</option>
              <option value="PENDING">Đang chờ duyệt</option>
              <option value="RETURNED">Cần bổ sung</option>
              <option value="APPROVED">Đã duyệt</option>
              <option value="REJECTED">Bị từ chối</option>
              <option value="CANCELLED">Đã hủy</option>
            </select>
          </div>
        </div>

        {isLoading ? (
          <LoadingSpinner label="Đang tải danh sách hồ sơ..." />
        ) : !data || data.items.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            Không tìm thấy hồ sơ nào. Bấm "Tạo hồ sơ mới" để bắt đầu!
          </div>
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>Mã hồ sơ</TableHeaderCell>
                <TableHeaderCell>Tiêu đề</TableHeaderCell>
                <TableHeaderCell>Quy trình</TableHeaderCell>
                <TableHeaderCell>Trạng thái</TableHeaderCell>
                <TableHeaderCell>Thời gian gửi đầu</TableHeaderCell>
                <TableHeaderCell>Thao tác</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {data.items.map((req) => {
                const meta = getRequestStatusMeta(req.status);
                return (
                  <TableRow key={req.id}>
                    <TableCell className="font-mono font-medium text-xs text-gray-800">
                      {req.requestCode || <span className="italic text-gray-400">Chưa cấp mã</span>}
                    </TableCell>
                    <TableCell className="font-medium text-gray-900">{req.title}</TableCell>
                    <TableCell>
                      <span className="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded font-medium">
                        {req.workflowName}
                      </span>
                    </TableCell>
                    <TableCell>
                      <Badge color={meta.color}>{meta.label}</Badge>
                    </TableCell>
                    <TableCell className="text-xs text-gray-500">
                      {formatDateTime(req.firstSubmittedAt)}
                    </TableCell>
                    <TableCell>
                      <Link to={`/my-requests/${req.id}`}>
                        <Button variant="outline" size="sm">
                          Chi tiết
                        </Button>
                      </Link>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        )}
      </Card>
    </div>
  );
};
