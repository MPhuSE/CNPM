import React, { createContext, useContext, useState, useEffect } from 'react';
import { CurrentUser, RoleCode } from '@/shared/types/auth';
import { authApi } from '@/features/auth/api/authApi';
import { tokenStorage } from '@/shared/api/client';
import { queryClient } from './QueryClientProvider';

interface AuthContextValue {
  user: CurrentUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: { username: string; password: string }) => Promise<void>;
  logout: () => Promise<void>;
  changePassword: (currentPassword: string, newPassword: string) => Promise<void>;
  hasRole: (role: RoleCode) => boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check initial session
    const logoutEpoch = tokenStorage.getLogoutEpoch();
    authApi
      .getCurrentUser()
      .then((u) => { if (tokenStorage.getLogoutEpoch() === logoutEpoch) setUser(u); })
      .catch(() => { if (tokenStorage.getLogoutEpoch() === logoutEpoch) setUser(null); })
      .finally(() => setIsLoading(false));
  }, []);

  const login = async (credentials: { username: string; password: string }) => {
    const res = await authApi.login(credentials);
    setUser(res.user);
    queryClient.clear(); // Reset query cache on new login
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } finally {
      tokenStorage.setToken(null);
      setUser(null);
      queryClient.clear();
    }
  };

  const changePassword = async (currentPassword: string, newPassword: string) => {
    await authApi.changePassword(currentPassword, newPassword);
    setUser(null);
    queryClient.clear();
  };

  const hasRole = (role: RoleCode) => {
    return !!user?.roles?.includes(role);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        changePassword,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
