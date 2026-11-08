import { ApiErrorResponse } from './types';
import { ENDPOINTS } from './endpoints';

let inMemoryToken: string | null = null;
let refreshPromise: Promise<string | null> | null = null;

export const tokenStorage = {
  getToken: () => inMemoryToken,
  setToken: (token: string | null) => {
    inMemoryToken = token;
  },
};

export class ApiError extends Error {
  code: string;
  details?: { field?: string; reason?: string }[];
  traceId?: string;
  status: number;

  constructor(status: number, errorData: ApiErrorResponse) {
    super(errorData.message || 'Lỗi xử lý yêu cầu');
    this.name = 'ApiError';
    this.status = status;
    this.code = errorData.code || 'UNKNOWN_ERROR';
    this.details = errorData.details;
    this.traceId = errorData.traceId;
  }
}

interface RequestOptions extends RequestInit {
  idempotencyKey?: string;
}

export async function apiClient<T>(url: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers || {});
  headers.set('Content-Type', 'application/json');

  if (inMemoryToken) {
    headers.set('Authorization', `Bearer ${inMemoryToken}`);
  }

  if (options.idempotencyKey) {
    headers.set('Idempotency-Key', options.idempotencyKey);
  }

  let response = await fetch(url, {
    ...options,
    headers,
    credentials: 'include', // sends refresh cookie
  });

  // Handle 401: attempt token refresh once
  if (response.status === 401 && !url.includes(ENDPOINTS.LOGIN) && !url.includes(ENDPOINTS.REFRESH)) {
    if (!refreshPromise) {
      refreshPromise = fetch(ENDPOINTS.REFRESH, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
      })
        .then(async (res) => {
          if (!res.ok) throw new Error('Refresh failed');
          const data = await res.json();
          tokenStorage.setToken(data.accessToken);
          return data.accessToken as string;
        })
        .catch(() => {
          tokenStorage.setToken(null);
          return null;
        })
        .finally(() => {
          refreshPromise = null;
        });
    }

    const newToken = await refreshPromise;
    if (newToken) {
      headers.set('Authorization', `Bearer ${newToken}`);
      response = await fetch(url, {
        ...options,
        headers,
        credentials: 'include',
      });
    }
  }

  if (!response.ok) {
    let errorData: ApiErrorResponse;
    try {
      errorData = await response.json();
    } catch {
      errorData = {
        code: `HTTP_${response.status}`,
        message: response.statusText || 'Yêu cầu không thành công',
      };
    }
    throw new ApiError(response.status, errorData);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}
