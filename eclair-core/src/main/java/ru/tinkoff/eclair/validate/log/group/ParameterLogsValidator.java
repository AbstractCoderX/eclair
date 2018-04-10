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

package ru.tinkoff.eclair.validate.log.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.validate.log.single.ParameterLogValidator;

import java.util.Map;

/**
 * @author Vyacheslav Klapatnyuk
 */
@Component
public class ParameterLogsValidator extends LoggerSpecificLogAnnotationsValidator {

    private final ParameterLogValidator parameterLogValidator;

    @Autowired
    public ParameterLogsValidator(GenericApplicationContext applicationContext,
                                  Map<String, EclairLogger> loggers,
                                  ParameterLogValidator parameterLogValidator) {
        super(applicationContext, loggers);
        this.parameterLogValidator = parameterLogValidator;
    }

    @Override
    public void validate(Object target, Errors errors) {
        super.validate(target, errors);
        ((Iterable<?>) target).forEach(log -> parameterLogValidator.validate(log, errors));
    }
}