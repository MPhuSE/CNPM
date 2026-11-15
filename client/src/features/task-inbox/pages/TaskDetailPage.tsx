import React, { useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { taskApi } from '../api/taskApi';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { Badge } from '@/shared/ui/Badge';
import { Alert } from '@/shared/ui/Alert';
import { Modal } from '@/shared/ui/Modal';
import { Input } from '@/shared/ui/Input';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { formatDateTime, formatVND, getTaskStatusMeta } from '@/shared/utils/formatters';
import { TaskAction } from '@/shared/types/task';
import { Check, X, RotateCcw, Hand, ArrowLeft } from 'lucide-react';

export const TaskDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const queryClient = useQueryClient();

  const [decisionModalOpen, setDecisionModalOpen] = useState(false);
  const [selectedAction, setSelectedAction] = useState<TaskAction | null>(null);
  const [reason, setReason] = useState('');
  const [actionError, setActionError] = useState<string | null>(null);

  const handleBack = () => {
    if (location.state?.returnTo) {
      navigate(location.state.returnTo);
    } else {
      navigate(-1);
    }
  };

  const { data: task, isLoading } = useQuery({
    queryKey: ['task-detail', id],
    queryFn: () => taskApi.getTaskDetail(id!),
    enabled: !!id,
  });

  const claimMutation = useMutation({
    mutationFn: () => {
      const idempotencyKey = crypto.randomUUID();
      return taskApi.claimTask(
        id!,
        {
          taskVersion: task!.taskVersion,
          requestVersion: task!.requestVersion,
          revisionId: task!.revisionId,
        },
        idempotencyKey
      );
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task-detail', id] });
      queryClient.invalidateQueries({ queryKey: ['task-inbox'] });
    },
    onError: (err: any) => {
      if (err?.status === 409 || err?.code === 'CONCURRENCY_CONFLICT') {
        setActionError('Xung đột dữ liệu: Nhiệm vụ đã được người khác xử lý hoặc nội dung hồ sơ vừa được cập nhật. Vui lòng tải lại trang.');
      } else {
        setActionError(err?.message || 'Không thể nhận tác vụ');
      }
    },
  });

  const decisionMutation = useMutation({
    mutationFn: () => {
      const idempotencyKey = crypto.randomUUID();
      return taskApi.decideTask(
        id!,
        {
          action: selectedAction!,
          reason: reason.trim() || null,
          taskVersion: task!.taskVersion,
          requestVersion: task!.requestVersion,
          revisionId: task!.revisionId,
        },
        idempotencyKey
      );
    },
    onSuccess: () => {
      setDecisionModalOpen(false);
      if (location.state?.returnTo) {
        navigate(location.state.returnTo);
      } else {
        navigate('/inbox');
      }
    },
    onError: (err: any) => {
      if (err?.status === 409 || err?.code === 'CONCURRENCY_CONFLICT') {
        setActionError('Xung đột dữ liệu: Quyết định không thể thực hiện vì nhiệm vụ đã có thay đổi mới từ người khác. Vui lòng tải lại trang.');
      } else {
        setActionError(err?.message || 'Không thể thực hiện quyết định');
      }
    },
  });

  if (isLoading || !task) {
    return <LoadingSpinner label="Đang tải chi tiết nhiệm vụ..." />;
  }

  const meta = getTaskStatusMeta(task.status, task.assignmentState);

  const openDecision = (action: TaskAction) => {
    setSelectedAction(action);
    setReason('');
    setActionError(null);
    setDecisionModalOpen(true);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">{task.requestTitle}</h1>
            <Badge color={meta.color}>{meta.label}</Badge>
          </div>
          <p className="text-xs text-gray-500 mt-1 font-mono">
            Hồ sơ: {task.requestCode || '—'} · Bước: {task.stepName} · Revision #{task.revisionNo}
          </p>
        </div>

        <div className="flex items-center gap-2">
          {task.allowedActions?.includes('CLAIM') && (
            <Button
              className="flex items-center gap-2"
              onClick={() => claimMutation.mutate()}
              isLoading={claimMutation.isPending}
            >
              <Hand className="w-4 h-4" />
              Nhận việc (Claim)
            </Button>
          )}

          {task.allowedActions?.includes('APPROVE') && (
            <Button
              variant="primary"
              className="bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500 flex items-center gap-2"
              onClick={() => openDecision('APPROVE')}
            >
              <Check className="w-4 h-4" />
              Chấp thuận
            </Button>
          )}

          {task.allowedActions?.includes('REQUEST_CHANGES') && (
            <Button
              variant="secondary"
              className="flex items-center gap-2"
              onClick={() => openDecision('REQUEST_CHANGES')}
            >
              <RotateCcw className="w-4 h-4" />
              Yêu cầu bổ sung
            </Button>
          )}

          {task.allowedActions?.includes('REJECT') && (
            <Button
              variant="danger"
              className="flex items-center gap-2"
              onClick={() => openDecision('REJECT')}
            >
              <X className="w-4 h-4" />
              Từ chối
            </Button>
          )}
        </div>
      </div>

      {actionError && (
        <Alert variant="error" className="mb-4">
          {actionError}
        </Alert>
      )}

      {task.assignmentState === 'NEEDS_ASSIGNMENT' && (
        <Alert variant="warning" className="mb-4">
          Nhiệm vụ này đang ở trạng thái <strong>Cần phân công</strong> (chưa có người duyệt hợp lệ hoặc phòng ban chưa có quản lý). Quản trị viên cần phân công lại tại màn hình Điều phối trước khi có thể phê duyệt.
        </Alert>
      )}

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card title="Nội dung hồ sơ cần xét duyệt (Revision snapshot)">
            {Object.keys(task.formData || {}).length === 0 ? (
              <p className="text-sm text-gray-400 italic">Không có dữ liệu trong biểu mẫu này.</p>
            ) : (
              <div className="space-y-3">
                {Object.entries(task.formData).map(([k, v]) => (
                  <div key={k} className="py-2 border-b border-gray-100 last:border-b-0">
                    <span className="text-xs font-semibold text-gray-500 uppercase">{k}</span>
                    <div className="mt-1 text-sm font-medium text-gray-900">
                      {typeof v === 'string' && v.startsWith('http') ? (
                        <a href={v} target="_blank" rel="noreferrer" className="text-indigo-600 underline">
                          {v}
                        </a>
                      ) : (
                        String(v)
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>

        <div className="space-y-6">
          <Card title="Thông tin nhiệm vụ">
            <div className="space-y-3 text-sm">
              <div>
                <span className="text-xs text-gray-500">Người nộp hồ sơ:</span>
                <div className="font-medium text-gray-800">{task.requesterName}</div>
              </div>
              <div>
                <span className="text-xs text-gray-500">Phân công cho:</span>
                <div className="font-medium text-gray-800">
                  {task.assigneeName || task.candidateGroupName || 'Chưa nhận việc'}
                </div>
              </div>
              <div>
                <span className="text-xs text-gray-500">Thời hạn xử lý (SLA):</span>
                <div className="font-medium text-gray-800">{formatDateTime(task.dueAt)}</div>
              </div>
              <div>
                <span className="text-xs text-gray-500">Tạo lúc:</span>
                <div className="text-gray-600">{formatDateTime(task.createdAt)}</div>
              </div>
            </div>
          </Card>
        </div>
      </div>

      {/* Decision Modal */}
      <Modal
        isOpen={decisionModalOpen}
        onClose={() => setDecisionModalOpen(false)}
        title={
          selectedAction === 'APPROVE'
            ? 'Xác nhận chấp thuận'
            : selectedAction === 'REJECT'
            ? 'Xác nhận từ chối hồ sơ'
            : 'Yêu cầu bổ sung thông tin'
        }
        description={
          selectedAction === 'APPROVE'
            ? 'Bạn đang duyệt nội dung của revision này. Hành động sẽ được ghi nhận vào nhật ký kiểm toán.'
            : 'Vui lòng cung cấp lý do cụ thể cho quyết định này.'
        }
      >
        <div className="space-y-4">
          <Input
            label={selectedAction === 'APPROVE' ? 'Ghi chú phê duyệt (Không bắt buộc)' : 'Lý do bắt buộc'}
            required={selectedAction !== 'APPROVE'}
            placeholder="Nhập lý do..."
            value={reason}
            onChange={(e) => setReason(e.target.value)}
          />

          <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <Button variant="outline" onClick={() => setDecisionModalOpen(false)}>
              Quay lại
            </Button>
            <Button
              variant={selectedAction === 'REJECT' ? 'danger' : 'primary'}
              disabled={selectedAction !== 'APPROVE' && !reason.trim()}
              isLoading={decisionMutation.isPending}
              onClick={() => decisionMutation.mutate()}
            >
              Xác nhận
            </Button>
          </div>
        </div>
      </Modal>

      {/* Floating Back Button at Bottom Right */}
      <div className="fixed bottom-6 right-6 z-40">
        <button
          type="button"
          onClick={handleBack}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-zinc-900 text-zinc-50 hover:bg-zinc-800 text-xs font-semibold rounded-full shadow-elevation border border-zinc-800 transition-all duration-150 active:scale-95 group select-none cursor-pointer"
          title="Quay về trang trước"
        >
          <ArrowLeft className="w-4 h-4 text-zinc-300 group-hover:-translate-x-0.5 transition-transform" strokeWidth={2} />
          <span>Quay lại</span>
        </button>
      </div>
    </div>
  );
};
