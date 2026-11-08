import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { Table, TableHead, TableBody, TableRow, TableHeaderCell, TableCell } from '@/shared/ui/Table';
import { Card } from '@/shared/ui/Card';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { formatDateTime } from '@/shared/utils/formatters';

export const AuditLogsPage: React.FC = () => {
  const { data: logs, isLoading } = useQuery({
    queryKey: ['audit-logs'],
    queryFn: () => apiClient<any>(ENDPOINTS.AUDIT_LOGS),
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Nhật ký kiểm toán (Audit Logs)</h1>
        <p className="text-sm text-gray-500 mt-1">
          Lưu vết bất biến (Append-only) mọi thay đổi trạng thái, quyền hạn và thao tác hệ thống
        </p>
      </div>

      <Card>
        {isLoading ? (
          <LoadingSpinner label="Đang tải nhật ký kiểm toán..." />
        ) : !logs || logs.items?.length === 0 ? (
          <div className="text-center py-12 text-gray-500">Chưa có nhật ký nào được ghi lại.</div>
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>Thời gian</TableHeaderCell>
                <TableHeaderCell>Người thực hiện</TableHeaderCell>
                <TableHeaderCell>Hành động</TableHeaderCell>
                <TableHeaderCell>Thực thể (Entity)</TableHeaderCell>
                <TableHeaderCell>Chi tiết (Details)</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {logs.items.map((log: any) => (
                <TableRow key={log.id}>
                  <TableCell className="text-xs text-gray-500 font-mono">
                    {formatDateTime(log.createdAt)}
                  </TableCell>
                  <TableCell className="font-medium text-gray-900 text-xs">
                    {log.actorName || 'Hệ thống'}
                  </TableCell>
                  <TableCell>
                    <span className="text-xs font-semibold bg-gray-100 text-gray-800 px-2 py-0.5 rounded">
                      {log.action}
                    </span>
                  </TableCell>
                  <TableCell className="text-xs font-mono">
                    {log.entityType}:{log.entityId}
                  </TableCell>
                  <TableCell className="text-xs text-gray-600 max-w-xs truncate font-mono">
                    {JSON.stringify(log.details)}
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
