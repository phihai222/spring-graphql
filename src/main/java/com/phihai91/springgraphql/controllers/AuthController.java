package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.AuthModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class AuthController {
    @MutationMapping
    AuthModel.RegistrationUserPayload registrationUser(@Argument AuthModel.RegistrationUserInput input) {
        log.info(input.usernameOrEmail());
        log.info(input.password());
        return AuthModel.RegistrationUserPayload.builder()
                .UUID("asdasdasd")
                .build();
    }

    @SchemaMapping(typeName = "RegistrationUserPayload", field = "credentials")
    AuthModel.VerifyOtpPayload credentials() {
        return AuthModel.VerifyOtpPayload.builder()
                .accessToken("test token")
                .type("Bearer")
                .build();
    }
}
