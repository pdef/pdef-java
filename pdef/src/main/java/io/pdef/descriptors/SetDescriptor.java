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

import java.util.HashSet;
import java.util.Set;

public class SetDescriptor<T> extends DataTypeDescriptor<Set<T>> {
	private final DataTypeDescriptor<T> element;

	@SuppressWarnings("unchecked")
	SetDescriptor(final DataTypeDescriptor<T> element) {
		super(TypeEnum.SET, (Class<Set<T>>) (Class<?>) Set.class);
		if (element == null) throw new NullPointerException("element");

		this.element = element;
	}

	/** Returns a set element descriptor. */
	public DataTypeDescriptor<T> getElement() {
		return element;
	}

	@Override
	public Set<T> getDefault() {
		return new HashSet<T>();
	}
}
