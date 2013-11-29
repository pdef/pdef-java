package io.pdef.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.pdef.Message;
import io.pdef.descriptors.*;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;

class JsonJacksonFormat {
	private static final JsonJacksonFormat INSTANCE = new JsonJacksonFormat();
	private final JsonFactory factory;
	private final JsonObjectFormat objectFormat;

	public static JsonJacksonFormat getInstance() {
		return INSTANCE;
	}

	private JsonJacksonFormat() {
		factory = new JsonFactory().enable(JsonParser.Feature.ALLOW_COMMENTS);
		objectFormat = JsonObjectFormat.getInstance();
	}

	// Serialization.

	/** Serializes an object into a string. */
	public <T> String write(final T object, final DataTypeDescriptor<T> descriptor,
			final boolean indent) throws IOException {
		if (descriptor == null) throw new NullPointerException("descriptor");

		StringWriter out = new StringWriter();
		JsonGenerator generator = factory.createGenerator(out);
		if (indent) {
			generator.useDefaultPrettyPrinter();
		}

		write(object, descriptor, generator);
		generator.flush();
		return out.toString();
	}

	/** Writes an object to an output stream as a JSON string, does not close the stream. */
	public <T> void write(final OutputStream stream, final T object,
			final DataTypeDescriptor<T> descriptor, final boolean indent) throws IOException {
		if (stream == null) throw new NullPointerException("out");
		if (descriptor == null) throw new NullPointerException("descriptor");

		JsonGenerator generator = factory.createGenerator(stream);
		if (indent) {
			generator.useDefaultPrettyPrinter();
		}

		write(object, descriptor, generator);
		generator.flush();
	}

	/** Writes an object to a writer as a JSON string, does not close the writer. */
	public <T> void write(final PrintWriter writer, final T object,
			final DataTypeDescriptor<T> descriptor, final boolean indent) throws IOException {
		if (writer == null) throw new NullPointerException("writer");
		if (descriptor == null) throw new NullPointerException("descriptor");

		JsonGenerator generator = factory.createGenerator(writer);
		if (indent) {
			generator.useDefaultPrettyPrinter();
		}

		write(object, descriptor, generator);
		generator.flush();
	}

	private <T> void write(final T object, final DataTypeDescriptor<T> descriptor,
			final JsonGenerator generator) throws IOException {
		if (object == null) {
			generator.writeNull();
			return;
		}

		switch (descriptor.getType()) {
			case BOOL: generator.writeBoolean((Boolean) object); break;
			case INT16: generator.writeNumber((Short) object); break;
			case INT32: generator.writeNumber((Integer) object); break;
			case INT64: generator.writeNumber((Long) object); break;
			case FLOAT: generator.writeNumber((Float) object); break;
			case DOUBLE: generator.writeNumber((Double) object); break;
			case STRING: generator.writeString((String) object); break;
			case DATETIME: writeDatetime((Date) object, generator); break;
			case LIST: writeList((List) object, (ListDescriptor) descriptor, generator); break;
			case SET: writeSet((Set) object, (SetDescriptor) descriptor, generator); break;
			case MAP: writeMap((Map) object, (MapDescriptor) descriptor, generator); break;
			case VOID: generator.writeNull(); break;
			case ENUM: writeEnum((Enum) object, generator); break;
			case MESSAGE: writeMessage((Message) object, generator); break;
			default: throw new JsonFormatException("Unsupported descriptor " + descriptor);
		}
	}

	private void writeDatetime(final Date date, final JsonGenerator generator) throws IOException {
		String s = objectFormat.writeDate(date);
		generator.writeString(s);
	}

	private <T> void writeList(@Nonnull final List<T> object, final ListDescriptor<T> descriptor,
			final JsonGenerator generator) throws IOException {
		DataTypeDescriptor<T> elementd = descriptor.getElement();

		generator.writeStartArray();
		for (T element : object) {
			write(element, elementd, generator);
		}
		generator.writeEndArray();
	}

	private <T> void writeSet(@Nonnull final Set<T> object, final SetDescriptor<T> descriptor,
			final JsonGenerator generator) throws IOException {
		DataTypeDescriptor<T> elementd = descriptor.getElement();

		generator.writeStartArray();
		for (T element : object) {
			write(element, elementd, generator);
		}
		generator.writeEndArray();
	}

	private <K, V> void writeMap(@Nonnull final Map<K, V> object,
			final MapDescriptor<K, V> descriptor, final JsonGenerator generator)
			throws IOException {
		DataTypeDescriptor<K> keyd = descriptor.getKey();
		DataTypeDescriptor<V> valued = descriptor.getValue();

		generator.writeStartObject();
		for (Map.Entry<K, V> entry : object.entrySet()) {
			K key = entry.getKey();
			V value = entry.getValue();
			if (key == null) {
				throw new JsonFormatException("Null map key");
			}

			String k = objectFormat.writeMapKey(key, keyd);
			generator.writeFieldName(k);
			write(value, valued, generator);
		}
		generator.writeEndObject();
	}

