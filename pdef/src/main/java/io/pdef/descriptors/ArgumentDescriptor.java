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

/**
 * ArgumentDescriptor provides a method argument name and type.
 *
 * @param <V> Argument class.
 */
public class ArgumentDescriptor<V> {
	private final String name;
	private final DataTypeDescriptor<V> type;
	private final boolean query;
	private final boolean post;

	public static <V> ArgumentDescriptor<V> of(final String name,
			final DataTypeDescriptor<V> type) {
		return new ArgumentDescriptor<V>(name, type, false, false);
	}

	public ArgumentDescriptor(final String name, final DataTypeDescriptor<V> type,
			final boolean query, final boolean post) {
		if (name == null) throw new NullPointerException("name");
		if (type == null) throw new NullPointerException("type");

		this.name = name;
		this.type = type;
		this.query = query;
		this.post = post;
	}

	@Override
	public String toString() {
		return "ArgumentDescriptor{'" + name + '\'' + ", " + type + '}';
	}

	/** Returns a method argument name. */
	public String getName() {
		return name;
	}

	/** Returns an argument type descriptor. */
	public DataTypeDescriptor<V> getType() {
		return type;
	}

	/** Returns whether this argument is an HTTP post argument. */
	public boolean isPost() {
		return post;
	}

	/** Returns whether this argument is an HTTP query argument. */
	public boolean isQuery() {
		return query;
	}
}
