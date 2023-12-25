package com.phihai91.springgraphql.integration.controllers.REST;

import com.phihai91.springgraphql.controllers.REST.RootController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.function.Predicate;

@SpringBootTest
@ActiveProfiles("integration-test")
public class RootControllerTest {

    @Autowired
    private RootController rootController;

    @Test
    public void testPing() {
        var setup = rootController.ping();

        Predicate<Long> pingPredicate = Objects::nonNull;

        StepVerifier.create(setup)
                .expectNextMatches(pingPredicate)
                .verifyComplete();
    }
}
