package com.study.shortlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.study.shortlink.mapper.UserMapper;
import com.study.shortlink.pojo.dto.LoginRequestDto;
import com.study.shortlink.pojo.dto.RegisterRequestDto;
import com.study.shortlink.pojo.entity.User;
import com.study.shortlink.pojo.vo.LoginResponseVo;
import com.study.shortlink.pojo.vo.UserInfoVo;
import com.study.shortlink.service.EmailService;
import com.study.shortlink.service.UserService;
import com.study.shortlink.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    @Override
    public LoginResponseVo login(LoginRequestDto loginRequestDto) {

        String userName = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", userName));
        if(user==null)
        {
            throw new RuntimeException("用户不存在");
        }
        if(!user.getPassword().equals(password))
            throw new RuntimeException("密码错误");
        LoginResponseVo loginResponseVo = new LoginResponseVo();
        loginResponseVo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername(), null));
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setId(user.getId());
        userInfoVo.setUsername(user.getUsername());
        userInfoVo.setEmail(user.getEmail());
        loginResponseVo.setUserInfo(userInfoVo);
        return loginResponseVo;
    }

    @Override
    public void register(RegisterRequestDto registerRequestDto) {
        String userName = registerRequestDto.getUsername();
        String password = registerRequestDto.getPassword();
        String email = registerRequestDto.getEmail();
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", userName));
        if (user != null) {
            throw new RuntimeException("用户名已存在");
        }
        //向邮箱发送验证码
        String code = mailService.sendMail(email);
        if (!code.equals(registerRequestDto.getCode())) {
            throw new RuntimeException("验证码错误");
        }
    }
}
