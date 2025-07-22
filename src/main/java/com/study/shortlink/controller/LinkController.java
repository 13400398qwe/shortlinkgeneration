package com.study.shortlink.controller;

import com.study.shortlink.common.Result;
import com.study.shortlink.pojo.dto.CreateLinkRequest;
import com.study.shortlink.pojo.vo.ShortLinkResponse;
import com.study.shortlink.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LinkController {
    private final LinkService linkService;

    @PostMapping("/links")
    public ResponseEntity<?> createShortLink(@RequestBody CreateLinkRequest request) {
        try {
            String shortUrl = linkService.createShortLink(request.getLongUrl(), request.getCustomCode());
            // 简单返回一个包含短链接的Map
            return ResponseEntity.ok(java.util.Map.of("shortUrl", shortUrl));
        } catch (IllegalArgumentException e) {
            // 如果自定义短码已被占用或不合法，返回400错误
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    /**
     * 为指定的短链接生成二维码
     * @param shortCode 短码
     * @return PNG格式的二维码图片
     */
    @GetMapping("/{shortCode}/qrcode")
    public ResponseEntity<byte[]> getQrCodeForLink(@PathVariable String shortCode) {
        try {
            byte[] qrCodeImage = linkService.generateQrCode(shortCode);

            // 设置HTTP响应头，告诉浏览器这是一个PNG图片
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            // 如果生成失败（例如短码不存在），可以返回服务器错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
