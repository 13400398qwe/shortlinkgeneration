package com.study.shortlink.service.impl;

import com.study.shortlink.service.EmailService;

public class EmailServiceImpl implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String text) {
        System.out.println("发送邮件：" + to + "，主题：" + subject + "，内容：" + text);
        System.out.println("邮件发送成功！");
    }
}
