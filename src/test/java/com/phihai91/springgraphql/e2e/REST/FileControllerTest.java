package com.phihai91.springgraphql.e2e.REST;

import com.phihai91.springgraphql.entities.Role;
import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.securities.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;
import java.util.List;


@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileControllerTest {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String accessToken;

    @Value("classpath:public/invalid_size.png")
    private Resource inValidResource;

    @Value("classpath:public/valid.jpg")
    private Resource validResource;

    @BeforeEach
    public void beforeEach() {
        User user = User.builder()
                .id("654a1f2f84d82218bd7d5eb4")
                .roles(List.of(Role.ROLE_USER))
                .build();

        accessToken = jwtTokenProvider.createToken(user).accessToken();
    }

    @Test
    public void given_fileLargerThan2Mb_when_invalid_returnError() throws IOException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", inValidResource);

        webClient.post()
                .uri("/api/v1/files/upload-single")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .contentLength(inValidResource.contentLength())
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void given_fileSmallerThan2Mb_when_invalid_returnError() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", validResource);

        webClient.post()
                .uri("/api/v1/files/upload-single")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk();
    }
}