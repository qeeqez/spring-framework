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

package org.springframework.web.servlet.mvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.ui.ModelMap;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.PathPatternsParameterizedTest;
import org.springframework.web.servlet.handler.PathPatternsTestUtils;
import org.springframework.web.testfixture.servlet.MockHttpServletRequest;
import org.springframework.web.testfixture.servlet.MockHttpServletResponse;
import org.springframework.web.util.ServletRequestPathUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 14.09.2005
 */
class UrlFilenameViewControllerTests {

	private static Stream<Named<Function<String, MockHttpServletRequest>>> pathPatternsArguments() {
		return PathPatternsTestUtils.requestArguments();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@PathPatternsParameterizedTest
	@MethodSource("org.springframework.web.servlet.mvc.UrlFilenameViewControllerTests#pathPatternsArguments")
	private @interface PathPatternsParameterizedArgumentsTest {
	}


	@PathPatternsParameterizedArgumentsTest
	void withPlainFilename(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		MockHttpServletRequest request = requestFactory.apply("/index");
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("index");
		assertThat(mv.getModel()).isEmpty();
	}

	@PathPatternsParameterizedArgumentsTest
	void withFilenamePlusExtension(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		MockHttpServletRequest request = requestFactory.apply("/index.html");
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("index");
		assertThat(mv.getModel()).isEmpty();
	}

	@PathPatternsParameterizedArgumentsTest
	void withFilenameAndMatrixVariables(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		MockHttpServletRequest request = requestFactory.apply("/index;a=A;b=B");
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("index");
		assertThat(mv.getModel()).isEmpty();
	}

	@PathPatternsParameterizedArgumentsTest
	void withPrefixAndSuffix(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		controller.setPrefix("mypre_");
		controller.setSuffix("_mysuf");
		MockHttpServletRequest request = requestFactory.apply("/index.html");
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("mypre_index_mysuf");
		assertThat(mv.getModel()).isEmpty();
	}

	@PathPatternsParameterizedArgumentsTest
	void withPrefix(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		controller.setPrefix("mypre_");
		MockHttpServletRequest request = requestFactory.apply("/index.html");
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("mypre_index");
		assertThat(mv.getModel()).isEmpty();
	}

	@PathPatternsParameterizedArgumentsTest
	void withSuffix(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		controller.setSuffix("_mysuf");
		MockHttpServletRequest request = requestFactory.apply("/index.html");
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("index_mysuf");
		assertThat(mv.getModel()).isEmpty();
	}

	@PathPatternsParameterizedArgumentsTest
	void multiLevel(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		MockHttpServletRequest request = requestFactory.apply("/docs/cvs/commit.html");
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("docs/cvs/commit");
		assertThat(mv.getModel()).isEmpty();
	}

	@PathPatternsParameterizedArgumentsTest
	void multiLevelWithMapping(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		MockHttpServletRequest request = requestFactory.apply("/docs/cvs/commit.html");
		exposePathInMapping(request, "/docs/**");
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("cvs/commit");
		assertThat(mv.getModel()).isEmpty();
	}

	@PathPatternsParameterizedArgumentsTest
	void multiLevelMappingWithFallback(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		MockHttpServletRequest request = requestFactory.apply("/docs/cvs/commit.html");
		exposePathInMapping(request, "/docs/cvs/commit.html");
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("docs/cvs/commit");
		assertThat(mv.getModel()).isEmpty();
	}

	@PathPatternsParameterizedArgumentsTest
	void withContextMapping(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myapp/docs/cvs/commit.html");
		request.setContextPath("/myapp");
		ServletRequestPathUtils.parseAndCache(request);
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("docs/cvs/commit");
		assertThat(mv.getModel()).isEmpty();
	}

	@Test
	void settingPrefixToNullCausesEmptyStringToBeUsed() {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		controller.setPrefix(null);
		assertThat(controller.getPrefix())
				.as("For setPrefix(..) with null, the empty string must be used instead.")
				.isNotNull();
		assertThat(controller.getPrefix())
				.as("For setPrefix(..) with null, the empty string must be used instead.").isEmpty();
	}

	@Test
	void settingSuffixToNullCausesEmptyStringToBeUsed() {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		controller.setSuffix(null);
		assertThat(controller.getSuffix())
				.as("For setPrefix(..) with null, the empty string must be used instead.")
				.isNotNull();
		assertThat(controller.getSuffix())
				.as("For setPrefix(..) with null, the empty string must be used instead.").isEmpty();
	}

	/**
	 * This is the expected behavior, and it now has a test to prove it.
	 * https://opensource.atlassian.com/projects/spring/browse/SPR-2789
	 */
	@PathPatternsParameterizedArgumentsTest
	void nestedPathisUsedAsViewName_InBreakingChangeFromSpring12Line(
			Function<String, MockHttpServletRequest> requestFactory) throws Exception {

		UrlFilenameViewController controller = new UrlFilenameViewController();
		MockHttpServletRequest request = requestFactory.apply("/products/view.html");
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("products/view");
		assertThat(mv.getModel()).isEmpty();
	}

	@PathPatternsParameterizedArgumentsTest
	void withFlashAttributes(Function<String, MockHttpServletRequest> requestFactory) throws Exception {
		UrlFilenameViewController controller = new UrlFilenameViewController();
		MockHttpServletRequest request = requestFactory.apply("/index");
		request.setAttribute(DispatcherServlet.INPUT_FLASH_MAP_ATTRIBUTE, new ModelMap("name", "value"));
		ModelAndView mv = controller.handleRequest(request, new MockHttpServletResponse());
		assertThat(mv.getViewName()).isEqualTo("index");
		assertThat(mv.getModel()).hasSize(1);
		assertThat(mv.getModel().get("name")).isEqualTo("value");
	}

	private void exposePathInMapping(MockHttpServletRequest request, String mapping) {
		String pathInMapping = new AntPathMatcher().extractPathWithinPattern(mapping, request.getRequestURI());
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, pathInMapping);
	}

}
