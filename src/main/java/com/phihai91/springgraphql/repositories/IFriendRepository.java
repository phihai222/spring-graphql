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
            "{ '$project': {friend: '$friends'}}", // Rename array
            "{ '$unwind': {path: '$friend'}}", // flat nest friend data.
            "{ '$sort': {'friend.addedDate': -1}}", // sort output array
            "{ '$limit': ?1}" // limit
    })
    Flux<Friend> findAllByUserIdStart(String userId, int first);

    @Aggregation(pipeline = {
            "{ '$match': { '_id' : new ObjectId(?0)}}", // Get friend data by userId
            "{ '$project': {friend: '$friends'}}", // Rename array
            "{ '$unwind': {path: '$friend'}}", // flat nest friend data.
            "{ '$match': {'friend._id': {$lt: new ObjectId(?1)}}}",
            "{ '$sort': {'friend.addedDate': -1}}", // sort output array
            "{ '$limit': ?2}" // limit
    })
    Flux<Friend> findAllByUserIdBefore(String userId, String cursor, int first);
}
