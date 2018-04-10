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

package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.definition.MethodMdc;
import ru.tinkoff.eclair.definition.ParameterMdc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class MethodMdcFactory {

    public static MethodMdc newInstance(Method method,
                                        List<String> parameterNames,
                                        Set<ParameterMdc> methodParameterMdcs,
                                        List<Set<ParameterMdc>> parameterMdcs) {
        if (methodParameterMdcs.isEmpty() && parameterMdcs.stream().allMatch(Collection::isEmpty)) {
            return null;
        }
        return MethodMdc.builder()
                .method(method)
                .parameterNames(parameterNames)
                .methodDefinitions(methodParameterMdcs)
                .parameterDefinitions(parameterMdcs)
                .build();
    }
}