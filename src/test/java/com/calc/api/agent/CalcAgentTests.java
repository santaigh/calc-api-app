package com.calc.api.agent;

import com.calc.api.dto.ChatResponse;
import com.calc.api.dto.ParsedCalculation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** ChatClient is mocked -- no real LLM call is made. Covers both call sites in CalcAgent. */
class CalcAgentTests {

	private ChatClient chatClient;
	private ChatClient.ChatClientRequestSpec requestSpec;
	private ChatClient.CallResponseSpec responseSpec;
	private AddAgent addAgent;
	private SubtractAgent subtractAgent;
	private MultiplyAgent multiplyAgent;
	private DivisionAgent divisionAgent;
	private CalcAgent calcAgent;

	@BeforeEach
	void setUp() {
		chatClient = mock(ChatClient.class);
		requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
		responseSpec = mock(ChatClient.CallResponseSpec.class);
		addAgent = mock(AddAgent.class);
		subtractAgent = mock(SubtractAgent.class);
		multiplyAgent = mock(MultiplyAgent.class);
		divisionAgent = mock(DivisionAgent.class);

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.system(anyString())).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);

		calcAgent = new CalcAgent(chatClient, addAgent, subtractAgent, multiplyAgent, divisionAgent);
	}

	@Test
	void routesAddToAddAgentAndComposesReply() {
		when(responseSpec.entity(ParsedCalculation.class)).thenReturn(new ParsedCalculation("add", 5, 3));
		when(addAgent.execute(5, 3)).thenReturn(8.0);
		when(responseSpec.content()).thenReturn("Five plus three is eight.");

		ChatResponse response = calcAgent.handle("5 add 3");

		assertThat(response.operation()).isEqualTo("add");
		assertThat(response.a()).isEqualTo(5);
		assertThat(response.b()).isEqualTo(3);
		assertThat(response.result()).isEqualTo(8.0);
		assertThat(response.reply()).isEqualTo("Five plus three is eight.");
		verify(addAgent).execute(5, 3);
		verifyNoInteractions(subtractAgent, multiplyAgent, divisionAgent);
	}

	@Test
	void routesSubtractToSubtractAgent() {
		when(responseSpec.entity(ParsedCalculation.class)).thenReturn(new ParsedCalculation("subtract", 5, 3));
		when(subtractAgent.execute(5, 3)).thenReturn(2.0);
		when(responseSpec.content()).thenReturn("Two.");

		ChatResponse response = calcAgent.handle("5 minus 3");

		assertThat(response.operation()).isEqualTo("subtract");
		assertThat(response.result()).isEqualTo(2.0);
		verify(subtractAgent).execute(5, 3);
	}

	@Test
	void routesMultiplyToMultiplyAgent() {
		when(responseSpec.entity(ParsedCalculation.class)).thenReturn(new ParsedCalculation("multiply", 4, 3));
		when(multiplyAgent.execute(4, 3)).thenReturn(12.0);
		when(responseSpec.content()).thenReturn("Twelve.");

		ChatResponse response = calcAgent.handle("4 times 3");

		assertThat(response.operation()).isEqualTo("multiply");
		assertThat(response.result()).isEqualTo(12.0);
		verify(multiplyAgent).execute(4, 3);
	}

	@Test
	void routesDivideToDivisionAgent() {
		when(responseSpec.entity(ParsedCalculation.class)).thenReturn(new ParsedCalculation("divide", 7, 2));
		when(divisionAgent.execute(7, 2)).thenReturn(3.5);
		when(responseSpec.content()).thenReturn("Three point five.");

		ChatResponse response = calcAgent.handle("7 divided by 2");

		assertThat(response.operation()).isEqualTo("divide");
		assertThat(response.result()).isEqualTo(3.5);
		verify(divisionAgent).execute(7, 2);
	}

	@Test
	void unrecognizedOperationThrowsBeforeAnySubAgent() {
		when(responseSpec.entity(ParsedCalculation.class)).thenReturn(new ParsedCalculation("modulo", 5, 3));

		assertThatThrownBy(() -> calcAgent.handle("5 mod 3"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("modulo");

		verifyNoInteractions(addAgent, subtractAgent, multiplyAgent, divisionAgent);
		verify(chatClient, times(1)).prompt();
	}

	@Test
	void divisionByZeroShortCircuitsBeforeSecondLlmCall() {
		when(responseSpec.entity(ParsedCalculation.class)).thenReturn(new ParsedCalculation("divide", 7, 0));
		when(divisionAgent.execute(7, 0)).thenThrow(new ArithmeticException("Division by zero is not allowed"));

		assertThatThrownBy(() -> calcAgent.handle("7 divided by 0"))
				.isInstanceOf(ArithmeticException.class)
				.hasMessage("Division by zero is not allowed");

		verify(chatClient, times(1)).prompt();
		verify(responseSpec, never()).content();
	}
}
