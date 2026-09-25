package com.calc.api.agent;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * The one way the sub-agents reach the calculator: a tools/call on the MCP server. This
 * replaced the four REST tool classes -- the MCP server is the tool layer now.
 *
 * A tool error (isError) is the calculator refusing the operation, e.g. division by
 * zero, and becomes an ArithmeticException carrying the server's message, so
 * ChatController answers 400 exactly as it did when DivisionTool threw it. Failing to
 * reach the MCP server at all is a different thing and becomes
 * {@link CalculatorUnavailableException}.
 */
@Component
public class CalcMcpClient {

	private final McpSyncClient client;

	public CalcMcpClient(List<McpSyncClient> clients) {
		if (clients.size() != 1) {
			throw new IllegalStateException("Expected exactly one MCP connection (calc), found " + clients.size());
		}
		this.client = clients.get(0);
	}

	public double call(String tool, int a, int b) {
		CallToolRequest request = CallToolRequest.builder(tool).arguments(Map.of("a", a, "b", b)).build();
		CallToolResult result;
		try {
			ensureInitialized();
			result = client.callTool(request);
		}
		catch (RuntimeException first) {
			result = retryOnFreshSession(request, first);
		}

		String text = text(result);
		if (Boolean.TRUE.equals(result.isError())) {
			throw new ArithmeticException(text);
		}
		return Double.parseDouble(text);
	}

	/**
	 * A restarted MCP server has forgotten this client's session, so the first call after
	 * a restart fails even though the server is back. Handshake again and try once more;
	 * without this, one question is lost after every restart.
	 */
	private synchronized CallToolResult retryOnFreshSession(CallToolRequest request, RuntimeException first) {
		try {
			client.initialize();
			return client.callTool(request);
		}
		catch (RuntimeException second) {
			if (second != first) {
				second.addSuppressed(first);
			}
			throw new CalculatorUnavailableException(second);
		}
	}

	/**
	 * spring.ai.mcp.client.initialized=false leaves the handshake to first use, so this
	 * app starts without the MCP server. Retried on every call until it succeeds.
	 */
	private synchronized void ensureInitialized() {
		if (!client.isInitialized()) {
			client.initialize();
		}
	}

	private static String text(CallToolResult result) {
		return ((TextContent) result.content().get(0)).text();
	}
}
