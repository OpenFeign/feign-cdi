/*
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package feign.cdi.test;


import feign.RequestLine;
import feign.cdi.api.FeignClient;
import feign.cdi.impl.FeignExtension;
import feign.ribbon.RibbonClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import static com.netflix.config.ConfigurationManager.getConfigInstance;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class AnnotatedInjectedClientTest {
    @FeignClient(url="http://myApp", client = RibbonClient.class)
    interface TestInterface {
        @RequestLine("POST /")
        String invoke();
    }

    @Deployment
    public static Archive<?> createArchive() {
        return ShrinkWrap.create(JavaArchive.class).addClass(TestInterface.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class, FeignExtension.class);
    }

    @Inject
    private TestInterface api;

    @Rule
    public final MockWebServer server = new MockWebServer();

    @Before
    public void setUp() throws Exception {
        getConfigInstance().setProperty("myApp.ribbon.listOfServers", "localhost:"+server.url("").url().getPort());
    }

    @Test
    public void shouldExecuteAgainstServer() {
        String body = "success!";
        server.enqueue(new MockResponse().setBody(body));
        assertThat(api.invoke()).isEqualTo(body);
    }
}
