# Eclair
> Java Spring library for AOP logging

Provides annotations for declarative logging of annotated method execution.
Includes abstractions for annotations processing, simple implementation and Spring Boot starter with auto-configuration.

## Features

* logging of events, detected by Spring AOP: *beginning*, *ending* or *emergency ending* of method execution
* flexible filtering `Throwable` types for logging
* configurable verbosity based on the enabled log level
* pre-defined printers for logging arguments or method return value in different formats:
    * `JSON` (by Jackson)
    * `XML` (by JAXB)
* declarative defining (with *SpEL*) and erasing of *Mapped Diagnostic Context* (MDC) based on scopes
* multiple logger definition
* annotations validation during application context start
* ability of use meta-annotations (applied to other annotations) and annotated method overriding
* manual logging with detection of invoker class is also available

## Getting started

Add this to your POM.

```xml
<dependency>
    <groupId>ru.tinkoff</groupId>
    <artifactId>eclair-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage examples

The examples assume that you are using a standard `SimpleLogger` and that you have the following YAML configuration: 

```yaml
logging.pattern.console: '%-5level [%X] %logger{80} %msg%n'
```

#### Basic usage

```java
class Example {
    
    /**
     * DEBUG [] ru.tinkoff.eclair.example.Example.simple > s="a", i=0, d=0.0
     * DEBUG [] ru.tinkoff.eclair.example.Example.simple < false
     */
    @Log
    public Boolean simple(String s, Integer i, Double d) {
        return false;
    }
    
    /**
     * if logger level <= DEBUG
     *
     * DEBUG [] ru.tinkoff.eclair.example.OuterClass.method >
     * ..
     * DEBUG [key=value, sum=2] ru.tinkoff.eclair.example.Example.mdcByMethod > Dto{i=0, s='null'}
     * ..
     * DEBUG [key=value, sum=2] ru.tinkoff.eclair.example.InnerClass.method >
     * DEBUG [key=value, sum=2] ru.tinkoff.eclair.example.InnerClass.method <
     * ..
     * DEBUG [key=value, sum=2] ru.tinkoff.eclair.example.Example.mdcByMethod <
     * ..
     * DEBUG [sum=2] ru.tinkoff.eclair.example.OuterClass.method <
     */
    @Mdc(key = "key", value = "value")
    @Mdc(key = "sum", value = "1 + 1", global = true)
    @Log
    public void mdcByMethod(Dto dto) {
    }
    
    @Autowired
    private ManualLogger logger;
    
    /**
     * if logger level <= DEBUG
     *
     * DEBUG [] ru.tinkoff.eclair.example.Example.manualLogging >
     * DEBUG [key=value] ru.tinkoff.eclair.example.Example.manualLogging - Manual logging: 0.123
     * INFO  [key=value] ru.tinkoff.eclair.example.Example.manualLogging - Lazy manual logging: 0.456
     * DEBUG [key=value] ru.tinkoff.eclair.example.Example.manualLogging <
     */
    @Log
    public void manualLogging() {
        MDC.put("key", "value");
        logger.debug("Manual logging: {}", new Random().nextDouble());
        logger.info("Lazy manual logging: {}", (Supplier) () -> new Random().nextDouble());
    }
}
```

#### Advanced usage

```java
class Advanced {

    /**
     * DEBUG [] ru.tinkoff.eclair.example.Advanced.verboseDtoToXml > dto=<dto><i>0</i></dto>
     */
    @Log.in(printer = "xml")
    public void verboseDtoToXml(Dto dto, Integer i) {
    }

    /**
     * if logger level = DEBUG
     * WARN  [] r.t.e.example.Advanced.levelIfEnabledErrorEvent ! java.lang.RuntimeException: message
     * java.lang.RuntimeException: message
     *     at r.t.e.example.Advanced.levelIfEnabledErrorEvent(Example.java:167)
     *
     * if logger level = INFO
     * (nothing to log)
     */
    @Log.error(ifEnabled = DEBUG)
    public void levelIfEnabledErrorEvent() {
        throw new RuntimeException("message");
    }
    
    /**
     * DEBUG [] r.t.e.example.Advanced.verboseToVariousPrinters > xmlDto=<dto><i>0</i></dto>, jsonDto={"i":0,"s":null}
     */
    public void verboseToVariousPrinters(@Log(printer = "xml") Dto xmlDto,
                                         @Log(printer = "json") Dto jsonDto,
                                         Integer i) {
    }
    
    /**
     * if logger level <= DEBUG
     *
     * DEBUG [] ru.tinkoff.eclair.example.OuterClass.method >
     * ..
     * DEBUG [length=3, staticString=some string] ru.tinkoff.eclair.example.Advanced.mdcByArg > Dto{i=0, s='null'}
     * DEBUG [length=3, staticString=some string] ru.tinkoff.eclair.example.Advanced.mdcByArg <
     * ..
     * DEBUG [staticString=some string] ru.tinkoff.eclair.example.OuterClass.method <
     */
    @Log
    public void mdcByArg(@Mdc(key = "length", value = "s.length()")
                         @Mdc(key = "staticString", value = "some string", global = true) Dto dto) {
    }
    
    @Autowired
    private ManualLogger logger;
    
    /**
     * if logger level = DEBUG
     */
    public void manualLevelLogging() {
        // WARN  [] r.t.eclair.example.Advanced.manualLevelLogging - warn
        // WARN  [] r.t.eclair.example.Advanced.manualLevelLogging - warnIfInfoEnabled
        // WARN  [] r.t.eclair.example.Advanced.manualLevelLogging - warnIfDebugEnabled
        // nothing to log
        logger.warn("warn");
        logger.warnIfInfoEnabled("warnIfInfoEnabled");
        logger.warnIfDebugEnabled("warnIfDebugEnabled");
        logger.warnIfTraceEnabled("warnIfTraceEnabled");
    }

    @Log
    @Log(logger = "audit")
    public void logMultiLogger() {
    }
}
```

## Release History

04.2018 - 1.0.0 Basic features

## License

Copyright 2018 Tinkoff Bank

Licensed under the Apache License, Version 2.0

[https://github.com/TinkoffCreditSystems/eclair](https://github.com/TinkoffCreditSystems/eclair)