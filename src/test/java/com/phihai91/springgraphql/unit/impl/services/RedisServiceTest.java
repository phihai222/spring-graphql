package com.phihai91.springgraphql.unit.impl.services;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.entities.UserInfo;
import com.phihai91.springgraphql.exceptions.BadRequestException;
import com.phihai91.springgraphql.services.impl.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

    @InjectMocks
    private RedisService redisService;

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveHashOperations<String, Object, Object> hashOperations;

    private final User userData = User.builder()
            .id("507f1f77bcf86cd799439011")
            .username("phihai91")
            .email("phihai91@gmail.com")
            .twoMFA(true)
            .roles(List.of())
            .registrationDate(LocalDateTime.now())
            .userInfo(UserInfo.builder().build())
            .build();

    @Test
    public void given_userId_when_saveSuccess_then_returnData() {
        // when
        when(redisTemplate.opsForHash())
                .thenReturn(hashOperations);

        when(redisTemplate.opsForHash().putAll(anyString(), anyMap()))
                .thenReturn(Mono.just(true));

        when(redisTemplate.expire(anyString(), any()))
                .thenReturn(Mono.just(true));

        // then
        var setup = redisService.saveOtp(userData.id(), userData.email(), "000000");

        Predicate<Boolean> predicate =  p -> p;

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void give_userId_when_notFound_then_returnError() {
        // when
        when(redisTemplate.hasKey(anyString()))
                .thenReturn(Mono.just(false));

        // then
        var setup = redisService.getOtp(userData.id());

        StepVerifier.create(setup)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    public void give_userId_when_found_then_returnObject() {
        // when
        when(redisTemplate.hasKey(anyString()))
                .thenReturn(Mono.just(true));

        when(redisTemplate.opsForHash())
                .thenReturn(hashOperations);

        when(redisTemplate.opsForHash().get(anyString(), any()))
                .thenReturn(Mono.just("123456"));

        // then
        var setup = redisService.getOtp(userData.id());

        Predicate<Object> predicate = o -> o.equals("123456");

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_userId_when_found_returnTrue() {
        // when
        when(redisTemplate.opsForHash())
                .thenReturn(hashOperations);

        when(redisTemplate.opsForHash().delete(anyString()))
                .thenReturn(Mono.just(true));

        // then
        var setup = redisService.removeOTP(userData.id());

        Predicate<Boolean> predicate = p -> p;

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }

    @Test
    public void given_userIdAndOtp_then_otpValid_returnTrue() {
        // when
        when(redisTemplate.hasKey(anyString()))
                .thenReturn(Mono.just(true));

        when(redisTemplate.opsForHash())
                .thenReturn(hashOperations);

        when(redisTemplate.opsForHash().get(anyString(), any()))
                .thenReturn(Mono.just("123456"));
        // then
        var setup = redisService.verifyOtp(userData.id(), "123456");

        Predicate<Boolean> predicate = p -> p;

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }
}
