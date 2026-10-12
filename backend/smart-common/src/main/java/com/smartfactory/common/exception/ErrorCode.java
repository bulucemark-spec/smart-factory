package com.smartfactory.common.exception;

import lombok.Getter;

/**
 * 通用错误码（业务错误码由各服务在 2000-6999 段自定义）
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "success"),
    UNAUTHORIZED(1001, "未认证或登录已过期"),
    FORBIDDEN(1003, "没有访问权限"),
    BAD_REQUEST(1004, "请求参数错误"),
    NOT_FOUND(1005, "资源不存在"),
    CONFLICT(1009, "状态冲突或非法操作"),
    INTERNAL_ERROR(1500, "系统繁忙，请稍后再试");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
