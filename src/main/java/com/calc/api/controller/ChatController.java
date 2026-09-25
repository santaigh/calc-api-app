package com.calc.api.controller;

import com.calc.api.agent.CalcAgent;
import com.calc.api.agent.CalculatorUnavailableException;
import com.calc.api.dto.ChatRequest;
import com.calc.api.dto.ChatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	private final CalcAgent calcAgent;

	public ChatController(CalcAgent calcAgent) {
		this.calcAgent = calcAgent;
	}

	@PostMapping
	public ChatResponse chat(@RequestBody ChatRequest request) {
		return calcAgent.handle(request.message());
	}

	@ExceptionHandler(ArithmeticException.class)
	public ResponseEntity<String> handleArithmeticException(ArithmeticException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	}

	@ExceptionHandler(CalculatorUnavailableException.class)
	public ResponseEntity<String> handleCalculatorUnavailable(CalculatorUnavailableException ex) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	}
}
