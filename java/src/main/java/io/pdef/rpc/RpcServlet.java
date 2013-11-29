package io.pdef.rpc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public final class RpcServlet<T> extends HttpServlet {
	public static final String CLIENT_ERROR_MESSAGE = "Client error";
	public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	public static final String TEXT_CONTENT_TYPE = "text/plain; charset=utf-8";
	public static final int APPLICATION_EXC_STATUS = 422;
	private final transient RpcHandler<T> handler;

	public RpcServlet(final RpcHandler<T> handler) {
		if (handler == null) throw new NullPointerException("handler");
		this.handler = handler;
	}

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		if (req == null) throw new NullPointerException("request");
		if (resp == null) throw new NullPointerException("response");

		RpcRequest request = getRpcRequest(req);
		try {
			RpcResult<?, ?> result = handler.handle(request);
			writeResult(result, resp);
		} catch (RpcException e) {
			writeRpcException(e, resp);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// VisibleForTesting
	RpcRequest getRpcRequest(final HttpServletRequest request) {
		String method = request.getMethod();

		String path = nullToEmpty(request.getRequestURI());
		String context = nullToEmpty(request.getContextPath());
		if (path.startsWith(context)) {
			path = path.substring(context.length());
		}

		// In servlets we cannot distinguish between query and post params,
		// so we use the same map for both. It is safe because Pdef HTTP RPC
		// always uses only one of them.
		Map<String, String> params = getParams(request);

		return new RpcRequest()
				.setMethod(method)
				.setPath(path)
				.setQuery(params)
				.setPost(params);
	}

	// VisibleForTesting
	void writeResult(final RpcResult<?, ?> result, final HttpServletResponse resp)
			throws IOException {
		if (result.isSuccess()) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType(JSON_CONTENT_TYPE);
		} else {
			resp.setStatus(APPLICATION_EXC_STATUS);
			resp.setContentType(JSON_CONTENT_TYPE);
		}

		PrintWriter writer = resp.getWriter();
		result.toJson(writer, true);
		writer.flush();
	}

	// VisibleForTesting
	void writeRpcException(final RpcException e, final HttpServletResponse resp)
			throws IOException {
		String message = e.getMessage() != null ? e.getMessage() : CLIENT_ERROR_MESSAGE;

		resp.setStatus(e.getStatus());
		resp.setContentType(TEXT_CONTENT_TYPE);
		resp.getWriter().write(message);
	}

	private Map<String, String> getParams(final HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();

		@SuppressWarnings("unchecked")
		Map<String, String[]> map = request.getParameterMap();
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			String key = entry.getKey();
			String[] values = entry.getValue();
			if (values == null || values.length == 0) {
				continue;
			}

			String value = values[0];
			params.put(key, value);
		}

		return params;
	}

	private static String nullToEmpty(final String s) {
		return s != null ? s : "";
	}
}
