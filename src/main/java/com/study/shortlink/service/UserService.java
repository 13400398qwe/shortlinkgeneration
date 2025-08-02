package com.study.shortlink.service;

import com.study.shortlink.pojo.dto.LoginRequestDto;
import com.study.shortlink.pojo.dto.RegisterRequestDto;
import com.study.shortlink.pojo.vo.LoginResoponseVo;

public interface UserService {
    LoginResoponseVo login(LoginRequestDto loginRequestDto);
    void register(RegisterRequestDto registerRequestDto);
}
