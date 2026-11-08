export type TaskStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED';
export type AssignmentState = 'ASSIGNED' | 'GROUP_OPEN' | 'NEEDS_ASSIGNMENT';
export type TaskAction = 'APPROVE' | 'REJECT' | 'REQUEST_CHANGES';

export interface TaskSummary {
  id: string;
  requestId: string;
  requestCode?: string | null;
  requestTitle: string;
  requesterName: string;
  workflowName: string;
  stepName: string;
  revisionId: string;
  revisionNo: number;
  status: TaskStatus;
  assignmentState: AssignmentState;
  assigneeName?: string | null;
  candidateGroupName?: string | null;
  dueAt?: string | null;
  isOverdue: boolean;
  createdAt: string;
  taskVersion: number;
  requestVersion: number;
}

export interface TaskDetail extends TaskSummary {
  formData: Record<string, any>;
  allowRequestChanges: boolean;
  allowedActions: ('CLAIM' | 'APPROVE' | 'REJECT' | 'REQUEST_CHANGES' | 'VIEW')[];
}
