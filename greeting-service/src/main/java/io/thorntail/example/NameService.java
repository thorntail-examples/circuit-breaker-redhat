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

import io.smallrye.faulttolerance.api.CircuitBreakerState;
import io.smallrye.faulttolerance.api.CircuitBreakerStateChanged;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "http://thorntail-circuit-breaker-name:8080/")
@ApplicationScoped
public interface NameService {
    @GET
    @Path("/api/name")
    @Produces(MediaType.TEXT_PLAIN)
    @CircuitBreaker(requestVolumeThreshold = 3)
    @Fallback(fallbackMethod = "fallback")
    String get();

    default String fallback() {
        return "Fallback";
    }

    @ApplicationScoped
    class CircuitBreakerObserver {
        private volatile CircuitBreakerState state = CircuitBreakerState.CLOSED;

        public void observe(@Observes CircuitBreakerStateChanged event) {
            if (NameService.class.equals(event.clazz) && "get".equals(event.method.getName())) {
                state = event.targetState;
                CircuitBreakerWebSocketEndpoint.send(event.targetState.name().toLowerCase());
            }
        }

        public CircuitBreakerState currentState() {
            return state;
        }
    }
}
