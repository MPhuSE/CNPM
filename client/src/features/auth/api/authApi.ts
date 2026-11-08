import { apiClient, tokenStorage } from '@/shared/api/client';
import { ENDPOINTS } from '@/shared/api/endpoints';
import { CurrentUser, LoginResponse } from '@/shared/types/auth';

export const authApi = {
  login: async (credentials: { username: string; password: string }): Promise<LoginResponse> => {
    const data = await apiClient<LoginResponse>(ENDPOINTS.LOGIN, {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
    tokenStorage.setToken(data.accessToken);
    return data;
  },

  getCurrentUser: async (): Promise<CurrentUser> => {
    return apiClient<CurrentUser>(ENDPOINTS.ME);
  },

  logout: async (): Promise<void> => {
    try {
      await apiClient(ENDPOINTS.LOGOUT, { method: 'POST' });
    } finally {
      tokenStorage.setToken(null);
    }
  },
};
