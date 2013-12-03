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

import io.pdef.descriptors.FieldDescriptor;
import io.pdef.descriptors.MessageDescriptor;
import io.pdef.json.JsonFormat;

import java.io.*;
import java.util.Map;

/**
 * Abstract class for a generated Pdef message.
 * */
public abstract class AbstractMessage implements Message, Serializable {
	protected AbstractMessage() {}

	protected AbstractMessage(final AbstractMessage another) {}

	@Override
	public Map<String, Object> toMap() {
		return JsonFormat.instance().writeMessage(this, uncheckedDescriptor());
	}

	@Override
	public String toJson() {
		return toJson(true);
	}

	@Override
	public String toJson(final boolean indent) {
		return JsonFormat.instance().write(this, uncheckedDescriptor(), indent);
	}

	@Override
	public void toJson(final PrintWriter writer, final boolean indent) {
		JsonFormat.instance().write(writer, this, uncheckedDescriptor(), indent);
	}

	@Override
	public void toJson(final OutputStream stream, final boolean indent) {
		JsonFormat.instance().write(stream, this, uncheckedDescriptor(), indent);
	}

	@Override
	public Message copy() {
		Message another = uncheckedDescriptor().newInstance();
		another.merge(this);
		return another;
	}

	@Override
	public void merge(final Message message) {}

	@Override
	public void merge(final Map<String, Object> map) {
		Message message = JsonFormat.instance().readObject(map, uncheckedDescriptor());
		merge(message);
	}

	@Override
	public void mergeJson(final String s) {
		Message message = JsonFormat.instance().read(s, uncheckedDescriptor());
		merge(message);
	}

	@Override
	public void mergeJson(final Reader reader) {
		Message message = JsonFormat.instance().read(reader, uncheckedDescriptor());
		merge(message);
	}

	@Override
	public void mergeJson(final InputStream stream) {
		Message message = JsonFormat.instance().read(stream, uncheckedDescriptor());
		merge(message);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append('{');

		String nextSeparator = "";
		MessageDescriptor<Message> descriptor = uncheckedDescriptor();
		for (FieldDescriptor<? super Message, ?> field : descriptor.getFields()) {
			Object value = field.get(this);
			if (value == null) {
				continue;
			}

			sb.append(nextSeparator);
			sb.append(field.getName()).append('=').append(value);
			nextSeparator = ", ";
		}

		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Message cast = (Message) o;
		MessageDescriptor<Message> descriptor = uncheckedDescriptor();
		for (FieldDescriptor<? super Message, ?> field : descriptor.getFields()) {
			Object value0 = field.get(this);
			Object value1 = field.get(cast);
			if (value0 != null ? !value0.equals(value1) : value1 != null) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = 0;

		MessageDescriptor<Message> descriptor = uncheckedDescriptor();
		for (FieldDescriptor<? super Message, ?> field : descriptor.getFields()) {
			Object value = field.get(this);
			result = 31 * result + (value == null ? 0 : value.hashCode());
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	MessageDescriptor<Message> uncheckedDescriptor() {
		return (MessageDescriptor<Message>) descriptor();
	}
}
