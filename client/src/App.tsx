import React from 'react';
import { RouterProvider } from 'react-router-dom';
import { router } from './app/router/routes';
import { AppQueryProvider } from './app/providers/QueryClientProvider';
import { AuthProvider } from './app/providers/AuthProvider';

export const App: React.FC = () => {
  return (
    <AppQueryProvider>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </AppQueryProvider>
  );
};
