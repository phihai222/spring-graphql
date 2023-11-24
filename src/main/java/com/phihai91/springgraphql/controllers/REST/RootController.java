package com.phihai91.springgraphql.controllers.REST;

import com.phihai91.springgraphql.payloads.PostModel;
import com.phihai91.springgraphql.services.IPostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@RestController
@SecurityRequirement(name = "bearerAuth")
public class RootController {
    @Autowired
    private IPostService postService;

    @GetMapping("/api/v1/ping")
    public Mono<Long> ping() {
        return Mono.just(new Date().getTime());
    }

    @GetMapping("/api/v1/test-reactive-flux")
    public Flux<PostModel.CreatePostPayload> getDelayResponse() {
        // Accept: text/event-stream
        return postService.getMyPosts()
                .delayElements(Duration.ofSeconds(1));
    }

    @GetMapping("/api/v1/test-get-post-current-user")
    public ResponseEntity<Flux<PostModel.CreatePostPayload>> getCurrentUserPost(
            @RequestParam("pageNumber") Integer pageNumber,
            @RequestParam("pageSize") Integer pageSize) {
        pageNumber = pageNumber == null ? 1 : pageNumber;
        pageSize = pageSize == null ? 10 : pageSize;

        var result = postService.getMyPosts(PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdDate")));

        return ResponseEntity.ok().body(result);
    }

}
