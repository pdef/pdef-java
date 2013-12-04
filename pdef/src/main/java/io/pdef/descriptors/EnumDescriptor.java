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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** EnumDescriptor holds enum values and parsing/serialization methods. */
public class EnumDescriptor<T extends Enum<T>> extends DataTypeDescriptor<T> {
	private final List<T> values;
	private final Map<String, T> namesToValues;

	public static <T extends Enum<T>> EnumDescriptor<T> of(final Class<T> javaClass) {
		return new EnumDescriptor<T>(javaClass);
	}

	private EnumDescriptor(final Class<T> javaClass) {
		super(TypeEnum.ENUM, javaClass);

		values = ImmutableCollections.list(javaClass.getEnumConstants());
		namesToValues = ImmutableCollections.map(valuesToMap(values));
	}

	@Override
	public String toString() {
		return "EnumDescriptor{" + getJavaClass().getSimpleName() + '}';
	}

	/** Returns a list of enum values or an empty list. */
	public List<T> getValues() {
		return values;
	}

	/** Returns an enum value by its name or {@literal null}. */
	public T getValue(final String name) {
		if (name == null) {
			return null;
		}
		String uppercased = name.toUpperCase();
		return namesToValues.get(uppercased);
	}

	private static <T extends Enum<T>> Map<String, T> valuesToMap(final List<T> values) {
		Map<String, T> temp = new LinkedHashMap<String, T>();
		for (T value : values) {
			temp.put(value.name().toUpperCase(), value);
		}
		return temp;
	}
}
