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

package feign.cdi.api;

import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FeignClient {
    Logger.Level logLevel() default Logger.Level.NONE;

    Class<? extends Contract> contract() default Contract.Default.class;

    Class<? extends Client> client() default CDIClient.class;

    Class<? extends Decoder> decoder() default Decoder.Default.class;

    Class<? extends Encoder> encoder() default Encoder.Default.class;

    Class<? extends ErrorDecoder> errorDecoder() default ErrorDecoder.Default.class;

    Class<? extends InvocationHandlerFactory> invocationHandlerFactory() default InvocationHandlerFactory.Default.class;

    Class<? extends Logger> logger() default Logger.NoOpLogger.class;

    Class<? extends Retryer> retryer() default Retryer.Default.class;

    Class<? extends RequestInterceptor>[] requestInterceptors() default {};

    String url();

    int connectTimeoutMillis() default 10 * 1000;

    int readTimeoutMillis() default 60 * 1000;

    boolean decode404() default false;
}
