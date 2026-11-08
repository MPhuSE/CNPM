import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { TaskSummary, TaskDetail, TaskAction } from '@/shared/types/task';
import { PageResponse } from '@/shared/api/types';

export const taskApi = {
  getTasks: async (params?: { scope?: 'mine' | 'group'; page?: number; size?: number }): Promise<PageResponse<TaskSummary>> => {
    const query = new URLSearchParams();
    if (params?.scope) query.set('scope', params.scope);
    if (params?.page !== undefined) query.set('page', params.page.toString());
    if (params?.size !== undefined) query.set('size', params.size.toString());
    return apiClient(`${ENDPOINTS.TASKS}?${query.toString()}`);
  },

  getTaskDetail: async (id: string): Promise<TaskDetail> => {
    return apiClient(`${ENDPOINTS.TASKS}/${id}`);
  },

  claimTask: async (
    id: string,
    payload: { taskVersion: number; requestVersion: number; revisionId: string },
    idempotencyKey: string
  ): Promise<TaskDetail> => {
    return apiClient(`${ENDPOINTS.TASKS}/${id}/claim`, {
      method: 'POST',
      body: JSON.stringify(payload),
      idempotencyKey,
    });
  },

  decideTask: async (
    id: string,
    payload: {
      action: TaskAction;
      reason?: string | null;
      taskVersion: number;
      requestVersion: number;
      revisionId: string;
    },
    idempotencyKey: string
  ): Promise<any> => {
    return apiClient(`${ENDPOINTS.TASKS}/${id}/decisions`, {
      method: 'POST',
      body: JSON.stringify(payload),
      idempotencyKey,
    });
  },
};
