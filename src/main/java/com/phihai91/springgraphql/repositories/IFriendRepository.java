package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.Friend;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IFriendRepository extends ReactiveMongoRepository<Friend, String> {
    @Aggregation(pipeline = {
            "{ '$match': { '_id' : new ObjectId(?0)}}",
            "{ '$sort' : { 'addedDate' : -1 } }",
            "{ '$limit': ?2 }"
    })
    Flux<Friend> findAllByUserIdStart(String userId, int first);

    //TODO Aggression Mongodb
    @Aggregation(pipeline = {
            "{ '$match': { '_id' : new ObjectId(?0)}}",
            "{ '$sort' : { 'addedDate' : -1 } }",
            "{ '$limit': ?2 }"
    })
    Flux<Friend> findAllByUserIdBefore(String userId, String cursor, int first);
}
