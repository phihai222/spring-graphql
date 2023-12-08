package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.Friend;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFriendRepository extends ReactiveMongoRepository<Friend, String> {
}
