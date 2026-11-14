import { RoleCode } from '@/shared/types/auth';

/**
 * Ba role quyền hệ thống chuẩn theo SRS 2.0 và CSDL FlowOps:
 * - ROLE_ADMIN: Quản trị tài khoản, phòng ban, nhóm phê duyệt, cấu hình và điều phối quy trình
 * - ROLE_EMPLOYEE: Nhân viên tạo nháp, gửi hồ sơ, bổ sung, hủy hồ sơ
 * - ROLE_APPROVER: Người phê duyệt nhận việc cá nhân/nhóm và ra quyết định
 * 
 * Lưu ý nghiệp vụ: Tài chính, Kế toán, IT là NHÓM PHÊ DUYỆT (approval_groups),
 * KHÔNG PHẢI là system roles. Trưởng bộ phận là quan hệ chức danh tổ chức.
 */
export type SystemRole = RoleCode;

export interface RoleOption {
  code: SystemRole;
  name: string;
  description: string;
  badgeColor: 'rose' | 'indigo' | 'blue';
}

export const SYSTEM_ROLE_OPTIONS: RoleOption[] = [
  {
    code: 'ROLE_ADMIN',
    name: 'Quản trị viên (Admin)',
    description: 'Toàn quyền quản trị tài khoản, tổ chức, cấu hình quy trình và điều phối sự cố.',
    badgeColor: 'rose',
  },
  {
    code: 'ROLE_APPROVER',
    name: 'Người phê duyệt (Approver)',
    description: 'Xem hộp thư duyệt, nhận task nhóm/cá nhân và thực hiện quyết định phê duyệt.',
    badgeColor: 'indigo',
  },
  {
    code: 'ROLE_EMPLOYEE',
    name: 'Nhân viên (Employee)',
    description: 'Tạo mới, chỉnh sửa nháp, gửi hồ sơ và theo dõi tiến trình xử lý.',
    badgeColor: 'blue',
  },
];

// ==========================================
// USER TYPES & SCHEMAS
// ==========================================

export interface UserItem {
  id: string;
  username: string;
  email: string;
  fullName: string;
  departmentId?: string | null;
  departmentName?: string | null;
  managerId?: string | null;
  managerName?: string | null;
  isActive: boolean;
  roles: SystemRole[];
  approvalGroups?: { id: string; name: string }[];
  lockVersion?: number;
  createdAt?: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  fullName: string;
  password?: string;
  departmentId?: string | null;
  managerId?: string | null;
  roles: SystemRole[];
  isActive?: boolean;
}

export interface UpdateUserRequest {
  fullName: string;
  email: string;
  departmentId?: string | null;
  managerId?: string | null;
  roles: SystemRole[];
  isActive?: boolean;
  lockVersion?: number;
}

export interface UserFilterParams {
  page?: number;
  size?: number;
  search?: string;
  role?: SystemRole | 'ALL';
  isActive?: boolean | 'ALL';
}

// ==========================================
// DEPARTMENT TYPES & SCHEMAS
// ==========================================

export interface DepartmentItem {
  id: string;
  code: string;
  name: string;
  parentId?: string | null;
  parentName?: string | null;
  managerId?: string | null;
  managerName?: string | null;
  isActive: boolean;
  lockVersion?: number;
}

export interface CreateDepartmentRequest {
  code: string;
  name: string;
  parentId?: string | null;
  managerId?: string | null;
  isActive?: boolean;
}

export interface UpdateDepartmentRequest {
  code: string;
  name: string;
  parentId?: string | null;
  managerId?: string | null;
  isActive?: boolean;
  lockVersion?: number;
}

// ==========================================
// APPROVAL GROUP TYPES & SCHEMAS
// ==========================================

export interface ApprovalGroupMemberItem {
  id: string;
  username: string;
  fullName: string;
  email?: string;
  roles?: SystemRole[];
}

export interface ApprovalGroupItem {
  id: string;
  code: string;
  name: string;
  isActive: boolean;
  memberCount?: number;
  members?: ApprovalGroupMemberItem[];
  lockVersion?: number;
}

export interface CreateApprovalGroupRequest {
  code: string;
  name: string;
  isActive?: boolean;
}

export interface UpdateApprovalGroupRequest {
  code?: string;
  name?: string;
  isActive?: boolean;
  memberIds?: string[];
  lockVersion?: number;
}
