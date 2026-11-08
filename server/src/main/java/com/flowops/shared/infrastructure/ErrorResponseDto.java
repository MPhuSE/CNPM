package com.flowops.shared.infrastructure;

import java.util.List;

public class ErrorResponseDto {
    private String code;
    private String message;
    private List<ErrorDetail> details;
    private String traceId;

    public ErrorResponseDto() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<ErrorDetail> getDetails() { return details; }
    public void setDetails(List<ErrorDetail> details) { this.details = details; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ErrorResponseDto dto = new ErrorResponseDto();
        public Builder code(String code) { dto.setCode(code); return this; }
        public Builder message(String message) { dto.setMessage(message); return this; }
        public Builder details(List<ErrorDetail> details) { dto.setDetails(details); return this; }
        public Builder traceId(String traceId) { dto.setTraceId(traceId); return this; }
        public ErrorResponseDto build() { return dto; }
    }

    public static class ErrorDetail {
        private String field;
        private String reason;

        public ErrorDetail() {}

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private final ErrorDetail detail = new ErrorDetail();
            public Builder field(String field) { detail.setField(field); return this; }
            public Builder reason(String reason) { detail.setReason(reason); return this; }
            public ErrorDetail build() { return detail; }
        }
    }
}
