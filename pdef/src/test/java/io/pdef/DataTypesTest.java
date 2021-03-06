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
import io.pdef.test.messages.PdefTestMessage;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DataTypesTest {
	private final Random random = new Random();

	@Test
	public void testCopy_message() throws Exception {
		PdefTestMessage message = createMessage();

		PdefTestMessage copy = DataTypes.copy(message);
		assertEquals(message, copy);
		assertNotSame(message, copy);
	}

	@Test
	public void testCopy_messageNull() throws Exception {
		Message message = DataTypes.copy((Message) null);
		assertNull(message);
	}

	@Test
	public void testCopy_list() throws Exception {
		PdefTestMessage message0 = new PdefTestMessage();
		PdefTestMessage message1 = new PdefTestMessage();
		List<PdefTestMessage> list = ImmutableList.of(message0, message1);

		List<PdefTestMessage> copy = DataTypes.copy(list);
		assertEquals(list, copy);
		assertNotSame(list, copy);
		assertNotSame(message0, copy.get(0));
		assertNotSame(message1, copy.get(1));
	}

	@Test
	public void testCopy_set() throws Exception {
		PdefTestMessage message0 = new PdefTestMessage();
		PdefTestMessage message1 = new PdefTestMessage();
		Set<PdefTestMessage> list = ImmutableSet.of(message0, message1);

		Set<PdefTestMessage> copy = DataTypes.copy(list);
		assertEquals(list, copy);
		assertNotSame(list, copy);
	}

	@Test
	public void testCopy_map() throws Exception {
		PdefTestMessage message0 = new PdefTestMessage();
		PdefTestMessage message1 = new PdefTestMessage();
		Map<Integer, PdefTestMessage> map = ImmutableMap.of(0, message0, 1, message1);

		Map<Integer, PdefTestMessage> copy = DataTypes.copy(map);
		assertEquals(map, copy);
		assertNotSame(map, copy);
		assertNotSame(message0, copy.get(0));
		assertNotSame(message1, copy.get(1));
	}

	private PdefTestMessage createMessage() {
		return new PdefTestMessage()
				.setBool0(random.nextBoolean())
				.setInt0(random.nextInt())
				.setString0(String.valueOf(random.nextInt()));
	}
}
