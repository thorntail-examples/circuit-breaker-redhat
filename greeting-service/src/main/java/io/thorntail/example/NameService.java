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

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import io.smallrye.faulttolerance.SimpleCommand;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;

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

    static boolean isCircuitBreakerOpen() {
        // MicroProfile Fault Tolerance doesn't provide access to circuit breaker status
        // and SmallRye Fault Tolerance doesn't expose an API for that either (yet);
        // so here we rely on SmallRye Fault Tolerance implementation details (Hystrix)
        try {
            Method method = NameService.class.getMethod("get");
            HystrixCircuitBreaker circuitBreaker = HystrixCircuitBreaker.Factory.getInstance(
                    HystrixCommandKey.Factory.asKey(SimpleCommand.getCommandKey(method)));
            if (circuitBreaker == null) {
                // not yet initialized, so not open (all circuit breakers start closed)
                return false;
            }
            return circuitBreaker.isOpen();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
