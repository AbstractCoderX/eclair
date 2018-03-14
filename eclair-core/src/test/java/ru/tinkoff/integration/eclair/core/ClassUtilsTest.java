package ru.tinkoff.integration.eclair.core;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ClassUtilsTest {

    @Test
    public void calculateInheritanceDistanceUnassignable() {
        // given
        Class<?> parent = Error.class;
        Class<?> child = RuntimeException.class;
        // when
        int distance = ClassUtils.calculateInheritanceDistance(parent, child);
        // then
        assertThat(distance, is(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateInheritanceDistanceAssignableInterfaces() {
        // given
        Class<?> parent = Advice.class;
        Class<?> child = MethodInterceptor.class;
        // when
        ClassUtils.calculateInheritanceDistance(parent, child);
        // then expected exception
    }

    @Test
    public void calculateInheritanceDistanceObjects() {
        // given
        Class<?> parent = Object.class;
        Class<?> child = Object.class;
        // when
        int distance = ClassUtils.calculateInheritanceDistance(parent, child);
        // then
        assertThat(distance, is(0));
    }

    @Test
    public void calculateInheritanceDistanceEquals() {
        // given
        Class<?> parent = String.class;
        Class<?> child = String.class;
        // when
        int distance = ClassUtils.calculateInheritanceDistance(parent, child);
        // then
        assertThat(distance, is(0));
    }

    @Test
    public void calculateInheritanceDistance() {
        // given
        Class<?> parent = Throwable.class;
        Class<?> child = ArrayIndexOutOfBoundsException.class;
        // when
        int distance = ClassUtils.calculateInheritanceDistance(parent, child);
        // then
        assertThat(distance, is(4));
    }

    @Test
    public void calculateInheritanceDistanceReverse() {
        // given
        Class<?> parent = ArrayIndexOutOfBoundsException.class;
        Class<?> child = Throwable.class;
        // when
        int distance = ClassUtils.calculateInheritanceDistance(parent, child);
        // then
        assertThat(distance, is(-1));
    }

    @Test
    public void findMostSpecificAncestorEmpty() {
        // given
        Set<Class<?>> parents = Collections.emptySet();
        Class<?> child = Object.class;
        // when
        Class<?> ancestor = ClassUtils.findMostSpecificAncestor(parents, child);
        // then
        assertThat(ancestor, nullValue());
    }

    @Test
    public void findMostSpecificAncestorNotFound() {
        // given
        Set<Class<?>> parents = new HashSet<>(asList(String.class, Integer.class, Void.class));
        Class<?> child = Double.class;
        // when
        Class<?> ancestor = ClassUtils.findMostSpecificAncestor(parents, child);
        // then
        assertThat(ancestor, nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findMostSpecificAncestorAssignableInterfaces() {
        // given
        Set<Class<?>> parents = Collections.singleton(Serializable.class);
        Class<?> child = Double.class;
        // when
        ClassUtils.findMostSpecificAncestor(parents, child);
        // then expected exception
    }

    @Test
    public void findMostSpecificAncestor() {
        // given
        Set<Class<?>> parents = new HashSet<>(asList(String.class, BigDecimal.class, Object.class, Number.class));
        Class<?> child = Number.class;
        // when
        Class<?> ancestor = ClassUtils.findMostSpecificAncestor(parents, child);
        // then
        assertEquals(Number.class, ancestor);
    }

    @Test
    public void reduceDescendantsEmpty() {
        // given
        List<Class<?>> classes = Collections.emptyList();
        // when
        Set<Class<?>> set = ClassUtils.reduceDescendants(classes);
        // then
        assertThat(set, is(empty()));
    }

    @Test
    public void reduceDescendants() {
        // given
        List<Class<?>> classes = asList(Number.class, BigDecimal.class, ArrayList.class, AbstractList.class);
        // when
        Set<Class<?>> reduced = ClassUtils.reduceDescendants(classes);
        // then
        assertThat(reduced, hasSize(2));
        assertThat(reduced, Matchers.<Class<?>>containsInAnyOrder(Number.class, AbstractList.class));
    }
}
