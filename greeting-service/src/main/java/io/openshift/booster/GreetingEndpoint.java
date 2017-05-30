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

import java.net.URI;
import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

@Path("/")
public class GreetingEndpoint {

    @Inject
    URI nameServiceUri;

    @Inject
    Client client;

    @GET
    @Path("/greeting")
    @Produces("application/json")
    public Greeting greeting() {
        return new Greeting(String.format("Hello, %s!", new NameCommand(nameServiceUri, client).execute()));
    }

    /**
     * This endpoint is used as Kubernetes liveness and readiness probe.
     *
     * @return the response
     */
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok().build();
    }

    static class Greeting {

        private final String content;

        private final String timestamp;

        public Greeting(String content) {
            this.content = content;
            this.timestamp = LocalDateTime.now().toString();
        }

        public String getContent() {
            return content;
        }

        public String getTimestamp() {
            return timestamp;
        }

    }

}
