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

package io.pdef;

import io.pdef.descriptors.MethodDescriptor;
import io.pdef.test.interfaces.PdefTestException;
import io.pdef.test.interfaces.PdefTestInterface;
import io.pdef.test.messages.PdefTestMessage;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.util.List;

public class InvocationTest {
	@Test
	public void testConstructor() throws Exception {
		Invocation invocation = Invocation.root(method(), new Object[]{1, 2});
		assertArrayEquals(new Object[]{1, 2}, invocation.getArgs());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_wrongMethodArgs() throws Exception {
		Invocation.root(method(), new Object[]{1, 2, 3, 4});
	}

	@Test
	public void testConstructor_copyArgs() throws Exception {
		PdefTestMessage message = new PdefTestMessage()
				.setBool0(true)
				.setString0("hello")
				.setInt0(-16);
		Invocation invocation = Invocation.root(messageMethod(), new Object[]{message});

		assertEquals(message, invocation.getArgs()[0]);
		assertTrue(message != invocation.getArgs()[0]);
	}

	@Test
	public void testConstructor_nullPrimitivesToDefaults() throws Exception {
		Invocation invocation = Invocation.root(method(), new Object[]{null, null});
		assertArrayEquals(new Object[]{0, 0}, invocation.getArgs());
	}

	@Test
	public void testGetMethod() throws Exception {
		Invocation invocation = Invocation.root(method(),
				new Object[]{1, 2});
		assertEquals(method(), invocation.getMethod());
	}

	@Test
	public void testToChain() throws Exception {
		List<Invocation> chain = Invocation
				.root(interfaceMethod(), new Object[]{1, 2})
				.next(stringMethod(), new Object[]{"hello"})
				.toChain();

		assertEquals(2, chain.size());
	}

	@Test
	public void testInvoke() throws Exception {
		PdefTestInterface iface = mock(PdefTestInterface.class);
		when(iface.method(1, 2)).thenReturn(3);

		Invocation invocation = Invocation.root(method(), new Object[]{1, 2});
		Object result = invocation.invoke(iface);
		assertEquals(3, result);
	}

	@Test
	public void testInvoke_chained() throws Exception {
		PdefTestInterface iface = mock(PdefTestInterface.class, RETURNS_DEEP_STUBS);
		when(iface.interface0(1, 2).string0("world")).thenReturn("goodbye");

		Invocation invocation = Invocation
				.root(interfaceMethod(), new Object[]{1, 2})
				.next(stringMethod(), new Object[]{"world"});

		Object result = invocation.invoke(iface);
		assertEquals("goodbye", result);
	}

	@Test(expected = PdefTestException.class)
	public void testInvoke_exc() throws Exception {
		PdefTestInterface iface = mock(PdefTestInterface.class);
		doThrow(new PdefTestException()).when(iface).exc0();
		Invocation invocation = Invocation.root(excMethod(), new Object[]{});

		invocation.invoke(iface);
	}

	private MethodDescriptor<?, ?> method() {
		return PdefTestInterface.DESCRIPTOR.getMethod("method");
	}

	private MethodDescriptor<?, ?> messageMethod() {
		return PdefTestInterface.DESCRIPTOR.getMethod("message0");
	}

	private MethodDescriptor<?, ?> interfaceMethod() {
		return PdefTestInterface.DESCRIPTOR.getMethod("interface0");
	}

	private MethodDescriptor<?, ?> stringMethod() {
		return PdefTestInterface.DESCRIPTOR.getMethod("string0");
	}

	private MethodDescriptor<?, ?> excMethod() {
		return PdefTestInterface.DESCRIPTOR.getMethod("exc0");
	}
}
