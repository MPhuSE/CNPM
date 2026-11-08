import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { formatDateTime } from '@/shared/utils/formatters';
import { Bell, Check } from 'lucide-react';

export const NotificationsPage: React.FC = () => {
  const queryClient = useQueryClient();

  const { data: notifications, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => apiClient<any>(ENDPOINTS.NOTIFICATIONS),
  });

  const markReadMutation = useMutation({
    mutationFn: (id: string) =>
      apiClient(`${ENDPOINTS.NOTIFICATIONS}/${id}/read`, { method: 'POST' }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Trung tâm thông báo</h1>
        <p className="text-sm text-gray-500 mt-1">
          Thông báo in-app về trạng thái hồ sơ, nhiệm vụ mới và cảnh báo quá hạn
        </p>
      </div>

      <Card>
        {isLoading ? (
          <LoadingSpinner label="Đang tải thông báo..." />
        ) : !notifications || notifications.items?.length === 0 ? (
          <div className="text-center py-12 text-gray-500 flex flex-col items-center gap-2">
            <Bell className="w-8 h-8 text-gray-300" />
            <span>Bạn không có thông báo nào mới.</span>
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {notifications.items.map((item: any) => (
              <div
                key={item.id}
                className={`py-4 flex items-start justify-between gap-4 transition-colors ${
                  !item.readAt ? 'bg-indigo-50/40 -mx-6 px-6' : ''
                }`}
              >
                <div>
                  <h4 className="text-sm font-semibold text-gray-900">{item.title}</h4>
                  <p className="text-xs text-gray-600 mt-0.5">{item.message}</p>
                  <span className="text-[11px] text-gray-400 mt-2 block">
                    {formatDateTime(item.createdAt)}
                  </span>
                </div>

                {!item.readAt && (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="shrink-0 text-xs text-indigo-600 hover:text-indigo-800"
                    onClick={() => markReadMutation.mutate(item.id)}
                    isLoading={markReadMutation.isPending}
                  >
                    <Check className="w-4 h-4 mr-1" />
                    Đã đọc
                  </Button>
                )}
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
};
