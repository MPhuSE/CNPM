import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { Card } from '@/shared/ui/Card';
import { LoadingSpinner } from '@/shared/ui/LoadingSpinner';
import { DashboardMetrics } from '@/shared/types/dashboard';
import { FileText, Clock, CheckCircle, XCircle, AlertTriangle, ShieldAlert } from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { data: metrics, isLoading } = useQuery<DashboardMetrics>({
    queryKey: ['dashboard-metrics'],
    queryFn: () => apiClient(ENDPOINTS.DASHBOARD),
  });

  if (isLoading || !metrics) {
    return <LoadingSpinner label="Đang tải dữ liệu báo cáo..." />;
  }

  const statCards = [
    {
      title: 'Hồ sơ đã gửi',
      value: metrics.totalSubmittedRequests,
      sub: 'Tính theo first_submitted_at',
      icon: <FileText className="w-6 h-6 text-indigo-600" />,
      bg: 'bg-indigo-50',
    },
    {
      title: 'Hồ sơ đang chờ duyệt',
      value: metrics.pendingRequests,
      sub: 'Đang nằm tại các bước',
      icon: <Clock className="w-6 h-6 text-blue-600" />,
      bg: 'bg-blue-50',
    },
    {
      title: 'Hồ sơ đã phê duyệt',
      value: metrics.approvedRequests,
      sub: 'Hoàn tất luồng duyệt',
      icon: <CheckCircle className="w-6 h-6 text-emerald-600" />,
      bg: 'bg-emerald-50',
    },
    {
      title: 'Hồ sơ bị từ chối',
      value: metrics.rejectedRequests,
      sub: 'Bị reject tại một bước',
      icon: <XCircle className="w-6 h-6 text-rose-600" />,
      bg: 'bg-rose-50',
    },
    {
      title: 'Nhiệm vụ quá hạn (Overdue)',
      value: metrics.overdueTasks,
      sub: 'Vượt mốc SLA',
      icon: <AlertTriangle className="w-6 h-6 text-amber-600" />,
      bg: 'bg-amber-50',
    },
    {
      title: 'Cần phân công (Needs Assignment)',
      value: metrics.needsAssignmentTasks,
      sub: 'Thiếu người duyệt hợp lệ',
      icon: <ShieldAlert className="w-6 h-6 text-rose-600" />,
      bg: 'bg-rose-50',
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard Quản trị</h1>
        <p className="text-sm text-gray-500 mt-1">
          Tổng hợp số liệu vận hành và hiệu năng theo chuẩn SRS 2.0 (UTC+7)
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {statCards.map((card) => (
          <div
            key={card.title}
            className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm flex items-start justify-between"
          >
            <div>
              <p className="text-sm font-medium text-gray-500">{card.title}</p>
              <h3 className="text-3xl font-extrabold text-gray-900 mt-2">{card.value}</h3>
              <p className="text-xs text-gray-400 mt-1">{card.sub}</p>
            </div>
            <div className={`p-3 rounded-xl ${card.bg}`}>{card.icon}</div>
          </div>
        ))}
      </div>

      <Card title="Ghi chú đối soát chỉ tiêu SRS">
        <div className="text-xs text-gray-600 space-y-1 leading-relaxed">
          <p>• Dữ liệu metric loại trừ toàn bộ các bản nháp chưa từng gửi (DRAFT).</p>
          <p>• Hồ sơ vừa quá hạn vừa cần phân công không bị cộng dồn thành 2 hồ sơ khác nhau.</p>
          <p>• Thời gian truy vấn chuyển đổi chính xác giữa giờ người dùng (UTC+7) và lưu trữ CSDL (UTC).</p>
        </div>
      </Card>
    </div>
  );
};
