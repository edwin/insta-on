package com.edw.controller;

import com.edw.service.InstagramService;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.*;

/**
 * <pre>
 *     com.edw.controller.MainController
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 13 Jan 2022 10:43
 */

@Path("/api")
public class MainController {

    // initialize threadpool
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
            .availableProcessors());

    @Inject
    InstagramService instagramService;

    @POST
    @Path("/inquiry")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Map inquiry(@FormParam("username") @NotEmpty @NotNull String username) throws Exception {

        // response object
        Map responseMap = new HashMap();

        // sanity check
        Map profile = instagramService.getUserProfile(username);
        if(profile == null) {
            responseMap.put("profile", null);
            return responseMap;
        } else {
            responseMap.putAll(profile);
        }

        // concurrent call
        List<Callable<Map>> callableTasks = new ArrayList<>();
        callableTasks.add(() -> instagramService.getInstagramFollowingFeed(username));
        callableTasks.add(() -> instagramService.getInstagramFollowerFeed(username));

        List<Future<Map>> futures = executorService.invokeAll(callableTasks);

        for (Future<Map> future : futures) {
            responseMap.putAll(future.get());
        }

        // calculate and populate
        List<String> followingsTemp = new ArrayList<>((List<String>)responseMap.get("following"));
        List<String> followersTemp = new ArrayList<>((List<String>)responseMap.get("followers"));

        followingsTemp.removeAll((List<String>)responseMap.get("followers"));
        followersTemp.removeAll((List<String>)responseMap.get("following"));

        responseMap.put("following", followingsTemp);
        responseMap.put("followers", followersTemp);

        // calculate who you follow and follow you back (i called them "true friends" lol)
        if(!followersTemp.isEmpty()) {
            // your "true friends" is number of your followers minus the followers you aren't following back
            int total_folback = ((int)responseMap.get("total_followers")) - followersTemp.size();
            responseMap.put("total_folback", total_folback);
        } else {
            responseMap.put("total_folback", "Account is private \uD83D\uDE1E");
        }


        // give response
        return responseMap;
    }
}
