export const API_BASE = '/api/v1';

export const ENDPOINTS = {
  // Auth
  LOGIN: `${API_BASE}/auth/login`,
  LOGOUT: `${API_BASE}/auth/logout`,
  REFRESH: `${API_BASE}/auth/refresh`,
  PASSWORD: `${API_BASE}/auth/password`,
  ME: `${API_BASE}/me`,

  // Organization & Identity
  USERS: `${API_BASE}/users`,
  DEPARTMENTS: `${API_BASE}/departments`,
  APPROVAL_GROUPS: `${API_BASE}/approval-groups`,

  // Workflows
  WORKFLOWS: `${API_BASE}/workflows`,
  WORKFLOWS_AVAILABLE: `${API_BASE}/workflows/available`,
  WORKFLOW_VERSIONS: `${API_BASE}/workflow-versions`,

  // Requests
  REQUESTS: `${API_BASE}/requests`,

  // Tasks
  TASKS: `${API_BASE}/tasks`,

  // Operations & Incidents
  INCIDENTS: `${API_BASE}/incidents`,

  // Notifications
  NOTIFICATIONS: `${API_BASE}/notifications`,

  // Audit
  AUDIT_LOGS: `${API_BASE}/audit-logs`,

  // Dashboard
  DASHBOARD: `${API_BASE}/dashboard`,
};
