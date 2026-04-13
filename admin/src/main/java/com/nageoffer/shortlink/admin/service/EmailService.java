package com.nageoffer.shortlink.admin.service;

import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;

public interface EmailService {

    String sendEmail(UserRegisterReqDTO request);

    void sendReviseMail(String mail);
}
