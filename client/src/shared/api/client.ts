import { ApiErrorResponse } from './types';
import { ENDPOINTS } from './endpoints';

let inMemoryToken: string | null = null;
let refreshPromise: Promise<string | null> | null = null;
let authEpoch = 0;
let logoutEpoch = 0;
let loggedOut = false;

export const tokenStorage = {
  getToken: () => inMemoryToken,
  setToken: (token: string | null) => {
    authEpoch++;
    if (token === null) {
      logoutEpoch++;
      loggedOut = true;
    } else {
      loggedOut = false;
    }
    inMemoryToken = token;
  },
  beginLogout: () => {
    authEpoch++;
    logoutEpoch++;
    loggedOut = true;
  },
  getLogoutEpoch: () => logoutEpoch,
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
  const authCommand = [ENDPOINTS.LOGIN, ENDPOINTS.REFRESH, ENDPOINTS.LOGOUT, ENDPOINTS.PASSWORD].includes(url);
  if (authCommand) headers.set('X-FlowOps-CSRF', '1');

  if (inMemoryToken && !headers.has('Authorization')) {
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
  const method = (options.method || 'GET').toUpperCase();
  if (response.status === 401 && (method === 'GET' || method === 'HEAD') && !authCommand && !loggedOut) {
    if (!refreshPromise) {
      const startingEpoch = authEpoch;
      refreshPromise = fetch(ENDPOINTS.REFRESH, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json', 'X-FlowOps-CSRF': '1' },
      })
        .then(async (res) => {
          if (!res.ok) throw new Error('Refresh failed');
          const data = await res.json();
          if (startingEpoch !== authEpoch || loggedOut) return null;
          tokenStorage.setToken(data.accessToken);
          return data.accessToken as string;
        })
        .catch(() => {
          if (startingEpoch === authEpoch) tokenStorage.setToken(null);
          return null;
        })
        .finally(() => {
          refreshPromise = null;
        });
    }

    const newToken = await refreshPromise;
    if (newToken && !loggedOut) {
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
