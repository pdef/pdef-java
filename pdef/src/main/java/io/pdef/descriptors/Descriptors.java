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
import java.util.Date;

/** Primitive and collection descriptors. */
public class Descriptors {
	public static DataTypeDescriptor<Boolean> bool = primitive(TypeEnum.BOOL, Boolean.class, false);
	public static DataTypeDescriptor<Short> int16 = primitive(TypeEnum.INT16, Short.class, (short) 0);
	public static DataTypeDescriptor<Integer> int32 = primitive(TypeEnum.INT32, Integer.class, 0);
	public static DataTypeDescriptor<Long> int64 = primitive(TypeEnum.INT64, Long.class, 0L);
	public static DataTypeDescriptor<Float> float0 = primitive(TypeEnum.FLOAT, Float.class, 0f);
	public static DataTypeDescriptor<Double> double0 = primitive(TypeEnum.DOUBLE, Double.class, 0d);
	public static DataTypeDescriptor<String> string = primitive(TypeEnum.STRING, String.class, "");
	public static DataTypeDescriptor<Date> datetime = primitive(TypeEnum.DATETIME, Date.class, null);
	public static DataTypeDescriptor<Void> void0 = primitive(TypeEnum.VOID, Void.class, null);

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

	private static <T> PrimitiveDescriptor<T> primitive(final TypeEnum type, final Class<T> cls,
			final T defaultValue) {
		return new PrimitiveDescriptor<T>(type, cls, defaultValue);
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
		private final T defaultValue;

		private PrimitiveDescriptor(final TypeEnum type, final Class<T> javaClass,
				final T defaultValue) {
			super(type, javaClass);
			this.defaultValue = defaultValue;
		}

		@Override
		public T getDefault() {
			return defaultValue;
		}
	}
}
