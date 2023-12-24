package com.phihai91.springgraphql.integration.repositories;

import com.phihai91.springgraphql.entities.Role;
import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.repositories.IUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Predicate;

@ExtendWith(SpringExtension.class)
@DataMongoTest
class IUserRepositoryTest {

    @Autowired
    private IUserRepository userRepository;

    @Test
    public void test_existsUserByUsernameEqualsOrEmailEquals() {
        User user = User.builder()
                .username("phihai91")
                .email("phihai91@gmail.com")
                .roles(List.of(Role.ROLE_USER))
                .build();

        Publisher<Boolean> setup = this.userRepository.deleteAll()
                .thenMany(this.userRepository.save(user))
                .thenMany(this.userRepository.existsUserByUsernameEqualsOrEmailEquals(user.username(), user.username()));

        Predicate<Boolean> existedPredicate = existed -> true;

        StepVerifier.create(setup)
                .expectNextMatches(existedPredicate)
                .verifyComplete();
    }
}