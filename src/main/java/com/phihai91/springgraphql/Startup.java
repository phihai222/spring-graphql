package com.phihai91.springgraphql;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class Startup implements CommandLineRunner {

    final UserRepository userRepository;

    public Startup(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        log.info("APPLICATION START");
        Mono<User> result = userRepository.save(User.builder()
                .email("phihai91@gmail.com")
                .password("Haideptrai")
                .build());

        result.block();
    }
}
