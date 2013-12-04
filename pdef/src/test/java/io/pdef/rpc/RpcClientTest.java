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

package io.pdef.rpc;

import com.google.common.collect.ImmutableMap;
import io.pdef.descriptors.Descriptors;
import io.pdef.Invocation;
import io.pdef.test.interfaces.PdefTestException;
import io.pdef.test.interfaces.PdefTestInterface;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class RpcClientTest {
	@Test
	public void testInvoke() throws Exception {
		RpcSession session = mock(RpcSession.class);
		RpcClient<PdefTestInterface> client = new RpcClient<PdefTestInterface>(PdefTestInterface.DESCRIPTOR, session);

		Invocation invocation = getInvocation(1, 2);
		client.invoke(invocation);

		RpcRequest request = new RpcRequest()
				.setPath("/query")
				.setQuery(ImmutableMap.of("arg0", "1", "arg1", "2"));
		verify(session).send(request, Descriptors.int32, PdefTestException.DESCRIPTOR);
	}

	private Invocation getInvocation(final int arg0, final int arg1) {
		return Invocation.root(PdefTestInterface.DESCRIPTOR.getMethod("query"),
				new Object[]{arg0, arg1});
	}
}
