package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.securities.JwtTokenProvider;
import com.phihai91.springgraphql.services.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Date;

@Controller
@Slf4j
public class AuthController {

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @MutationMapping
    Mono<AuthModel.RegistrationUserPayload> registrationUser(@Argument AuthModel.RegistrationUserInput input) {
        return userService.registrationUser(input);
    }
    @SchemaMapping(typeName = "RegistrationUserPayload", field = "credentials")
    Mono<AuthModel.VerifyOtpPayload> credentials() {
        AuthModel.RegistrationUserInput registrationUserPayload = AuthModel.RegistrationUserInput
                .builder()
                .usernameOrEmail("phihai91")
                .password("abc")
                .build();

        Mono<AuthModel.RegistrationUserInput> authRequest = Mono.just(registrationUserPayload);

        return authRequest
                .flatMap(login -> this.authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(
                                login.usernameOrEmail(), login.password()))
                        .map(jwtTokenProvider::createToken))
                .map(jwt -> {
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
                    return AuthModel.VerifyOtpPayload.builder()
                            .accessToken(jwt)
                            .expiredDate(2323232l)
                            .signedDate(23232323l)
                            .build();
                });
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
