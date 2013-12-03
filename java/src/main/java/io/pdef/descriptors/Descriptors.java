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

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;

/** Primitive and collection descriptors. */
public class Descriptors {
	public static DataTypeDescriptor<Boolean> bool = primitive(TypeEnum.BOOL, Boolean.class);
	public static DataTypeDescriptor<Short> int16 = primitive(TypeEnum.INT16, Short.class);
	public static DataTypeDescriptor<Integer> int32 = primitive(TypeEnum.INT32, Integer.class);
	public static DataTypeDescriptor<Long> int64 = primitive(TypeEnum.INT64, Long.class);
	public static DataTypeDescriptor<Float> float0 = primitive(TypeEnum.FLOAT, Float.class);
	public static DataTypeDescriptor<Double> double0 = primitive(TypeEnum.DOUBLE, Double.class);
	public static DataTypeDescriptor<String> string = primitive(TypeEnum.STRING, String.class);
	public static DataTypeDescriptor<Date> datetime = primitive(TypeEnum.DATETIME, Date.class);
	public static DataTypeDescriptor<Void> void0 = primitive(TypeEnum.VOID, Void.class);

	private Descriptors() {}

	public static <T> ListDescriptor<T> list(final DataTypeDescriptor<T> element) {
		return new ListDescriptor<T>(element);
	}

	public static <T> SetDescriptor<T> set(final DataTypeDescriptor<T> element) {
		return new SetDescriptor<T>(element);
	}

	public static <K, V> MapDescriptor<K, V> map(final DataTypeDescriptor<K> key,
			final DataTypeDescriptor<V> value) {
		return new MapDescriptor<K, V>(key, value);
	}

	private static <T> PrimitiveDescriptor<T> primitive(final TypeEnum type, final Class<T> cls) {
		return new PrimitiveDescriptor<T>(type, cls);
	}

	/** Returns an interface descriptor or throws an IllegalArgumentException. */
	@Nullable
	public static <T> InterfaceDescriptor<T> findInterfaceDescriptor(final Class<T> cls) {
		if (!cls.isInterface()) {
			throw new IllegalArgumentException("Interface required, got " + cls);
		}

		Field field;
		try {
			field = cls.getField("DESCRIPTOR");
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("No DESCRIPTOR field in " + cls);
		}

		if (!InterfaceDescriptor.class.isAssignableFrom(field.getType())) {
			throw new IllegalArgumentException("Not an InterfaceDescriptor field, " + field);
		}

		try {
			// Get the static TYPE field.
			@SuppressWarnings("unchecked")
			InterfaceDescriptor<T> descriptor = (InterfaceDescriptor<T>) field.get(null);
			return descriptor;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static class PrimitiveDescriptor<T> extends DataTypeDescriptor<T> {
		private PrimitiveDescriptor(final TypeEnum type, final Class<T> javaClass) {
			super(type, javaClass);
		}
	}
}
