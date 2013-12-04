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
