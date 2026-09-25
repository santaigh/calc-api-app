package com.calc.api.controller;

import com.calc.api.service.CalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalculatorController.class)
class CalculatorControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CalculatorService calculatorService;

	@Test
	void addsTwoIntegers() throws Exception {
		when(calculatorService.add(2, 3)).thenReturn(5);

		mockMvc.perform(post("/api/calc/add")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"a\":2,\"b\":3}"))
				.andExpect(status().isOk())
				.andExpect(content().string("5"));
	}

	@Test
	void subtractsTwoIntegers() throws Exception {
		when(calculatorService.subtract(5, 3)).thenReturn(2);

		mockMvc.perform(post("/api/calc/subtract")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"a\":5,\"b\":3}"))
				.andExpect(status().isOk())
				.andExpect(content().string("2"));
	}

	@Test
	void multipliesTwoIntegers() throws Exception {
		when(calculatorService.multiply(4, 3)).thenReturn(12);

		mockMvc.perform(post("/api/calc/multiply")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"a\":4,\"b\":3}"))
				.andExpect(status().isOk())
				.andExpect(content().string("12"));
	}

	@Test
	void dividesTwoIntegersAsDecimal() throws Exception {
		when(calculatorService.divide(7, 2)).thenReturn(3.5);

		mockMvc.perform(post("/api/calc/divide")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"a\":7,\"b\":2}"))
				.andExpect(status().isOk())
				.andExpect(content().string("3.5"));
	}

	@Test
	void divisionByZeroReturnsBadRequest() throws Exception {
		when(calculatorService.divide(7, 0)).thenThrow(new ArithmeticException("Division by zero is not allowed"));

		mockMvc.perform(post("/api/calc/divide")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"a\":7,\"b\":0}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Division by zero is not allowed"));
	}
}
