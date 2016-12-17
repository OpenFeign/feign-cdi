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

package feign.cdi.impl;

import feign.Feign;
import feign.cdi.api.FeignClient;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.util.ArrayList;
import java.util.List;

public class FeignExtension implements Extension{
    private final List<Class<?>> feignClientClasses = new ArrayList<Class<?>>();
    private boolean builderBeanExists = false;

    public void collectFeignClients(@Observes @WithAnnotations(FeignClient.class)ProcessAnnotatedType<?> pat) {
        feignClientClasses.add(pat.getAnnotatedType().getJavaClass());
    }

    public void checkForBuilderBeans(@Observes ProcessProducer<?,? extends Feign.Builder> pat) {
        this.builderBeanExists = true;
    }

    public void addFeignClientBeans(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        for(Class<?> clientClass : feignClientClasses) {
            if(builderBeanExists) {
                afterBeanDiscovery.addBean(new DelegatingFeignClientBean(clientClass));
            }
            else {
                afterBeanDiscovery.addBean(new AnnotatedFeignClientBean(clientClass));
            }
        }
    }
}
