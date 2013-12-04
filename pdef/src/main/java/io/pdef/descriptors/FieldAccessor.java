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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface FieldAccessor<M, V> {
	/**
	 * Returns a field value in a message.
	 */
	V get(M message);

	/**
	 * Sets a field in a message.
	 */
	void set(M message, V value);

	static class ReflectionFieldAccessor<M, V> implements FieldAccessor<M, V> {
		private final String name;
		private final Class<M> cls;
		private final Method getter;
		private final Method setter;

		public ReflectionFieldAccessor(final String name, final Class<M> cls) {
			if (name == null) {
				throw new NullPointerException("field name");
			}
			if (cls == null) {
				throw new NullPointerException("message class");
			}
			this.name = name;
			this.cls = cls;

			String upperFirst = name.substring(0, 1).toUpperCase() + name.substring(1);
			getter = findMethodOrDie("get" + upperFirst, cls);
			setter = findMethodOrDie("set" + upperFirst, cls);
		}

		@SuppressWarnings("unchecked")
		@Override
		public V get(final M message) {
			try {
				return (V) getter.invoke(message);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getCause());
			}
		}

		@Override
		public void set(final M message, final V value) {
			try {
				setter.invoke(message, value);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getCause());
			}
		}

		private static <M> Method findMethodOrDie(final String name, final Class<M> cls) {
			for (Method method : cls.getMethods()) {
				if (method.getName().equals(name)) {
					return method;
				}
			}

			try {
				throw new NoSuchMethodException(name);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
