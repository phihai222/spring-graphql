package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.FriendRequest;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface IFriendRequestRepository extends ReactiveMongoRepository<FriendRequest, String> {
    Mono<FriendRequest> findFirstByFromUserEqualsAndToUserEquals(String from, String to);

    @Aggregation(pipeline = {
            "{ '$match': { 'toUser' : ?0, 'isIgnore': false, '_id': {$lt: new ObjectId(?1)}}}",
            "{ '$sort' : { 'createdDate' : -1 } }",
            "{ '$limit': ?2 }"
    })
    Flux<FriendRequest> findAllByUserIdBefore(String userId, String beforeCursor, Integer limit);

    @Aggregation(pipeline = {
            "{ '$match': { 'toUser' : ?0, 'isIgnore': false }}",
            "{ '$sort' : { 'createdDate' : -1 } }",
            "{ '$limit': ?1 }"
    })
    Flux<FriendRequest> findAllByUserIdStart(String userId, Integer limit);
}
