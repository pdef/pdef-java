package world;

import com.google.common.collect.ImmutableList;
import world.continents.Continents;

import java.util.List;

class ExampleWorld implements World {
	private final ExampleHumans humans = new ExampleHumans();
	private final ExampleContinents continents = new ExampleContinents();

	@Override
	public Humans humans() {
		return humans;
	}

	@Override
	public Continents continents() {
		return continents;
	}

	@Override
	public Void switchDayNight() {
		return null;
	}

	@Override
	public List<Event> events(final Integer limit, final Long offset) {
		return ImmutableList.of();
	}
}
