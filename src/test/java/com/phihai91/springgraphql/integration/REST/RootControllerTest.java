package com.phihai91.springgraphql.integration.REST;

import com.phihai91.springgraphql.entities.Role;
import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.payloads.PostModel;
import com.phihai91.springgraphql.securities.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RootControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String accessToken;

    @BeforeEach
    public void beforeEach() {
        User user = User.builder()
                .id("654a1f2f84d82218bd7d5eb4")
                .roles(List.of(Role.ROLE_USER))
                .build();

        accessToken = jwtTokenProvider.createToken(user).accessToken();
    }

    @Test
    @DisplayName("Health check test")
    void testPing() {
        webClient.get().uri("/api/v1/ping")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);
    }

    @Test
    @DisplayName("Get Posts")
    @WithMockUser(roles = "USER")
    void testGetPost() {
        var res = webClient.get().uri("/api/v1/test-reactive-flux")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .exchange()
                .expectStatus().isOk()
                .returnResult(PostModel.CreatePostPayload.class);

        var postFlux = res.getResponseBody();

        StepVerifier.create(postFlux)
                .expectNextCount(0)
                .thenCancel()
                .verify();
    }
}