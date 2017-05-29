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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
@Path("/")
public class NameEndpoint {

    private AtomicBoolean isOn;

    @PostConstruct
    void init() {
        isOn = new AtomicBoolean(true);
    }

    @GET
    @Path("/name")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getName() {
        return isOn.get() ? Response.ok("World").build() : Response.serverError().build();
    }

    @PUT
    @Path("/toggle")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response toggle(ServiceInfo info) {
        isOn.set(info.isOn());
        return Response.status(200).build();
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceInfo getInfo() {
        return isOn.get() ? ServiceInfo.INFO_OK : ServiceInfo.INFO_FAIL;
    }

}
