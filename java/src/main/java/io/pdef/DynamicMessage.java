package io.pdef;

import io.pdef.descriptors.FieldDescriptor;
import io.pdef.descriptors.MessageDescriptor;

public abstract class DynamicMessage extends AbstractMessage {
	@Override
	public void merge(final Message message) {
		if (!(getClass().isInstance(message))) {
			return;
		}

		MessageDescriptor<Message> descriptor = uncheckedDescriptor();
		for (FieldDescriptor<? super Message, ?> field : descriptor.getFields()) {
			mergeField(field, message);
		}
	}

	private <V> void mergeField(final FieldDescriptor<? super Message, V> field,
			final Message message) {
		V value = field.get(message);
		if (value == null) {
			return;
		}

		V copy = DataTypes.copy(value, field.getType());
		field.set(this, copy);
	}
}
