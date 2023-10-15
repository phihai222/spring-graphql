package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.AuthModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.Date;

@Controller
@Slf4j
public class AuthController {
    @MutationMapping
    AuthModel.RegistrationUserPayload registrationUser(@Argument AuthModel.RegistrationUserInput input) {
        return AuthModel.RegistrationUserPayload.builder()
                .UUID(input.usernameOrEmail())
                .build();
    }

    @SchemaMapping(typeName = "RegistrationUserPayload", field = "credentials")
    AuthModel.VerifyOtpPayload credentials() {
        return AuthModel.VerifyOtpPayload.builder()
                .accessToken("test token")
                .type("Bearer")
                .build();
    }

    @MutationMapping
    AuthModel.LoginUserPayload loginUser(@Argument AuthModel.LoginUserInput input) {
        return AuthModel.LoginUserPayload.builder()
                .sentTo(input.userOrEmail())
                .otp("9282833")
                .build();
    }

    @MutationMapping
    AuthModel.VerifyOtpPayload verifyOtp(@Argument AuthModel.VerifyOtpInput input) {
        return AuthModel.VerifyOtpPayload.builder()
                .accessToken(input.userOrEmail())
                .expiredDate(new Date().getTime() + 1000)
                .signedDate(new Date().getTime())
                .type("Bearer")
                .build();
    }
}
