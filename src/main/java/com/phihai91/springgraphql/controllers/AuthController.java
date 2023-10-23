package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.services.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Date;

@Controller
@Slf4j
public class AuthController {

    @Autowired
    private IUserService userService;

    @MutationMapping
    Mono<AuthModel.RegistrationUserPayload> registrationUser(@Argument AuthModel.RegistrationUserInput input) {
        return userService.registrationUser(input);
    }
    @SchemaMapping(typeName = "RegistrationUserPayload", field = "credentials")
    Mono<AuthModel.VerifyOtpPayload> credentials(AuthModel.RegistrationUserPayload registrationUserPayload) {
        return userService.getToken(registrationUserPayload.id());
    }

    @MutationMapping
    Mono<AuthModel.LoginUserPayload> loginUser(@Argument AuthModel.LoginUserInput input) {
        return userService.login(input);
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
