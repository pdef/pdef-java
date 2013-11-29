package io.pdef.json;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.pdef.descriptors.*;
import io.pdef.test.inheritance.Base;
import io.pdef.test.inheritance.MultiLevelSubtype;
import io.pdef.test.messages.TestEnum;
import io.pdef.test.messages.TestMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonObjectFormatTest {
	private JsonObjectFormat format = JsonObjectFormat.getInstance();

	private <T> void testPrimitive(final DataTypeDescriptor<T> descriptor, final T object,
			final String s) throws Exception {
		assertNull(format.read(null, descriptor));
		assertEquals(object, format.read(s, descriptor));
		assertEquals(object, format.read(object, descriptor));

		assertNull(format.write(null, descriptor));
		assertEquals(object, format.write(object, descriptor));
	}

	@Test
	public void testBool() throws Exception {
		testPrimitive(Descriptors.bool, true, "TRUE");
		testPrimitive(Descriptors.bool, false, "False");
	}

	@Test
	public void testInt16() throws Exception {
		testPrimitive(Descriptors.int16, (short) 16, "16");
	}

	@Test
	public void testInt32() throws Exception {
		testPrimitive(Descriptors.int32, 32, "32");
	}

	@Test
	public void testInt64() throws Exception {
		testPrimitive(Descriptors.int64, 64L, "64");
	}

	@Test
	public void testFloat() throws Exception {
		testPrimitive(Descriptors.float0, 1.5f, "1.5");
	}

	@Test
	public void testDouble() throws Exception {
		testPrimitive(Descriptors.double0, 2.5d, "2.5");
	}

	@Test
	public void testString() throws Exception {
		testPrimitive(Descriptors.string, "Hello, world", "Hello, world");
	}

	@Test
	public void testDatetime() throws Exception {
		testValue(Descriptors.datetime, new Date(0), "1970-01-01T00:00:00Z");
	}

	@Test
	public void testEnum() throws Exception {
		EnumDescriptor<TestEnum> descriptor = TestEnum.DESCRIPTOR;

		testValue(descriptor, TestEnum.TWO, "two");
		assertEquals(TestEnum.TWO, format.read("two", descriptor));
	}

	private <T> void testValue(final DataTypeDescriptor<T> descriptor, final T object,
			final Object serialized) throws Exception {
		assertEquals(object, format.read(serialized, descriptor));
		assertNull(format.read(null, descriptor));
		assertNull(format.write(null, descriptor));
		assertEquals(serialized, format.write(object, descriptor));
	}

	@Test
	public void testList() throws Exception {
		List<Map<String, Object>> serialized = Lists.newArrayList();
		serialized.add(fixtureMap());

		List<TestMessage> parsed = Lists.newArrayList();
		parsed.add(fixtureMessage());

		ListDescriptor<TestMessage> descriptor = Descriptors.list(TestMessage.DESCRIPTOR);
		testValue(descriptor, parsed, serialized);
	}

	@Test
	public void testSet() throws Exception {
		Set<Map<String, Object>> serialized = Sets.newHashSet();
		serialized.add(fixtureMap());

		Set<TestMessage> parsed = Sets.newHashSet();
		parsed.add(fixtureMessage());

		SetDescriptor<TestMessage> descriptor = Descriptors.set(TestMessage.DESCRIPTOR);
		testValue(descriptor, parsed, serialized);
	}

	@Test
	public void testMap() throws Exception {
		Map<Integer, TestMessage> object = Maps.newHashMap();
		object.put(123, fixtureMessage());

		Map<String, Map<String, Object>> serialized = Maps.newHashMap();
		serialized.put("123", fixtureMap());

		MapDescriptor <Integer, TestMessage> descriptor = Descriptors
				.map(Descriptors.int32, TestMessage.DESCRIPTOR);

		testValue(descriptor, object, serialized);
	}

	@Test
	public void testMessage() throws Exception {
		Map<String, Object> serialized = fixtureMap();
		TestMessage parsed = fixtureMessage();

		testValue(TestMessage.DESCRIPTOR, parsed, serialized);
	}

	@Test
	public void testPolymorphicMessage() throws Exception {
		MultiLevelSubtype parsed = new MultiLevelSubtype()
				.setField("field")
				.setSubfield("subfield")
				.setMfield("multi-level-field");
		ImmutableMap<String, Object> serialized = ImmutableMap.<String, Object>of(
				"type", "multilevel_subtype",
				"field", "field",
				"subfield", "subfield",
				"mfield", "multi-level-field");

		testValue(Base.DESCRIPTOR, parsed, serialized);
	}

	private TestMessage fixtureMessage() {
		return new TestMessage()
				.setBool0(true)
				.setInt0(123)
				.setString0("hello");
	}

	private Map<String, Object> fixtureMap() {
		return ImmutableMap.<String, Object>of(
				"bool0", true,
				"int0", 123,
				"string0", "hello");
	}
}
