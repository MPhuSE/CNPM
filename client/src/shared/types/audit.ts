export interface AuditLogItem {
  id: string;
  eventKey: string;
  actorName?: string | null;
  action: string;
  entityType: string;
  entityId: string;
  details: Record<string, any>;
  ipAddress?: string | null;
  createdAt: string;
}

export interface RequestEventItem {
  id: string;
  eventKey: string;
  eventType: string;
  actorName?: string | null;
  reason?: string | null;
  details: Record<string, any>;
  createdAt: string;
}
