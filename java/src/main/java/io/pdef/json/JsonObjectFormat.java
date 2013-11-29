package io.pdef.json;

import io.pdef.Message;
import io.pdef.TypeEnum;
import io.pdef.descriptors.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class JsonObjectFormat {
	private static final JsonObjectFormat INSTANCE = new JsonObjectFormat();
	private final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			TimeZone tz = TimeZone.getTimeZone("UTC");
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			format.setTimeZone(tz);
			return format;
		}
	};

	public static JsonObjectFormat getInstance() {
		return INSTANCE;
	}

	private JsonObjectFormat() {}

	// Serializing.

	@SuppressWarnings("unchecked")
	public <T> Object write(final T object, final DataTypeDescriptor<T> descriptor)
			throws Exception {
		if (descriptor == null) throw new NullPointerException("descriptor");
		if (object == null) return null;

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
			case ENUM: return writeEnum((Enum) object);
			case LIST: return writeList((List) object, (ListDescriptor) descriptor);
			case SET: return writeSet((Set) object, (SetDescriptor) descriptor);
			case MAP: return writeMap((Map) object, (MapDescriptor) descriptor);
			case MESSAGE: return writeMessage((Message) object);
			case VOID: return null;
			default: throw new IllegalArgumentException("Unsupported descriptor " + descriptor);
		}
	}

	String writeDate(final Date date) {
		return date == null ? "null" : dateFormat.get().format(date);
	}

	<E extends Enum<E>> String writeEnum(final E value) {
		return value == null ? "null" : value.toString().toLowerCase();
	}

	private <E> List<Object> writeList(final List<E> list, final ListDescriptor<E> descriptor)
			throws Exception {
		if (list == null) {
			return null;
		}

		DataTypeDescriptor<E> elementd = descriptor.getElement();
		List<Object> result = new ArrayList<Object>();

		for (E element : list) {
			Object serialized = write(element, elementd);
			result.add(serialized);
		}

		return result;
	}

	private <E> Set<Object> writeSet(final Set<E> set, final SetDescriptor<E> descriptor)
			throws Exception {
		if (set == null) {
			return null;
		}

		DataTypeDescriptor<E> elementd = descriptor.getElement();
		Set<Object> result = new HashSet<Object>();
		for (E element : set) {
			Object serialized = write(element, elementd);
			result.add(serialized);
		}

		return result;
	}

	private <K, V> Map<Object, Object> writeMap(final Map<K, V> map,
			final MapDescriptor<K, V> descriptor) throws Exception {
		if (map == null) {
			return null;
		}

		DataTypeDescriptor<K> keyd = descriptor.getKey();
		DataTypeDescriptor<V> valued = descriptor.getValue();
		Map<Object, Object> result = new HashMap<Object, Object>();

		for (Map.Entry<K, V> e : map.entrySet()) {
			K key = e.getKey();
			V value = e.getValue();
			if (key == null) {
				throw new JsonFormatException("Null map key");
			}

			String k = writeMapKey(key, keyd);
			Object v = write(value, valued);
			result.put(k, v);
		}

		return result;
	}

	public <K> String writeMapKey(final K key, final DataTypeDescriptor<K> descriptor) {
		if (key == null) {
			throw new JsonFormatException("Null map key, must be a pdef primitive.");
		}

		switch (descriptor.getType()) {
			case BOOL: return ((Boolean) key) ? "true" : "false";
			case INT16:
			case INT32:
			case INT64:
			case FLOAT:
			case DOUBLE:
			case STRING: return key.toString();
			case DATETIME: return dateFormat.get().format((Date) key);
			default: throw new JsonFormatException("Unsupported map key descriptor " + descriptor);
		}
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

		Object serialized = write(value, field.getType());
		map.put(field.getName(), serialized);
	}

	// Parsing.

	public <T> T read(final Object object, final DataTypeDescriptor<T> descriptor)
			throws Exception {
		if (descriptor == null) throw new NullPointerException("descriptor");

		return doRead(object, descriptor);
	}

	@SuppressWarnings("unchecked")
	private <T> T doRead(final Object object, final DataTypeDescriptor<T> descriptor)
			throws Exception {
		if (object == null) {
			return null;
		}

		TypeEnum typeEnum = descriptor.getType();
		switch (typeEnum) {
			case BOOL: return (T) readBoolean(object);
			case INT16: return (T) readShort(object);
			case INT32: return (T) readInt(object);
			case INT64: return (T) readLong(object);
			case FLOAT: return (T) readFloat(object);
			case DOUBLE: return (T) readDouble(object);
			case STRING: return (T) readString(object);
			case DATETIME: return (T) readDatetime(object);
			case LIST: return (T) readList(object, (ListDescriptor<?>) descriptor);
			case SET: return (T) readSet(object, (SetDescriptor<?>) descriptor);
			case MAP: return (T) readMap(object, (MapDescriptor<?, ?>) descriptor);
			case ENUM: return (T) readEnum(object, (EnumDescriptor<? extends Enum<?>>) descriptor);
			case MESSAGE: return (T) readMessage(object, (MessageDescriptor<? extends Message>) descriptor);
			case VOID: return null;
			default: throw new IllegalArgumentException("Unsupported descriptor " + descriptor);
		}
	}

	private Boolean readBoolean(final Object input) {
		if (input instanceof Boolean) {
			return (Boolean) input;
		}
		return Boolean.parseBoolean((String) input);
	}

	private Short readShort(final Object input) {
		if (input instanceof Number) {
			return ((Number) input).shortValue();
		}
		return Short.parseShort((String) input);
	}

	private Integer readInt(final Object input) {
		if (input instanceof Number) {
			return ((Number) input).intValue();
		}
		return Integer.parseInt((String) input);
	}

	private Long readLong(final Object input) {
		if (input instanceof Number) {
			return ((Number) input).longValue();
		}
		return Long.parseLong((String) input);
	}

	private Float readFloat(final Object input) {
		if (input instanceof Number) {
			return ((Number) input).floatValue();
		}
		return Float.parseFloat((String) input);
	}

	private Double readDouble(final Object input) {
		if (input instanceof Number) {
			return ((Number) input).doubleValue();
		}
		return Double.parseDouble((String) input);
	}

	private String readString(final Object input) {
		if (input == null) {
			return null;
		}
		return (String) input;
	}

	private Date readDatetime(final Object input) throws ParseException {
		if (input instanceof Date) {
			return new Date(((Date) input).getTime());
		}
		return dateFormat.get().parse((String) input);
	}

	private <T extends Enum<T>> T readEnum(final Object input,
			final EnumDescriptor<T> descriptor) {
		if (input instanceof Enum<?>) {
			return descriptor.getJavaClass().cast(input);
		}
		return descriptor.getValue((String) input);
	}

	private <E> List<E> readList(final Object input, final ListDescriptor<E> descriptor)
			throws Exception {
		Collection<?> collection = (Collection<?>) input;
		DataTypeDescriptor<E> elementd = descriptor.getElement();
		List<E> result = new ArrayList<E>();

		for (Object element : collection) {
			E parsed = doRead(element, elementd);
			result.add(parsed);
		}

		return result;
	}

	private <E> Set<E> readSet(final Object input, final SetDescriptor<E> descriptor)
			throws Exception {
		Collection<?> collection = (Collection<?>) input;
		Set<E> result = new HashSet<E>();
		DataTypeDescriptor<E> elementd = descriptor.getElement();

		for (Object element : collection) {
			E parsed = doRead(element, elementd);
			result.add(parsed);
		}

		return result;
	}

	private <K, V> Map<K, V> readMap(final Object input, final MapDescriptor<K, V> descriptor)
			throws Exception {
		Map<?, ?> map = (Map<?, ?>) input;
		Map<K, V> result = new HashMap<K, V>();
		DataTypeDescriptor<K> keyd = descriptor.getKey();
		DataTypeDescriptor<V> valued = descriptor.getValue();

		for (Map.Entry<?, ?> e : map.entrySet()) {
			Object key = e.getKey();
			Object value = e.getValue();

			K k = readMapKey(key, keyd);
			V v = doRead(value, valued);
			result.put(k, v);
		}

		return result;
	}

	public <K> K readMapKey(final Object key, final DataTypeDescriptor<K> descriptor)
			throws Exception {
		return doRead(key, descriptor);
	}

	private <M extends Message> M readMessage(final Object input,
			MessageDescriptor<M> descriptor) throws Exception {
		Map<?, ?> map = (Map<?, ?>) input;
		FieldDescriptor<? super M, ?> discriminator = descriptor.getDiscriminator();

		// Mind polymorphic messages.
		if (discriminator != null) {
			Object fieldValue = map.get(discriminator.getName());
			if (fieldValue != null) {
				Enum<?> discriminatorValue = (Enum<?>) doRead(fieldValue, discriminator.getType());
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
		V value = doRead(fieldInput, field.getType());
		field.set(message, value);
	}
}
