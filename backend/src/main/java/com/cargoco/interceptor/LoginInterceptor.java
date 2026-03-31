package com.cargoco.interceptor;

import com.cargoco.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty()) {
            sendError(response, 401, "未登录，请先登录");
            return false;
        }

        try {
            if (jwtUtil.isTokenExpired(token)) {
                sendError(response, 401, "登录已过期，请重新登录");
                return false;
            }
            Claims claims = jwtUtil.parseToken(token);
            // 将用户信息放入请求属性中
            request.setAttribute("userId", Long.valueOf(claims.get("userId").toString()));
            request.setAttribute("username", claims.get("username").toString());
            request.setAttribute("role", Integer.valueOf(claims.get("role").toString()));
            return true;
        } catch (Exception e) {
            sendError(response, 401, "Token无效，请重新登录");
            return false;
        }
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(map));
    }
}
