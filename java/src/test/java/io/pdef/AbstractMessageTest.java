package io.pdef;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.pdef.test.inheritance.Base;
import io.pdef.test.inheritance.MultiLevelSubtype;
import io.pdef.test.inheritance.PolymorphicType;
import io.pdef.test.inheritance.Subtype;
import io.pdef.test.messages.TestComplexMessage;
import io.pdef.test.messages.TestEnum;
import io.pdef.test.messages.TestMessage;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AbstractMessageTest {
	@Test
	public void testEquals() throws Exception {
		assertEquals(createComplexMessage(), createComplexMessage());
	}

	@Test
	public void testHashCode() throws Exception {
		TestComplexMessage msg = createComplexMessage();
		int h = msg.hashCode();
		assertTrue(h != 0);
		assertEquals(h, createComplexMessage().hashCode());
	}

	@Test
	public void testToMap() throws Exception {
		Message msg = createComplexMessage();
		Map<String, Object> map = msg.toMap();
		Map<String, Object> expected = createComplexMessageMap();
		assertEquals(expected, map);
	}

	@Test
	public void testFromMap() throws Exception {
		Map<String, Object> map = createComplexMessageMap();
		Message msg = TestComplexMessage.fromMap(map);
		Message expected = createComplexMessage();
		assertEquals(expected, msg);
	}

	@Test
	public void testInitNullFields() throws Exception {
		TestComplexMessage message = new TestComplexMessage();
		List<Integer> list = message.getList0();
		Set<Integer> set = message.getSet0();
		Map<Integer, Float> map = message.getMap0();
		TestMessage testMessage = message.getMessage0();

		assertNotNull(list);
		assertNotNull(set);
		assertNotNull(map);
		assertNotNull(testMessage);

		assertSame(list, message.getList0());
		assertSame(set, message.getSet0());
		assertSame(map, message.getMap0());
		assertSame(testMessage, message.getMessage0());
	}

	@Test
	public void testCopy() throws Exception {
		TestComplexMessage message = createComplexMessage();
		TestComplexMessage copy = message.copy();

		assertEquals(message, copy);
		assertNotSame(message, copy);
	}

	@Test
	public void testMerge() throws Exception {
		TestComplexMessage message = createComplexMessage();
		TestComplexMessage another = new TestComplexMessage();
		another.merge(message);

		assertEquals(message, another);
	}

	@Test
	public void testMerge_superType() throws Exception {
		Base base = new Base().setField("hello");
		MultiLevelSubtype subtype = new MultiLevelSubtype();
		subtype.merge(base);

		assertEquals("hello", subtype.getField());
	}

	@Test
	public void testMerge_subtype() throws Exception {
		MultiLevelSubtype subtype = new MultiLevelSubtype().setField("hello");
		Base base = new Base();
		base.merge(subtype);

		assertEquals("hello", base.getField());
	}

	@Test
	public void testMerge_skipDicriminatorFields() throws Exception {
		Subtype subtype = new Subtype();
		assertEquals(PolymorphicType.SUBTYPE, subtype.getType());

		MultiLevelSubtype msubtype = new MultiLevelSubtype();
		assertEquals(PolymorphicType.MULTILEVEL_SUBTYPE, msubtype.getType());

		msubtype.merge(subtype);
		assertEquals(PolymorphicType.MULTILEVEL_SUBTYPE, msubtype.getType());
	}

	private TestComplexMessage createComplexMessage() {
		return new TestComplexMessage()
				.setEnum0(TestEnum.THREE)
				.setBool0(true)
				.setInt0(-32)
				.setShort0((short) -16)
				.setLong0(-64L)
				.setFloat0(-1.5f)
				.setDouble0(-2.5d)
				.setString0("hello")
				.setDatetime0(new Date(0))
				.setList0(ImmutableList.of(1, 2))
				.setSet0(ImmutableSet.of(1, 2))
				.setMap0(ImmutableMap.<Integer, Float>of(1, 1.5f))
				.setMessage0(null);
	}

	private Map<String, Object> createComplexMessageMap() {
		return ImmutableMap.<String, Object>builder()
				.put("enum0", TestEnum.THREE)
				.put("bool0", true)
				.put("int0", -32)
				.put("short0", (short) -16)
				.put("long0", -64L)
				.put("float0", -1.5f)
				.put("double0", -2.5d)
				.put("string0", "hello")
				.put("datetime0", new Date(0))
				.put("list0", ImmutableList.of(1, 2))
				.put("set0", ImmutableSet.of(1, 2))
				.put("map0", ImmutableMap.of(1, 1.5f))
				.build();
	}
}
