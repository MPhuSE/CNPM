import { ApiError, apiClient, tokenStorage } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { CurrentUser, LoginResponse } from '@/shared/types/auth';

export const authApi = {
  login: async (credentials: { username: string; password: string }): Promise<LoginResponse> => {
    const logoutEpoch = tokenStorage.getLogoutEpoch();
    const data = await apiClient<LoginResponse>(ENDPOINTS.LOGIN, {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
    if (tokenStorage.getLogoutEpoch() !== logoutEpoch) throw new Error('Login was superseded by logout');
    tokenStorage.setToken(data.accessToken);
    return data;
  },

  getCurrentUser: async (): Promise<CurrentUser> => {
    return apiClient<CurrentUser>(ENDPOINTS.ME);
  },

  logout: async (): Promise<void> => {
    tokenStorage.beginLogout();
    try {
      try {
        await apiClient(ENDPOINTS.LOGOUT, { method: 'POST' });
      } catch (error) {
        if (!(error instanceof ApiError) || error.status !== 401) throw error;
        // Access JWT may have expired while the server session is still valid.
        // Refresh solely to revoke this sid; never restore a token to app memory.
        const response = await fetch(ENDPOINTS.REFRESH, {
          method: 'POST',
          credentials: 'include',
          headers: { 'X-FlowOps-CSRF': '1' },
        });
        if (!response.ok) throw error;
        const { accessToken } = await response.json() as { accessToken: string };
        await apiClient(ENDPOINTS.LOGOUT, {
          method: 'POST',
          headers: { Authorization: `Bearer ${accessToken}` },
        });
      }
    } finally {
      tokenStorage.setToken(null);
    }
  },

  changePassword: async (currentPassword: string, newPassword: string): Promise<void> => {
    await apiClient(ENDPOINTS.PASSWORD, {
      method: 'POST',
      body: JSON.stringify({ currentPassword, newPassword }),
    });
    tokenStorage.setToken(null);
  },
};
