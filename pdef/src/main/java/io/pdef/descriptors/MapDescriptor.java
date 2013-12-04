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

import java.util.HashMap;
import java.util.Map;

public class MapDescriptor<K, V> extends DataTypeDescriptor<Map<K, V>> {
	private final DataTypeDescriptor<K> key;
	private final DataTypeDescriptor<V> value;

	@SuppressWarnings("unchecked")
	MapDescriptor(final DataTypeDescriptor<K> key, final DataTypeDescriptor<V> value) {
		super(TypeEnum.MAP, (Class<Map<K, V>>) (Class<?>) Map.class);
		if (key == null) throw new NullPointerException("key");
		if (value == null) throw new NullPointerException("value");

		this.key = key;
		this.value = value;
	}

	/** Returns a map key descriptor. */
	public DataTypeDescriptor<K> getKey() {
		return key;
	}

	/** Returns a map value descriptor. */
	public DataTypeDescriptor<V> getValue() {
		return value;
	}

	@Override
	public Map<K, V> getDefault() {
		return new HashMap<K, V>();
	}
}
