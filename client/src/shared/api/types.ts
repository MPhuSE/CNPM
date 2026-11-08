export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
}

export interface ApiErrorDetail {
  field?: string;
  reason?: string;
}

export interface ApiErrorResponse {
  code: string;
  message: string;
  details?: ApiErrorDetail[];
  traceId?: string;
}
