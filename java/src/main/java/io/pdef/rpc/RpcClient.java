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

import io.pdef.Invocation;
import io.pdef.InvocationProxy;
import io.pdef.Invoker;
import io.pdef.Message;
import io.pdef.descriptors.*;

public class RpcClient<T> implements Invoker {
	private final InterfaceDescriptor<T> descriptor;
	private final RpcSession session;
	private final RpcProtocol protocol;

	public RpcClient(final InterfaceDescriptor<T> descriptor, final String url) {
		this(descriptor, new HttpUrlConnectionRpcSession(url));
	}

	public RpcClient(final InterfaceDescriptor<T> descriptor, final RpcSession session) {
		if (descriptor == null) throw new NullPointerException("descriptor");
		if (session == null) throw new NullPointerException("session");

		this.descriptor = descriptor;
		this.session = session;
		protocol = new RpcProtocol();
	}

	public T proxy() {
		return InvocationProxy.create(descriptor, this);
	}

	/**
	 * Serializes an invocation, sends an rpc request and returns the result.
	 */
	@Override
	public Object invoke(final Invocation invocation) throws Exception {
		if (invocation == null) throw new NullPointerException("invocation");

		MethodDescriptor<?, ?> method = invocation.getMethod();
		DataTypeDescriptor<?> resultd = (DataTypeDescriptor<?>) method.getResult();
		MessageDescriptor<? extends Message> excd = descriptor.getExc();

		RpcRequest request = protocol.getRequest(invocation);
		return session.send(request, resultd, excd);
	}
}
