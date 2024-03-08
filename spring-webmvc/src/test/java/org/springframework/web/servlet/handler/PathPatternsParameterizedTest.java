/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Annotation for tests parameterized to use either
 * {@link org.springframework.web.util.pattern.PathPatternParser} or
 * {@link org.springframework.util.PathMatcher} for URL pattern matching.
 *
 * <p><b>Usage:</b> You need to create own annotation with arguments provided
 * by annotation of type {@link ArgumentsSource}
 * (for example {@link MethodSource})
 * <p><b>Note:</b> Do not auto-close arguments since {@code ConfigurableWebApplicationContext} implements
 * {@code AutoCloseable} and is shared between parameterized test invocations.
 *
 * @author Rossen Stoyanchev
 * @since 5.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
//
@ParameterizedTest(name = "[{index}] {0}", autoCloseArguments = false)
public @interface PathPatternsParameterizedTest {
}
