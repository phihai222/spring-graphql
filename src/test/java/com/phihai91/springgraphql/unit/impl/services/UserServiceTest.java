package com.phihai91.springgraphql.unit.impl.services;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.payloads.UserModel;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IAuthService;
import com.phihai91.springgraphql.services.IRedisService;
import com.phihai91.springgraphql.services.impl.UserService;
import com.phihai91.springgraphql.ultis.UserHelper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IAuthService authService;

    @Mock
    private IRedisService redisService;

    @Test
    public void given_currentUser_when_accountExisted_then_returnUserInfo() {
        User user = User.builder()
                .id(new ObjectId().toString())
                .username("phihai91")
                .email("phihai91@gmail.com")
                .twoMFA(false)
                .registrationDate(LocalDateTime.now())
                .build();

        mockStatic(ReactiveSecurityContextHolder.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(ReactiveSecurityContextHolder.getContext())
                .thenReturn(Mono.just(securityContext));

        mockStatic(UserHelper.class);
        when(UserHelper.getUserDetails(any()))
                .thenReturn(AppUserDetails.builder()
                        .id(user.id())
                        .username(user.username())
                        .twoMFA(user.twoMFA())
                        .build());

        when(this.userRepository.findById(anyString()))
                .thenReturn(Mono.just(user));

        var setup = userService.getCurrentUserInfo();

        Predicate<UserModel.User> predicate = u -> u.id().equals(user.id());

        StepVerifier.create(setup)
                .expectNextMatches(predicate)
                .verifyComplete();
    }
}
