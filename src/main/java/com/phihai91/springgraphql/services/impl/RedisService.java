package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.exceptions.BadRequestException;
import com.phihai91.springgraphql.services.IRedisService;
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
    public Mono<Boolean> saveOtp(String userId, String sentTo, String otp) {
        Map<String, String> otpMap = new HashMap<>();
        otpMap.put(Constants.REDIS_KEY_EMAIL, sentTo);
        otpMap.put(Constants.REDIS_KEY_OTP, otp);

        return redisTemplate.opsForHash().putAll(
                        Constants.REDIS_OTP_PREFIX + userId, otpMap)
                .flatMap(r -> redisTemplate.expire(
                        Constants.REDIS_OTP_PREFIX + userId,
                        Duration.ofSeconds(otpExpirationSeconds)));
    }

    @Override
    public Mono<Object> getOtp(String userId) {
        return redisTemplate.hasKey(Constants.REDIS_OTP_PREFIX + userId)
                .flatMap(aBoolean -> aBoolean ?
                        redisTemplate.opsForHash().get(Constants.REDIS_OTP_PREFIX + userId, Constants.REDIS_KEY_OTP)
                        : Mono.error(new BadRequestException("Invalid OTP")));
    }

    @Override
    public Mono<Boolean> removeOTP(String userId) {
        return redisTemplate.opsForHash().delete(Constants.REDIS_OTP_PREFIX + userId);
    }

    @Override
    public Mono<Boolean> verifyOtp(String userId, String otp) {
        return getOtp(userId)
                .map(o -> o.equals(otp));
    }
}
