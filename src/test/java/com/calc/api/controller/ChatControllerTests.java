package com.calc.api.controller;

import com.calc.api.agent.CalcAgent;
import com.calc.api.agent.CalculatorUnavailableException;
import com.calc.api.dto.ChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CalcAgent calcAgent;

	@Test
	void chatReturnsAgentResponse() throws Exception {
		when(calcAgent.handle("5 add 3"))
				.thenReturn(new ChatResponse("Five plus three is eight.", "add", 5, 3, 8.0));

		mockMvc.perform(post("/api/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"5 add 3\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reply").value("Five plus three is eight."))
				.andExpect(jsonPath("$.operation").value("add"))
				.andExpect(jsonPath("$.result").value(8.0));
	}

	@Test
	void divisionByZeroReturnsBadRequest() throws Exception {
		when(calcAgent.handle("7 divided by 0"))
				.thenThrow(new ArithmeticException("Division by zero is not allowed"));

		mockMvc.perform(post("/api/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"7 divided by 0\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Division by zero is not allowed"));
	}

	@Test
	void unrecognizedOperationReturnsBadRequest() throws Exception {
		when(calcAgent.handle("what color is the sky"))
				.thenThrow(new IllegalArgumentException("Unrecognized operation: none"));

		mockMvc.perform(post("/api/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"what color is the sky\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Unrecognized operation: none"));
	}

	@Test
	void mcpServerDownReturnsServiceUnavailable() throws Exception {
		when(calcAgent.handle("5 add 3"))
				.thenThrow(new CalculatorUnavailableException(new IllegalStateException("Connection refused")));

		mockMvc.perform(post("/api/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"5 add 3\"}"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(content().string("The calculator is unavailable right now. Please try again later."));
	}
}
