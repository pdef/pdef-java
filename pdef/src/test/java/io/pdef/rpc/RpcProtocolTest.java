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
import com.google.common.util.concurrent.Atomics;
import io.pdef.Invocation;
import io.pdef.InvocationProxy;
import io.pdef.Invoker;
import io.pdef.descriptors.Descriptors;
import io.pdef.descriptors.MethodDescriptor;
import io.pdef.test.interfaces.PdefTestInterface;
import io.pdef.test.messages.PdefTestEnum;
import io.pdef.test.messages.PdefTestMessage;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RpcProtocolTest {
	private RpcProtocol protocol = new RpcProtocol();

	// GetRequest.

	@Test
	public void testGetRequest() throws Exception {
		AtomicReference<Invocation> ref = Atomics.newReference();
		proxy(ref).method(1, 2);

		RpcRequest request = protocol.getRequest(ref.get());
		assertEquals(RpcRequest.GET, request.getMethod());
		assertEquals("/method/1/2", request.getRelativePath());
		assertTrue(request.getQuery().isEmpty());
		assertTrue(request.getPost().isEmpty());
	}

	@Test
	public void testGetRequest_forbidNullPathArguments() throws Exception {
		AtomicReference<Invocation> ref = Atomics.newReference();
		proxy(ref).string0(null);

		try {
			protocol.getRequest(ref.get());
			fail();
		} catch (NullPointerException e) {
			assertTrue(e.getMessage().contains("Path method argument"));
		}
	}

	@Test
	public void testGetRequest_query() throws Exception {
		AtomicReference<Invocation> ref = Atomics.newReference();
		proxy(ref).query(1, 2);

		RpcRequest request = protocol.getRequest(ref.get());
		assertEquals("/query", request.getRelativePath());
		assertEquals(ImmutableMap.of("arg0", "1", "arg1", "2"), request.getQuery());
		assertTrue(request.getPost().isEmpty());
	}

	@Test
	public void testGetRequest_post() throws Exception {
		AtomicReference<Invocation> ref = Atomics.newReference();
		proxy(ref).post(1, 2);

		RpcRequest request = protocol.getRequest(ref.get());
		assertEquals(RpcRequest.POST, request.getMethod());
		assertEquals("/post", request.getRelativePath());
		assertTrue(request.getQuery().isEmpty());
		assertEquals(ImmutableMap.of("arg0", "1", "arg1", "2"), request.getPost());
	}

	@Test
	public void testGetRequest_chainedMethods() throws Exception {
		AtomicReference<Invocation> ref = Atomics.newReference();
		proxy(ref).interface0(1, 2).method(3, 4);

		RpcRequest request = protocol.getRequest(ref.get());
		assertEquals(RpcRequest.GET, request.getMethod());
		assertEquals("/interface0/1/2/method/3/4", request.getRelativePath());
		assertTrue(request.getQuery().isEmpty());
		assertTrue(request.getPost().isEmpty());
	}

	@Test
	public void testGetRequest_urlencodePathArgs() throws Exception {
		AtomicReference<Invocation> ref = Atomics.newReference();
		proxy(ref).string0("привет");

		RpcRequest request = protocol.getRequest(ref.get());
		assertEquals("/string0/%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82", request.getRelativePath());
	}

	@Test
	public void testGetRequest_urlencodePathArgsWithSlashes() throws Exception {
		AtomicReference<Invocation> ref = Atomics.newReference();
		proxy(ref).string0("Привет/мир");

		RpcRequest request = protocol.getRequest(ref.get());
		assertEquals("/string0/%D0%9F%D1%80%D0%B8%D0%B2%D0%B5%D1%82%2F%D0%BC%D0%B8%D1%80",
				request.getRelativePath());
	}

	// write.

	@Test
	public void testToJson_stringNoQuotes() throws Exception {
		String result = protocol.toJson(Descriptors.string, "Привет,\" мир!");
		assertEquals("Привет,\\\" мир!", result);
	}

	@Test
	public void testToJson_datetimeNoQuotes() throws Exception {
		String result = protocol.toJson(Descriptors.datetime, new Date(0));
		assertEquals("1970-01-01T00:00:00Z", result);
	}

	@Test
	public void testToJson_enumNoQuotes() throws Exception {
		String result = protocol.toJson(PdefTestEnum.DESCRIPTOR, PdefTestEnum.ONE);
		assertEquals("one", result);
	}

	// GetInvocation.

	@Test
	public void testGetInvocation() throws Exception {
		RpcRequest request = new RpcRequest().setRelativePath("/method/1/2/");

		Invocation invocation = protocol.getInvocation(request, PdefTestInterface.DESCRIPTOR);
		assertEquals("method", invocation.getMethod().getName());
		assertArrayEquals(new Object[]{1, 2}, invocation.getArgs());
	}

	@Test
	public void testGetInvocation_queryMethod() throws Exception {
		RpcRequest request = new RpcRequest()
				.setRelativePath("/query")
				.setQuery(ImmutableMap.of("arg0", "1"));

		Invocation invocation = protocol.getInvocation(request, PdefTestInterface.DESCRIPTOR);
		assertEquals(queryMethod(), invocation.getMethod());
		assertArrayEquals(new Object[]{1, null}, invocation.getArgs());
	}

	@Test
	public void testGetInvocation_postMethod() throws Exception {
		RpcRequest request = new RpcRequest()
				.setMethod(RpcRequest.POST)
				.setRelativePath("/post")
				.setPost(ImmutableMap.of("arg0", "1"));

		Invocation invocation = protocol.getInvocation(request, PdefTestInterface.DESCRIPTOR);
		assertEquals(postMethod(), invocation.getMethod());
		assertArrayEquals(new Object[]{1, null}, invocation.getArgs());
	}

	@Test(expected = RpcException.class)
	public void testGetInvocation_postMethod_getNotAllowed() throws Exception {
		RpcRequest request = new RpcRequest().setRelativePath("/post");

		protocol.getInvocation(request, PdefTestInterface.DESCRIPTOR);
	}

	@Test
	public void testGetInvocation_chainedMethods() throws Exception {
		RpcRequest request = new RpcRequest().setRelativePath("/interface0/1/2/query/")
				.setQuery(ImmutableMap.of("arg0", "3", "arg1", "4"));

		List<Invocation> chain = protocol.getInvocation(request, PdefTestInterface.DESCRIPTOR).toChain();
		assertEquals(2, chain.size());

		Invocation invocation0 = chain.get(0);
		assertEquals(interfaceMethod(), invocation0.getMethod());
		assertArrayEquals(new Object[]{1, 2}, invocation0.getArgs());

		Invocation invocation1 = chain.get(1);
		assertEquals(queryMethod(), invocation1.getMethod());
		assertArrayEquals(new Object[]{3, 4}, invocation1.getArgs());
	}

	@Test(expected = RpcException.class)
	public void testGetInvocation_lastMethodNotTerminal() throws Exception {
		RpcRequest request = new RpcRequest().setRelativePath("/interface0/1/2");

		protocol.getInvocation(request, PdefTestInterface.DESCRIPTOR);
	}

	@Test
	public void testGetInvocation_urldecodePathArgs() throws Exception {
		RpcRequest request = new RpcRequest()
				.setRelativePath("/string0/%D0%9F%D1%80%D0%B8%D0%B2%D0%B5%D1%82");

		Invocation invocation = protocol.getInvocation(request, PdefTestInterface.DESCRIPTOR);
		assertEquals(stringMethod(), invocation.getMethod());
		assertArrayEquals(new Object[]{"Привет"}, invocation.getArgs());
	}

	@Test
	public void testGetInvocation_urldecodePathArgsWithSlashes() throws Exception {
		RpcRequest request = new RpcRequest()
				.setRelativePath(
						"/string0/%D0%9F%D1%80%D0%B8%D0%B2%D0%B5%D1%82%2F%D0%BC%D0%B8%D1%80");

		Invocation invocation = protocol.getInvocation(request, PdefTestInterface.DESCRIPTOR);
		assertEquals(stringMethod(), invocation.getMethod());
		assertArrayEquals(new Object[]{"Привет/мир"}, invocation.getArgs());
	}

	// read

	@Test
	public void testFromJson() throws Exception {
		PdefTestMessage expected = new PdefTestMessage()
				.setString0("Привет")
				.setBool0(true)
				.setInt0(123);
		String json = expected.toJson();
		PdefTestMessage result = protocol.fromJson(PdefTestMessage.DESCRIPTOR, json);
		assertEquals(expected, result);
	}

	@Test
	public void testFromJson_unquotedString() throws Exception {
		String result = protocol.fromJson(Descriptors.string, "Привет");
		assertEquals("Привет", result);
	}

	private PdefTestInterface proxy(final AtomicReference<Invocation> ref) {
		return proxy(new Invoker() {
			@Override
			public Object invoke(final Invocation invocation) {
				ref.set(invocation);
				return null;
			}
		});
	}

	private PdefTestInterface proxy(final Invoker handler) {
		return InvocationProxy.create(PdefTestInterface.DESCRIPTOR, handler);
	}

	private MethodDescriptor<?, ?> method() {
		return PdefTestInterface.DESCRIPTOR.getMethod("method");
	}

	private MethodDescriptor<?, ?> queryMethod() {
		return PdefTestInterface.DESCRIPTOR.getMethod("query");
	}

	private MethodDescriptor<?, ?> postMethod() {
		return PdefTestInterface.DESCRIPTOR.getMethod("post");
	}

	private MethodDescriptor<?, ?> stringMethod() {
		return PdefTestInterface.DESCRIPTOR.getMethod("string0");
	}

	private MethodDescriptor<?, ?> interfaceMethod() {
		return PdefTestInterface.DESCRIPTOR.getMethod("interface0");
	}
}
