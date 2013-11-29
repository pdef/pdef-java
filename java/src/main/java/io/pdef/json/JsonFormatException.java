package io.pdef.json;

/**
 * JsonFormatException wraps all parsing/serialization exceptions.
 * */
public class JsonFormatException extends RuntimeException {
	public JsonFormatException() {}

	public JsonFormatException(final String s) {
		super(s);
	}

	public JsonFormatException(final String s, final Throwable throwable) {
		super(s, throwable);
	}

	public JsonFormatException(final Throwable throwable) {
		super(throwable);
	}
}
