package com.study.shortlink.pojo.vo;

import lombok.Data;

@Data
public class LoginResponseVo {
    private String token;
    private UserInfoVo userInfo;
}
