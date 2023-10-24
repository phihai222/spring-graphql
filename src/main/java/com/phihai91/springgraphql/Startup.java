package com.phihai91.springgraphql;

import com.phihai91.springgraphql.repositories.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Startup implements CommandLineRunner {
    @Autowired
    private IUserRepository IUserRepository;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Override
    public void run(String... args) {
        log.info("APPLICATION START");

        ReactiveListOperations<String, String> reactiveListOps = redisTemplate.opsForList();

        reactiveListOps.leftPushAll("LIST_NAME", "first", "second")
                .log("Pushed")
                .block();

        redisTemplate.opsForValue().set("KEY_NAME", "Value").block();

        //TODO Test Redis Hash
        var hashOperations = redisTemplate.opsForHash();


//        Mono<User> result = IUserRepository.save(User.builder()
//                .email("phihai91@gmail.com")
//                .password("Haideptrai")
//                .build());
//
//        result.block();
    }
}
