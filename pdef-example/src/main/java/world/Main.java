package world;

import io.pdef.rpc.RpcClient;
import io.pdef.rpc.RpcHandler;
import io.pdef.rpc.RpcServlet;
import world.continents.ContinentName;
import world.space.Location;

public class Main {
	public static void format() {
		// Read a human from a JSON string or stream.
		Human human = Human.fromJson("");
		human.setContinent(ContinentName.NORTH_AMERICA);

		// Serialize a human to a JSON string.
		String json = human.toJson();
		human.toJson();
	}

	public static void client() {
		// Create an HTTP RPC client.
		RpcClient<World> client = new RpcClient<World>(World.DESCRIPTOR, "http://example.com/world/");
		World world = client.proxy();

		// Create a man.
		Human man = world.humans().create(new Human()
				.setId(1)
				.setName("John")
				.setSex(Sex.MALE)
				.setContinent(ContinentName.ASIA));

		// Switch day/night.
		world.switchDayNight();

		// Move the continents.
		world.continents().move(ContinentName.AFRICA, new Location()
				.setLat(8.887629)
				.setLng(22.719154));
	}

	public static void server() {
		World world = getMyWorldImplementation();
		RpcHandler<World> handler = new RpcHandler<World>(World.DESCRIPTOR, world);
		RpcServlet<World> servlet = new RpcServlet<World>(handler);

		// Pass it to your servlet container,
		// or wrap in another servlet as a delegate.
	}

	public static World getMyWorldImplementation() {
		return null;
	}
}
