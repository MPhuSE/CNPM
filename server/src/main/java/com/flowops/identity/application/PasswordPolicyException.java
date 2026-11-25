package com.flowops.identity.application;

public class PasswordPolicyException extends RuntimeException {
    public PasswordPolicyException() {
        super("Mật khẩu mới phải có ít nhất 15 ký tự Unicode và tối đa 72 byte UTF-8");
    }
}
