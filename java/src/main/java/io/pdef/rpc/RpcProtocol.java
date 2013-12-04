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
import io.pdef.descriptors.*;
import io.pdef.json.JsonFormat;
import io.pdef.Invocation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class RpcProtocol {
	public static final String CHARSET_NAME = "UTF-8";
	private final JsonFormat format;

	public RpcProtocol() {
		this(JsonFormat.instance());
	}

	public RpcProtocol(final JsonFormat format) {
		if (format == null) throw new NullPointerException("format");
		this.format = format;
	}

	/** Converts an invocation into an rpc request. */
	public RpcRequest getRequest(final Invocation invocation) {
		if (invocation == null) throw new NullPointerException("invocation");

		MethodDescriptor<?, ?> method = invocation.getMethod();
		if (!method.isTerminal()) {
			throw new IllegalArgumentException("Last invocation method must be terminal");
		}

		RpcRequest request = new RpcRequest(method.isPost() ? RpcRequest.POST : RpcRequest.GET);
		for (Invocation invocation1 : invocation.toChain()) {
			writeInvocation(request, invocation1);
		}

		return request;
	}

	private void writeInvocation(final RpcRequest request, final Invocation invocation) {
		MethodDescriptor<?, ?> method = invocation.getMethod();

		Object[] args = invocation.getArgs();
		List<ArgumentDescriptor<?>> argds = method.getArgs();

		StringBuilder path = new StringBuilder(request.getPath())
				.append("/")
				.append(method.getName());

		Map<String, String> post = request.getPost();
		Map<String, String> query = request.getQuery();

		for (int i = 0; i < args.length; i++) {
			ArgumentDescriptor argd = argds.get(i);
			Object arg = args[i];

			String name = argd.getName();
			String value = toJson(argd.getType(), arg);

			if (argd.isPost()) {
				post.put(name, value);
			} else if (argd.isQuery()) {
				query.put(name, value);
			} else {
				path.append("/").append(urlencode(value));
			}
		}

		request.setPath(path.toString());
	}

	// VisibleForTesting
	/** Serializes an argument to JSON, strips the quotes. */
	<V> String toJson(final DataTypeDescriptor<V> descriptor, final V arg) {
		String s = format.write(arg, descriptor, false);
		TypeEnum type = descriptor.getType();
		if (type == TypeEnum.STRING || type == TypeEnum.ENUM || type == TypeEnum.DATETIME) {
			// Remove the quotes.
			s = s.substring(1, s.length() - 1);
		}

		return s;
	}

	/** Parses an invocation from an rpc request. */
	public Invocation getInvocation(final RpcRequest request, InterfaceDescriptor<?> descriptor) {
		if (request == null) throw new NullPointerException("request");
		if (descriptor == null) throw new NullPointerException("descriptor");

		Invocation invocation = null;
		LinkedList<String> parts = splitPath(request.getPath());

		while (!parts.isEmpty()) {
			String part = parts.removeFirst();

			// Find a method by a name.
			MethodDescriptor<?, ?> method = descriptor.getMethod(part);
			if (method == null) {
				throw RpcException.methodNotFound("Method is not found: " + part);
			}

			// Check the required HTTP method.
			if (method.isPost() && !request.isPost()) {
				throw RpcException.methodNotAllowed("Method not allowed, POST required");
			}

			// Parse arguments.
			List<Object> args = readArgs(method, parts, request.getQuery(), request.getPost());

			// Create a root invocation,
			// or a next invocation in a chain.
			invocation = invocation != null ? invocation.next(method, args.toArray())
			                                : Invocation.root(method, args.toArray());
			if (method.isTerminal()) {
				break;
			}

			// It's an interface method.
			// Get the next interface and proceed parsing the parts.
			descriptor = (InterfaceDescriptor<?>) method.getResult();
		}

		if (!parts.isEmpty()) {
			// No more interface descriptors in a chain, but the parts are still present.
			throw RpcException.methodNotFound("Failed to parse an invocation chain");
		}

		if (invocation == null) {
			throw RpcException.methodNotFound("Methods required");
		}

		if (!invocation.getMethod().isTerminal()) {
			throw RpcException.methodNotFound("The last method must be a terminal one. "
					+ "It must return a data type or be void.");
		}

		return invocation;
	}

	private List<Object> readArgs(final MethodDescriptor<?, ?> method,
			final LinkedList<String> parts, final Map<String, String> query,
			final Map<String, String> post) {
		List<Object> args = new ArrayList<Object>();

		for (ArgumentDescriptor<?> argd : method.getArgs()) {
			String value;
			String name = argd.getName();

			if (argd.isPost()) {
				value = post.get(name);
			} else if (argd.isQuery()) {
				value = query.get(name);
			} else if (parts.isEmpty()) {
				throw RpcException.methodNotFound("Wrong number of method args");
			} else {
				value = urldecode(parts.removeFirst());
			}

			Object arg = fromJson(argd.getType(), value);
			args.add(arg);
		}

		return args;
	}

	private LinkedList<String> splitPath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		// The split() method discards trailing empty strings (i.e. the last slash).
		String[] partsArray = path.split("/");
		LinkedList<String> parts = new LinkedList<String>();
		Collections.addAll(parts, partsArray);
		return parts;
	}

	// VisibleForTesting
	/** Parses an argument from an unquoted JSON string. */
	<V> V fromJson(final DataTypeDescriptor<V> descriptor, String value) {
		if (value == null) {
			return null;
		}

		TypeEnum type = descriptor.getType();
		if (type == TypeEnum.STRING || type == TypeEnum.DATETIME || type == TypeEnum.ENUM) {
			if (!value.startsWith("\"") && !value.endsWith("\"")) {
				// Return the quotes to parse a value as valid json strings.
				value = "\"" + value + "\"";
			}
		}

		return format.read(value, descriptor);
	}

	/** Url-encodes a string. */
	static String urlencode(final String s) {
		try {
			return URLEncoder.encode(s, CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/** Url-decodes a string. */
	static String urldecode(final String s) {
		try {
			return URLDecoder.decode(s, CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
