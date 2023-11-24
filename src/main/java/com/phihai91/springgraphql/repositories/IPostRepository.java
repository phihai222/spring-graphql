package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.Post;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IPostRepository extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findAllByUserIdEquals(String userId);

    @Query("{userId: ?0}")
    Flux<Post> findAllByUserId(String userId, Pageable pageable);
    @Aggregation(pipeline = {
            "{ '$match': { 'userId' : ?0 } }",
            "{ '$sort' : { 'createdDate' : 1 } }",
            "{ '$skip' : ?1 }",
            "{ '$limit': ?2 }"
    })
    Flux<Post> findAllByUserId(String userId, Integer skip, Integer limit);
}
