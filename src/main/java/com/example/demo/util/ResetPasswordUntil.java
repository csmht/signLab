package com.example.demo.util;

import com.example.demo.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResetPasswordUntil {
    @Value("slz.resetPassword:syjx@")
    private String RESET_PASSWORD;

    public String getResetPassword(String username) {
       return RESET_PASSWORD + ((username.length() >= 4) ?
               username.substring(username.length() - 4) :
               username);
    }

}
