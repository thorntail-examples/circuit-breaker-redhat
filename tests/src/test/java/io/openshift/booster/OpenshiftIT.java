/*
 *
 *  Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.openshift.booster;

import java.util.concurrent.TimeUnit;

import javax.json.Json;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.arquillian.cube.openshift.impl.enricher.AwaitRoute;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.get;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class OpenshiftIT {

    private static final String NAME_SERVICE_APP = "thorntail-circuit-breaker-name";
    private static final String GREETING_SERVICE_APP = "thorntail-circuit-breaker-greeting";

    private static final String OK = "ok";
    private static final String FAIL = "fail";
    private static final String CLOSED = "closed";
    private static final String OPEN = "open";
    private static final String HELLO_OK = "Hello, World!";
    private static final String HELLO_FALLBACK = "Hello, Fallback!";

    // See also circuitBreaker.sleepWindowInMilliseconds
    private static final long SLEEP_WINDOW = 5000l;
    // See also circuitBreaker.requestVolumeThreshold
    private static final long REQUEST_THRESHOLD = 3;

    @RouteURL(NAME_SERVICE_APP)
    @AwaitRoute(path = "/api/info")
    private String nameServiceUrl;

    @RouteURL(GREETING_SERVICE_APP)
    @AwaitRoute
    private String greetingServiceUrl;

    @Test
    public void testCircuitBreaker() throws InterruptedException {
        assertCircuitBreaker(CLOSED);
        assertGreeting(HELLO_OK);
        changeNameServiceState(FAIL);
        for (int i = 0; i < REQUEST_THRESHOLD; i++) {
            assertGreeting(HELLO_FALLBACK);
        }
        // Circuit breaker should be open now
        // Wait a little to get the current health counts - see also metrics.healthSnapshot.intervalInMilliseconds
        await().atMost(5, TimeUnit.SECONDS).until(() -> testCircuitBreakerState(OPEN));
        changeNameServiceState(OK);
        // See also circuitBreaker.sleepWindowInMilliseconds
        await().atMost(7, TimeUnit.SECONDS).pollDelay(SLEEP_WINDOW, TimeUnit.MILLISECONDS).until(() -> testGreeting(HELLO_OK));
        // The health counts should be reset
        assertCircuitBreaker(CLOSED);
    }

    private Response greetingResponse() {
        return RestAssured.when().get(greetingServiceUrl + "api/greeting");
    }

    private void assertGreeting(String expected) {
        Response response = greetingResponse();
        response.then().statusCode(200).body(containsString(expected));
    }

    private boolean testGreeting(String expected) {
        Response response = greetingResponse();
        response.then().statusCode(200);
        return response.getBody().asString().contains(expected);
    }

    private Response circuitBreakerResponse() {
        return RestAssured.when().get(greetingServiceUrl + "api/cb-state");
    }

    private void assertCircuitBreaker(String expectedState) {
        Response response = circuitBreakerResponse();
        response.then().statusCode(200).body("state", equalTo(expectedState));
    }

    private boolean testCircuitBreakerState(String expectedState) {
        Response response = circuitBreakerResponse();
        response.then().statusCode(200);
        return response.getBody().asString().contains(expectedState);
    }

    private void changeNameServiceState(String state) {
        RestAssured.given()
                .header("Content-type", "application/json")
                .body(Json.createObjectBuilder().add("state", state).build().toString())
                .put(nameServiceUrl + "api/state")
                .then()
                .assertThat()
                .statusCode(200)
                .body("state", equalTo(state));
    }
}
