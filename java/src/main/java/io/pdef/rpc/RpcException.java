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

import java.net.HttpURLConnection;

public class RpcException extends RuntimeException {
	private final int status;

	public RpcException(final int status) {
		this.status = status;
	}

	public RpcException(final int status, final String s) {
		super(s);
		this.status = status;
	}

	public RpcException(final int status, final Throwable throwable) {
		super(throwable);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public static RpcException methodNotAllowed(final String s) {
		return new RpcException(HttpURLConnection.HTTP_BAD_METHOD, s);
	}

	public static RpcException methodNotFound(final String s) {
		return new RpcException(HttpURLConnection.HTTP_NOT_FOUND, s);
	}
}
