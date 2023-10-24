package com.phihai91.springgraphql.services;

import com.phihai91.springgraphql.configs.AppUserDetails;
import com.phihai91.springgraphql.entities.Role;
import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.exceptions.ConflictResourceException;
import com.phihai91.springgraphql.payloads.AuthModel;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.JwtTokenProvider;
import com.phihai91.springgraphql.ultis.UserHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class AuthService implements IAuthService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private IRedisService redisService;

    @Override
    public Mono<AuthModel.RegistrationUserPayload> registrationUser(AuthModel.RegistrationUserInput input) {
        Boolean isEmail = UserHelper.isEmail(input.usernameOrEmail());

        User newUser = User.builder()
                .username(isEmail ? null : input.usernameOrEmail())
                .email(isEmail ? input.usernameOrEmail() : null)
                .password(passwordEncoder.encode(input.password()))
                .twoMF(false)
                .roles(List.of(Role.USER))
                .build();

        log.info(newUser.toString());

        return userRepository.existsUserByUsernameEqualsOrEmailEquals(input.usernameOrEmail(), input.usernameOrEmail())
                .flatMap(exists -> (exists) ? Mono.error(new ConflictResourceException("Username or email existed"))
                        : userRepository.save(newUser))
                .map(user -> user.toGetRegistrationUserPayload(user.id()));
    }

    @Override
    public Mono<AuthModel.VerifyOtpPayload> getToken(String userId) {
        return userRepository.findById(userId)
                .map(user -> jwtTokenProvider.createToken(user));
    }

    @Override
    public Mono<AuthModel.LoginUserPayload> login(AuthModel.LoginUserInput input) {
        return Mono.just(input)
                .flatMap(login -> this.authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(
                                login.userOrEmail(), login.password()))
                        .map(authentication -> {
                            AppUserDetails appUser = (AppUserDetails) authentication.getPrincipal();
                            return getOtp(appUser);
                        }))
                .flatMap(loginUserPayload->  redisService.saveOtp(loginUserPayload));
    }

    @Override
    public AuthModel.LoginUserPayload getOtp(AppUserDetails appUser) {
        String otp = "999999";

        return AuthModel.LoginUserPayload.builder()
                .userId(appUser.getId())
                .twoMF(appUser.getTwoMF())
                .sentTo(appUser.getTwoMF() ? appUser.getEmail() : null)
                .otp(appUser.getTwoMF() ? otp : null)
                .build();
    }
}
