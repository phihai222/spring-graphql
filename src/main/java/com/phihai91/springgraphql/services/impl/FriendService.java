package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.Friend;
import com.phihai91.springgraphql.entities.FriendRequest;
import com.phihai91.springgraphql.entities.User;
import com.phihai91.springgraphql.exceptions.BadRequestException;
import com.phihai91.springgraphql.exceptions.NotFoundException;
import com.phihai91.springgraphql.payloads.CommonModel;
import com.phihai91.springgraphql.payloads.FriendModel;
import com.phihai91.springgraphql.repositories.IFriendRepository;
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

import java.util.ArrayList;
import java.util.function.Function;

@Service
@Slf4j
public class FriendService implements IFriendService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IFriendRequestRepository friendRequestRepository;

    @Autowired
    private IFriendRepository friendRepository;

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<CommonModel.CommonPayload> sendRequest(FriendModel.AddFriendInput input) {
        // Get current user data
        Mono<AppUserDetails> currentUser = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        // Check user existed or not
        Mono<User> targetUser = userRepository.findById(input.userId())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")));

        return currentUser
                // Check request to themselves
                .flatMap(appUserDetails -> appUserDetails.getId().equals(input.userId()) ?
                        Mono.error(new BadRequestException("Cannot request to yourself")) : Mono.just(appUserDetails))
                // Check already friend or not
                .flatMap(appUserDetails -> checkIsAlreadyFriend(appUserDetails.getId(), input.userId())
                        .flatMap(aBoolean -> aBoolean ?
                                Mono.error(new BadRequestException("Already Friend")) : Mono.just(appUserDetails)))
                .zipWith(targetUser, (appUserDetails, user) ->
                        // Find request existed or not
                        friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(appUserDetails.getId(), user.id())
                                // Withdraw request by delete
                                .flatMap(this::deleteFriendRequest)
                                // If not existed, create a new request
                                .switchIfEmpty(saveFriendRequestOrAccept(input, appUserDetails)))
                .flatMap(Function.identity()); // Convert Mono<Mono<T>> to Mono<T>
    }

    private Mono<Boolean> checkIsAlreadyFriend(String currentUserId, String targetUserId) {
        return friendRepository.findById(currentUserId)
                .map(friendData -> friendData.friends().contains(targetUserId))
                .switchIfEmpty(Mono.just(false));
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
                .flatMap(fr -> updateFriendData(fr.fromUser(), fr.toUser())
                        .map(friend -> fr))
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

    @Override
    public Mono<Friend> updateFriendData(String userId, String friendId) {
        // Find friend data of current user, if not -> create new data
        var currentFriendData = friendRepository.findById(userId)
                .switchIfEmpty(friendRepository.save(Friend.builder()
                        .id(userId)
                        .friends(new ArrayList<>())
                        .build()));

        // Find friend data of target user, if not -> create new data
        var targetFriendData = friendRepository.findById(friendId)
                .switchIfEmpty(friendRepository.save(Friend.builder()
                        .id(friendId)
                        .friends(new ArrayList<>())
                        .build()));

        return currentFriendData.zipWith(targetFriendData, (current, target) -> {
                    // Add friend data to current User
                    var currentFriendList = current.friends();
                    currentFriendList.add(friendId);

                    //add friend data to target user
                    var targetFriendList = target.friends();
                    targetFriendList.add(userId);

                    return friendRepository.save(target.withFriends(targetFriendList))
                            .map(targetUserFriend -> current.withFriends(currentFriendList));
                })
                .flatMap(Function.identity())
                // Add friend data to current User
                .flatMap(currentUserFriendData -> friendRepository.save(currentUserFriendData));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<CommonModel.CommonPayload> ignoreFriendRequest(String userId) {
        var rejectSuccessRes = CommonModel.CommonPayload.builder()
                .status(CommonModel.CommonStatus.SUCCESS)
                .message("Ignore Success")
                .build();

        // Get current user data
        Mono<AppUserDetails> currentUser = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        return currentUser
                .flatMap(u -> friendRequestRepository.findFirstByFromUserEqualsAndToUserEquals(userId, u.getId())
                        .switchIfEmpty(Mono.error(new NotFoundException("Request not found"))))
                .flatMap(friendRequest -> friendRequestRepository.save(friendRequest.withIsIgnore(true)))
                .then(Mono.just(rejectSuccessRes));
    }
}
