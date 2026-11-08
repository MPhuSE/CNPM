import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { Badge } from '@/shared/ui/Badge';
import { Alert } from '@/shared/ui/Alert';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { CheckCircle, AlertTriangle, ArrowLeft } from 'lucide-react';

export const WorkflowEditorPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const { data: workflow, isLoading } = useQuery({
    queryKey: ['workflow-detail', id],
    queryFn: () => apiClient<any>(`${ENDPOINTS.WORKFLOWS}/${id}`),
    enabled: !!id,
  });

  const publishMutation = useMutation({
    mutationFn: (versionId: string) =>
      apiClient(`${ENDPOINTS.WORKFLOW_VERSIONS}/${versionId}/publish`, {
        method: 'POST',
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflow-detail', id] });
    },
    onError: (err: any) => setError(err?.message || 'Không thể xuất bản phiên bản này'),
  });

  if (isLoading || !workflow) {
    return <LoadingSpinner label="Đang tải chi tiết quy trình..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="sm" onClick={() => navigate('/workflows')}>
            <ArrowLeft className="w-4 h-4" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{workflow.name}</h1>
            <p className="text-xs text-gray-500 font-mono">Mã: {workflow.code}</p>
          </div>
        </div>
      </div>

      {error && <Alert variant="error">{error}</Alert>}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card title="Các phiên bản quy trình">
            <div className="space-y-4">
              {(workflow.versions || []).map((ver: any) => (
                <div key={ver.id} className="p-4 rounded-xl border border-gray-200 flex items-center justify-between">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-gray-900">Phiên bản #{ver.versionNo}</span>
                      <Badge color={ver.status === 'PUBLISHED' ? 'emerald' : ver.status === 'DRAFT' ? 'amber' : 'gray'}>
                        {ver.status}
                      </Badge>
                    </div>
                    <p className="text-xs text-gray-500 mt-1">
                      {ver.changeNote || 'Không có ghi chú thay đổi'}
                    </p>
                  </div>

                  <div className="flex items-center gap-2">
                    {ver.status === 'DRAFT' && (
                      <Button
                        size="sm"
                        onClick={() => publishMutation.mutate(ver.id)}
                        isLoading={publishMutation.isPending}
                      >
                        Xuất bản (Publish)
                      </Button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </div>

        <div>
          <Card title="Quy tắc thiết kế bất biến">
            <div className="text-xs text-gray-600 space-y-2 leading-relaxed">
              <p>• Sau khi xuất bản (PUBLISHED), phiên bản quy trình sẽ trở thành bất biến.</p>
              <p>• Các hồ sơ đã tạo sẽ giữ nguyên phiên bản đã pin cho đến khi hoàn tất.</p>
              <p>• Để cập nhật các trường biểu mẫu hoặc bước duyệt, vui lòng tạo phiên bản mới (Clone DRAFT).</p>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};
