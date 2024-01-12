package com.phihai91.springgraphql.e2e.REST;

import com.phihai91.springgraphql.entities.File;
import com.phihai91.springgraphql.entities.Role;
import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.repositories.IFileRepository;
import com.phihai91.springgraphql.securities.JwtTokenProvider;
import org.junit.jupiter.api.AfterAll;
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
import org.apache.commons.io.FileUtils;

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

    @Autowired
    private IFileRepository fileRepository;

    private String accessToken;
    private String anotherToken;

    @Value("classpath:images/invalid_size.png")
    private Resource inValidResource;

    @Value("classpath:images/valid.jpg")
    private Resource validResource;

    @Value("${fileSrc}")
    private String fileSrc;

    @BeforeEach
    public void beforeEach() {
        User user = User.builder()
                .id("654a1f2f84d82218bd7d5eb4")
                .roles(List.of(Role.ROLE_USER))
                .build();

        User other = User.builder()
                .id("654a1f2f84d82218bd7d5eb9")
                .roles(List.of(Role.ROLE_USER))
                .build();

        accessToken = jwtTokenProvider.createToken(user).accessToken();
        anotherToken = jwtTokenProvider.createToken(other).accessToken();
    }

    @AfterAll
    public void finish() throws IOException {
        fileRepository.deleteAll().block();
        FileUtils.cleanDirectory(new java.io.File(fileSrc));
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

    @Test
    public void given_fileId_when_fileNotFound_returnError() {
        webClient.get()
                .uri("/api/v1/files/invalid_id")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void given_fileId_when_found_returnFile() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", validResource);

        var res = webClient.post()
                .uri("/api/v1/files/upload-single")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .returnResult(File.class);

        File file = res.getResponseBody().blockFirst();
        assert file != null;

        webClient.get()
                .uri("/api/v1/files/" + file.id())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void given_fileIdForDelete_whenNotFound_returnError() {
        webClient.delete()
                .uri("/api/v1/files/invalid-id")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void given_fileIdForDelete_found_returnSuccess() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", validResource);

        var res = webClient.post()
                .uri("/api/v1/files/upload-single")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .returnResult(File.class);

        File file = res.getResponseBody().blockFirst();
        assert file != null;

        webClient.delete()
                .uri("/api/v1/files/" + file.id())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void given_fileIdForDelete_dontHavePermission_returnError() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", validResource);

        var res = webClient.post()
                .uri("/api/v1/files/upload-single")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .returnResult(File.class);

        File file = res.getResponseBody().blockFirst();
        assert file != null;

        webClient.delete()
                .uri("/api/v1/files/" + file.id())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(anotherToken))
                .exchange()
                .expectStatus().isForbidden();
    }
}
