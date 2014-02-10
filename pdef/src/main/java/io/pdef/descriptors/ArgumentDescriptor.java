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
	private boolean query;
	private boolean post;

	public static <V> ArgumentDescriptor<V> of(final String name,
			final DataTypeDescriptor<V> type) {
		return new ArgumentDescriptor<V>(name, type);
	}
	
	public ArgumentDescriptor(final String name, final DataTypeDescriptor<V> type) {
		if (name == null) throw new NullPointerException("name");
		if (type == null) throw new NullPointerException("type");

		this.name = name;
		this.type = type;
	}

	@Deprecated
	public ArgumentDescriptor(final String name, final DataTypeDescriptor<V> type,
			@Deprecated final boolean query, @Deprecated final boolean post) {
		this(name, type);
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

	/** Deprecated, only methods can be post.
	 * 
	 * Returns whether this argument is an HTTP post argument. */
	@Deprecated
	public boolean isPost() {
		return post;
	}
	
	/** Deprecated, all terminal method arguments are considered as query. 
	 * 
	 * Returns whether this argument is an HTTP query argument. */
	@Deprecated
	public boolean isQuery() {
		return query;
	}
}
