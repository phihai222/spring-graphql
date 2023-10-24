package com.phihai91.springgraphql.ultis;

import com.phihai91.springgraphql.configs.AppUserDetails;
import com.phihai91.springgraphql.payloads.AuthModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserHelper {
    public static Boolean isEmail(String userNameOrEmail) {
        return Pattern.compile("^\\S+@\\S+\\.\\S+$")
                .matcher(userNameOrEmail)
                .find();
    }

    public static AuthModel.LoginUserPayload getOtp(AppUserDetails appUser) {
        return AuthModel.LoginUserPayload.builder()
                .userId(appUser.getId())
                .twoMF(appUser.getTwoMF())
                .sentTo(appUser.getTwoMF() ? appUser.getEmail() : null)
                .otp(appUser.getTwoMF() ? "9999999" : null)
                .build();
    }
}
