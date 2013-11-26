package io.pdef;

import io.pdef.descriptors.MessageDescriptor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public interface Message {
	/**
	 * Returns this message descriptor.
	 */
	MessageDescriptor<? extends Message> descriptor();
	
	/**
	 * Serializes this message to a map.
	 */
	Map<String, Object> toMap();

	/**
	 * Serializes this message to a JSON string without indentation.
	 */
	String toJson();

	/**
	 * Serializes this message to a JSON string with optional indentation.
	 */
	String toJson(boolean indent);

	/**
	 * Serializes this message to JSON and writes to a writer.
	 */
	void toJson(PrintWriter writer, boolean indent);

	/**
	 * Serializes this message to JSON and writes to an input stream.
	 */
	void toJson(OutputStream stream, boolean indent);

	/**
	 * Returns a deep copy of this message.
	 */
	Message copy();

	/**
	 * Deeply copies all present fields from another message into this message.
	 */
	void merge(Message message);

	/**
	 * Parse a message from a map and merge it into this message.
	 */
	void merge(Map<String, Object> map);

	/**
	 * Parse a message from a JSON string and merge it into this message.
	 */
	void mergeJson(String s);

	/**
	 * Parse a message from a JSON reader and merge it into this message.
	 */
	void mergeJson(Reader reader);

	/**
	 * Parse a message from a JSON input stream and merge it into this message.
	 */
	void mergeJson(InputStream stream);
}
