package com.phihai91.springgraphql.services;

import reactor.core.publisher.Mono;

public interface IRedisService {
    Mono<Boolean> saveOtp(String userId, String sentTo, String otp);

    Mono<Object> getOtp(String userId);

    Mono<Boolean> removeOTP(String userId);
}
