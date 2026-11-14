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
import { RoleGuard } from '@/shared/guards/RoleGuard';
import { ForbiddenPage } from '@/shared/pages/ForbiddenPage';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/403',
    element: <ForbiddenPage />,
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
      // Admin zone - Được bảo vệ nghiêm ngặt bằng RoleGuard (Admin only)
      {
        path: 'dashboard',
        element: (
          <RoleGuard allowedRoles={['ROLE_ADMIN']}>
            <DashboardPage />
          </RoleGuard>
        ),
      },
      {
        path: 'coordination',
        element: (
          <RoleGuard allowedRoles={['ROLE_ADMIN']}>
            <CoordinationPage />
          </RoleGuard>
        ),
      },
      {
        path: 'workflows',
        element: (
          <RoleGuard allowedRoles={['ROLE_ADMIN']}>
            <WorkflowListPage />
          </RoleGuard>
        ),
      },
      {
        path: 'workflows/:id',
        element: (
          <RoleGuard allowedRoles={['ROLE_ADMIN']}>
            <WorkflowEditorPage />
          </RoleGuard>
        ),
      },
      {
        path: 'organization',
        element: (
          <RoleGuard allowedRoles={['ROLE_ADMIN']}>
            <OrganizationPage />
          </RoleGuard>
        ),
      },
      {
        path: 'audit',
        element: (
          <RoleGuard allowedRoles={['ROLE_ADMIN']}>
            <AuditLogsPage />
          </RoleGuard>
        ),
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
