package com.phihai91.springgraphql.ultis;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserHelper {
    public static Boolean isEmail(String userNameOrEmail) {
        return Pattern.compile("^\\S+@\\S+\\.\\S+$")
                .matcher(userNameOrEmail)
                .find();
    }

    public static String getOtp(String userId) {
        System.out.println(userId);
        //TODO Create TOTP
        return "999999";
    }
}
