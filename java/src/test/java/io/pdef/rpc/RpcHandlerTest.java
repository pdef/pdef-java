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

import io.pdef.descriptors.Descriptors;
import io.pdef.test.interfaces.PdefTestInterface;
import io.pdef.test.interfaces.PdefTestException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RpcHandlerTest {
	PdefTestInterface service;
	RpcHandler<PdefTestInterface> handler;

	@Before
	public void setUp() throws Exception {
		service = mock(PdefTestInterface.class);
		handler = new RpcHandler<PdefTestInterface>(PdefTestInterface.DESCRIPTOR, service);
	}

	@Test(expected = RpcException.class)
	public void testHandle_rpcException() throws Exception {
		RpcRequest request = new RpcRequest().setPath("/hello/world/wrong/path");
		handler.handle(request);
	}

	@Test
	public void testHandle_ok() throws Exception {
		when(service.method(1, 2)).thenReturn(3);
		RpcRequest request = getRequest();

		RpcResult<?, ?> result = handler.handle(request);
		assertTrue(result.isSuccess());
		assertEquals(3, result.getData());
		assertEquals(Descriptors.int32, result.getDataDescriptor());
	}

	@Test
	public void testHandle_applicationException() throws Exception {
		PdefTestException e = new PdefTestException().setText("Hello, world");
		when(service.method(1, 2)).thenThrow(e);
		RpcRequest request = getRequest();

		RpcResult<?, ?> result = handler.handle(request);
		assertFalse(result.isSuccess());
		assertEquals(e, result.getError());
		assertEquals(PdefTestException.DESCRIPTOR, result.getErrorDescriptor());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHandle_unexpectedException() throws Exception {
		when(service.method(1, 2)).thenThrow(new IllegalArgumentException());
		RpcRequest request = getRequest();

		handler.handle(request);
	}

	private RpcRequest getRequest() {
		return new RpcRequest().setPath("/method/1/2");
	}
}
