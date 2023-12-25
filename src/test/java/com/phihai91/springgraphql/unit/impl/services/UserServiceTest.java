package com.phihai91.springgraphql.unit.impl.services;

import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.services.IAuthService;
import com.phihai91.springgraphql.services.IRedisService;
import com.phihai91.springgraphql.services.impl.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void given_registration_when_accountExisted_then_badRequestError() {
    }

    @Test
    void given_registration_when_valid_then_successful() {
    }
}
