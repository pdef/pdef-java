package io.pdef.json;

import io.pdef.Message;
import io.pdef.descriptors.DataTypeDescriptor;
import io.pdef.descriptors.MessageDescriptor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

/** JsonFormat parses and serializes Pdef value types from/to JSON. */
public class JsonFormat {
	private static final JsonFormat INSTANCE = new JsonFormat();
	private final JsonJacksonFormat jsonFormat;
	private final JsonObjectFormat objectFormat;

	protected JsonFormat() {
		jsonFormat = JsonJacksonFormat.getInstance();
		objectFormat = JsonObjectFormat.getInstance();
	}

	public static JsonFormat instance() {
		return INSTANCE;
	}

	// Serialization.

	/** Serializes an object into a string. */
	public <T> String write(final T object, final DataTypeDescriptor<T> descriptor,
			final boolean indent) {
		try {
			return jsonFormat.write(object, descriptor, indent);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	/** Writes an object to an output stream as a JSON string, does not close the stream. */
	public <T> void write(final OutputStream stream, final T object,
			final DataTypeDescriptor<T> descriptor, final boolean indent) {
		try {
			jsonFormat.write(stream, object, descriptor, indent);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	/** Writes an object to a writer as a JSON string, does not close the writer. */
	public <T> void write(final PrintWriter writer, final T object,
			final DataTypeDescriptor<T> descriptor, final boolean indent) {
		try {
			jsonFormat.write(writer, object, descriptor, indent);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	/** Parses an object from a string. */
	public <T> T read(final String s, final DataTypeDescriptor<T> descriptor) {
		try {
			return jsonFormat.read(s, descriptor);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	/** Parses an object from an input stream, does not close the input stream. */
	public <T> T read(final InputStream stream, final DataTypeDescriptor<T> descriptor) {
		try {
			return jsonFormat.read(stream, descriptor);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	/** Parses an object from a reader, does not close the reader. */
	public <T> T read(final Reader reader, final DataTypeDescriptor<T> descriptor) {
		try {
			return jsonFormat.read(reader, descriptor);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	/** Converts a message into a JSON-compatible map. */
	@SuppressWarnings("unchecked")
	public <T extends Message> Map<String, Object> writeMessage(final T message,
			final MessageDescriptor<T> descriptor) {
		try {
			return (Map<String, Object>) objectFormat.write(message, descriptor);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	/** Converts an object into a JSON-compatible object. */
	public <T> Object writeObject(final T object, final DataTypeDescriptor<T> descriptor) {
		try {
			return objectFormat.write(object, descriptor);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	/** Parses an object from a JSON-compatible object. */
	public <T> T readObject(final Object object, final DataTypeDescriptor<T> descriptor) {
		try {
			return objectFormat.read(object, descriptor);
		} catch (Exception e) {
			throw propagate(e);
		}
	}

	protected JsonFormatException propagate(final Exception e) {
		if (e instanceof JsonFormatException) {
			return (JsonFormatException) e;
		}
		return new JsonFormatException(e);
	}
}
