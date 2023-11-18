package com.phihai91.springgraphql.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Date;

@RestController
public class RootController {
    @GetMapping("/api/v1/ping")
    public Mono<Long> ping() {
        return Mono.just(new Date().getTime());
    }
}
