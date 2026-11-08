import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { taskApi } from '../api/taskApi';
import { Table, TableHead, TableBody, TableRow, TableHeaderCell, TableCell } from '@/shared/ui/Table';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { formatDateTime, getTaskStatusMeta } from '@/shared/utils/formatters';
import { Clock, UserCheck, Users } from 'lucide-react';

export const TaskInboxPage: React.FC = () => {
  const [scope, setScope] = useState<'mine' | 'group'>('mine');
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['task-inbox', scope, page],
    queryFn: () => taskApi.getTasks({ scope, page, size: 20 }),
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Hộp thư duyệt</h1>
        <p className="text-sm text-gray-500 mt-1">
          Quản lý các nhiệm vụ cần phê duyệt cá nhân và nhóm của bạn
        </p>
      </div>

      <div className="flex border-b border-gray-200">
        <button
          onClick={() => {
            setScope('mine');
            setPage(0);
          }}
          className={`flex items-center gap-2 py-3 px-6 text-sm font-semibold border-b-2 transition-colors ${
            scope === 'mine'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          <UserCheck className="w-4 h-4" />
          Nhiệm vụ của tôi (Đã nhận)
        </button>

        <button
          onClick={() => {
            setScope('group');
            setPage(0);
          }}
          className={`flex items-center gap-2 py-3 px-6 text-sm font-semibold border-b-2 transition-colors ${
            scope === 'group'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-gray-500 hover:text-gray-700'
          }`}
        >
          <Users className="w-4 h-4" />
          Nhiệm vụ nhóm (Cần nhận việc)
        </button>
      </div>

      <Card>
        {isLoading ? (
          <LoadingSpinner label="Đang tải danh sách tác vụ..." />
        ) : !data || data.items.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            {scope === 'mine'
              ? 'Bạn không có nhiệm vụ nào cần xử lý lúc này.'
              : 'Không có nhiệm vụ nhóm nào đang chờ nhận việc.'}
          </div>
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>Mã hồ sơ</TableHeaderCell>
                <TableHeaderCell>Hồ sơ &amp; Quy trình</TableHeaderCell>
                <TableHeaderCell>Bước duyệt</TableHeaderCell>
                <TableHeaderCell>Người yêu cầu</TableHeaderCell>
                <TableHeaderCell>Hạn xử lý (SLA)</TableHeaderCell>
                <TableHeaderCell>Trạng thái</TableHeaderCell>
                <TableHeaderCell>Thao tác</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {data.items.map((task) => {
                const meta = getTaskStatusMeta(task.status, task.assignmentState);
                return (
                  <TableRow key={task.id}>
                    <TableCell className="font-mono font-medium text-xs text-gray-800">
                      {task.requestCode || '—'}
                    </TableCell>
                    <TableCell>
                      <div className="font-medium text-gray-900">{task.requestTitle}</div>
                      <div className="text-xs text-gray-500">{task.workflowName} (Rev #{task.revisionNo})</div>
                    </TableCell>
                    <TableCell>
                      <span className="text-xs bg-indigo-50 text-indigo-700 px-2 py-1 rounded font-medium">
                        {task.stepName}
                      </span>
                    </TableCell>
                    <TableCell className="text-sm text-gray-700">{task.requesterName}</TableCell>
                    <TableCell className="text-xs text-gray-600">
                      {task.dueAt ? (
                        <div className="flex items-center gap-1.5">
                          <Clock className={`w-3.5 h-3.5 ${task.isOverdue ? 'text-rose-500' : 'text-gray-400'}`} />
                          <span className={task.isOverdue ? 'text-rose-600 font-semibold' : ''}>
                            {formatDateTime(task.dueAt)}
                          </span>
                        </div>
                      ) : (
                        '—'
                      )}
                    </TableCell>
                    <TableCell>
                      <Badge color={meta.color}>{meta.label}</Badge>
                    </TableCell>
                    <TableCell>
                      <Link to={`/inbox/${task.id}`}>
                        <Button variant="outline" size="sm">
                          Xử lý
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
