package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.Post;
import com.phihai91.springgraphql.entities.Visibility;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface IPostRepository extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findAllByUserIdEquals(String userId);

    @Query("{userId: ?0}")
    Flux<Post> findAllByUserId(String userId, Pageable pageable);
    @Aggregation(pipeline = {
            "{ '$match': { 'userId' : ?0, '_id': {$lt: new ObjectId(?1)}}}",
            "{ '$sort' : { 'createdDate' : -1 } }",
            "{ '$limit': ?2 }"
    })
    Flux<Post> findAllByUserIdBefore(String userId, String beforeCursor, Integer limit);

    @Aggregation(pipeline = {
            "{ '$match': { 'userId' : ?0 }}",
            "{ '$sort' : { 'createdDate' : -1 } }",
            "{ '$limit': ?1 }"
    })
    Flux<Post> findAllByUserIdStart(String userId, Integer limit);

    @Aggregation(pipeline = {
            "{ '$match': { 'userId' : ?0, 'visibility' : {$in: ?1} }}",
            "{ '$sort' : { 'createdDate' : -1 } }",
            "{ '$limit': ?2 }"
    })
    Flux<Post> findAllByUserIdStartWithVisibility(String userId, List<Visibility> friendOnly, int first);

    @Aggregation(pipeline = {
            "{ '$match': { 'userId' : ?0, 'visibility' : {$in: ?1}, '_id': {$lt: new ObjectId(?2)}}}",
            "{ '$sort' : { 'createdDate' : -1 } }",
            "{ '$limit': ?3 }"
    })
    Flux<Post> findAllByUserIdBeforeWithVisibility(String userId, List<Visibility> friendOnly, String cursor, int first);

    @Aggregation(pipeline = {
            "{ '$match': { 'userId' : {$in: ?0}, 'visibility' : {$in: ?1} }}",
            "{ '$sort' : { 'createdDate' : -1 } }",
            "{ '$limit': ?2 }"
    })
    Flux<Post> findAllByUserIdsStartWithVisibility(List<String> userId, List<Visibility> visibilities, int first);

    @Aggregation(pipeline = {
            "{ '$match': { 'userId' : {$in: ?0}, 'visibility' : {$in: ?1}, '_id': {$lt: new ObjectId(?3)}}}",
            "{ '$sort' : { 'createdDate' : -1 } }",
            "{ '$limit': ?2 }"
    })
    Flux<Post> findAllByUserIdsBeforeWithVisibility(List<String> userId, List<Visibility> visibilities, int first, String cursor);
}
