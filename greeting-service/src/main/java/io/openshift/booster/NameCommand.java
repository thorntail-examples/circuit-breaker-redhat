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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

/**
 *
 * @author Martin Kouba
 */
public class NameCommand extends HystrixCommand<String> {

    static final HystrixCommandKey KEY = HystrixCommandKey.Factory.asKey(NameCommand.class.getSimpleName());

    static final HystrixCommandGroupKey GROUP_KEY = HystrixCommandGroupKey.Factory.asKey(NameCommand.class.getPackage().getName());

    private final URI nameServiceUri;

    NameCommand(URI nameServiceUri) {
        super(Setter.withGroupKey(GROUP_KEY).andCommandKey(KEY));
        this.nameServiceUri = nameServiceUri;
    }

    @Override
    protected String run() throws Exception {
        Client client = ClientBuilder.newClient();
        Response response = client.target(nameServiceUri).request(MediaType.TEXT_PLAIN_TYPE).get();
        if (response.getStatus() != 200) {
            throw new RuntimeException("Cannot get name from " + nameServiceUri);
        }
        String value = response.readEntity(String.class);
        response.close();
        return value;
    }

    @Override
    protected String getFallback() {
        return "Fallback";
    }

}
