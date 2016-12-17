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
import feign.RetryableException;
import feign.cdi.api.FeignClient;
import feign.cdi.impl.FeignExtension;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.ThrowableAssert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import static com.netflix.config.ConfigurationManager.getConfigInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(Arquillian.class)
public class DelegatingFeignClientBeanTest {

    @FeignClient(url="http://myApp")
    interface TestInterface {
        @RequestLine("POST /")
        String invoke();
    }

    @Deployment
    public static Archive<?> createArchive() {
        return ShrinkWrap.create(JavaArchive.class).addClasses(TestInterface.class, BuilderProvider.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class, FeignExtension.class);
    }

    @Rule
    public MockWebServer server = new MockWebServer();

    @Inject
    private TestInterface api;

    @Test
    public void shouldHandleFailuresAndSuccesses() {
        getConfigInstance().setProperty("myApp.ribbon.listOfServers", "localhost:"+server.url("").url().getPort());
        server.enqueue(new MockResponse().setResponseCode(500).setBody("Failure"));
        assertThatThrownBy(() -> api.invoke())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Error 500 while invoking TestInterface#invoke()");

        String body = "Success";
        server.enqueue(new MockResponse().setBody(body));
        String result = api.invoke();
        assertThat(result).isEqualTo(body);
    }

}
