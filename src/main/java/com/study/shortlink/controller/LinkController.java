package com.study.shortlink.controller;

import com.study.shortlink.common.Result;
import com.study.shortlink.pojo.dto.CreateLinkRequest;
import com.study.shortlink.pojo.vo.ShortLinkResponse;
import com.study.shortlink.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LinkController {
    private final LinkService linkService;

    @PostMapping("/api/v1/links")
    public ResponseEntity<Object> createShortLink(@RequestBody CreateLinkRequest request) {
        String shortUrl = linkService.createShortLink(request.getLongUrl());
        Map<String, String> result = new HashMap<>();
        result.put("shortUrl", shortUrl);
        return ResponseEntity.ok(Result.success(result));
    }
}
