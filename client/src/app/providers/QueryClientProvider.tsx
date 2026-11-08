import React from 'react';
import { QueryClient, QueryClientProvider as TanStackQueryClientProvider } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 30, // 30s
      refetchOnWindowFocus: false,
      retry: (failureCount, error: any) => {
        // Do not retry 401, 403, 404, 409, 422
        if (error?.status && [401, 403, 404, 409, 422].includes(error.status)) {
          return false;
        }
        return failureCount < 2;
      },
    },
  },
});

export const AppQueryProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <TanStackQueryClientProvider client={queryClient}>
      {children}
    </TanStackQueryClientProvider>
  );
};
