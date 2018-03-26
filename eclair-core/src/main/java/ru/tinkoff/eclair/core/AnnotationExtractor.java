package ru.tinkoff.eclair.core;

import org.springframework.util.ReflectionUtils;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.*;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedRepeatableAnnotations;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;
import static ru.tinkoff.eclair.core.AnnotationAttribute.LOGGER;

/**
 * @author Viacheslav Klapatniuk
 */
public final class AnnotationExtractor {

    /**
     * In order of supposed popularity
     */
    private static final List<Class<? extends Annotation>> METHOD_TARGET_ANNOTATION_CLASSES = asList(
            Log.class,
            Log.in.class,
            Log.out.class,
            Log.error.class,
            Mdc.class
    );

    /**
     * In order of supposed popularity
     */
    private static final List<Class<? extends Annotation>> PARAMETER_TARGET_ANNOTATION_CLASSES = asList(
            Log.class,
            Mdc.class
    );

    private static final ReversedBridgeMethodResolver bridgeMethodResolver = ReversedBridgeMethodResolver.getInstance();
    private static final AnnotationExtractor instance = new AnnotationExtractor();

    private AnnotationExtractor() {
    }

    public static AnnotationExtractor getInstance() {
        return instance;
    }

    public Set<Method> getCandidateMethods(Class<?> clazz) {
        return Stream.of(ReflectionUtils.getUniqueDeclaredMethods(clazz))
                .filter(method -> method.getDeclaringClass() != Object.class)
                .filter(method -> !(method.isBridge() || method.isSynthetic()))
                .collect(toSet());
    }

    public boolean hasAnyAnnotation(Method method) {
        return METHOD_TARGET_ANNOTATION_CLASSES.stream()
                .anyMatch(annotationClass -> !findMergedRepeatableAnnotations(method, annotationClass).isEmpty());
    }

    public boolean hasAnyAnnotation(Parameter parameter) {
        return PARAMETER_TARGET_ANNOTATION_CLASSES.stream()
                .anyMatch(annotationClass -> !findMergedRepeatableAnnotations(parameter, annotationClass).isEmpty());
    }

    public Set<Log> getLogs(Method method) {
        return findAnnotationOnMethodOrBridge(method, Log.class);
    }

    public Set<Log.in> getLogIns(Method method) {
        return findAnnotationOnMethodOrBridge(method, Log.in.class);
    }

    public Set<Log.out> getLogOuts(Method method) {
        return findAnnotationOnMethodOrBridge(method, Log.out.class);
    }

    public Set<Log.error> getLogErrors(Method method) {
        return findAnnotationOnMethodOrBridge(method, Log.error.class);
    }

    public Set<Mdc> getMdcs(Method method) {
        return findAnnotationOnMethodOrBridge(method, Mdc.class);
    }

    private <T extends Annotation> Set<T> findAnnotationOnMethodOrBridge(Method method, Class<T> annotationClass) {
        Set<T> logs = findAnnotationOnMethod(method, annotationClass);
        if (logs.isEmpty()) {
            Method bridgeMethod = bridgeMethodResolver.findBridgeMethod(method);
            if (nonNull(bridgeMethod)) {
                return findAnnotationOnMethod(bridgeMethod, annotationClass);
            }
        }
        return logs;
    }

    private <T extends Annotation> Set<T> findAnnotationOnMethod(Method method, Class<T> annotationClass) {
        return findMergedRepeatableAnnotations(method, annotationClass);
    }

    /**
     * TODO: rename 'logArg => argLog' everywhere
     */
    public List<Set<Log>> getLogArgs(Method method) {
        return Stream.of(method.getParameters())
                .map(parameter -> findAnnotationOnParameter(parameter, Log.class))
                .collect(toList());
    }

    public List<Set<Mdc>> getParametersMdcs(Method method) {
        return Stream.of(method.getParameters())
                .map(parameter -> findAnnotationOnParameter(parameter, Mdc.class))
                .collect(toList());
    }

    private <T extends Annotation> Set<T> findAnnotationOnParameter(Parameter parameter, Class<T> annotationClass) {
        return findMergedRepeatableAnnotations(parameter, annotationClass);
    }

    Log findLog(Method method, Set<String> loggers) {
        return filterAndFindFirstAnnotation(getLogs(method), loggers);
    }

    Log.in findLogIn(Method method, Set<String> loggers) {
        return filterAndFindFirstAnnotation(getLogIns(method), loggers);
    }

    Log.out findLogOut(Method method, Set<String> loggers) {
        return filterAndFindFirstAnnotation(getLogOuts(method), loggers);
    }

    Set<Log.error> findLogErrors(Method method, Set<String> loggers) {
        return filterAnnotations(getLogErrors(method), loggers);
    }

    List<Log> findLogArgs(Method method, Set<String> loggers) {
        return getLogArgs(method).stream()
                .map(logArgs -> filterAndFindFirstAnnotation(logArgs, loggers))
                .collect(toList());
    }

    private <T extends Annotation> T filterAndFindFirstAnnotation(Collection<T> annotations, Set<?> loggers) {
        return annotations.stream()
                .filter(getLoggerPredicate(loggers))
                .findFirst()
                .orElse(null);
    }

    private <T extends Annotation> Set<T> filterAnnotations(Collection<T> annotations, Set<?> loggers) {
        return annotations.stream()
                .filter(getLoggerPredicate(loggers))
                .collect(toCollection(LinkedHashSet::new));
    }

    private <T extends Annotation> Predicate<T> getLoggerPredicate(Set<?> loggers) {
        return annotation -> loggers.contains(LOGGER.extract(annotation));
    }

    Log.in synthesizeLogIn(Log log) {
        return synthesizeAnnotation(getAnnotationAttributes(log), Log.in.class, null);
    }

    Log.out synthesizeLogOut(Log log) {
        return synthesizeAnnotation(getAnnotationAttributes(log), Log.out.class, null);
    }
}
