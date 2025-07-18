package com.study.shortlink.controller;

import com.study.shortlink.service.LinkService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class RedirectController {
    private final LinkService linkService;

    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        String longUrl = linkService.getLongUrl(shortCode);
        if (longUrl != null) {
            // 执行302重定向
            response.sendRedirect(longUrl);
        } else {
            // 如果找不到，返回404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}