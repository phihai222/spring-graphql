package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.Friend;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IFriendRepository extends ReactiveMongoRepository<Friend, String> {
    @Aggregation(pipeline = {
            "{ '$match': { '_id' : new ObjectId(?0)}}", // Get friend data by userId
            "{ '$project': {friend: { $slice: [\"$friends\", ?1] }}}", // limit nest object by slice, rename output field
            "{ '$unwind': {path: \"$friend\"}}", // flat nest friend data.
            "{ '$sort': {'friend.addedDate': -1}}", // sort output array
    })
    Flux<Friend> findAllByUserIdStart(String userId, int first);

    //TODO complete this query
    @Aggregation(pipeline = {
            "{ '$match': { '_id' : new ObjectId(?0)}}",
            "{ '$project': {friends: { $slice: [\"$friends\", ?1] }}}",
    })
    Flux<Friend> findAllByUserIdBefore(String userId, String cursor, int first);
}
