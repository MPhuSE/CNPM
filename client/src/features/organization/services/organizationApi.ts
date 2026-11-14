import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { PageResponse } from '@/shared/api/types';
import {
  UserItem,
  CreateUserRequest,
  UpdateUserRequest,
  UserFilterParams,
  DepartmentItem,
  CreateDepartmentRequest,
  UpdateDepartmentRequest,
  ApprovalGroupItem,
  CreateApprovalGroupRequest,
  UpdateApprovalGroupRequest,
  SYSTEM_ROLE_OPTIONS,
  RoleOption,
} from '../types/organization.types';

export const organizationApi = {
  // ==========================================
  // USERS API
  // ==========================================

  getUsers: async (params: UserFilterParams = {}): Promise<PageResponse<UserItem>> => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.set('page', String(params.page));
    if (params.size !== undefined) query.set('size', String(params.size));
    if (params.search && params.search.trim() !== '') query.set('search', params.search.trim());
    if (params.role && params.role !== 'ALL') query.set('role', params.role);
    if (params.isActive !== undefined && params.isActive !== 'ALL') {
      query.set('isActive', String(params.isActive));
    }

    const queryString = query.toString();
    const url = queryString ? `${ENDPOINTS.USERS}?${queryString}` : ENDPOINTS.USERS;
    return apiClient<PageResponse<UserItem>>(url, { method: 'GET' });
  },

  createUser: async (payload: CreateUserRequest): Promise<UserItem> => {
    return apiClient<UserItem>(ENDPOINTS.USERS, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  updateUser: async (id: string, payload: UpdateUserRequest): Promise<UserItem> => {
    const body: Record<string, any> = {
      fullName: payload.fullName,
      email: payload.email,
      departmentId: payload.departmentId,
      managerId: payload.managerId,
      roles: payload.roles,
    };

    if (payload.isActive !== undefined) {
      body.isActive = payload.isActive;
    }

    // Optimistic locking: chỉ truyền lockVersion nếu đối tượng có
    if (payload.lockVersion !== undefined && payload.lockVersion !== null) {
      body.lockVersion = payload.lockVersion;
    }

    return apiClient<UserItem>(`${ENDPOINTS.USERS}/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(body),
    });
  },

  toggleUserStatus: async (
    id: string,
    isActive: boolean,
    lockVersion?: number
  ): Promise<UserItem> => {
    const body: Record<string, any> = { isActive };
    if (lockVersion !== undefined && lockVersion !== null) {
      body.lockVersion = lockVersion;
    }

    return apiClient<UserItem>(`${ENDPOINTS.USERS}/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(body),
    });
  },

  // ==========================================
  // ROLES API
  // ==========================================

  getRoles: async (): Promise<RoleOption[]> => {
    // 3 system roles cố định theo SRS 2.0 (ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_APPROVER)
    return Promise.resolve(SYSTEM_ROLE_OPTIONS);
  },

  // ==========================================
  // DEPARTMENTS API
  // ==========================================

  getDepartments: async (params: { page?: number; size?: number; isActive?: boolean } = {}): Promise<PageResponse<DepartmentItem>> => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.set('page', String(params.page));
    if (params.size !== undefined) query.set('size', String(params.size));
    if (params.isActive !== undefined) query.set('isActive', String(params.isActive));

    const queryString = query.toString();
    const url = queryString ? `${ENDPOINTS.DEPARTMENTS}?${queryString}` : ENDPOINTS.DEPARTMENTS;
    return apiClient<PageResponse<DepartmentItem>>(url, { method: 'GET' });
  },

  createDepartment: async (payload: CreateDepartmentRequest): Promise<DepartmentItem> => {
    return apiClient<DepartmentItem>(ENDPOINTS.DEPARTMENTS, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  updateDepartment: async (id: string, payload: UpdateDepartmentRequest): Promise<DepartmentItem> => {
    const body: Record<string, any> = {
      code: payload.code,
      name: payload.name,
      parentId: payload.parentId ?? null,
      managerId: payload.managerId ?? null,
    };

    if (payload.isActive !== undefined) {
      body.isActive = payload.isActive;
    }

    // RÀNG BUỘC TUYỆT ĐỐI - KHÔNG GỬI VERSION GIẢ:
    // Chỉ gửi lockVersion khi lấy được từ response GET trước đó.
    // Nếu entity chưa có lock_version ở schema DB hiện hành, không tự sinh version giả.
    if (payload.lockVersion !== undefined && payload.lockVersion !== null) {
      body.lockVersion = payload.lockVersion;
    }

    return apiClient<DepartmentItem>(`${ENDPOINTS.DEPARTMENTS}/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(body),
    });
  },

  // ==========================================
  // APPROVAL GROUPS API
  // ==========================================

  getApprovalGroups: async (params: { page?: number; size?: number } = {}): Promise<PageResponse<ApprovalGroupItem>> => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.set('page', String(params.page));
    if (params.size !== undefined) query.set('size', String(params.size));

    const queryString = query.toString();
    const url = queryString ? `${ENDPOINTS.APPROVAL_GROUPS}?${queryString}` : ENDPOINTS.APPROVAL_GROUPS;
    return apiClient<PageResponse<ApprovalGroupItem>>(url, { method: 'GET' });
  },

  createApprovalGroup: async (payload: CreateApprovalGroupRequest): Promise<ApprovalGroupItem> => {
    return apiClient<ApprovalGroupItem>(ENDPOINTS.APPROVAL_GROUPS, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  updateApprovalGroup: async (id: string, payload: UpdateApprovalGroupRequest): Promise<ApprovalGroupItem> => {
    const body: Record<string, any> = {};
    if (payload.code !== undefined) body.code = payload.code;
    if (payload.name !== undefined) body.name = payload.name;
    if (payload.isActive !== undefined) body.isActive = payload.isActive;
    if (payload.memberIds !== undefined) body.memberIds = payload.memberIds;

    // RÀNG BUỘC TUYỆT ĐỐI - KHÔNG GỬI VERSION GIẢ:
    if (payload.lockVersion !== undefined && payload.lockVersion !== null) {
      body.lockVersion = payload.lockVersion;
    }

    return apiClient<ApprovalGroupItem>(`${ENDPOINTS.APPROVAL_GROUPS}/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(body),
    });
  },

  addGroupMember: async (
    groupId: string,
    userId: string,
    currentMemberIds: string[] = [],
    lockVersion?: number
  ): Promise<ApprovalGroupItem> => {
    const updatedIds = Array.from(new Set([...currentMemberIds, userId]));
    return organizationApi.updateApprovalGroup(groupId, {
      memberIds: updatedIds,
      lockVersion,
    });
  },

  removeGroupMember: async (
    groupId: string,
    userId: string,
    currentMemberIds: string[] = [],
    lockVersion?: number
  ): Promise<ApprovalGroupItem> => {
    const updatedIds = currentMemberIds.filter((id) => id !== userId);
    return organizationApi.updateApprovalGroup(groupId, {
      memberIds: updatedIds,
      lockVersion,
    });
  },
};
