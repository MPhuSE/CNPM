import React, { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { requestApi } from '../api/requestApi';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { Button } from '@/shared/ui/Button';
import { Alert } from '@/shared/ui/Alert';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';

export const CreateRequestPage: React.FC = () => {
  const navigate = useNavigate();
  const [selectedWorkflowVersionId, setSelectedWorkflowVersionId] = useState('');
  const [title, setTitle] = useState('');
  const [error, setError] = useState<string | null>(null);

  const { data: workflows, isLoading } = useQuery({
    queryKey: ['workflows-available'],
    queryFn: () => apiClient<any[]>(ENDPOINTS.WORKFLOWS_AVAILABLE),
  });

  const createDraftMutation = useMutation({
    mutationFn: requestApi.createDraft,
    onSuccess: (res) => {
      navigate(`/my-requests/${res.id}`);
    },
    onError: (err: any) => {
      setError(err?.message || 'Không thể tạo hồ sơ nháp');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedWorkflowVersionId || !title.trim()) {
      setError('Vui lòng chọn loại quy trình và nhập tiêu đề hồ sơ');
      return;
    }
    createDraftMutation.mutate({
      workflowVersionId: selectedWorkflowVersionId,
      title: title.trim(),
    });
  };

  if (isLoading) {
    return <LoadingSpinner label="Đang tải danh sách quy trình khả dụng..." />;
  }

  const workflowOptions = [
    { label: '-- Chọn quy trình --', value: '' },
    ...(workflows || []).map((w: any) => ({
      label: `${w.name} (v${w.activeVersionNo || 1})`,
      value: w.activeVersionId || w.id,
    })),
  ];

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Tạo hồ sơ mới</h1>
        <p className="text-sm text-gray-500 mt-1">
          Khởi tạo bản nháp hồ sơ theo quy trình đã xuất bản
        </p>
      </div>

      <Card>
        {error && (
          <Alert variant="error" className="mb-4">
            {error}
          </Alert>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <Select
            label="Loại quy trình"
            required
            options={workflowOptions}
            value={selectedWorkflowVersionId}
            onChange={(e) => setSelectedWorkflowVersionId(e.target.value)}
          />

          <Input
            label="Tiêu đề hồ sơ"
            required
            placeholder="VD: Mua sắm màn hình Dell 27 inch cho bộ phận Dev"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />

          <div className="pt-4 flex justify-end gap-3 border-t border-gray-100">
            <Button type="button" variant="outline" onClick={() => navigate('/my-requests')}>
              Hủy
            </Button>
            <Button type="submit" isLoading={createDraftMutation.isPending}>
              Tạo bản nháp
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
