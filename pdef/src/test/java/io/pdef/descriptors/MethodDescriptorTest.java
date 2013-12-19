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

package io.pdef.descriptors;

import io.pdef.test.interfaces.PdefTestInterface;
import org.junit.Test;
import io.pdef.test.interfaces.PdefTestException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MethodDescriptorTest {
	@Test
	public void testGetName() throws Exception {
		MethodDescriptor<?, ?> method = method();
		assertNotNull(method);
		assertEquals("method", method.getName());
	}

	@Test
	public void testIndexPostTerminal() throws Exception {
		MethodDescriptor<?, ?> index = method();
		MethodDescriptor<?, ?> query = PdefTestInterface.DESCRIPTOR.getMethod("query");
		MethodDescriptor<?, ?> post = PdefTestInterface.DESCRIPTOR.getMethod("post");
		MethodDescriptor<?, ?> iface = PdefTestInterface.DESCRIPTOR.getMethod("interface0");

		assertTrue(index.isTerminal());
		assertFalse(index.isPost());

		assertTrue(query.isTerminal());
		assertFalse(query.isPost());

		assertTrue(post.isTerminal());
		assertTrue(post.isPost());

		assertFalse(iface.isTerminal());
		assertFalse(iface.isPost());
	}

	@Test
	public void testInvoke() throws Exception {
		MethodDescriptor<? super PdefTestInterface, ?> method = method();
		assert method != null;

		PdefTestInterface object = mock(PdefTestInterface.class);
		method.invoke(object, new Object[] {1, 2});
		verify(object).method(1, 2);
	}

	@Test(expected = PdefTestException.class)
	public void testInvoke_exception() throws Exception {
		MethodDescriptor<? super PdefTestInterface, ?> method = PdefTestInterface.DESCRIPTOR
				.getMethod("exc0");
		assert method != null;

		PdefTestInterface object = mock(PdefTestInterface.class);
		doThrow(new PdefTestException()).when(object).exc0();

		method.invoke(object, null);
	}

	private MethodDescriptor<? super PdefTestInterface, ?> method() {
		return PdefTestInterface.DESCRIPTOR.getMethod("method");
	}
}
