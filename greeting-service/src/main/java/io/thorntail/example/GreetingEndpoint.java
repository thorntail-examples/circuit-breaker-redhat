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

import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalTime;

@Path("/")
public class GreetingEndpoint {
    @Inject
    @RestClient
    private NameService nameService;

    @GET
    @Path("/greeting")
    @Produces(MediaType.APPLICATION_JSON)
    public Greeting greeting() {
        return new Greeting(String.format("Hello, %s!", nameService.get()));
    }

    static class Greeting {
        private final String content;
        private final String time;

        public Greeting(String content) {
            this.content = content;
            this.time = LocalTime.now().toString();
        }

        public String getContent() {
            return content;
        }

        public String getTime() {
            return time;
        }
    }
}
