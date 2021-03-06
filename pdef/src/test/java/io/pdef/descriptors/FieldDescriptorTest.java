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

import io.pdef.test.inheritance.PdefBase;
import io.pdef.test.inheritance.PdefPolymorphicType;
import io.pdef.test.messages.PdefTestMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class FieldDescriptorTest {
	@Test
	public void test() throws Exception {
		FieldDescriptor<?, ?> bool0 = PdefTestMessage.DESCRIPTOR.getField("bool0");
		FieldDescriptor<?, ?> string0 = PdefTestMessage.DESCRIPTOR.getField("string0");

		assertEquals("bool0", bool0.getName());
		assertEquals(Descriptors.bool, bool0.getType());
		assertFalse(bool0.isDiscriminator());

		assertEquals("string0", string0.getName());
		assertEquals(Descriptors.string, string0.getType());
		assertFalse(string0.isDiscriminator());
	}

	@Test
	public void testDiscriminator() throws Exception {
		FieldDescriptor<?, ?> field = PdefBase.DESCRIPTOR.getField("type");

		assertEquals("type", field.getName());
		assertEquals(PdefPolymorphicType.DESCRIPTOR, field.getType());
		assertTrue(field.isDiscriminator());
	}

	@Test
	public void testGetSet() throws Exception {
		PdefTestMessage msg = new PdefTestMessage();
		FieldDescriptor<? super PdefTestMessage, String> field = stringField();

		field.set(msg, "Hello, world");
		String s = field.get(msg);

		assertEquals("Hello, world", s);
	}

	@SuppressWarnings("unchecked")
	private FieldDescriptor<? super PdefTestMessage, String> stringField() {
		return (FieldDescriptor<? super PdefTestMessage, String>)
				PdefTestMessage.DESCRIPTOR.getField("string0");
	}
}
