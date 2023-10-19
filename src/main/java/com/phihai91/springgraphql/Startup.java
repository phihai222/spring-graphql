package com.phihai91.springgraphql;

import com.phihai91.springgraphql.repositories.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Startup implements CommandLineRunner {

    final IUserRepository IUserRepository;

    public Startup(IUserRepository IUserRepository) {
        this.IUserRepository = IUserRepository;
    }

    @Override
    public void run(String... args) {
        log.info("APPLICATION START");
//        Mono<User> result = IUserRepository.save(User.builder()
//                .email("phihai91@gmail.com")
//                .password("Haideptrai")
//                .build());
//
//        result.block();
    }
}
