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

package io.pdef.rpc;

import io.pdef.test.inheritance.PdefBase;
import io.pdef.test.inheritance.PdefMultiLevelSubtype;
import io.pdef.test.interfaces.PdefTestException;
import io.pdef.test.interfaces.PdefTestSubException;
import io.pdef.test.interfaces.PdefTestSubInterface;
import io.pdef.test.messages.PdefTestEnum;
import io.pdef.test.messages.PdefTestMessage;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class RpcTest {
	PdefTestSubInterface service;
	String address;
	Server server;
	Thread serverThread;

	@Before
	public void setUp() throws Exception {
		service = mock(PdefTestSubInterface.class);

		RpcHandler<PdefTestSubInterface> handler = new RpcHandler<PdefTestSubInterface>(
				PdefTestSubInterface.DESCRIPTOR, service);
		HttpServlet servlet = new Servlet(new RpcServlet<PdefTestSubInterface>(handler));
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/context");
		context.addServlet(new ServletHolder(servlet), "/servlet/*");

		server = new Server(0);
		server.setHandler(context);
		server.start();

		address = "http://localhost:" + server.getConnectors()[0].getLocalPort() + "/context/servlet";
		serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					server.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		serverThread.start();
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
		serverThread.interrupt();
	}

	@Test
	public void test() throws Exception {
		PdefTestMessage message = new PdefTestMessage()
				.setString0("Привет, как дела?")
				.setBool0(false)
				.setInt0(-123);
		PdefBase base = new PdefMultiLevelSubtype()
				.setField("hello")
				.setMfield("world")
				.setSubfield("really");
		Date date = new Date(0);

		when(service.method(1, 2)).thenReturn(3);
		when(service.query(3, 4)).thenReturn(7);
		when(service.post(5, 6)).thenReturn(11);
		when(service.string0("Привет")).thenReturn("Привет");
		when(service.datetime0(date)).thenReturn(new Date(date.getTime()));
		when(service.enum0(PdefTestEnum.THREE)).thenReturn(PdefTestEnum.THREE);
		when(service.message0(message.copy())).thenReturn(message.copy());
		when(service.polymorphic0(base)).thenReturn(base.copy());
		when(service.interface0(1, 2)).thenReturn(service);
		doThrow(new PdefTestSubException().setText("Application exception")).when(service).exc0();
		doThrow(new RuntimeException("Test server exception")).when(service).serverError();

		PdefTestSubInterface client = new RpcClient<PdefTestSubInterface>(
				PdefTestSubInterface.DESCRIPTOR, address).proxy();
		assertEquals(3, client.method(1, 2));
		assertEquals(7, client.query(3, 4));
		assertEquals(11, client.post(5, 6));
		assertEquals("Привет", client.string0("Привет"));
		assertEquals(date, client.datetime0(date));
		assertEquals(PdefTestEnum.THREE, client.enum0(PdefTestEnum.THREE));
		assertEquals(message, client.message0(message));
		assertEquals(base, client.polymorphic0(base));
		assertEquals(7, client.interface0(1, 2).query(3, 4));
		client.void0(); // No Exception.

		try {
			client.exc0();
			fail();
		} catch (PdefTestException e) {
			PdefTestException exc = new PdefTestException().setText("Application exception");
			assertEquals(exc, e);
		}

		try {
			client.serverError();
			fail();
		} catch (RpcException e) {
			// Ignore exception.
		}

		client.subMethod();
		verify(service).subMethod();
	}

	static class Servlet extends HttpServlet {
		private final RpcServlet<PdefTestSubInterface> delegate;

		Servlet(final RpcServlet<PdefTestSubInterface> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected void service(final HttpServletRequest req, final HttpServletResponse resp)
				throws ServletException, IOException {
			try {
				delegate.service(req, resp);
			} catch (RuntimeException e) {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				// Ignore in tests.
			}
		}
	}
}
