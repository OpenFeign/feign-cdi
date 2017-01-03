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
import feign.Request;
import feign.RequestInterceptor;
import feign.cdi.api.FeignClient;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

class AnnotatedFeignClientBean implements Bean<Object> {
    private final Class<?> interfaceClass;
    private final FeignClient feignClient;

    AnnotatedFeignClientBean(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        feignClient = this.interfaceClass.getAnnotation(FeignClient.class);
    }

    @Override
    public Class<?> getBeanClass() {
        return interfaceClass;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Object create(CreationalContext<Object> creationalContext) {
        Feign.Builder builder = mergeBuilders(createBuilder());
        return doTarget(builder, feignClient.url());
    }

    @Override
    public void destroy(Object instance, CreationalContext<Object> creationalContext) {

    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> types = new LinkedHashSet<Type>();
        types.add(interfaceClass);
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> annotations = new LinkedHashSet<Annotation>();
        annotations.add(new DefaultLiteral());
        return annotations;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        for(Annotation annotation : interfaceClass.getAnnotations()) {
            if(annotation.getClass().getAnnotation(Scope.class) != null) {
                return annotation.getClass();
            }
        }
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }


    protected Feign.Builder createBuilder() {
        return Feign.builder();
    }

    <T> T getBean(Class<T> clazz) {
        try {
            return CDI.current().select(clazz).get();
        }
        catch (Exception e) {
            // may not be a CDI bean, manually create
            try {
                return clazz.newInstance();
            } catch (InstantiationException e1) {
                throw new RuntimeException("Unable to instantiate "+clazz.getName(), e1);
            } catch (IllegalAccessException e1) {
                throw new RuntimeException("Unable to instantiate "+clazz.getName(), e1);
            }
        }
    }

    Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    protected Object doTarget(Feign.Builder builder, String url) {
        return builder.target(interfaceClass, url);
    }

    protected Feign.Builder mergeBuilders(Feign.Builder builder) {
        System.out.println("merging builders in base class");
        builder.options(new Request.Options(feignClient.connectTimeoutMillis(), feignClient.readTimeoutMillis()));
        if(feignClient.decode404()) {
            builder.decode404();
        }
        builder.client(getBean(feignClient.client()));
        builder.contract(getBean(feignClient.contract()));
        builder.decoder(getBean(feignClient.decoder()));
        builder.encoder(getBean(feignClient.encoder()));
        builder.errorDecoder(getBean(feignClient.errorDecoder()));
        if(useInvocationHandlerFactory()) {
            builder.invocationHandlerFactory(getBean(feignClient.invocationHandlerFactory()));
        }
        builder.logger(getBean(feignClient.logger()));
        builder.logLevel(feignClient.logLevel());
        builder.retryer(getBean(feignClient.retryer()));
        for(Class<? extends RequestInterceptor> requestInterceptorClass : feignClient.requestInterceptors()) {
            builder.requestInterceptor(getBean(requestInterceptorClass));
        }

        return builder;
    }

    protected boolean useInvocationHandlerFactory() {
        return true;
    }

    private static final class DefaultLiteral extends AnnotationLiteral<Default> implements Default {

    }
}
