package com.smartfactory.common.constant;

/**
 * 鉴权与透传请求头常量
 */
public interface SecurityConstants {

    String AUTH_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";

    String HEADER_USER_ID = "X-User-Id";
    String HEADER_USER_NAME = "X-User-Name";
    String HEADER_ORG_ID = "X-User-Org";
    String HEADER_ROLES = "X-User-Roles";
}
