import { RoleCode } from './auth';

export interface UserSummary {
  id: string;
  username: string;
  email: string;
  fullName: string;
  departmentId?: string | null;
  departmentName?: string | null;
  managerId?: string | null;
  managerName?: string | null;
  isActive: boolean;
  roles: RoleCode[];
  approvalGroups: { id: string; name: string }[];
  lockVersion: number;
  createdAt: string;
}

export interface Department {
  id: string;
  code: string;
  name: string;
  parentId?: string | null;
  parentName?: string | null;
  managerId?: string | null;
  managerName?: string | null;
  isActive: boolean;
}

export interface ApprovalGroup {
  id: string;
  code: string;
  name: string;
  isActive: boolean;
  memberCount?: number;
  members?: { id: string; username: string; fullName: string }[];
}
