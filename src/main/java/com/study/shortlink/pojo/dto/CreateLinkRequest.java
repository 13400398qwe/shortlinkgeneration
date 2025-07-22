package com.study.shortlink.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLinkRequest {
    @NotNull
    private String longUrl;
    private String customCode; // 可选的自定义短码
}
