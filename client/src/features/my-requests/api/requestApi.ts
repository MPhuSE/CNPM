import { apiClient } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { RequestDetail, RequestSummary, RequestRevisionSummary } from '@/shared/types/request';
import { PageResponse } from '@/shared/api/types';

export const requestApi = {
  getMyRequests: async (params?: { page?: number; size?: number; status?: string }): Promise<PageResponse<RequestSummary>> => {
    const query = new URLSearchParams();
    if (params?.page !== undefined) query.set('page', params.page.toString());
    if (params?.size !== undefined) query.set('size', params.size.toString());
    if (params?.status) query.set('status', params.status);
    return apiClient(`${ENDPOINTS.REQUESTS}?${query.toString()}`);
  },

  getRequestDetail: async (id: string): Promise<RequestDetail> => {
    return apiClient(`${ENDPOINTS.REQUESTS}/${id}`);
  },

  createDraft: async (payload: { workflowVersionId: string; title: string }): Promise<RequestDetail> => {
    return apiClient(ENDPOINTS.REQUESTS, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  updateDraft: async (id: string, payload: { title?: string; formData: Record<string, any>; requestVersion: number; editVersion: number }): Promise<RequestDetail> => {
    return apiClient(`${ENDPOINTS.REQUESTS}/${id}/draft`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    });
  },

  submitRequest: async (id: string, payload: { requestVersion: number; editVersion: number }, idempotencyKey: string): Promise<RequestDetail> => {
    return apiClient(`${ENDPOINTS.REQUESTS}/${id}/submit`, {
      method: 'POST',
      body: JSON.stringify(payload),
      idempotencyKey,
    });
  },

  resubmitRequest: async (id: string, payload: { requestVersion: number; editVersion: number; baseRevisionId: string }, idempotencyKey: string): Promise<RequestDetail> => {
    return apiClient(`${ENDPOINTS.REQUESTS}/${id}/resubmit`, {
      method: 'POST',
      body: JSON.stringify(payload),
      idempotencyKey,
    });
  },

  cancelRequest: async (id: string, payload: { requestVersion: number; reason: string }, idempotencyKey: string): Promise<RequestDetail> => {
    return apiClient(`${ENDPOINTS.REQUESTS}/${id}/cancel`, {
      method: 'POST',
      body: JSON.stringify(payload),
      idempotencyKey,
    });
  },

  getRevisions: async (id: string): Promise<RequestRevisionSummary[]> => {
    return apiClient(`${ENDPOINTS.REQUESTS}/${id}/revisions`);
  },
};
