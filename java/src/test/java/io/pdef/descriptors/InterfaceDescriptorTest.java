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

import io.pdef.test.interfaces.TestException;
import io.pdef.test.interfaces.TestInterface;
import static org.junit.Assert.*;
import org.junit.Test;

public class InterfaceDescriptorTest {
	@Test
	public void test() throws Exception {
		InterfaceDescriptor<TestInterface> descriptor = TestInterface.DESCRIPTOR;
		assertEquals(TestInterface.class, descriptor.getJavaClass());
		assertEquals(TestException.DESCRIPTOR, descriptor.getExc());
		assertEquals(11, descriptor.getMethods().size());
	}

	@Test
	public void testFindDescriptor() throws Exception {
		InterfaceDescriptor descriptor = Descriptors.findInterfaceDescriptor(
				TestInterface.class);
		assertTrue(descriptor == TestInterface.DESCRIPTOR);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindDescriptor_notFound() throws Exception {
		Descriptors.findInterfaceDescriptor(Runnable.class);
	}
}
