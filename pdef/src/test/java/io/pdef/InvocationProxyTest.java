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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

public class InvocationProxyTest {
	@Mock Invoker invoker;

	@Before
	public void setUp() throws Exception {
		initMocks(this);
	}

	@Test
	public void testInvoke_handle() throws Throwable {
		PdefTestInterface iface = createProxy();
		when(invoker.invoke(any(Invocation.class))).thenReturn(3);

		Object result = iface.method(1, 2);
		assertEquals(3, result);
	}

	@Test(expected = PdefTestException.class)
	public void testInvoke_handleExc() throws Exception {
		PdefTestInterface iface = createProxy();
		when(invoker.invoke(any(Invocation.class))).thenThrow(new PdefTestException());

		iface.exc0();
	}

	@Test
	public void testInvoke_capture() throws Exception {
		PdefTestInterface iface = createProxy();
		ArgumentCaptor<Invocation> captor = ArgumentCaptor.forClass(Invocation.class);
		when(invoker.invoke(any(Invocation.class))).thenReturn(null);

		iface.method(1, 2);
		verify(invoker).invoke(captor.capture());

		Invocation invocation = captor.getValue();
		MethodDescriptor<PdefTestInterface, ?> method = PdefTestInterface.DESCRIPTOR.getMethod(
				"method");
		assertEquals(method, invocation.getMethod());
		assertArrayEquals(new Object[]{1, 2}, invocation.getArgs());
	}

	@Test
	public void testInvoke_captureChain() throws Exception {
		PdefTestInterface iface = createProxy();
		ArgumentCaptor<Invocation> captor = ArgumentCaptor.forClass(Invocation.class);
		when(invoker.invoke(any(Invocation.class))).thenReturn(null);

		iface.interface0(1, 2).method(3, 4);
		verify(invoker).invoke(captor.capture());

		List<Invocation> chain = captor.getValue().toChain();
		assertEquals(2, chain.size());

		Invocation invocation0 = chain.get(0);
		Invocation invocation1 = chain.get(1);
		assertArrayEquals(new Object[]{1, 2}, invocation0.getArgs());
		assertArrayEquals(new Object[]{3, 4}, invocation1.getArgs());
	}

	private PdefTestInterface createProxy() {
		return InvocationProxy.create(PdefTestInterface.DESCRIPTOR, invoker);
	}
}
