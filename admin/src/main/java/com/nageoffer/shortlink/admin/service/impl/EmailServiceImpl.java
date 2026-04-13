package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    private final StringRedisTemplate stringRedisTemplate;

    public static final String USER_REGISTER_CODE_KEY="user_register_code_%s";

    public static final String USER_REVISE_CODE_KEY="user_revise_code_%s";

    @Override
    public String sendEmail(UserRegisterReqDTO request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2797857024@qq.com");
        //邮箱接收者
        message.setTo(request.getMail());
        //邮箱主题
        String subject = "验证码";
        message.setSubject(subject);
        String code = RandomUtil.randomNumbers(6);
        //邮箱内容
        String text = "欢迎注册短链接平台用户，你的验证码是：" +code+"，请妥善保存，在三分钟之内填写";
        message.setText(text);
        mailSender.send(message);
        stringRedisTemplate.opsForValue().set(String.format(USER_REGISTER_CODE_KEY,request.getMail()),code,3, TimeUnit.MINUTES);
        return code;
    }

    @Override
    public void sendReviseMail(String mail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2797857024@qq.com");
        //邮箱接收者
        message.setTo(mail);
        //邮箱主题
        String subject = "验证码";
        message.setSubject(subject);
        String code = RandomUtil.randomNumbers(6);
        //邮箱内容
        String text = "您正在修改密码，如果不是本人操作请忽略，并且提高防护，你的验证码是：" +code+"，请妥善保存，在三分钟之内填写";
        message.setText(text);
        mailSender.send(message);
        stringRedisTemplate.opsForValue().set(String.format(USER_REVISE_CODE_KEY,mail),code,3, TimeUnit.MINUTES);
    }
}
