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

import io.pdef.*;
import io.pdef.descriptors.DataTypeDescriptor;
import io.pdef.descriptors.InterfaceDescriptor;
import io.pdef.descriptors.MessageDescriptor;
import io.pdef.descriptors.MethodDescriptor;

public class RpcHandler<T> {
	private final InterfaceDescriptor<T> descriptor;
	private final RpcProtocol protocol;
	private final Invoker invoker;

	public RpcHandler(final InterfaceDescriptor<T> descriptor, final T service) {
		this(descriptor, Invokers.of(service));
	}

	public RpcHandler(final InterfaceDescriptor<T> descriptor, final Provider<T> provider) {
		this(descriptor, Invokers.of(provider));
	}

	public RpcHandler(final InterfaceDescriptor<T> descriptor, final Invoker invoker) {
		if (descriptor == null) throw new NullPointerException("descriptor");
		if (invoker == null) throw new NullPointerException("invoker");

		this.descriptor = descriptor;
		this.invoker = invoker;
		protocol = new RpcProtocol();
	}

	@SuppressWarnings("unchecked")
	public RpcResult<?, ?> handle(final RpcRequest request) throws Exception {
		if (request == null) throw new NullPointerException("request");

		Invocation invocation = protocol.getInvocation(request, descriptor);
		MethodDescriptor<?, ?> method = invocation.getMethod();
		DataTypeDescriptor<Object> resultd = (DataTypeDescriptor<Object>) method.getResult();
		MessageDescriptor<Message> excd = (MessageDescriptor<Message>) descriptor.getExc();

		try {
			Object result = invoker.invoke(invocation);
			return new RpcResult<Object, Message>(resultd, excd)
					.setData(result)
					.setSuccess(true);

		} catch (Exception e) {
			if (excd != null && excd.getJavaClass().isAssignableFrom(e.getClass())) {
				// It's an application exception.
				return new RpcResult<Object, Message>(resultd, excd)
						.setError((Message) e)
						.setSuccess(false);
			}
			
			throw e;
		}
	}
}
