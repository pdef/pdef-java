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

import com.google.common.collect.ImmutableList;
import io.pdef.test.messages.PdefTestEnum;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.List;

public class EnumDescriptorTest {
	@Test
	public void testGetValues() throws Exception {
		List<PdefTestEnum> values = PdefTestEnum.DESCRIPTOR.getValues();
		assertEquals(ImmutableList.<PdefTestEnum>of(
				PdefTestEnum.ONE, PdefTestEnum.TWO, PdefTestEnum.THREE), values);
	}
}
