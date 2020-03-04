package com.prez.test;

import com.prez.config.AuthorizationServerApplication;
import com.prez.test.dummy.DummyResourceApp;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

// need dummy-oauth-authorization-server to run
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = AuthorizationServerApplication.class)
@TestPropertySource(properties = {
        "server.port=8082"
})
public class TokenLiveTest {

    @Test
    public void whenObtainingAccessToken_thenCorrect() {
        final Response authServerResponse = obtainAccessToken("randomClientId", "john", "123");
        final String accessToken = authServerResponse.jsonPath().getString("access_token");
        System.out.println();
        System.out.println();
        System.out.println("response:" + authServerResponse.asString());
        System.out.println();
        System.out.println();
        assertNotNull(accessToken);

        final Response resourceServerResponse = RestAssured.given().header("Authorization", "Bearer " + accessToken).get("http://localhost:8082/employee/100");
        assertThat(resourceServerResponse.getStatusCode(), equalTo(404));
        System.out.println("foo:" + resourceServerResponse.asString());

    }

    //

    private Response obtainAccessToken(String clientId, String username, String password) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("grant_type", "password");
        params.put("client_id", clientId);
        params.put("username", username);
        params.put("password", password);
        return RestAssured.given().auth().preemptive().basic(clientId, "DontDoThisAtHome").and().with().params(params).when().post("http://localhost:8081/shady-authorization-server/oauth/token");
        // response.jsonPath().getString("refresh_token");
        // response.jsonPath().getString("access_token")
    }

    private String obtainRefreshedToken(String clientId, final String refreshToken) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("grant_type", "refresh_token");
        params.put("client_id", clientId);
        params.put("refresh_token", refreshToken);
        final Response response = RestAssured.given().auth().preemptive().basic(clientId, "DontDoThisAtHome").and().with().params(params).when().post("http://localhost:8081/shady-authorization-server/oauth/token");
        return response.jsonPath().getString("access_token");
    }

    private void authorizeClient(String clientId) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("response_type", "code");
        params.put("client_id", clientId);
        params.put("scope", "read,write");
        final Response response = RestAssured.given().auth().preemptive().basic(clientId, "DontDoThisAtHome").and().with().params(params).when().post("http://localhost:8081/shady-authorization-server/oauth/authorize");
    }

    private Response checkToken(final String clientId, String accessToken) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("token", accessToken);
        return RestAssured.given().with().auth().preemptive().basic(clientId, "DontDoThisAtHome").params(params).when().post("http://localhost:8081/shady-authorization-server/oauth/check_token");
    }

    @Test
    public void checkExpiredToken() {
        final Response response = checkToken("randomClientId", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJqb2huIiwic2NvcGUiOlsiZm9vIiwicmVhZCIsIndyaXRlIl0sIm9yZ2FuaXphdGlvbiI6ImpvaG5ZZHBDIiwiZXhwIjoxNTc2ODUyNDQ4LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiZGQ0MGU5YzctYzJlZC00N2M3LWJiYzEtNmU2MjY3OGMwZTJmIiwiY2xpZW50X2lkIjoiZm9vQ2xpZW50SWRQYXNzd29yZCJ9.nBpTFviwG6i7nln1MP3UPwkcrpbqcIICxi2JY_815eg");
        System.out.println("response:" + response.asString());
        //assertThat(response.statusCode());
        // TODO check error
    }

    @Test
    public void getTokenKey() {
        String clientId = "randomClientId";
        final Response tokenKey =  RestAssured.given().with().auth().preemptive().basic(clientId, "DontDoThisAtHome").when().get("http://localhost:8081/shady-authorization-server/oauth/token_key");
        System.out.println("tokenKey:" + tokenKey.asString());
        Response accessTokenResponse = obtainAccessToken(clientId, "john", "123");
        System.out.println("accessToken:" + accessTokenResponse.asString());
    }
}