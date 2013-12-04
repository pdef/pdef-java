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

import java.util.ArrayList;
import java.util.List;

public class ListDescriptor<T> extends DataTypeDescriptor<List<T>> {
	private final DataTypeDescriptor<T> element;

	@SuppressWarnings("unchecked")
	ListDescriptor(final DataTypeDescriptor<T> element) {
		super(TypeEnum.LIST, (Class<List<T>>) (Class<?>) List.class);
		if (element == null) throw new NullPointerException("element");

		this.element = element;
	}

	/** Returns a list element descriptor. */
	public DataTypeDescriptor<T> getElement() {
		return element;
	}

	@Override
	public List<T> getDefault() {
		return new ArrayList<T>();
	}
}
