package io.pdef.rpc;

import io.pdef.descriptors.DataTypeDescriptor;

public interface RpcSession {
	<T, E> T send(RpcRequest request, DataTypeDescriptor<T> datad, DataTypeDescriptor<E> errord)
			throws Exception;
}
