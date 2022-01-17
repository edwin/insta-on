package com.edw.service;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.users.UserAction;
import com.github.instagram4j.instagram4j.models.user.Profile;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 *     com.edw.service.InstagramService
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 13 Jan 2022 23:47
 */
@ApplicationScoped
public class InstagramService {

    private IGClient client = null;

    @ConfigProperty(name = "instagram.username")
    String username;

    @ConfigProperty(name = "instagram.password")
    String password;

    @PostConstruct
    public void populateData() throws Exception {
        File clientFile = new File("clientfile");
        File cookieFile = new File("cookiefile");

        if(!clientFile.exists() || !cookieFile.exists() ) {
            client = IGClient.builder()
                    .username(username)
                    .password(password)
                    .login();

            client.serialize(clientFile, cookieFile);
        } else {
            client = IGClient.deserialize(clientFile, cookieFile);
        }

    }

    public Map getInstagramFollowingFeed(String username) {
        List<Profile> followings = client.actions().users()
                .findByUsername(username)
                .thenApply(userAction -> userAction.followingFeed().stream()
                        .flatMap(feedUsersResponse -> feedUsersResponse.getUsers().stream()).limit(1000).collect(Collectors.toList()) )
                .join();

        return new HashMap() {{
            put("following", followings.stream().map(following -> following.getUsername()).collect(Collectors.toList()));
        }};
    }

    public Map getInstagramFollowerFeed(String username) {
        List<Profile> followers = client.actions().users()
                .findByUsername(username)
                .thenApply(userAction -> userAction.followersFeed().stream()
                        .flatMap(feedUsersResponse -> feedUsersResponse.getUsers().stream()).limit(1000).collect(Collectors.toList()) )
                .join();

        return new HashMap() {{
            put("followers", followers.stream().map(follower -> follower.getUsername()).collect(Collectors.toList()));
        }};
    }

    public Map getUserProfile(String username) {
        UserAction userAction = client.actions().users().findByUsername(username).join();
        if(userAction == null) {
            return null;
        }
        return new HashMap() {{
            put("username", userAction.getUser().getUsername());
            put("total_followers", userAction.getUser().getFollower_count());
            put("total_following", userAction.getUser().getFollowing_count());
            put("biography", userAction.getUser().getBiography());
            put("full_name", userAction.getUser().getFull_name());
        }};
    }

}
