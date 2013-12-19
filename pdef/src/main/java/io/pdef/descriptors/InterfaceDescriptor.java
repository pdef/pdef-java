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

import io.pdef.Message;
import io.pdef.TypeEnum;

import javax.annotation.Nullable;
import java.util.*;

public class InterfaceDescriptor<T> extends Descriptor<T> {
	private final InterfaceDescriptor<? super T> base;
	private final MessageDescriptor<? extends Message> exc;

	private final List<MethodDescriptor<T, ?>> declaredMethods;
	private final List<MethodDescriptor<? super T, ?>> methods;
	private final Map<String, MethodDescriptor<? super T, ?>> methodMap;

	private InterfaceDescriptor(final Builder<T> builder) {
		super(TypeEnum.INTERFACE, builder.javaClass);

		base = builder.base;
		exc = builder.exc;
		declaredMethods = ImmutableCollections.list(builder.methods);
		methods = joinMethods(declaredMethods, base);
		methodMap = methodMap(methods);
	}

	public static <T> Builder<T> builder() {
		return new Builder<T>();
	}

	@Override
	public String toString() {
		return "InterfaceDescriptor{" + getJavaClass().getSimpleName() + '}';
	}

	/** Returns this interface base or @literal{null}. */
	public InterfaceDescriptor<? super T> getBase() {
		return base;
	}

	/** Returns a list of methods declared in this interface (not in its base) or an empty list. */
	public List<MethodDescriptor<T, ?>> getDeclaredMethods() {
		return declaredMethods;
	}

	/** Returns a list of method descriptors or an empty list. */
	public List<MethodDescriptor<? super T, ?>> getMethods() {
		return methods;
	}

	/** Returns an exception descriptor or {@literal null}. */
	@Nullable
	public MessageDescriptor<? extends Message> getExc() {
		return exc != null ? exc : (base != null ? base.exc : null);
	}

	/** Returns a method descriptor by its name and returns it or {@literal null}. */
	@Nullable
	public MethodDescriptor<? super T, ?> getMethod(final String name) {
		return methodMap.get(name);
	}

	public static class Builder<T> {
		private Class<T> javaClass;
		private InterfaceDescriptor<? super T> base;
		private MessageDescriptor<? extends Message> exc;
		private List<MethodDescriptor<T, ?>> methods;

		public Builder() {
			methods = new ArrayList<MethodDescriptor<T, ?>>();
		}

		public Builder<T> setJavaClass(final Class<T> javaClass) {
			this.javaClass = javaClass;
			return this;
		}

		public Builder<T> setBase(final InterfaceDescriptor<? super T> base) {
			this.base = base;
			return this;
		}

		public Builder<T> setExc(final MessageDescriptor<? extends Message> exc) {
			this.exc = exc;
			return this;
		}

		public Builder<T> addMethod(final MethodDescriptor<T, ?> method) {
			this.methods.add(method);
			return this;
		}

		public InterfaceDescriptor<T> build() {
			return new InterfaceDescriptor<T>(this);
		}
	}

	private static <T> List<MethodDescriptor<? super T, ?>> joinMethods(
			final List<MethodDescriptor<T, ?>> declaredMethods,
			@Nullable final InterfaceDescriptor<? super T> base) {
		List<MethodDescriptor<? super T, ?>> result = new ArrayList<MethodDescriptor<? super T, ?>>();
		if (base != null) {
			result.addAll(base.getMethods());
		}
		result.addAll(declaredMethods);
		return Collections.unmodifiableList(result);
	}

	private static <T> Map<String, MethodDescriptor<? super T, ?>> methodMap(
			final List<MethodDescriptor<? super T, ?>> methods) {
		Map<String, MethodDescriptor<? super T, ?>> map = new HashMap<String,
				MethodDescriptor<? super T, ?>>();
		for (MethodDescriptor<? super T, ?> method : methods) {
			map.put(method.getName(), method);
		}
		return Collections.unmodifiableMap(map);
	}
}
