package com.phihai91.springgraphql.repositories;

import com.phihai91.springgraphql.entities.User;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface IUserRepository extends ReactiveMongoRepository<User, String> {
    Mono<Boolean> existsUserByUsernameEqualsOrEmailEquals(String username, String email);
    Mono<User> findByUsernameEqualsOrEmailEquals(String username, String email);
    Mono<Boolean> existsUserByEmailEquals(String email);
    Mono<Boolean> existsUserByUsernameEquals(String username);

    @Aggregation(pipeline = {
            "{ '$match': {_id: {$in : ?0}}}",
            "{ '$sort' : {_id: -1}}",
    })
    Flux<User> findAllByIdInAndOrderById(Iterable<String> ids);
}
