package com.study.shortlink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 启用Spring Security的CORS支持
                .cors(Customizer.withDefaults())
                // ... 其他配置，如csrf禁用等
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 2. 明确放行所有OPTIONS请求
                        .requestMatchers("/**").permitAll()
                        // ... 其他路径的权限配置
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}