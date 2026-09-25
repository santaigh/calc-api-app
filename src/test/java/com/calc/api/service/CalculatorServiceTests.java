package com.calc.api.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculatorServiceTests {

	private final CalculatorService calculatorService = new CalculatorService();

	@Test
	void adds() {
		assertThat(calculatorService.add(2, 3)).isEqualTo(5);
	}

	@Test
	void subtracts() {
		assertThat(calculatorService.subtract(5, 3)).isEqualTo(2);
	}

	@Test
	void multiplies() {
		assertThat(calculatorService.multiply(4, 3)).isEqualTo(12);
	}

	@Test
	void dividesAsDecimal() {
		assertThat(calculatorService.divide(7, 2)).isEqualTo(3.5);
	}

	@Test
	void divisionByZeroThrows() {
		assertThatThrownBy(() -> calculatorService.divide(7, 0))
				.isInstanceOf(ArithmeticException.class)
				.hasMessage("Division by zero is not allowed");
	}
}
