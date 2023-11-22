package com.phihai91.springgraphql.ultis;

import com.phihai91.springgraphql.securities.AppUserDetails;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserHelper {
    public static Boolean isEmail(String userNameOrEmail) {
        return Pattern.compile("^\\S+@\\S+\\.\\S+$")
                .matcher(userNameOrEmail)
                .find();
    }

    public static AppUserDetails getUserDetails(SecurityContext securityContext) {
        return (AppUserDetails) securityContext.getAuthentication().getPrincipal();
    }
}
