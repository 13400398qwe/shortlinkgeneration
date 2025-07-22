package com.study.shortlink.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor // 使用Lombok自动生成构造函数
public class RateLimitInterceptor implements HandlerInterceptor {

    // 注入Spring Boot自动配置好的StringRedisTemplate
    private final StringRedisTemplate redisTemplate;

    // --- 限流配置 ---
    private static final int MAX_REQUESTS = 5; // 时间窗口内最大请求数
    private static final long TIME_WINDOW_SECONDS = 60; // 时间窗口：60秒

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取客户端IP地址作为唯一标识
        String ipAddress = getClientIp(request);
        if (ipAddress == null) {
            // 如果无法获取IP，为安全起见直接拒绝
            sendErrorResponse(response, "无法识别的请求来源");
            return false;
        }

        // 2. 构建存储在Redis中的Key
        String redisKey = "rate_limit:" + ipAddress;

        // 3. 使用Redis的INCR命令，原子性地增加计数
        // 如果key不存在，INCR会先创建key并初始化为0，再执行+1，所以第一次请求后值为1
        Long currentRequests = redisTemplate.opsForValue().increment(redisKey);

        // 4. 如果是窗口内的第一次请求，则为这个Key设置过期时间
        if (currentRequests != null && currentRequests == 1) {
            redisTemplate.expire(redisKey, TIME_WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        // 5. 判断是否超过了限流阈值
        if (currentRequests != null && currentRequests > MAX_REQUESTS) {
            // 超过阈值，拒绝请求
            sendErrorResponse(response, "请求过于频繁，请稍后再试");
            return false; // 返回false，请求将被拦截，不会到达Controller
        }

        // 未超过阈值，放行请求
        return true; // 返回true，请求继续向下执行
    }
    /**
     * 获取客户端IP地址的辅助方法
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 发送错误响应的辅助方法
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 设置HTTP状态码 429
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}