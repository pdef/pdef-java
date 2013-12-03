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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DescriptorsTest {
	@Test
	public void testList() throws Exception {
		ListDescriptor<Integer> descriptor = Descriptors.list(Descriptors.int32);

		assertEquals(List.class, descriptor.getJavaClass());
		assertEquals(Descriptors.int32, descriptor.getElement());
	}

	@Test
	public void testSet() throws Exception {
		SetDescriptor<Integer> descriptor = Descriptors.set(Descriptors.int32);

		assertEquals(Set.class, descriptor.getJavaClass());
		assertEquals(Descriptors.int32, descriptor.getElement());
	}

	@Test
	public void testMap() throws Exception {
		MapDescriptor<Integer, String> descriptor = Descriptors
				.map(Descriptors.int32, Descriptors.string);

		assertEquals(Map.class, descriptor.getJavaClass());
		assertEquals(Descriptors.int32, descriptor.getKey());
		assertEquals(Descriptors.string, descriptor.getValue());
	}
}
