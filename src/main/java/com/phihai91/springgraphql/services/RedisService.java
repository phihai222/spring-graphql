package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.ultis.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class RedisService implements IRedisService {
    @Value("${otpExpirationSeconds}")
    private int otpExpirationSeconds;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;
    @Override
    public Mono<AuthModel.LoginUserPayload> saveOtp(AuthModel.LoginUserPayload loginUserPayload) {
        if(!loginUserPayload.twoMF())
            return Mono.just(loginUserPayload);

        Map<String, String> otpMap = new HashMap<>();
        otpMap.put(Constants.REDIS_KEY_EMAIL, loginUserPayload.sentTo());
        otpMap.put(Constants.REDIS_KEY_OTP, loginUserPayload.otp());

        return redisTemplate.opsForHash().putAll(loginUserPayload.userId(), otpMap)
                .flatMap(r -> redisTemplate.expire(loginUserPayload.userId(), Duration.ofSeconds(otpExpirationSeconds)))
                .map(r -> loginUserPayload);
    }
}
