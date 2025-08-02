package com.study.shortlink.service.impl;

import com.study.shortlink.pojo.dto.LoginRequestDto;
import com.study.shortlink.pojo.dto.RegisterRequestDto;
import com.study.shortlink.pojo.vo.LoginResoponseVo;
import com.study.shortlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public LoginResoponseVo login(LoginRequestDto loginRequestDto) {
        return null;
    }

    @Override
    public void register(RegisterRequestDto registerRequestDto) {

    }
}
