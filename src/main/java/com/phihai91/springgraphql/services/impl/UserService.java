package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.payloads.UserModel;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneId;

@Service
public class UserService implements IUserService {
    @Autowired
    private IUserRepository userRepository;

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<UserModel.User> getCurrentUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (AppUserDetails) securityContext.getAuthentication().getPrincipal())
                .flatMap(appUserDetails -> userRepository.findById(appUserDetails.getId()))
                .map(user -> UserModel.User.builder()
                        .id(user.id())
                        .email(user.email())
                        .firstName(user.userInfo() != null ? user.userInfo().firstName() : null)
                        .lastName(user.userInfo() != null ? user.userInfo().lastName() : null)
                        .registrationDate(user.registrationDate()
                                .atZone(ZoneId.systemDefault())
                                .toEpochSecond())
                        .avatarUrl(user.userInfo() != null ? user.userInfo().avatarUrl() : null)
                        .build());
    }
}
