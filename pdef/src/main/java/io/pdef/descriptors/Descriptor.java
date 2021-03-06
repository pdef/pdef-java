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

import io.pdef.TypeEnum;

/**
 * Descriptor is a base class for Pdef descriptors. Descriptors provides information about a Java
 * type in runtime. For example, a message descriptor allows to explore declared fields, inherited
 * fields, etc.
 */
public class Descriptor<T> {
	private final TypeEnum type;
	private final Class<T> javaClass;

	protected Descriptor(final TypeEnum type, final Class<T> javaClass) {
		if (type == null) throw new NullPointerException("type");
		if (javaClass == null) throw new NullPointerException("javaClass");

		this.type = type;
		this.javaClass = javaClass;
	}

	/** Returns a pdef type. */
	public TypeEnum getType() {
		return type;
	}

	/** Returns a java class. */
	public Class<T> getJavaClass() {
		return javaClass;
	}

	@Override
	public String toString() {
		return "Descriptor{" + type + "," + javaClass.getSimpleName() + '}';
	}
}
