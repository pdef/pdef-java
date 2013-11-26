package io.pdef.formats;

import io.pdef.Message;
import io.pdef.TypeEnum;
import io.pdef.descriptors.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataFormat {
	private static final DataFormat INSTANCE = new DataFormat();
	private final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			TimeZone tz = TimeZone.getTimeZone("UTC");
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			format.setTimeZone(tz);
			return format;
		}
	};

	private DataFormat() {}

	public static DataFormat getInstance() {
		return INSTANCE;
	}

	// Serializing.

	@SuppressWarnings("unchecked")
	public <T extends Message> Map<String, Object> writeMessage(final T message,
			final MessageDescriptor<T> descriptor) throws FormatException {
		return (Map<String, Object>) this.write(message, descriptor);
	}

	public <T> Object write(final T object, final DataTypeDescriptor<T> descriptor)
			throws FormatException {
		if (descriptor == null) throw new NullPointerException("descriptor");

		try {
			return doWrite(object, descriptor);
		} catch (FormatException e) {
			throw e;
		} catch (Exception e) {
			throw new FormatException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Object doWrite(final T object, final DataTypeDescriptor<T> descriptor)
			throws Exception {
		if (object == null) {
			return null;
		}

		TypeEnum typeEnum = descriptor.getType();
		switch (typeEnum) {
			case BOOL:
			case INT16:
			case INT32:
			case INT64:
			case FLOAT:
			case DOUBLE:
			case STRING: return object;
			case DATETIME: return writeDate((Date) object);
			case LIST: return writeList((List) object, (ListDescriptor) descriptor);
			case SET: return writeSet((Set) object, (SetDescriptor) descriptor);
			case MAP: return writeMap((Map) object, (MapDescriptor) descriptor);
			case ENUM: return writeEnum((Enum) object);
			case MESSAGE: return writeMessage((Message) object);
			case VOID: return null;
			default:
				throw new IllegalArgumentException("Unsupported descriptor " + descriptor);
		}
	}

	private Date writeDate(final Date date) {
		if (date == null) {
			return null;
		}

		return new Date(date.getTime());
	}

	private <E> List<Object> writeList(final List<E> list, final ListDescriptor<E> descriptor)
			throws Exception {
		if (list == null) {
			return null;
		}

		DataTypeDescriptor<E> element = descriptor.getElement();
		List<Object> result = new ArrayList<Object>();

		for (E elem : list) {
			Object serialized = doWrite(elem, element);
			result.add(serialized);
		}

		return result;
	}

	private <E> Set<Object> writeSet(final Set<E> set, final SetDescriptor<E> descriptor)
			throws Exception {
		if (set == null) {
			return null;
		}

		DataTypeDescriptor<E> element = descriptor.getElement();
		Set<Object> result = new HashSet<Object>();
		for (E elem : set) {
			Object serialized = doWrite(elem, element);
			result.add(serialized);
		}

		return result;
	}

	private <K, V> Map<Object, Object> writeMap(final Map<K, V> map,
			final MapDescriptor<K, V> descriptor) throws Exception {
		if (map == null) {
			return null;
		}

		DataTypeDescriptor<K> key = descriptor.getKey();
		DataTypeDescriptor<V> value = descriptor.getValue();
		Map<Object, Object> result = new HashMap<Object, Object>();

		for (Map.Entry<K, V> e : map.entrySet()) {
			Object k = doWrite(e.getKey(), key);
			Object v = doWrite(e.getValue(), value);
			result.put(k, v);
		}

		return result;
	}

	private <E extends Enum<E>> E writeEnum(final E value) {
		return value;
	}

	private <M extends Message> Map<String, Object> writeMessage(final M message)
			throws Exception {
		if (message == null) {
			return null;
		}

		// Mind polymorphic messages.
		@SuppressWarnings("unchecked")
		MessageDescriptor<M> polymorphicType = (MessageDescriptor<M>) message.descriptor();
		Map<String, Object> result = new LinkedHashMap<String, Object>();

		for (FieldDescriptor<? super M, ?> field : polymorphicType.getFields()) {
			@SuppressWarnings("unchecked")
			FieldDescriptor<M, ?> uncheckedField = (FieldDescriptor<M, ?>) field;
			writeField(uncheckedField, message, result);
		}

		return result;
	}

	private <M extends Message, V> void writeField(final FieldDescriptor<M, V> field,
			final M message, final Map<String, Object> map) throws Exception {
		V value = field.get(message);
		if (value == null) {
			return;
		}

		Object serialized = doWrite(value, field.getType());
		map.put(field.getName(), serialized);
	}

	// Parsing.

	public <T> T read(final Object data, final DataTypeDescriptor<T> descriptor)
			throws FormatException {
		if (descriptor == null) throw new NullPointerException("descriptor");

		try {
			return read(descriptor, data);
		} catch (FormatException e) {
			throw e;
		} catch (Exception e) {
			throw new FormatException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T read(final DataTypeDescriptor<T> descriptor, final Object input) throws Exception {
		if (input == null) {
			return null;
		}

		TypeEnum typeEnum = descriptor.getType();
		switch (typeEnum) {
			case BOOL: return (T) readBoolean(input);
			case INT16: return (T) readShort(input);
			case INT32: return (T) readInt(input);
			case INT64: return (T) readLong(input);
			case FLOAT: return (T) readFloat(input);
			case DOUBLE: return (T) readDouble(input);
			case STRING: return (T) readString(input);
			case DATETIME: return (T) readDatetime(input);
			case LIST: return (T) readList(input, (ListDescriptor<?>) descriptor);
			case SET: return (T) readSet(input, (SetDescriptor<?>) descriptor);
			case MAP: return (T) readMap(input, (MapDescriptor<?, ?>) descriptor);
			case ENUM: return (T) readEnum(input, (EnumDescriptor<? extends Enum<?>>) descriptor);
			case MESSAGE: return (T) readMessage(input, (MessageDescriptor<? extends Message>) descriptor);
			case VOID: return null;
			default: throw new IllegalArgumentException("Unsupported descriptor " + descriptor);
		}
	}

	private Boolean readBoolean(final Object input) {
		if (input instanceof Boolean) {
			return (Boolean) input;
		} else if (input instanceof String) {
			return Boolean.parseBoolean((String) input);
		}
		throw new FormatException("Cannot read a boolean from " + input);
	}

	private Short readShort(final Object input) {
		if (input instanceof Number) {
			return ((Number) input).shortValue();
		} else if (input instanceof String) {
			return Short.parseShort((String) input);
		}
		throw new FormatException("Cannot read a short from " + input);
	}

	private Integer readInt(final Object input) {
		if (input instanceof Number) {
			return ((Number) input).intValue();
		} else if (input instanceof String) {
			return Integer.parseInt((String) input);
		}
		throw new FormatException("Cannot read an int from " + input);
	}

	private Long readLong(final Object input) {
		if (input instanceof Number) {
			return ((Number) input).longValue();
		} else if (input instanceof String) {
			return Long.parseLong((String) input);
		}
		throw new FormatException("Cannot read a long from " + input);
	}

	private Float readFloat(final Object input) {
		if (input instanceof Number) {
			return ((Number) input).floatValue();
		} else if (input instanceof String) {
			return Float.parseFloat((String) input);
		}
		throw new FormatException("Cannot read a float from " + input);
	}

	private Double readDouble(final Object input) {
		if (input instanceof Number) {
			return ((Number) input).doubleValue();
		} else if (input instanceof String) {
			return Double.parseDouble((String) input);
		}
		throw new FormatException("Cannot read a double from " + input);
	}

	private String readString(final Object input) {
		if (input == null) {
			return null;
		} else if (input instanceof String) {
			return (String) input;
		}
		throw new FormatException("Cannot read a string from " + input);
	}

	private Date readDatetime(final Object input) {
		if (input == null) {
			return null;
		} else if (input instanceof Date) {
			return (Date) input;
		} else if (input instanceof String) {
			try {
				return dateFormat.get().parse((String) input);
			} catch (ParseException e) {
				throw new FormatException("Failed to read a datetime from " + input, e);
			}
		}
		throw new FormatException("Cannot read a datetime from " + input);
	}

	private <E> List<E> readList(final Object input, final ListDescriptor<E> descriptor)
			throws Exception {
		if (!(input instanceof Collection)) {
			throw new FormatException("Cannot read a list from " + input);
		}

		Collection<?> collection = (Collection<?>) input;
		DataTypeDescriptor<E> element = descriptor.getElement();
		List<E> result = new ArrayList<E>();

		for (Object elem : collection) {
			E parsed = read(element, elem);
			result.add(parsed);
		}

		return result;
	}

	private <E> Set<E> readSet(final Object input, final SetDescriptor<E> descriptor)
			throws Exception {
		if (!(input instanceof Collection)) {
			throw new FormatException("Cannot read a set from " + input);
		}

		Collection<?> collection = (Collection<?>) input;
		Set<E> result = new HashSet<E>();
		DataTypeDescriptor<E> element = descriptor.getElement();

		for (Object elem : collection) {
			E parsed = read(element, elem);
			result.add(parsed);
		}

		return result;
	}

	private <K, V> Map<K, V> readMap(final Object input, final MapDescriptor<K, V> descriptor)
			throws Exception {
		if (!(input instanceof Map)) {
			throw new FormatException("Cannot read a map from " + input);
		}

		Map<?, ?> map = (Map<?, ?>) input;
		Map<K, V> result = new HashMap<K, V>();
		DataTypeDescriptor<K> key = descriptor.getKey();
		DataTypeDescriptor<V> value = descriptor.getValue();

		for (Map.Entry<?, ?> e : map.entrySet()) {
			K k = read(key, e.getKey());
			V v = read(value, e.getValue());
			result.put(k, v);
		}

		return result;
	}

	private <T extends Enum<T>> T readEnum(final Object input,
			final EnumDescriptor<T> descriptor) {
		if (input instanceof Enum<?>) {
			return descriptor.getJavaClass().cast(input);
		} else if (input instanceof String) {
			return descriptor.getValue((String) input);
		}
		throw new FormatException("Cannot read an enum from " + input);
	}

	private <M extends Message> M readMessage(final Object input,
			MessageDescriptor<M> descriptor) throws Exception {
		if (!(input instanceof Map)) {
			throw new FormatException("Cannot read a map from " + input);
		}

		Map<?, ?> map = (Map<?, ?>) input;
		FieldDescriptor<? super M, ?> discriminator = descriptor.getDiscriminator();

		// Mind polymorphic messages.
		if (discriminator != null) {
			Object fieldValue = map.get(discriminator.getName());
			if (fieldValue != null) {
				Enum<?> discriminatorValue = (Enum<?>) read(discriminator.getType(), fieldValue);
				@SuppressWarnings("unchecked")
				MessageDescriptor<M> subtype = (MessageDescriptor<M>) descriptor
						.getSubtype(discriminatorValue);

				descriptor = subtype != null ? subtype : descriptor;
			}
		}

		M message = descriptor.newInstance();
		for (FieldDescriptor<? super M, ?> field : descriptor.getFields()) {
			@SuppressWarnings("unchecked")
			FieldDescriptor<M, ?> uncheckedField = (FieldDescriptor<M, ?>) field;
			parseField(uncheckedField, message, map);
		}

		return message;
	}

	private <M extends Message, V> void parseField(final FieldDescriptor<M, V> field,
			final M message, final Map<?, ?> map) throws Exception {
		Object fieldInput = map.get(field.getName());
		V value = read(field.getType(), fieldInput);
		field.set(message, value);
	}
}
