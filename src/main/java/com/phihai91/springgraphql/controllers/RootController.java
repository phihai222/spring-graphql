package com.phihai91.springgraphql.controllers;

import com.phihai91.springgraphql.entities.Post;
import com.phihai91.springgraphql.repositories.IPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@RestController
public class RootController {
    @Autowired
    private IPostRepository postRepository;

    @GetMapping("/api/v1/ping")
    public Mono<Long> ping() {
        return Mono.just(new Date().getTime());
    }

    @GetMapping("/api/v1/test-reactive-flux")
    public Flux<Post> getDelayResponse() {
        // Accept: text/event-stream
        return postRepository.findAll()
                .delayElements(Duration.ofSeconds(1));
    }
}
