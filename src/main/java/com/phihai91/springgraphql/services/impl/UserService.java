package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.exceptions.BadRequestException;
import com.phihai91.springgraphql.exceptions.ForbiddenException;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.UserModel;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IAuthService;
import com.phihai91.springgraphql.services.IRedisService;
import com.phihai91.springgraphql.services.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.ZoneId;

@Service
@Slf4j
public class UserService implements IUserService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IAuthService authService;

    @Autowired
    private IRedisService redisService;

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

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<UserModel.User> updateUserInfo(UserModel.UpdateUserInput input) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (AppUserDetails) securityContext.getAuthentication().getPrincipal())
                .flatMap(u -> userRepository.findById(u.getId()))// Get user from database
                .flatMap(u -> (u.twoMFA() && input.email() != null) ? // Start validate 2MF is enable or not.
                        Mono.error(new ForbiddenException("2MFA must be deactivate to change email")) : Mono.just(u)) // If 2MF is enable, reject change email
                .flatMap(u -> userRepository.existsUserByEmailEquals(input.email()) // Validate new email existed or not
                        .flatMap(existed -> (existed) ?
                                Mono.error(new BadRequestException("Email existed")) : Mono.just(u)))
                .map(user -> user // Clone object and modify data
                        .withEmail(input.email() != null ? input.email() : user.email())
                        .withUserInfo(user.userInfo()
                                .withAvatarUrl(input.avatarUrl() != null ? input.avatarUrl() : user.userInfo().avatarUrl())
                                .withLastName(input.lastName() != null ? input.lastName() : user.userInfo().lastName())
                                .withFirstName(input.firstName() != null ? input.firstName() : user.userInfo().firstName())
                        ))
                .flatMap(user -> userRepository.save(user)) // Save user data with same ID
                .map(u -> UserModel.User.builder() // Map and return response
                        .id(u.id())
                        .username(u.username())
                        .email(u.email())
                        .avatarUrl(u.userInfo().avatarUrl())
                        .firstName(u.userInfo().firstName())
                        .lastName(u.userInfo().lastName())
                        .build());
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<UserModel.SetTwoMFAPayload> setTwoMFA() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (AppUserDetails) securityContext.getAuthentication().getPrincipal())
                .flatMap(appUserDetails -> userRepository.findById(appUserDetails.getId()))
                //TODO check user set email or not
                .map(User::toAppUserDetails)
                .map(appUserDetails -> UserModel.SetTwoMFAPayload.builder()
                        .userId(appUserDetails.getId())
                        .otp(authService.getOtp(appUserDetails))
                        .sentTo(appUserDetails.getEmail())
                        .build())
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(setTwoMFAPayload -> redisService.saveOtp(
                                setTwoMFAPayload.userId(),
                                setTwoMFAPayload.sentTo(),
                                setTwoMFAPayload.otp())
                        .subscribe());
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<CommonModel.CommonPayload> verifyTwoMFOtp(String otp) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (AppUserDetails) securityContext.getAuthentication().getPrincipal())
                .flatMap(userDetails -> redisService.verifyOtp(userDetails.getId(), otp)
                        .flatMap(aBoolean -> aBoolean ? Mono.just(userDetails) : Mono.error(new BadRequestException("Invalid OTP"))))
                .flatMap(appUserDetails -> userRepository.findById(appUserDetails.getId()) //Get User from db
                        .map(user -> user.withTwoMFA(!user.twoMFA())))
                .publishOn(Schedulers.parallel())
                .flatMap(user -> userRepository.save(user))
                .flatMap(user -> redisService.removeOTP(user.id()))//Update user
                .map(commonPayload -> CommonModel.CommonPayload.builder()
                        .message("Success")
                        .status(CommonModel.CommonStatus.SUCCESS)
                        .build());
    }
}
