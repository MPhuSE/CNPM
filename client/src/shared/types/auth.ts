export type RoleCode = 'ROLE_ADMIN' | 'ROLE_EMPLOYEE' | 'ROLE_APPROVER';

export interface CurrentUser {
  id: string;
  username: string;
  email: string;
  fullName: string;
  departmentId?: string | null;
  departmentName?: string | null;
  roles: RoleCode[];
  approvalGroupIds: string[];
}

export interface AuthSession {
  accessToken: string;
  user: CurrentUser;
}

export interface LoginResponse {
  accessToken: string;
  user: CurrentUser;
}
