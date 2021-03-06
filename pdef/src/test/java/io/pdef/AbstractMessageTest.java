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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.pdef.test.inheritance.PdefBase;
import io.pdef.test.inheritance.PdefMultiLevelSubtype;
import io.pdef.test.inheritance.PdefPolymorphicType;
import io.pdef.test.inheritance.PdefSubtype;
import io.pdef.test.messages.PdefTestComplexMessage;
import io.pdef.test.messages.PdefTestEnum;
import io.pdef.test.messages.PdefTestMessage;
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
		PdefTestComplexMessage msg = createComplexMessage();
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
		Message msg = PdefTestComplexMessage.fromMap(map);
		Message expected = createComplexMessage();
		assertEquals(expected, msg);
	}

	@Test
	public void testInitNullFields() throws Exception {
		PdefTestComplexMessage message = new PdefTestComplexMessage();
		List<Integer> list = message.getList0();
		Set<Integer> set = message.getSet0();
		Map<Integer, Float> map = message.getMap0();
		PdefTestMessage testMessage = message.getMessage0();

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
		PdefTestComplexMessage message = createComplexMessage();
		PdefTestComplexMessage copy = message.copy();

		assertEquals(message, copy);
		assertNotSame(message, copy);
	}

	@Test
	public void testMerge() throws Exception {
		PdefTestComplexMessage message = createComplexMessage();
		PdefTestComplexMessage another = new PdefTestComplexMessage();
		another.merge(message);

		assertEquals(message, another);
	}

	@Test
	public void testMerge_superType() throws Exception {
		PdefBase base = new PdefBase().setField("hello");
		PdefMultiLevelSubtype subtype = new PdefMultiLevelSubtype();
		subtype.merge(base);

		assertEquals("hello", subtype.getField());
	}

	@Test
	public void testMerge_subtype() throws Exception {
		PdefMultiLevelSubtype subtype = new PdefMultiLevelSubtype().setField("hello");
		PdefBase base = new PdefBase();
		base.merge(subtype);

		assertEquals("hello", base.getField());
	}

	@Test
	public void testMerge_skipDicriminatorFields() throws Exception {
		PdefSubtype subtype = new PdefSubtype();
		assertEquals(PdefPolymorphicType.SUBTYPE, subtype.getType());

		PdefMultiLevelSubtype msubtype = new PdefMultiLevelSubtype();
		assertEquals(PdefPolymorphicType.MULTILEVEL_SUBTYPE, msubtype.getType());

		msubtype.merge(subtype);
		assertEquals(PdefPolymorphicType.MULTILEVEL_SUBTYPE, msubtype.getType());
	}

	private PdefTestComplexMessage createComplexMessage() {
		return new PdefTestComplexMessage()
				.setEnum0(PdefTestEnum.THREE)
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
				.put("string0", "hello")
				.put("bool0", true)
				.put("int0", -32)
				.put("short0", (short) -16)
				.put("long0", -64L)
				.put("float0", -1.5f)
				.put("double0", -2.5d)
				.put("datetime0", "1970-01-01T00:00:00Z")
				.put("list0", ImmutableList.of(1, 2))
				.put("set0", ImmutableSet.of(1, 2))
				.put("map0", ImmutableMap.of("1", 1.5f))
				.put("enum0", "three")
				.build();
	}
}
