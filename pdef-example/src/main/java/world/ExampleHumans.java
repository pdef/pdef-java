package world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

class ExampleHumans implements Humans {
	private final AtomicLong idSequence = new AtomicLong();
	private final Map<Long, Human> map = Maps.newHashMap();
	private final List<Human> list = Lists.newArrayList();

	@Override
	public synchronized Human find(final long id) {
		Human human = map.get(id);
		return human == null ? null : human.copy();
	}

	@Override
	public synchronized List<Human> all(int limit, int offset) {
		if (limit < 0 || offset < 0) {
			return ImmutableList.of();
		}

		int toIndex = offset + limit;
		if (offset > list.size()) {
			return ImmutableList.of();
		}
		toIndex = toIndex <= list.size() ? toIndex : list.size();

		List<Human> result = Lists.newArrayList();
		for (Human human : list.subList(offset, toIndex)) {
			result.add(human.copy());
		}
		return result;
	}

	@Override
	public synchronized Human create(final Human human) {
		if (human == null) {
			return null;
		}

		Human newHuman = new Human();
		newHuman.merge(human);
		newHuman.setId(idSequence.incrementAndGet());

		map.put(newHuman.getId(), newHuman);
		list.add(newHuman);
		return newHuman.copy();
	}
}
