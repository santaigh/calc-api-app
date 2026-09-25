package com.calc.api.controller;

import com.calc.api.dto.CalculationRequest;
import com.calc.api.service.CalculatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calc")
public class CalculatorController {

	private final CalculatorService calculatorService;

	public CalculatorController(CalculatorService calculatorService) {
		this.calculatorService = calculatorService;
	}

	@PostMapping("/add")
	public int add(@RequestBody CalculationRequest request) {
		return calculatorService.add(request.a(), request.b());
	}

	@PostMapping("/subtract")
	public int subtract(@RequestBody CalculationRequest request) {
		return calculatorService.subtract(request.a(), request.b());
	}

	@PostMapping("/multiply")
	public int multiply(@RequestBody CalculationRequest request) {
		return calculatorService.multiply(request.a(), request.b());
	}

	@PostMapping("/divide")
	public double divide(@RequestBody CalculationRequest request) {
		return calculatorService.divide(request.a(), request.b());
	}

	@ExceptionHandler(ArithmeticException.class)
	public ResponseEntity<String> handleArithmeticException(ArithmeticException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	}
}
