package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.Comment;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ICommentRepository extends ReactiveMongoRepository<Comment, String> {
    @Aggregation(pipeline = {
            "{ '$match': { 'postId' : ?0, '_id': {$lt: new ObjectId(?1)}}}",
            "{ '$sort' : { 'createdDate' : -1 } }",
            "{ '$limit': ?2 }"
    })
    Flux<Comment> findAllByPostIdBefore(String postId, String beforeCursor, Integer limit);
    @Aggregation(pipeline = {
            "{ '$match': { 'postId' : ?0 }}",
            "{ '$sort' : { 'createdDate' : -1 } }",
            "{ '$limit': ?1 }"
    })
    Flux<Comment> findAllByPostIdStart(String postId, Integer limit);

}
