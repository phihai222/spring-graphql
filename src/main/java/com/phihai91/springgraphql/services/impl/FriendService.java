package com.phihai91.springgraphql.services.impl;

import com.phihai91.springgraphql.entities.Friend;
import com.phihai91.springgraphql.entities.FriendData;
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
import com.phihai91.springgraphql.ultis.CursorUtils;
import com.phihai91.springgraphql.ultis.UserHelper;
import graphql.relay.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FriendService implements IFriendService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IFriendRequestRepository friendRequestRepository;

    @Autowired
    private IFriendRepository friendRepository;

    @Autowired
    private CursorUtils cursorUtils;

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

    @Override
    public Mono<Boolean> checkIsAlreadyFriend(String currentUserId, String targetUserId) {
        return friendRepository.findById(currentUserId)
                .map(friendData -> friendData.friends().stream()
                        .anyMatch(f -> f.userId().contains(targetUserId))
                )
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
                        .isIgnore(false)
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
                    var currentFriendList = new ArrayList<>(current.friends());
                    currentFriendList.add(FriendData.builder()
                            .id(new ObjectId().toString())
                            .userId(friendId)
                            .addedDate(LocalDateTime.now()).build());

                    //add friend data to target user
                    var targetFriendList = new ArrayList<>(target.friends());
                    targetFriendList.add(FriendData.builder()
                            .id(new ObjectId().toString())
                            .userId(userId)
                            .addedDate(LocalDateTime.now()).build());

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

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<Connection<FriendModel.FriendRequest>> getFriendRequest(Integer first, String cursor) {
        Mono<AppUserDetails> appUserDetailsMono = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        return appUserDetailsMono
                .flatMap(appUserDetails ->
                        getFriendRequest(appUserDetails.getId(), first, cursor)
                                .map(fr -> (Edge<FriendModel.FriendRequest>) new DefaultEdge<>(fr, cursorUtils.from(fr.id())))
                                .collect(Collectors.toUnmodifiableList()))
                .map(edges -> {
                    DefaultPageInfo pageInfo = new DefaultPageInfo(
                            cursorUtils.getFirstCursorFrom(edges),
                            cursorUtils.getLastCursorFrom(edges),
                            cursor != null,
                            edges.size() >= first);

                    return new DefaultConnection<>(edges, pageInfo);
                });
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<Connection<FriendModel.Friend>> getFriendList(Integer first, String cursor) {
        Mono<AppUserDetails> appUserDetailsMono = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        return appUserDetailsMono
                .flatMap(u -> getFriendList(u.getId(), first, cursor)
                        .map(friend -> (Edge<FriendModel.Friend>) new DefaultEdge<>(friend, cursorUtils.from(friend.cursor())))
                        .collect(Collectors.toUnmodifiableList()))
                .map(edges -> {
                    DefaultPageInfo pageInfo = new DefaultPageInfo(
                            cursorUtils.getFirstCursorFrom(edges),
                            cursorUtils.getLastCursorFrom(edges),
                            cursor != null,
                            edges.size() >= first);

                    return new DefaultConnection<>(edges, pageInfo);
                });


    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<CommonModel.CommonPayload> unfriend(String targetUserId) {
        Mono<AppUserDetails> appUserDetailsMono = ReactiveSecurityContextHolder.getContext()
                .map(UserHelper::getUserDetails);

        Mono<User> targetUser = userRepository.findById(targetUserId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not existed")));

        Mono<Friend> currentUserFriendData = appUserDetailsMono.zipWith(targetUser, (appUserDetails, user) ->
                        // Find friend data of current user
                        friendRepository.findById(appUserDetails.getId())
                                // Check target user existed on friend list or not
                                .flatMap(friend -> friend.friends().stream().anyMatch(f -> f.userId().equals(targetUserId))
                                        // Return Friend data if existed or throw error.
                                        ? Mono.just(friend) : Mono.error(new NotFoundException("This user is not your friend before"))))
                // If Friend data is not existed, throw the exception.
                .switchIfEmpty(Mono.error(new NotFoundException("Your friend list is empty")))
                // Convert Mono<Mono<T>> to Mono<T> because use zip-with
                .flatMap(Function.identity());

        Mono<Friend> targetUserFriendData = friendRepository.findById(targetUserId);

        return currentUserFriendData.zipWith(targetUserFriendData, Tuples::of)
                .flatMap(tuples -> Mono.when(
                        // Remove targetUserId of current user's friend list
                        removeUserIdFromFriendList(targetUserId, tuples.getT1()),
                        // Remove currentUserId of target user's friend list
                        removeUserIdFromFriendList(tuples.getT1().id(), tuples.getT2())))
                .then(Mono.just(CommonModel.CommonPayload.builder()
                        .status(CommonModel.CommonStatus.SUCCESS)
                        .message("Unfriend successfully")
                        .build()));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public Mono<Friend> getFriendList(String userId) {
        return friendRepository.findById(userId);
    }

    private Mono<Friend> removeUserIdFromFriendList(String userId, Friend friend) {
        // Filter friend exclude target userId
        var newFriendList = friend.friends().stream().filter(f -> !f.userId().equals(userId)).toList();
        // Replace Friend Data with new friendList
        return friendRepository.save(friend.withFriends(newFriendList)).subscribeOn(Schedulers.parallel());
    }

    private Flux<FriendModel.FriendRequest> getFriendRequest(String userId, int first, String cursor) {
        return cursor == null ? friendRequestRepository.findAllByUserIdStart(userId, first).map(FriendRequest::toFriendRequestPayload)
                : friendRequestRepository.findAllByUserIdBefore(userId, cursor, first).map(FriendRequest::toFriendRequestPayload);
    }

    private Flux<FriendModel.Friend> getFriendList(String userId, int first, String cursor) {
        return cursor == null ? friendRepository.findAllByUserIdStart(userId, first).map(Friend::toFriendPayload)
                : friendRepository.findAllByUserIdBefore(userId, cursor, first).map(Friend::toFriendPayload);
    }
}
