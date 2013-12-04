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

package io.pdef.json;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.pdef.descriptors.DataTypeDescriptor;
import io.pdef.descriptors.Descriptors;
import io.pdef.test.inheritance.PdefBase;
import io.pdef.test.inheritance.PdefMultiLevelSubtype;
import io.pdef.test.inheritance.PdefSubtype;
import io.pdef.test.inheritance.PdefSubtype2;
import io.pdef.test.messages.PdefTestComplexMessage;
import io.pdef.test.messages.PdefTestEnum;
import io.pdef.test.messages.PdefTestMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import java.util.Date;

public class JsonFormatTest {
	private JsonFormat format = JsonFormat.instance();

	private <T> void test(final DataTypeDescriptor<T> descriptor, final T parsed,
			final String serialized) {
		assertEquals(serialized, format.write(parsed, descriptor, false));
		assertEquals(parsed, format.read(serialized, descriptor));

		// Nulls.
		assertEquals("null", format.write(null, descriptor, false));
		assertNull(format.read((String) null, descriptor));
		assertNull(format.read("null", descriptor));
	}

	@Test
	public void testBoolean() throws Exception {
		test(Descriptors.bool, Boolean.TRUE, "true");
		test(Descriptors.bool, Boolean.FALSE, "false");
	}

	@Test
	public void testInt16() throws Exception {
		test(Descriptors.int16, (short) -16, "-16");
	}

	@Test
	public void testInt32() throws Exception {
		test(Descriptors.int32, -32, "-32");
	}

	@Test
	public void testInt64() throws Exception {
		test(Descriptors.int64, Long.MIN_VALUE, String.valueOf(Long.MIN_VALUE));
	}

	@Test
	public void testFloat() throws Exception {
		test(Descriptors.float0, -1.5f, "-1.5");
	}

	@Test
	public void testDouble() throws Exception {
		test(Descriptors.double0, -2.5, "-2.5");
	}

	@Test
	public void testString() throws Exception {
		test(Descriptors.string, "привет", "\"привет\"");
	}

	@Test
	public void testDatetime() throws Exception {
		test(Descriptors.datetime, new Date(0), "\"1970-01-01T00:00:00Z\"");
	}

	@Test
	public void testEnum() throws Exception {
		test(PdefTestEnum.DESCRIPTOR, PdefTestEnum.THREE, "\"three\"");
		assertEquals(PdefTestEnum.TWO, format.read("\"tWo\"", PdefTestEnum.DESCRIPTOR));
	}

	@Test
	public void testMessage() throws Exception {
		test(PdefTestComplexMessage.DESCRIPTOR, createComplexMessage(), MESSAGE_JSON);
	}

	@Test
	public void testVoid() throws Exception {
		test(Descriptors.void0, null, "null");
	}

	@Test
	public void testPolymorphicMessage() throws Exception {
		PdefBase base = new PdefBase().setField("field");
		PdefSubtype subtype = new PdefSubtype().setField("field").setSubfield("subfield");
		PdefSubtype2 subtype2 = new PdefSubtype2().setField("field").setSubfield2("subfield2");
		PdefMultiLevelSubtype msubtype = new PdefMultiLevelSubtype()
				.setField("field")
				.setSubfield("subfield")
				.setMfield("mfield");

		test(PdefBase.DESCRIPTOR, base, "{\"field\":\"field\"}");
		test(PdefBase.DESCRIPTOR, subtype,
				"{\"type\":\"subtype\",\"field\":\"field\",\"subfield\":\"subfield\"}");
		test(PdefBase.DESCRIPTOR, subtype2,
				"{\"type\":\"subtype2\",\"field\":\"field\",\"subfield2\":\"subfield2\"}");
		test(PdefBase.DESCRIPTOR, msubtype,
				"{\"type\":\"multilevel_subtype\",\"field\":\"field\",\"subfield\":\"subfield\","
						+ "\"mfield\":\"mfield\"}");
	}

	private PdefTestComplexMessage createComplexMessage() {
		return new PdefTestComplexMessage()
				.setEnum0(PdefTestEnum.THREE)
				.setBool0(true)
				.setShort0((short) 16)
				.setInt0(32)
				.setLong0(64L)
				.setFloat0(1.5f)
				.setDouble0(2.5d)
				.setString0("hello")
				.setList0(ImmutableList.of(1, 2))
				.setSet0(ImmutableSet.of(1, 2))
				.setMap0(ImmutableMap.<Integer, Float>of(1, 1.5f))
				.setMessage0(new PdefTestMessage()
						.setBool0(true)
						.setInt0(16)
						.setString0("hello"))
				.setPolymorphic(new PdefMultiLevelSubtype()
						.setField("field")
						.setSubfield("subfield")
						.setMfield("mfield"));
	}

	private final String MESSAGE_JSON = "{"
			+ "\"string0\":\"hello\","
			+ "\"bool0\":true,"
			+ "\"int0\":32,"
			+ "\"short0\":16,"
			+ "\"long0\":64,"
			+ "\"float0\":1.5,"
			+ "\"double0\":2.5,"
			+ "\"list0\":[1,2],"
			+ "\"set0\":[1,2],"
			+ "\"map0\":{\"1\":1.5},"
			+ "\"enum0\":\"three\","
			+ "\"message0\":{\"string0\":\"hello\",\"bool0\":true,\"int0\":16},"
			+ "\"polymorphic\":{\"type\":\"multilevel_subtype\",\"field\":\"field\","
			+ "\"subfield\":\"subfield\",\"mfield\":\"mfield\"}}";
}
