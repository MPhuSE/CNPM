import React from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { MainLayout } from '../layout/MainLayout';
import { LoginPage } from '@/features/auth/pages/LoginPage';
import { MyRequestsPage } from '@/features/my-requests/pages/MyRequestsPage';
import { CreateRequestPage } from '@/features/my-requests/pages/CreateRequestPage';
import { RequestDetailPage } from '@/features/my-requests/pages/RequestDetailPage';
import { TaskInboxPage } from '@/features/task-inbox/pages/TaskInboxPage';
import { TaskDetailPage } from '@/features/task-inbox/pages/TaskDetailPage';
import { CoordinationPage } from '@/features/coordination/pages/CoordinationPage';
import { WorkflowListPage } from '@/features/workflow-builder/pages/WorkflowListPage';
import { WorkflowEditorPage } from '@/features/workflow-builder/pages/WorkflowEditorPage';
import { OrganizationPage } from '@/features/organization/pages/OrganizationPage';
import { AuditLogsPage } from '@/features/audit/pages/AuditLogsPage';
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage';
import { NotificationsPage } from '@/features/notifications/pages/NotificationsPage';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: <MainLayout />,
    children: [
      {
        index: true,
        element: <Navigate to="/my-requests" replace />,
      },
      // Employee zone
      {
        path: 'my-requests',
        element: <MyRequestsPage />,
      },
      {
        path: 'my-requests/new',
        element: <CreateRequestPage />,
      },
      {
        path: 'my-requests/:id',
        element: <RequestDetailPage />,
      },
      // Approver zone
      {
        path: 'inbox',
        element: <TaskInboxPage />,
      },
      {
        path: 'inbox/:id',
        element: <TaskDetailPage />,
      },
      // Admin zone
      {
        path: 'dashboard',
        element: <DashboardPage />,
      },
      {
        path: 'coordination',
        element: <CoordinationPage />,
      },
      {
        path: 'workflows',
        element: <WorkflowListPage />,
      },
      {
        path: 'workflows/:id',
        element: <WorkflowEditorPage />,
      },
      {
        path: 'organization',
        element: <OrganizationPage />,
      },
      {
        path: 'audit',
        element: <AuditLogsPage />,
      },
      // Common
      {
        path: 'notifications',
        element: <NotificationsPage />,
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
]);
