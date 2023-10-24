package com.phihai91.springgraphql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Startup implements CommandLineRunner {
//    @Autowired
//    private ReactiveStringRedisTemplate redisTemplate;

    @Override
    public void run(String... args) {
        log.info("APPLICATION START");

//        redisTemplate.getConnectionFactory().getReactiveConnection().serverCommands().flushAll().block();
//
//        redisTemplate.opsForList().leftPushAll("LIST_NAME", "first", "second")
//                .log("Pushed")
//                .block();
//
//        redisTemplate.opsForValue().set("KeyTest", "valueTest").block();
//
//        redisTemplate.opsForHash().put("Hash", "userID", "22323232").block();
//        redisTemplate.opsForHash().put("Hash", "username", "phihai").block();
//
//        redisTemplate.opsForHash().get("Hash", "userID").log().block();
    }
}
