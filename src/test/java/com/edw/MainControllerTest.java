package com.edw;

import com.edw.service.InstagramService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * <pre>
 *     com.edw.MainControllerTest
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 17 Jan 2022 21:43
 */
@QuarkusTest
public class MainControllerTest {

    @InjectMock
    InstagramService instagramService;

    @BeforeEach
    public void setUp() {

        //invalid username
        Mockito.when(instagramService.getUserProfile("someinvalidusername")).thenReturn(null);

        //valid and public username
        Mockito.when(instagramService.getUserProfile("somevalidusername")).thenReturn(new HashMap() {{
            put("username", "somevalidusername");
            put("total_followers", 3);
            put("total_following", 3);
            put("biography", "some biography");
            put("full_name", "some full name");
        }});
        Mockito.when(instagramService.getInstagramFollowerFeed("somevalidusername")).thenReturn(new HashMap() {{
            put("followers", Arrays.asList(new String[]{"userone", "usertwo", "userthree"}));
        }});
        Mockito.when(instagramService.getInstagramFollowingFeed("somevalidusername")).thenReturn(new HashMap() {{
            put("following", Arrays.asList(new String[]{"userone", "userfour", "userfive"}));
        }});

        //valid and private username
        Mockito.when(instagramService.getUserProfile("somevalidprivateusername")).thenReturn(new HashMap() {{
            put("username", "somevalidprivateusername");
            put("total_followers", 100);
            put("total_following", 200);
            put("biography", "some biography");
            put("full_name", "some full name");
        }});
        Mockito.when(instagramService.getInstagramFollowerFeed("somevalidprivateusername")).thenReturn(new HashMap() {{
            put("followers", Collections.EMPTY_LIST);
        }});
        Mockito.when(instagramService.getInstagramFollowingFeed("somevalidprivateusername")).thenReturn(new HashMap() {{
            put("following", Collections.EMPTY_LIST);
        }});
    }

    @Test
    public void test404() {
        given()
                .log().all()
                .when().get("/hello")
                .then()
                .statusCode(404)
                .log().all();
    }

    @Test
    public void test200() {
        given()
                .log().all()
                .when().get("/")
                .then()
                .statusCode(200)
                .log().all();
    }

    @Test
    public void testNullUsernameApiInquiry() {
        given()
                .log().all()
                .when().post("/api/inquiry")
                .then()
                .statusCode(400)
                .log().all();
    }

    @Test
    public void testInvalidUsernameApiInquiry() {
        given()
                .log().all()
                .param("username", "")
                .when().post("/api/inquiry")
                .then()
                .statusCode(400)
                .log().all();

        given()
                .log().all()
                .param("username", "         ")
                .when().post("/api/inquiry")
                .then()
                .statusCode(400)
                .log().all();

        given()
                .log().all()
                .param("username", "abc")
                .when().post("/api/inquiry")
                .then()
                .statusCode(400)
                .log().all();

        given()
                .log().all()
                .param("username", "         abc")
                .when().post("/api/inquiry")
                .then()
                .statusCode(400)
                .log().all();

        given()
                .log().all()
                .param("username", "abcd efg ")
                .when().post("/api/inquiry")
                .then()
                .statusCode(400)
                .log().all();

        given()
                .log().all()
                .param("username", " abcdefghij")
                .when().post("/api/inquiry")
                .then()
                .statusCode(400)
                .log().all();
    }

    @Test
    public void testValidUsernameApiInquiry() {

        // username not found
        given()
                .log().all()
                .param("username", "someinvalidusername")
                .when().post("/api/inquiry")
                .then()
                .statusCode(200)
                .body("isEmpty()", is(true))
                .log().all();

        // username found and public
        given()
                .log().all()
                .param("username", "somevalidusername")
                .when().post("/api/inquiry")
                .then()
                .statusCode(200)
                .body("total_folback", equalTo(1))
                .body("followers", hasSize(2))
                .body("following", hasSize(2))
                .body("full_name", is("some full name"))
                .log().all();

        // username found and private
        given()
                .log().all()
                .param("username", "somevalidprivateusername")
                .when().post("/api/inquiry")
                .then()
                .statusCode(200)
                .body("total_folback", equalTo("Account is private \uD83D\uDE1E"))
                .body("followers", hasSize(0))
                .body("following", hasSize(0))
                .body("full_name", is("some full name"))
                .log().all();
    }
}