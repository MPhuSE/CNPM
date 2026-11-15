/**
 * Dữ liệu Seed Giả lập cho Kiểm thử E2E
 */

export interface TestUser {
  id: string;
  username: string;
  fullName: string;
  email: string;
  roles: string[];
  departmentName?: string;
}

export const TEST_USERS: Record<string, TestUser> = {
  admin: {
    id: '1',
    username: 'admin',
    fullName: 'Quản trị viên Hệ thống (BOD)',
    email: 'admin@flowops.local',
    roles: ['ROLE_ADMIN', 'ROLE_EMPLOYEE'],
    departmentName: 'Ban Quản trị & Điều hành',
  },
  employee: {
    id: '2',
    username: 'employee',
    fullName: 'Nguyễn Văn A (Nhân viên Kinh doanh)',
    email: 'employee@flowops.local',
    roles: ['ROLE_EMPLOYEE'],
    departmentName: 'Phòng Kinh doanh',
  },
  approver: {
    id: '3',
    username: 'approver',
    fullName: 'Trần Thị Alice (Quản lý Phê duyệt)',
    email: 'approver@flowops.local',
    roles: ['ROLE_EMPLOYEE', 'ROLE_APPROVER'],
    departmentName: 'Phòng Kinh doanh',
  },
  finance_bob: {
    id: '4',
    username: 'finance_bob',
    fullName: 'Lê Văn Bob (Ban Tài chính)',
    email: 'finance@flowops.local',
    roles: ['ROLE_APPROVER'],
    departmentName: 'Ban Tài chính & Đầu tư',
  },
};

export const MOCK_TASK_SEED = {
  mineTasks: [
    {
      id: '1',
      requestId: '101',
      requestCode: 'REQ-2026-001',
      requestTitle: 'Đề xuất mua màn hình Dell UltraSharp 27 inch',
      requesterName: 'Nguyễn Văn A (Nhân viên Dev)',
      workflowName: 'Quy trình Phê duyệt Mua sắm & Chi phí',
      stepName: 'Trưởng bộ phận phê duyệt',
      revisionId: '1',
      revisionNo: 1,
      status: 'PENDING',
      assignmentState: 'ASSIGNED',
      assigneeName: 'Trần Thị Alice',
      candidateGroupName: null,
      dueAt: new Date(Date.now() + 86400000).toISOString(),
      isOverdue: false,
      createdAt: new Date().toISOString(),
      taskVersion: 1,
      requestVersion: 1,
    },
    {
      id: '2',
      requestId: '102',
      requestCode: 'REQ-2026-002',
      requestTitle: 'Yêu cầu thanh toán chi phí tiếp khách đối tác quý 1',
      requesterName: 'Trần Thị B (Kế toán)',
      workflowName: 'Quy trình Thanh toán & Tạm ứng',
      stepName: 'Kế toán trưởng kiểm soát',
      revisionId: '2',
      revisionNo: 2,
      status: 'PENDING',
      assignmentState: 'NEEDS_ASSIGNMENT',
      assigneeName: null,
      candidateGroupName: null,
      dueAt: new Date(Date.now() + 43200000).toISOString(),
      isOverdue: false,
      createdAt: new Date().toISOString(),
      taskVersion: 2,
      requestVersion: 2,
    },
  ],
  groupTasks: [
    {
      id: '4',
      requestId: '104',
      requestCode: 'REQ-2026-004',
      requestTitle: 'Thẩm định hồ sơ Onboarding nhân viên mới',
      requesterName: 'Phạm Thị D (HR Specialist)',
      workflowName: 'Quy trình Tiếp nhận Nhân sự mới',
      stepName: 'Ban IT cấp phát thiết bị',
      revisionId: '1',
      revisionNo: 1,
      status: 'PENDING',
      assignmentState: 'GROUP_OPEN',
      assigneeName: null,
      candidateGroupName: 'Nhóm Kỹ thuật & Hạ tầng IT',
      dueAt: new Date(Date.now() + 172800000).toISOString(),
      isOverdue: false,
      createdAt: new Date().toISOString(),
      taskVersion: 1,
      requestVersion: 1,
    },
  ],
};

