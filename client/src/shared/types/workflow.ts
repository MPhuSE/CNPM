export type VersionStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type AssigneeType = 'USER' | 'GROUP' | 'REQUESTER_MANAGER' | 'DEPARTMENT_MANAGER';
export type TargetType = 'STEP' | 'END_APPROVED';

export interface FormFieldSchema {
  key: string;
  label: string;
  type: 'text' | 'number' | 'vnd' | 'date' | 'select' | 'url';
  required: boolean;
  affectsApproval: boolean;
  maxLength?: number;
  min?: number;
  max?: number;
  options?: { label: string; value: string }[];
}

export interface FormSchema {
  fields: FormFieldSchema[];
}

export interface TransitionRuleCondition {
  field?: string;
  op?: 'EQ' | 'NE' | 'GT' | 'GTE' | 'LT' | 'LTE' | 'IN';
  value?: string | number | string[];
  all?: TransitionRuleCondition[];
  any?: TransitionRuleCondition[];
}

export interface WorkflowTransition {
  id?: string;
  fromStepKey: string;
  targetType: TargetType;
  toStepKey?: string | null;
  conditionExpression: TransitionRuleCondition;
  isDefault: boolean;
  priority: number;
}

export interface WorkflowStep {
  id?: string;
  stepKey: string;
  stepName: string;
  displayOrder: number;
  assigneeType: AssigneeType;
  assigneeUserId?: string | null;
  assigneeGroupId?: string | null;
  slaHours?: number | null;
  allowRequestChanges: boolean;
}

export interface WorkflowVersionDetail {
  id: string;
  workflowDefinitionId: string;
  versionNo: number;
  status: VersionStatus;
  formSchema: FormSchema;
  startStepKey?: string | null;
  steps: WorkflowStep[];
  transitions: WorkflowTransition[];
  changeNote?: string | null;
  lockVersion: number;
  publishedAt?: string | null;
}

export interface WorkflowSummary {
  id: string;
  code: string;
  name: string;
  description?: string;
  isActive: boolean;
  activeVersionNo?: number | null;
  activeVersionId?: string | null;
}
