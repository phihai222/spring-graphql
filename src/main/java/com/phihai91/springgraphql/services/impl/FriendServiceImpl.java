package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.FriendRequest;
import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.exceptions.BadRequestException;
import com.phihai91.springgraphql.exceptions.NotFoundException;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.FriendModel;
import com.phihai91.springgraphql.repositories.IFriendRequestRepository;
import com.phihai91.springgraphql.repositories.IUserRepository;
import com.phihai91.springgraphql.securities.AppUserDetails;
import com.phihai91.springgraphql.services.IFriendService;
import com.phihai91.springgraphql.ultis.UserHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
@Slf4j
public class FriendServiceImpl implements IFriendService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IFriendRequestRepository friendRequestRepository;

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<CommonModel.CommonPayload> sendRequest(FriendModel.AddFriendInput input) {
        // Get current user data
        Mono<AppUserDetails> currentUser = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        // Check user existed or not
        Mono<User> targetUser = userRepository.findById(input.userId())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")));

        var alreadyFriend = CommonModel.CommonPayload.builder()
                .status(CommonModel.CommonStatus.FAILED)
                .message("Already friend")
                .build();


        return currentUser
                // Check request to themselves
                .flatMap(appUserDetails -> appUserDetails.getId().equals(input.userId()) ?
                        Mono.error(new BadRequestException("Cannot request to yourself")) : Mono.just(appUserDetails))
                .zipWith(targetUser, (appUserDetails, user) ->
                        checkIsAlreadyFriend() ?  Mono.just(alreadyFriend) :
                        // Find request existed or not
                        friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(appUserDetails.getId(), user.id())
                                // Withdraw request by delete
                                .flatMap(this::deleteFriendRequest)
                                // If not existed, create a new request
                                .switchIfEmpty(saveFriendRequestOrAccept(input, appUserDetails)))
                .flatMap(Function.identity()); // Convert Mono<Mono<T>> to Mono<T>
    }

    private Boolean checkIsAlreadyFriend() {
        // TODO check both are friend or not
        return false;
    }

    private Mono<CommonModel.CommonPayload> saveFriendRequestOrAccept(FriendModel.AddFriendInput input, AppUserDetails appUserDetails) {
        var successRes = CommonModel.CommonPayload.builder()
                .status(CommonModel.CommonStatus.SUCCESS)
                .message("Request send")
                .build();

        var acceptedRes = CommonModel.CommonPayload.builder()
                .message("Accepted friend")
                .status(CommonModel.CommonStatus.SUCCESS)
                .build();

        var friendRequestFromDb = friendRequestRepository
                .findFirstByFromUserEqualsAndToUserEquals(input.userId(), appUserDetails.getId());

        return friendRequestFromDb
                .map(fr -> {
                    //TODO Add friend data
                    log.info("Create Friend Data");
                    return fr;
                })
                .flatMap(fr -> friendRequestRepository.deleteById(fr.id())
                        .then(Mono.fromCallable(() -> acceptedRes)))
                .switchIfEmpty(friendRequestRepository.save(FriendRequest.builder() // Create Friend Request
                        .message(input.message())
                        .fromUser(appUserDetails.getId())
                        .toUser(input.userId())
                        .build()).map(friendRequest -> successRes));
    }

    private Mono<CommonModel.CommonPayload> deleteFriendRequest(FriendRequest friendRequest) {
        var withdrewRes = CommonModel.CommonPayload.builder()
                .status(CommonModel.CommonStatus.SUCCESS)
                .message("Withdrew")
                .build();

        return friendRequestRepository.deleteById(friendRequest.id())
                .then(Mono.fromCallable(() -> withdrewRes));
    }
}
