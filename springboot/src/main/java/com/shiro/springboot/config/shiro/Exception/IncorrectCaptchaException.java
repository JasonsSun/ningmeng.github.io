package com.shiro.springboot.config.shiro.Exception;

import org.apache.shiro.authc.AuthenticationException;

// 校验码异常
public class IncorrectCaptchaException extends AuthenticationException {

    private static final long serivalVersionUID = 1L;

    public IncorrectCaptchaException() {
        super();
    }

    public IncorrectCaptchaException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectCaptchaException(String message) {
        super(message);
    }

    public IncorrectCaptchaException(Throwable cause) {
        super(cause);
    }
}
