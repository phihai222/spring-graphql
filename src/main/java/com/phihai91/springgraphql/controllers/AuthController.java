package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.services.IAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class AuthController {

    @Autowired
    private IAuthService authService;

    @MutationMapping
    Mono<AuthModel.RegistrationUserPayload> registrationUser(@Argument AuthModel.RegistrationUserInput input) {
        return authService.registrationUser(input);
    }

    @SchemaMapping(typeName = "RegistrationUserPayload", field = "credentials")
    Mono<AuthModel.VerifyOtpPayload> credentials(AuthModel.RegistrationUserPayload registrationUserPayload) {
        return authService.getToken(registrationUserPayload.id());
    }


    @MutationMapping
    Mono<AuthModel.LoginUserPayload> loginUser(@Argument AuthModel.LoginUserInput input) {
        return authService.login(input);
    }

    @SchemaMapping(typeName = "LoginUserPayload", field = "credentials")
    Mono<AuthModel.VerifyOtpPayload> credentials(AuthModel.LoginUserPayload loginUserPayload) {
        if (loginUserPayload.twoMFA()) return null;
        return authService.getToken(loginUserPayload.userId());
    }

    @MutationMapping
    Mono<AuthModel.VerifyOtpPayload> verifyOtp(@Argument AuthModel.VerifyOtpInput input) {
        return authService.verifyOtp(input);
    }
}
