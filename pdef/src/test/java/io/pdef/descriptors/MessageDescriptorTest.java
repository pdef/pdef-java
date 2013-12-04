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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.pdef.test.inheritance.*;
import io.pdef.test.messages.PdefTestComplexMessage;
import io.pdef.test.messages.PdefTestMessage;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;

public class MessageDescriptorTest {
	@Test
	public void test() throws Exception {
		MessageDescriptor<PdefTestMessage> descriptor = PdefTestMessage.DESCRIPTOR;

		assertEquals(PdefTestMessage.class, descriptor.getJavaClass());
		assertNull(descriptor.getBase());
		assertNull(descriptor.getDiscriminator());
		assertNull(descriptor.getDiscriminatorValue());
		assertEquals(3, descriptor.getFields().size());
		assertEquals(0, descriptor.getSubtypes().size());
	}

	@Test
	public void test_nonpolymorphicInheritance() throws Exception {
		MessageDescriptor<PdefTestMessage> base = PdefTestMessage.DESCRIPTOR;
		MessageDescriptor<PdefTestComplexMessage> message = PdefTestComplexMessage.DESCRIPTOR;

		assertEquals(PdefTestComplexMessage.class, message.getJavaClass());
		assertEquals(base, message.getBase());

		List<FieldDescriptor<? super PdefTestComplexMessage, ?>> fields = Lists.newArrayList();
		fields.addAll(base.getFields());
		fields.addAll(message.getDeclaredFields());
		assertEquals(fields, message.getFields());
		assertEquals(0, message.getSubtypes().size());
	}

	@Test
	public void test__polymorphicInheritance() throws Exception {
		MessageDescriptor<PdefBase> base = PdefBase.DESCRIPTOR;
		MessageDescriptor<PdefSubtype> subtype = PdefSubtype.DESCRIPTOR;
		MessageDescriptor<PdefSubtype2> subtype2 = PdefSubtype2.DESCRIPTOR;
		MessageDescriptor<PdefMultiLevelSubtype> msubtype = PdefMultiLevelSubtype.DESCRIPTOR;
		FieldDescriptor<? super PdefBase, ?> discriminator = base.getField("type");

		assertNull(base.getBase());
		assertEquals(base, subtype.getBase());
		assertEquals(base, subtype2.getBase());
		assertEquals(subtype, msubtype.getBase());

		assertEquals(discriminator, base.getDiscriminator());
		assertEquals(discriminator, subtype.getDiscriminator());
		assertEquals(discriminator, subtype2.getDiscriminator());
		assertEquals(discriminator, msubtype.getDiscriminator());

		assertNull(base.getDiscriminatorValue());
		assertEquals(PdefPolymorphicType.SUBTYPE, subtype.getDiscriminatorValue());
		assertEquals(PdefPolymorphicType.SUBTYPE2, subtype2.getDiscriminatorValue());
		assertEquals(PdefPolymorphicType.MULTILEVEL_SUBTYPE, msubtype.getDiscriminatorValue());

		assertEquals(ImmutableSet.of(subtype, subtype2, msubtype), base.getSubtypes());
		assertEquals(ImmutableSet.of(msubtype), subtype.getSubtypes());
		assertTrue(subtype2.getSubtypes().isEmpty());
		assertTrue(msubtype.getSubtypes().isEmpty());

		assertNull(base.getSubtype(null));
		assertEquals(subtype, base.getSubtype(PdefPolymorphicType.SUBTYPE));
		assertEquals(subtype2, base.getSubtype(PdefPolymorphicType.SUBTYPE2));
		assertEquals(msubtype, base.getSubtype(PdefPolymorphicType.MULTILEVEL_SUBTYPE));
		assertEquals(msubtype, subtype.getSubtype(PdefPolymorphicType.MULTILEVEL_SUBTYPE));
	}
}
