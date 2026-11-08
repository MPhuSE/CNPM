import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { Card } from '@/shared/ui/Card';
import { Table, TableHead, TableBody, TableRow, TableHeaderCell, TableCell } from '@/shared/ui/Table';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Modal } from '@/shared/ui/Modal';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { Alert } from '@/shared/ui/Alert';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { formatDateTime } from '@/shared/utils/formatters';
import { UserCheck, ShieldAlert, CheckCircle } from 'lucide-react';

export const CoordinationPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [reassignModalOpen, setReassignModalOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<any | null>(null);
  const [newAssigneeId, setNewAssigneeId] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState<string | null>(null);

  // Fetch tasks that need assignment
  const { data: needsAssignmentTasks, isLoading: loadingTasks } = useQuery({
    queryKey: ['tasks-needs-assignment'],
    queryFn: () => apiClient<any>(`${ENDPOINTS.TASKS}?assignmentState=NEEDS_ASSIGNMENT`),
  });

  // Fetch open incidents
  const { data: incidents, isLoading: loadingIncidents } = useQuery({
    queryKey: ['open-incidents'],
    queryFn: () => apiClient<any>(`${ENDPOINTS.INCIDENTS}?status=OPEN`),
  });

  // Fetch approvers for reassignment
  const { data: approvers } = useQuery({
    queryKey: ['approver-users'],
    queryFn: () => apiClient<any>(`${ENDPOINTS.USERS}?role=ROLE_APPROVER&active=true`),
  });

  const reassignMutation = useMutation({
    mutationFn: () => {
      const idempotencyKey = crypto.randomUUID();
      return apiClient(`${ENDPOINTS.TASKS}/${selectedTask.id}/reassign`, {
        method: 'POST',
        idempotencyKey,
        body: JSON.stringify({
          newAssigneeId,
          reason,
          taskVersion: selectedTask.taskVersion,
          requestVersion: selectedTask.requestVersion,
        }),
      });
    },
    onSuccess: () => {
      setReassignModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['tasks-needs-assignment'] });
      queryClient.invalidateQueries({ queryKey: ['open-incidents'] });
    },
    onError: (err: any) => setError(err?.message || 'Không thể phân công lại nhiệm vụ'),
  });

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Điều phối Admin &amp; Sự cố hệ thống</h1>
        <p className="text-sm text-gray-500 mt-1">
          Hàng đợi nhiệm vụ thiếu người duyệt và các sự cố định tuyến cần khắc phục
        </p>
      </div>

      {error && <Alert variant="error">{error}</Alert>}

      {/* Needs Assignment Tasks Queue */}
      <Card
        title="Hàng đợi cần phân công (Needs Assignment)"
        subtitle="Các nhiệm vụ không tìm thấy người nhận hợp lệ (quản lý bị khóa hoặc chưa có thành viên)"
      >
        {loadingTasks ? (
          <LoadingSpinner label="Đang kiểm tra hàng đợi..." />
        ) : !needsAssignmentTasks || needsAssignmentTasks.items?.length === 0 ? (
          <div className="flex items-center gap-2 text-emerald-600 py-4 text-sm font-medium">
            <CheckCircle className="w-5 h-5" />
            Không có nhiệm vụ nào cần phân công tại thời điểm này.
          </div>
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>Mã hồ sơ</TableHeaderCell>
                <TableHeaderCell>Hồ sơ</TableHeaderCell>
                <TableHeaderCell>Bước xử lý</TableHeaderCell>
                <TableHeaderCell>Hạn SLA</TableHeaderCell>
                <TableHeaderCell>Thao tác</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {needsAssignmentTasks.items.map((t: any) => (
                <TableRow key={t.id}>
                  <TableCell className="font-mono text-xs">{t.requestCode || '—'}</TableCell>
                  <TableCell className="font-medium text-gray-900">{t.requestTitle}</TableCell>
                  <TableCell>
                    <span className="text-xs bg-rose-50 text-rose-700 px-2 py-1 rounded font-medium">
                      {t.stepName}
                    </span>
                  </TableCell>
                  <TableCell className="text-xs text-gray-500">{formatDateTime(t.dueAt)}</TableCell>
                  <TableCell>
                    <Button
                      size="sm"
                      onClick={() => {
                        setSelectedTask(t);
                        setReason('');
                        setNewAssigneeId('');
                        setError(null);
                        setReassignModalOpen(true);
                      }}
                    >
                      Phân công người duyệt
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>

      {/* Open Incidents Queue */}
      <Card
        title="Sự cố vận hành (Open Incidents)"
        subtitle="Các sự cố ROUTING_ERROR hoặc cờ hệ thống đang mở"
      >
        {loadingIncidents ? (
          <LoadingSpinner label="Đang kiểm tra sự cố..." />
        ) : !incidents || incidents.items?.length === 0 ? (
          <div className="flex items-center gap-2 text-emerald-600 py-4 text-sm font-medium">
            <CheckCircle className="w-5 h-5" />
            Hệ thống ổn định. Không có sự cố vận hành nào mở.
          </div>
        ) : (
          <Table>
            <TableHead>
              <TableRow>
                <TableHeaderCell>Loại sự cố</TableHeaderCell>
                <TableHeaderCell>Mã lỗi</TableHeaderCell>
                <TableHeaderCell>Hồ sơ liên quan</TableHeaderCell>
                <TableHeaderCell>Thời điểm mở</TableHeaderCell>
                <TableHeaderCell>Trạng thái</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {incidents.items.map((inc: any) => (
                <TableRow key={inc.id}>
                  <TableCell className="font-semibold text-rose-700">{inc.incidentType}</TableCell>
                  <TableCell className="font-mono text-xs text-gray-800">{inc.errorCode}</TableCell>
                  <TableCell className="text-sm">#{inc.requestId}</TableCell>
                  <TableCell className="text-xs text-gray-500">{formatDateTime(inc.openedAt)}</TableCell>
                  <TableCell>
                    <Badge color="rose">{inc.status}</Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>

      {/* Reassign Modal */}
      <Modal
        isOpen={reassignModalOpen}
        onClose={() => setReassignModalOpen(false)}
        title="Phân công người duyệt cho nhiệm vụ"
        description="Chỉ những người có ROLE_APPROVER và thỏa mãn điều kiện thẩm quyền mới được phân công."
      >
        <div className="space-y-4">
          <Select
            label="Chọn người duyệt mới"
            required
            value={newAssigneeId}
            onChange={(e) => setNewAssigneeId(e.target.value)}
            options={[
              { label: '-- Chọn người dùng --', value: '' },
              ...(approvers?.items || []).map((u: any) => ({
                label: `${u.fullName} (@${u.username})`,
                value: u.id,
              })),
            ]}
          />

          <Input
            label="Lý do phân công"
            required
            placeholder="VD: Quản lý trực tiếp nghỉ phép dài hạn"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
          />

          <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <Button variant="outline" onClick={() => setReassignModalOpen(false)}>
              Hủy
            </Button>
            <Button
              disabled={!newAssigneeId || !reason.trim()}
              isLoading={reassignMutation.isPending}
              onClick={() => reassignMutation.mutate()}
            >
              Lưu phân công
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
