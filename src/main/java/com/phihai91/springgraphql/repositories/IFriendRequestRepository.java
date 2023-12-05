package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.FriendRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface IFriendRequestRepository extends ReactiveMongoRepository<FriendRequest, String> {
    Mono<FriendRequest> findFirstByFromUserEqualsAndToUserEquals(String from, String to);
}
