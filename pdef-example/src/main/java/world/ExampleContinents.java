package world;

import com.google.common.collect.ImmutableSet;
import world.continents.Continent;
import world.continents.ContinentName;
import world.continents.Continents;
import world.space.Location;

import java.util.Set;

class ExampleContinents implements Continents {
	@Override
	public Set<Continent> all() {
		return ImmutableSet.of();
	}

	@Override
	public void move(final ContinentName name, final Location to) {}
}
