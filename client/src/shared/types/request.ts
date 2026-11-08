import { FormSchema } from './workflow';

export type RequestStatus = 'DRAFT' | 'PENDING' | 'RETURNED' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface RequestSummary {
  id: string;
  requestCode?: string | null;
  workflowName: string;
  workflowCode: string;
  workflowVersionNo: number;
  title: string;
  status: RequestStatus;
  requesterName: string;
  firstSubmittedAt?: string | null;
  lastSubmittedAt?: string | null;
  completedAt?: string | null;
  flags: string[];
}

export interface RequestRevisionSummary {
  id: string;
  revisionNo: number;
  submittedAt: string;
  submittedByName: string;
  changedFields: string[];
  restartRequired: boolean;
}

export interface RequestDetail {
  id: string;
  requestCode?: string | null;
  workflowVersionId: string;
  workflowName: string;
  formSchema: FormSchema;
  requesterId: string;
  requesterName: string;
  title: string;
  status: RequestStatus;
  currentRevisionId?: string | null;
  currentRevisionNo?: number;
  formData: Record<string, any>;
  workingCopyData?: Record<string, any> | null;
  firstSubmittedAt?: string | null;
  lastSubmittedAt?: string | null;
  cancelReason?: string | null;
  requestVersion: number;
  editVersion?: number;
  flags: string[];
  allowedActions: ('EDIT' | 'SUBMIT' | 'RESUBMIT' | 'CANCEL' | 'DELETE' | 'VIEW')[];
}
