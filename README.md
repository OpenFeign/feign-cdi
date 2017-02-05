# Feign CDI
Integration for OpenFeign with Java Contexts & Dependency Injection, allowing you to inject your type safe restful interfaces

## Adding to your project
Feign CDI is built using Apache Maven.  Artifacts are published, though there's only a single artifact at this time.

```xml
<dependency>
    <groupId>io.github.openfeign.cdi</groupId>
    <artifactId>feign-cdi</artifactId>
    <version>0.1.6</version>
</dependency>
```

## Creating clients
Feign CDI uses the normal Feign interfaces for declaring your rest client.  You can add additional annotations to your interface to support CDI integration

### @FeignClient

```java
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
```

`@FeignClient` is an annotation to place on your interfaces where you can define the normal configuration options for Feign interfaces.  The defaults here match the defaults in `Feign.Builder`, except for the client implementation to support no-arg constructors.

Any class configured here will follow one of two options:

- Managed CDI Beans
- No-args constructor

Any classes you use here should be normal scoped beans - don't use `@Dependent` as there is no creational context.  If no bean is found, then this extension will instantiate the class using its default constructor.

### Managing Scopes

Any `@Scope` declared on the interface will be used by the bean.

### Registering the extension

The CDI extension class to register is `feign.cdi.impl.FeignExtension`, which will add annotated client beans for each interface discovered.