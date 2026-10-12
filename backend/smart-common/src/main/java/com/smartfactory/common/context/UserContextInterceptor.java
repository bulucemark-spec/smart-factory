package com.smartfactory.common.context;

import com.smartfactory.common.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 从 gateway 透传头解析当前用户，写入 ThreadLocal
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(SecurityConstants.HEADER_USER_ID);
        if (userId != null && !userId.isEmpty()) {
            UserContext context = UserContext.builder()
                    .userId(Long.valueOf(userId))
                    .username(request.getHeader(SecurityConstants.HEADER_USER_NAME))
                    .orgId(parseLong(request.getHeader(SecurityConstants.HEADER_ORG_ID)))
                    .roles(request.getHeader(SecurityConstants.HEADER_ROLES))
                    .build();
            UserContextHolder.set(context);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private Long parseLong(String v) {
        if (v == null || v.isEmpty() || "null".equals(v)) {
            return null;
        }
        return Long.valueOf(v);
    }
}
