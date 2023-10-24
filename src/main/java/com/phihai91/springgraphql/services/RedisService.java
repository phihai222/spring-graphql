package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.payloads.AuthModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RedisService implements IRedisService {
    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;
    @Override
    public Mono<AuthModel.LoginUserPayload> saveOtp(AuthModel.LoginUserPayload loginUserPayload) {
        //TODO Save parallel hash redis
        if(!loginUserPayload.twoMF())
            return Mono.just(loginUserPayload);

        return redisTemplate.opsForHash().put(loginUserPayload.userId(), "email", loginUserPayload.sentTo())
                .map(aBoolean -> loginUserPayload);
    }
}