	private <T extends Enum<T>> void writeEnum(@Nonnull final T object,
			final JsonGenerator generator) throws IOException {
		String value = objectFormat.writeEnum(object);
		generator.writeString(value);
	}

	private <T extends Message> void writeMessage(@Nonnull final T object,
			final JsonGenerator generator) throws IOException {
		// Mind polymorphic messages.
		@SuppressWarnings("unchecked")
		MessageDescriptor<T> polymorphic = (MessageDescriptor<T>) object.descriptor();

		generator.writeStartObject();
		for (FieldDescriptor<? super T, ?> field : polymorphic.getFields()) {
			writeMessageField(field, object, generator);
		}
		generator.writeEndObject();
	}

	private <T extends Message, V> void writeMessageField(
			final FieldDescriptor<? super T, V> field, final T message,
			final JsonGenerator generator) throws IOException {
		V value = field.get(message);
		if (value == null) {
			// Skip null fields.
			return;
		}

		DataTypeDescriptor<V> type = field.getType();
		generator.writeFieldName(field.getName());
		write(value, type, generator);
	}

	// Parsing.

	/** Parses an object from a string. */
	public <T> T read(final String s, final DataTypeDescriptor<T> descriptor)
			throws Exception {
		if (descriptor == null) throw new NullPointerException("descriptor");
		if (s == null) return null;

		JsonParser parser = factory.createParser(s);
		return read(parser, descriptor);
	}

	/** Parses an object from an input stream, does not close the input stream. */
	public <T> T read(final InputStream stream, final DataTypeDescriptor<T> descriptor)
			throws Exception {
		if (stream == null) throw new NullPointerException("input");
		if (descriptor == null) throw new NullPointerException("descriptor");

		JsonParser parser = factory.createParser(stream);
		return read(parser, descriptor);
	}

	/** Parses an object from a reader, does not close the reader. */
	public <T> T read(final Reader reader, final DataTypeDescriptor<T> descriptor)
			throws Exception {
		if (reader == null) throw new NullPointerException("reader");
		if (descriptor == null) throw new NullPointerException("descriptor");

		JsonParser parser = factory.createParser(reader);
		return read(parser, descriptor);
	}

	private <T> T read(final JsonParser parser, final DataTypeDescriptor<T> descriptor)
			throws Exception {
		Object jsonObject;

		try {
			parser.nextToken();
			jsonObject = read(parser);
		} finally {
			parser.close();
		}

		return objectFormat.read(jsonObject, descriptor);
	}

	private Object read(final JsonParser parser) throws IOException {
		JsonToken current = parser.getCurrentToken();
		if (current == null || current == JsonToken.VALUE_NULL) {
			return null;
		}

		switch (current) {
			case VALUE_NULL: return null;
			case VALUE_TRUE: return true;
			case VALUE_FALSE: return false;
			case VALUE_STRING: return parser.getValueAsString();
			case VALUE_NUMBER_INT:
			case VALUE_NUMBER_FLOAT: return parser.getNumberValue();
			case START_ARRAY: return readArray(parser);
			case START_OBJECT: return readMap(parser);
			default: throw new JsonFormatException("Bad JSON string");
		}
	}

	private List<?> readArray(final JsonParser parser) throws IOException {
		JsonToken current = parser.getCurrentToken();
		if (current != JsonToken.START_ARRAY) {
			throw new JsonFormatException("Bad JSON string, failed to read an array");
		}

		List<Object> list = new ArrayList<Object>();
		while (true) {
			JsonToken next = parser.nextToken();
			if (next == null) {
				throw new JsonFormatException("End of file");
			} else if (next == JsonToken.END_ARRAY) {
				break;
			}

			Object element = read(parser);
			list.add(element);
		}

		return list;
	}

	private Map<String, Object> readMap(final JsonParser parser) throws IOException {
		JsonToken current = parser.getCurrentToken();
		if (current != JsonToken.START_OBJECT) {
			throw new JsonFormatException("Bad JSON string, failed to read an object");
		}

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		while (true) {
			JsonToken next = parser.nextToken();
			if (next == null) {
				throw new JsonFormatException("End of file");
			} else if (next == JsonToken.END_OBJECT) {
				break;
			} else if (next != JsonToken.FIELD_NAME) {
				throw new JsonFormatException("Failed to read a field name from " + next);
			}

			String field = parser.getCurrentName();
			parser.nextToken();
			Object value = read(parser);
			map.put(field, value);
		}

		return map;
	}
}
