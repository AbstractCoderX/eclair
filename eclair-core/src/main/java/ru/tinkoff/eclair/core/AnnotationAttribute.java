/*
 * Copyright 2018 Tinkoff Bank
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

package ru.tinkoff.eclair.core;

import java.lang.annotation.Annotation;

import static org.springframework.core.annotation.AnnotationUtils.getValue;

/**
 * @author Vyacheslav Klapatnyuk
 */
public enum AnnotationAttribute {
    LEVEL("level"),
    IF_ENABLED("ifEnabled"),
    LOGGER("logger");

    private final String name;

    AnnotationAttribute(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public <T> T extract(Annotation annotation) {
        return (T) getValue(annotation, name);
    }
}
