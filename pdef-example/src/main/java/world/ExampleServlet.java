package world;

import io.pdef.rpc.RpcHandler;
import io.pdef.rpc.RpcServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ExampleServlet extends HttpServlet {
	private transient RpcServlet<World> delegate;
	private World world;

	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);

		world = new ExampleWorld();
		RpcHandler<World> handler = new RpcHandler<World>(World.DESCRIPTOR, world);
		delegate = new RpcServlet<World>(handler);
	}

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		delegate.service(req, resp);
	}
}
