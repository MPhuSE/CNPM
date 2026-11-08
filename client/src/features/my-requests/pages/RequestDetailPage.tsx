import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { requestApi } from '../api/requestApi';
import { DynamicFormRenderer } from '../components/DynamicFormRenderer';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { Badge } from '@/shared/ui/Badge';
import { Alert } from '@/shared/ui/Alert';
import { Modal } from '@/shared/ui/Modal';
import { Input } from '@/shared/ui/Input';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { formatDateTime, getRequestStatusMeta } from '@/shared/utils/formatters';

export const RequestDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [formData, setFormData] = useState<Record<string, any>>({});
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [cancelModalOpen, setCancelModalOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const [actionError, setActionError] = useState<string | null>(null);

  const { data: request, isLoading } = useQuery({
    queryKey: ['request-detail', id],
    queryFn: () => requestApi.getRequestDetail(id!),
    enabled: !!id,
  });

  const { data: revisions } = useQuery({
    queryKey: ['request-revisions', id],
    queryFn: () => requestApi.getRevisions(id!),
    enabled: !!id && request?.status !== 'DRAFT',
  });

  useEffect(() => {
    if (request) {
      setFormData(request.workingCopyData || request.formData || {});
    }
  }, [request]);

  const isEditable = request?.allowedActions?.includes('EDIT');

  // Mutations
  const updateDraftMutation = useMutation({
    mutationFn: () =>
      requestApi.updateDraft(id!, {
        formData,
        requestVersion: request!.requestVersion,
        editVersion: request!.editVersion || 0,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['request-detail', id] });
    },
    onError: (err: any) => setActionError(err?.message || 'Lỗi khi lưu bản nháp'),
  });

  const submitMutation = useMutation({
    mutationFn: () => {
      const idempotencyKey = crypto.randomUUID();
      return requestApi.submitRequest(
        id!,
        {
          requestVersion: request!.requestVersion,
          editVersion: request!.editVersion || 0,
        },
        idempotencyKey
      );
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['request-detail', id] });
      queryClient.invalidateQueries({ queryKey: ['my-requests'] });
    },
    onError: (err: any) => setActionError(err?.message || 'Lỗi khi gửi hồ sơ'),
  });

  const resubmitMutation = useMutation({
    mutationFn: () => {
      const idempotencyKey = crypto.randomUUID();
      return requestApi.resubmitRequest(
        id!,
        {
          requestVersion: request!.requestVersion,
          editVersion: request!.editVersion || 0,
          baseRevisionId: request!.currentRevisionId || '1',
        },
        idempotencyKey
      );
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['request-detail', id] });
      queryClient.invalidateQueries({ queryKey: ['my-requests'] });
    },
    onError: (err: any) => setActionError(err?.message || 'Lỗi khi gửi lại hồ sơ'),
  });

  const cancelMutation = useMutation({
    mutationFn: () => {
      const idempotencyKey = crypto.randomUUID();
      return requestApi.cancelRequest(
        id!,
        {
          requestVersion: request!.requestVersion,
          reason: cancelReason,
        },
        idempotencyKey
      );
    },
    onSuccess: () => {
      setCancelModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['request-detail', id] });
      queryClient.invalidateQueries({ queryKey: ['my-requests'] });
    },
    onError: (err: any) => setActionError(err?.message || 'Lỗi khi hủy hồ sơ'),
  });

  if (isLoading || !request) {
    return <LoadingSpinner label="Đang tải chi tiết hồ sơ..." />;
  }

  const meta = getRequestStatusMeta(request.status);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">{request.title}</h1>
            <Badge color={meta.color}>{meta.label}</Badge>
          </div>
          <p className="text-xs text-gray-500 mt-1 font-mono">
            Mã: {request.requestCode || 'CHƯA_CẤP'} · Quy trình: {request.workflowName} · Người tạo: {request.requesterName}
          </p>
        </div>

        <div className="flex items-center gap-3">
          {request.allowedActions?.includes('CANCEL') && (
            <Button variant="danger" size="sm" onClick={() => setCancelModalOpen(true)}>
              Hủy hồ sơ
            </Button>
          )}

          {request.status === 'DRAFT' && (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={() => updateDraftMutation.mutate()}
                isLoading={updateDraftMutation.isPending}
              >
                Lưu nháp
              </Button>
              <Button
                size="sm"
                onClick={() => submitMutation.mutate()}
                isLoading={submitMutation.isPending}
              >
                Gửi phê duyệt
              </Button>
            </>
          )}

          {request.status === 'RETURNED' && (
            <Button
              size="sm"
              onClick={() => resubmitMutation.mutate()}
              isLoading={resubmitMutation.isPending}
            >
              Gửi lại hồ sơ
            </Button>
          )}
        </div>
      </div>

      {actionError && (
        <Alert variant="error" className="mb-4">
          {actionError}
        </Alert>
      )}

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card title="Dữ liệu biểu mẫu">
            <DynamicFormRenderer
              schema={request.formSchema}
              values={formData}
              onChange={(k, v) => setFormData((prev) => ({ ...prev, [k]: v }))}
              readOnly={!isEditable}
              errors={formErrors}
            />
          </Card>
        </div>

        <div className="space-y-6">
          <Card title="Thông tin quy trình">
            <div className="space-y-3 text-sm">
              <div>
                <span className="text-xs text-gray-500">Trạng thái:</span>
                <div className="font-semibold text-gray-900">{meta.label}</div>
              </div>
              <div>
                <span className="text-xs text-gray-500">Gửi lần đầu:</span>
                <div className="text-gray-800">{formatDateTime(request.firstSubmittedAt)}</div>
              </div>
              <div>
                <span className="text-xs text-gray-500">Cập nhật lần cuối:</span>
                <div className="text-gray-800">{formatDateTime(request.lastSubmittedAt)}</div>
              </div>
              {request.cancelReason && (
                <div>
                  <span className="text-xs text-rose-500 font-semibold">Lý do hủy:</span>
                  <div className="text-rose-700 bg-rose-50 p-2 rounded text-xs mt-1">
                    {request.cancelReason}
                  </div>
                </div>
              )}
            </div>
          </Card>

          {/* Revisions list */}
          {revisions && revisions.length > 0 && (
            <Card title="Lịch sử các lần gửi (Revisions)">
              <div className="space-y-3 text-xs">
                {revisions.map((rev) => (
                  <div key={rev.id} className="p-3 bg-gray-50 rounded-lg border border-gray-100">
                    <div className="flex items-center justify-between font-semibold text-gray-800">
                      <span>Revision #{rev.revisionNo}</span>
                      <span className="text-gray-400 font-normal">{formatDateTime(rev.submittedAt)}</span>
                    </div>
                    <div className="mt-1 text-gray-600">
                      Người gửi: {rev.submittedByName}
                    </div>
                    {rev.restartRequired && (
                      <span className="inline-block mt-1 text-[10px] text-amber-700 bg-amber-100 px-1.5 py-0.5 rounded">
                        Duyệt lại từ đầu
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </Card>
          )}
        </div>
      </div>

      {/* Cancel Modal */}
      <Modal
        isOpen={cancelModalOpen}
        onClose={() => setCancelModalOpen(false)}
        title="Xác nhận hủy hồ sơ"
        description="Sau khi hủy, hồ sơ sẽ chuyển sang trạng thái CANCELLED và không thể mở lại."
      >
        <div className="space-y-4">
          <Input
            label="Lý do hủy hồ sơ"
            required
            placeholder="Nhập lý do chi tiết..."
            value={cancelReason}
            onChange={(e) => setCancelReason(e.target.value)}
          />
          <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <Button variant="outline" onClick={() => setCancelModalOpen(false)}>
              Quay lại
            </Button>
            <Button
              variant="danger"
              disabled={!cancelReason.trim()}
              isLoading={cancelMutation.isPending}
              onClick={() => cancelMutation.mutate()}
            >
              Xác nhận hủy
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
