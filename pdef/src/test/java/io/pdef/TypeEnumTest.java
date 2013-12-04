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

import io.pdef.test.interfaces.PdefTestInterface;
import io.pdef.test.interfaces.PdefTestException;
import io.pdef.test.messages.PdefTestEnum;
import io.pdef.test.messages.PdefTestMessage;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TypeEnumTest {
	@Test
	public void testDataTypeOf() throws Exception {
		assertEquals(TypeEnum.BOOL, TypeEnum.dataTypeOf(Boolean.class));
		assertEquals(TypeEnum.INT16, TypeEnum.dataTypeOf(Short.class));
		assertEquals(TypeEnum.INT32, TypeEnum.dataTypeOf(Integer.class));
		assertEquals(TypeEnum.INT64, TypeEnum.dataTypeOf(Long.class));
		assertEquals(TypeEnum.FLOAT, TypeEnum.dataTypeOf(Float.class));
		assertEquals(TypeEnum.DOUBLE, TypeEnum.dataTypeOf(Double.class));
		assertEquals(TypeEnum.STRING, TypeEnum.dataTypeOf(String.class));
		assertEquals(TypeEnum.LIST, TypeEnum.dataTypeOf(ArrayList.class));
		assertEquals(TypeEnum.SET, TypeEnum.dataTypeOf(HashSet.class));
		assertEquals(TypeEnum.MAP, TypeEnum.dataTypeOf(HashMap.class));
		assertEquals(TypeEnum.VOID, TypeEnum.dataTypeOf(Void.class));
		assertEquals(TypeEnum.ENUM, TypeEnum.dataTypeOf(PdefTestEnum.class));
		assertEquals(TypeEnum.MESSAGE, TypeEnum.dataTypeOf(PdefTestMessage.class));
		assertEquals(TypeEnum.MESSAGE, TypeEnum.dataTypeOf(PdefTestException.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDataTypeOf_unsupported() throws Exception {
		TypeEnum.dataTypeOf(PdefTestInterface.class);
	}

	@Test(expected = NullPointerException.class)
	public void testDataTypeOf_null() throws Exception {
		TypeEnum.dataTypeOf(null);
	}
}
