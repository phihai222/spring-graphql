package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.payloads.AuthModel;
import reactor.core.publisher.Mono;

public interface IRedisService {
    Mono<AuthModel.LoginUserPayload> saveOtp(AuthModel.LoginUserPayload loginUserPayload);

    Mono<Object> getOtp(AuthModel.VerifyOtpInput input);


    Mono<Boolean> removeOTP(String userId, AuthModel.VerifyOtpPayload verifyOtpPayload);
}
