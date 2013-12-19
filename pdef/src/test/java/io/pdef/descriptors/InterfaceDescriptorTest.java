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
import io.pdef.test.interfaces.PdefTestException;
import io.pdef.test.interfaces.PdefTestSubException;
import io.pdef.test.interfaces.PdefTestSubInterface;
import static org.junit.Assert.*;
import org.junit.Test;

public class InterfaceDescriptorTest {
	@Test
	public void test() throws Exception {
		InterfaceDescriptor<PdefTestInterface> descriptor = PdefTestInterface.DESCRIPTOR;
		assertEquals(PdefTestInterface.class, descriptor.getJavaClass());
		assertEquals(PdefTestException.DESCRIPTOR, descriptor.getExc());
		assertEquals(12, descriptor.getMethods().size());
	}

	@Test
	public void testInheritance() throws Exception {
		InterfaceDescriptor<PdefTestSubInterface> descriptor = PdefTestSubInterface.DESCRIPTOR;
		assertTrue(descriptor.getBase() == PdefTestInterface.DESCRIPTOR);
		assertTrue(descriptor.getExc() == PdefTestSubException.DESCRIPTOR);
		assertEquals(1, descriptor.getDeclaredMethods().size());
		assertEquals(13, descriptor.getMethods().size());
	}

	@Test
	public void testFindDescriptor() throws Exception {
		InterfaceDescriptor descriptor = Descriptors.findInterfaceDescriptor(
				PdefTestInterface.class);
		assertTrue(descriptor == PdefTestInterface.DESCRIPTOR);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindDescriptor_notFound() throws Exception {
		Descriptors.findInterfaceDescriptor(Runnable.class);
	}
}
