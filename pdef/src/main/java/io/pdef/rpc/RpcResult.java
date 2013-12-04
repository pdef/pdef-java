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

import io.pdef.DynamicMessage;
import io.pdef.Message;
import io.pdef.Provider;
import io.pdef.descriptors.DataTypeDescriptor;
import io.pdef.descriptors.Descriptors;
import io.pdef.descriptors.MessageDescriptor;

import javax.annotation.Nullable;

/** Generic RpcResult. */
public class RpcResult<T, E> extends DynamicMessage {
	private final DataTypeDescriptor<T> datad;
	private final DataTypeDescriptor<E> errord;
	private T data;
	private E error;
	private boolean success;

	public RpcResult(final DataTypeDescriptor<T> datad) {
		this(datad, null);
	}

	@SuppressWarnings("unchecked")
	public RpcResult(final DataTypeDescriptor<T> datad,
			@Nullable final DataTypeDescriptor<E> errord) {
		if (datad == null) {
			throw new NullPointerException("datad");
		}
		this.datad = datad;
		this.errord = errord != null ? errord : (DataTypeDescriptor<E>) Descriptors.void0;
	}

	public DataTypeDescriptor<T> getDataDescriptor() {
		return datad;
	}

	public DataTypeDescriptor<E> getErrorDescriptor() {
		return errord;
	}

	public T getData() {
		return data;
	}

	public RpcResult<T, E> setData(final T data) {
		this.data = data;
		return this;
	}

	public E getError() {
		return error;
	}

	public RpcResult<T, E> setError(final E error) {
		this.error = error;
		return this;
	}

	public boolean isSuccess() {
		return success;
	}

	public RpcResult<T, E> setSuccess(final boolean success) {
		this.success = success;
		return this;
	}

	@Override
	public MessageDescriptor<? extends Message> descriptor() {
		@SuppressWarnings("unchecked")
		Class<RpcResult<T, E>> messageClass = (Class<RpcResult<T, E>>) getClass();

		return MessageDescriptor.<RpcResult<T, E>>builder()
				.setJavaClass(messageClass)
				.setProvider(new Provider<RpcResult<T, E>>() {
					@Override
					public RpcResult<T, E> get() {
						return new RpcResult<T, E>(datad, errord);
					}
				})
				.addField("data", datad, messageClass)
				.addField("error", errord, messageClass)
				.build();
	}
}
