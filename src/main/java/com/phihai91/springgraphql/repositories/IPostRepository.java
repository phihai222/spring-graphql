package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IPostRepository extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findAllByUserIdEquals(String userId);
}
