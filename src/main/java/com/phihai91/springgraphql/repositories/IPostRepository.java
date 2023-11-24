package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IPostRepository extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findAllByUserIdEquals(String userId);

    @Query("{userId: ?0}")
    Flux<Post> findAllByUserId(String userId, Pageable pageable);
}
