package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.payloads.AuthModel;
import reactor.core.publisher.Mono;

public interface IAuthService {
    Mono<AuthModel.RegistrationUserPayload> registrationUser(AuthModel.RegistrationUserInput input);
    Mono<AuthModel.VerifyOtpPayload> getToken(String userId);
    Mono<AuthModel.LoginUserPayload> login(AuthModel.LoginUserInput input);
    String getOtp(AppUserDetails appUser);
    Mono<AuthModel.VerifyOtpPayload> verifyOtp(AuthModel.VerifyOtpInput input);
}
