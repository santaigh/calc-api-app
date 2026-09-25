package com.calc.api.agent;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** McpSyncClient is mocked -- no MCP server needed. */
class CalcMcpClientTests {

	private McpSyncClient mcpClient;
	private CalcMcpClient calcMcpClient;

	@BeforeEach
	void setUp() {
		mcpClient = mock(McpSyncClient.class);
		when(mcpClient.isInitialized()).thenReturn(true);
		calcMcpClient = new CalcMcpClient(List.of(mcpClient));
	}

	@Test
	void callsTheNamedToolWithBothOperandsAndParsesTheResult() {
		when(mcpClient.callTool(any())).thenReturn(result("84.0", false));

		assertThat(calcMcpClient.call("multiply", 12, 7)).isEqualTo(84.0);

		ArgumentCaptor<CallToolRequest> request = ArgumentCaptor.forClass(CallToolRequest.class);
		verify(mcpClient).callTool(request.capture());
		assertThat(request.getValue().name()).isEqualTo("multiply");
		assertThat(request.getValue().arguments()).isEqualTo(Map.of("a", 12, "b", 7));
	}

	@Test
	void aToolErrorBecomesArithmeticExceptionWithTheServersMessage() {
		when(mcpClient.callTool(any())).thenReturn(result("Division by zero is not allowed", true));

		assertThatThrownBy(() -> calcMcpClient.call("divide", 7, 0))
				.isInstanceOf(ArithmeticException.class)
				.hasMessage("Division by zero is not allowed");
	}

	@Test
	void anUnreachableMcpServerBecomesCalculatorUnavailable() {
		when(mcpClient.isInitialized()).thenReturn(false);
		when(mcpClient.initialize()).thenThrow(new IllegalStateException("Connection refused"));

		assertThatThrownBy(() -> calcMcpClient.call("add", 1, 1))
				.isInstanceOf(CalculatorUnavailableException.class)
				.hasMessage("The calculator is unavailable right now. Please try again later.");
		verify(mcpClient, never()).callTool(any());
	}

	@Test
	void aSessionLostToAServerRestartIsReopenedAndTheCallRetriedOnce() {
		when(mcpClient.callTool(any()))
				.thenThrow(new IllegalStateException("MCP session with server terminated"))
				.thenReturn(result("84.0", false));

		assertThat(calcMcpClient.call("multiply", 12, 7)).isEqualTo(84.0);

		verify(mcpClient).initialize();
		verify(mcpClient, times(2)).callTool(any());
	}

	@Test
	void theRetryFailingTooIsCalculatorUnavailable() {
		when(mcpClient.callTool(any())).thenThrow(new IllegalStateException("Connection refused"));

		assertThatThrownBy(() -> calcMcpClient.call("multiply", 12, 7))
				.isInstanceOf(CalculatorUnavailableException.class);
		verify(mcpClient, times(2)).callTool(any());
	}

	@Test
	void initialisesTheSessionOnFirstUseOnly() {
		when(mcpClient.isInitialized()).thenReturn(false);
		when(mcpClient.callTool(any())).thenReturn(result("5.0", false));

		calcMcpClient.call("add", 2, 3);

		verify(mcpClient).initialize();
	}

	@Test
	void anAlreadyOpenSessionIsNotInitialisedAgain() {
		when(mcpClient.callTool(any())).thenReturn(result("5.0", false));

		calcMcpClient.call("add", 2, 3);

		verify(mcpClient, never()).initialize();
	}

	private static CallToolResult result(String text, boolean error) {
		return CallToolResult.builder().addTextContent(text).isError(error).build();
	}
}
