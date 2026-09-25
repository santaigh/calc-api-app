package com.calc.api.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Each sub-agent calls exactly its own MCP tool -- a wrong name here would silently compute the wrong thing. */
class OperationAgentTests {

	private CalcMcpClient calcMcpClient;

	@BeforeEach
	void setUp() {
		calcMcpClient = mock(CalcMcpClient.class);
	}

	@Test
	void addAgentCallsAdd() {
		when(calcMcpClient.call("add", 2, 3)).thenReturn(5.0);
		assertThat(new AddAgent(calcMcpClient).execute(2, 3)).isEqualTo(5.0);
	}

	@Test
	void subtractAgentCallsSubtract() {
		when(calcMcpClient.call("subtract", 5, 3)).thenReturn(2.0);
		assertThat(new SubtractAgent(calcMcpClient).execute(5, 3)).isEqualTo(2.0);
	}

	@Test
	void multiplyAgentCallsMultiply() {
		when(calcMcpClient.call("multiply", 4, 3)).thenReturn(12.0);
		assertThat(new MultiplyAgent(calcMcpClient).execute(4, 3)).isEqualTo(12.0);
	}

	@Test
	void divisionAgentCallsDivide() {
		when(calcMcpClient.call("divide", 7, 2)).thenReturn(3.5);
		assertThat(new DivisionAgent(calcMcpClient).execute(7, 2)).isEqualTo(3.5);
	}
}
