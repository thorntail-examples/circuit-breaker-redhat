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
package io.thorntail.example;

import io.thorntail.openshift.test.OpenShiftTest;
import io.thorntail.openshift.test.injection.TestResource;
import io.thorntail.openshift.test.injection.WithName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@OpenShiftTest
public class OpenshiftIT {
    private static final String NAME_SERVICE_APP = "thorntail-circuit-breaker-name";
    private static final String GREETING_SERVICE_APP = "thorntail-circuit-breaker-greeting";

    private static final String OK = "ok";
    private static final String FAIL = "fail";
    private static final String CLOSED = "closed";
    private static final String OPEN = "open";
    private static final String HELLO_OK = "Hello, World!";
    private static final String HELLO_FALLBACK = "Hello, Fallback!";

    // circuitBreaker.delay, in seconds
    private static final long SLEEP_WINDOW = 5;
    // circuitBreaker.requestVolumeThreshold
    private static final long REQUEST_THRESHOLD = 3;

    @TestResource
    @WithName(NAME_SERVICE_APP)
    private URL nameServiceUrl;

    @TestResource
    @WithName(GREETING_SERVICE_APP)
    private URL greetingServiceUrl;

    @Test
    public void testCircuitBreaker() {
        assertCircuitBreaker(CLOSED);
        assertGreeting(HELLO_OK);

        changeNameServiceState(FAIL);
        for (int i = 0; i < REQUEST_THRESHOLD; i++) {
            assertGreeting(HELLO_FALLBACK);
        }
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertCircuitBreaker(OPEN));

        changeNameServiceState(OK);
        await().atMost(SLEEP_WINDOW + 5, TimeUnit.SECONDS).pollDelay(SLEEP_WINDOW, TimeUnit.SECONDS).untilAsserted(() -> {
            assertGreeting(HELLO_OK);
            assertCircuitBreaker(CLOSED);
        });
    }

    private void assertGreeting(String expected) {
        given()
                .baseUri(greetingServiceUrl.toString())
        .when()
                .get("/api/greeting")
        .then()
                .statusCode(200)
                .body(containsString(expected));
    }

    private void assertCircuitBreaker(String expected) {
        given()
                .baseUri(greetingServiceUrl.toString())
        .when()
                .get("/api/cb-state")
        .then()
                .statusCode(200)
                .body("state", equalTo(expected));
    }

    private void changeNameServiceState(String state) {
        String json = "{\"state\":\"" + state + "\"}";
        given()
                .baseUri(nameServiceUrl.toString())
        .when()
                .header("Content-type", "application/json")
                .body(json)
                .put("/api/state")
        .then()
                .statusCode(200)
                .body("state", equalTo(state));
    }
}
