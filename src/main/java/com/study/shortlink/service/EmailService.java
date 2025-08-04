package com.study.shortlink.service;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}
