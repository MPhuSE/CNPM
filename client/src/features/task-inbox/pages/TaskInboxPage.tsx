import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { taskApi } from '../api/taskApi';
import { Table, TableHead, TableBody, TableRow, TableHeaderCell, TableCell } from '@/shared/ui/Table';
import { Badge } from '@/shared/ui/Badge';
import { Card } from '@/shared/ui/Card';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { formatDateTime, getTaskStatusMeta } from '@/shared/utils/formatters';
import { Clock, UserCheck, Users } from 'lucide-react';

export const TaskInboxPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const scope = searchParams.get('scope') === 'group' ? 'group' : 'mine';
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
            setSearchParams({ scope: 'mine' });
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
            setSearchParams({ scope: 'group' });
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

      <Card noPadding>
        {isLoading ? (
          <div className="py-12">
            <LoadingSpinner label="Đang tải danh sách tác vụ..." />
          </div>
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
                <TableHeaderCell className="w-32 whitespace-nowrap">Mã hồ sơ</TableHeaderCell>
                <TableHeaderCell>Hồ sơ &amp; Quy trình</TableHeaderCell>
                <TableHeaderCell className="w-48 whitespace-nowrap">Bước duyệt</TableHeaderCell>
                <TableHeaderCell className="w-44 whitespace-nowrap">Người yêu cầu</TableHeaderCell>
                <TableHeaderCell className="w-44 whitespace-nowrap">Hạn xử lý (SLA)</TableHeaderCell>
                <TableHeaderCell className="w-32 text-center whitespace-nowrap">Trạng thái</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {data.items.map((task) => {
                const meta = getTaskStatusMeta(task.status, task.assignmentState);
                return (
                  <TableRow
                    key={task.id}
                    onClick={() => navigate(`/inbox/${task.id}`, { state: { returnTo: `/inbox?scope=${scope}` } })}
                    className="cursor-pointer hover:bg-zinc-50/80 transition-colors group"
                    title="Bấm để xem chi tiết & phê duyệt"
                  >
                    <TableCell className="whitespace-nowrap">
                      <span className="font-mono text-xs font-semibold text-zinc-700 group-hover:text-zinc-950 transition-colors">
                        {task.requestCode || '—'}
                      </span>
                    </TableCell>
                    <TableCell>
                      <div className="font-semibold text-sm text-zinc-900 group-hover:text-zinc-950 transition-colors leading-snug">
                        {task.requestTitle}
                      </div>
                      <div className="text-xs text-zinc-500 mt-0.5 font-mono">
                        {task.workflowName} (Rev #{task.revisionNo})
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="text-sm font-medium text-zinc-800 leading-snug">
                        {task.stepName}
                      </div>
                    </TableCell>
                    <TableCell className="text-sm text-zinc-700 whitespace-nowrap">
                      {task.requesterName}
                    </TableCell>
                    <TableCell className="text-sm text-zinc-600 whitespace-nowrap tabular-nums">
                      {task.dueAt ? (
                        <div className="flex items-center gap-1.5">
                          <Clock className={`w-3.5 h-3.5 flex-shrink-0 ${task.isOverdue ? 'text-rose-500' : 'text-zinc-400'}`} strokeWidth={1.75} />
                          <span className={task.isOverdue ? 'text-rose-600 font-semibold' : ''}>
                            {formatDateTime(task.dueAt)}
                          </span>
                        </div>
                      ) : (
                        '—'
                      )}
                    </TableCell>
                    <TableCell className="text-center whitespace-nowrap">
                      <Badge color={meta.color}>{meta.label}</Badge>
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
