/*
 * Copyright: 2013 Pdef <http://pdef.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pdef.rpc;

import com.google.common.collect.ImmutableMap;
import io.pdef.descriptors.Descriptors;
import io.pdef.test.interfaces.PdefTestException;
import io.pdef.test.interfaces.PdefTestInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;

public class RpcServletTest {
	@Mock RpcHandler<PdefTestInterface> handler;
	RpcServlet<PdefTestInterface> servlet;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		servlet = new RpcServlet<PdefTestInterface>(handler);
	}

	@Test
	public void testGetRpcRequest() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn(RpcRequest.GET);
		when(request.getContextPath()).thenReturn("/context");
		when(request.getServletPath()).thenReturn("/app");
		when(request.getPathInfo()).thenReturn("/method1/method2");
		when(request.getRequestURI()).thenReturn("/context/app/method1/method2");
		when(request.getParameterMap()).thenReturn(ImmutableMap.of(
				"key0", new String[]{"value0"},
				"key1", new String[]{"value1", "value11"}));

		RpcRequest req = servlet.getRpcRequest(request);
		assertEquals(RpcRequest.GET, req.getMethod());
		assertEquals("/method1/method2", req.getRelativePath());
		assertEquals(ImmutableMap.of("key0", "value0", "key1", "value1"), req.getQuery());
		assertEquals(ImmutableMap.of("key0", "value0", "key1", "value1"), req.getPost());
	}

	@Test
	public void testWriteResult_ok() throws Exception {
		RpcResult<String, Void> result = new RpcResult<String, Void>(Descriptors.string)
				.setSuccess(true)
				.setData("Привет");
		HttpServletResponse response = mockResponse();
		servlet.writeResult(result, response);

		verify(response).setStatus(HttpURLConnection.HTTP_OK);
		verify(response).setContentType(RpcServlet.JSON_CONTENT_TYPE);
	}

	@Test
	public void testWriteResult_applicationException() throws Exception {
		PdefTestException e = new PdefTestException().setText("Привет");
		RpcResult<Void, PdefTestException> result =
				new RpcResult<Void, PdefTestException>(Descriptors.void0, PdefTestException.DESCRIPTOR)
						.setSuccess(false)
						.setError(e);
		HttpServletResponse response = mockResponse();
		servlet.writeResult(result, response);

		verify(response).setStatus(RpcServlet.APPLICATION_EXC_STATUS);
		verify(response).setContentType(RpcServlet.JSON_CONTENT_TYPE);
	}

	@Test
	public void testWriteRpcException() throws Exception {
		RpcException exception = RpcException.badRequest("Method not found");
		HttpServletResponse response = mockResponse();
		servlet.writeRpcException(exception, response);

		verify(response).setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
		verify(response).setContentType(RpcServlet.TEXT_CONTENT_TYPE);
	}

	@Test
	public void testGetRelativePath() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn("/method/1/2");
		when(request.getContextPath()).thenReturn("/context");
		when(request.getServletPath()).thenReturn("/app");
		when(request.getRequestURI()).thenReturn("/context/app/method/1/2");

		String relativePath = servlet.getRelativePath(request);
		assertEquals("/method/1/2", relativePath);
	}

	@Test
	public void testGetRelativePath_noPathInfo() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getPathInfo()).thenReturn(null);
		when(request.getContextPath()).thenReturn("/context");
		when(request.getServletPath()).thenReturn("/method/1/2");
		when(request.getRequestURI()).thenReturn("/context/method/1/2");

		String relativePath = servlet.getRelativePath(request);
		assertEquals("/method/1/2", relativePath);
	}

	private HttpServletResponse mockResponse() {
		return mock(HttpServletResponse.class, RETURNS_DEEP_STUBS);
	}
}
