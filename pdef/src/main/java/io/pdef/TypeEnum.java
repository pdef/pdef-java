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

package io.pdef;

import java.util.*;

/** TypeEnum enumerates Pdef types. */
public enum TypeEnum {
	// Values.
	BOOL, INT16, INT32, INT64, FLOAT, DOUBLE, STRING, DATETIME,

	// Collections.
	LIST, SET, MAP,

	// Void type (can be used only as a method result).
	VOID,

	// User-defined types.
	ENUM,
	MESSAGE,
	INTERFACE;

	private static final EnumSet<TypeEnum> PRIMITIVES;  // Java primitives.
	private static final Map<Class<?>, TypeEnum> VALUES;
	static {
		PRIMITIVES = EnumSet.of(BOOL, INT16, INT32, INT64, FLOAT, DOUBLE);

		VALUES = new HashMap<Class<?>, TypeEnum>();
		VALUES.put(Boolean.class, BOOL);
		VALUES.put(Short.class, INT16);
		VALUES.put(Integer.class, INT32);
		VALUES.put(Long.class, INT64);
		VALUES.put(Float.class, FLOAT);
		VALUES.put(Double.class, DOUBLE);
		VALUES.put(String.class, STRING);
		VALUES.put(Date.class, DATETIME);
		VALUES.put(Void.class, VOID);
	}

	/** Returns a pdef data type of a java class or throws IllegalArgumentException. */
	public static TypeEnum dataTypeOf(final Class<?> cls) {
		if (cls == null) throw new NullPointerException("cls");
		else if (VALUES.containsKey(cls)) return VALUES.get(cls);
		else if (cls.isEnum()) return TypeEnum.ENUM;
		else if (List.class.isAssignableFrom(cls)) return TypeEnum.LIST;
		else if (Set.class.isAssignableFrom(cls)) return TypeEnum.SET;
		else if (Map.class.isAssignableFrom(cls)) return TypeEnum.MAP;
		else if (Message.class.isAssignableFrom(cls)) return TypeEnum.MESSAGE;
		throw new IllegalArgumentException("Unsupported value type " + cls);
	}

	public boolean isDataType() {
		return this != INTERFACE;
	}

	public boolean isPrimitive() {
		return PRIMITIVES.contains(this);
	}
}
