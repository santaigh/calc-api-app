package com.calc.api.dto;

public record ChatResponse(String reply, String operation, int a, int b, double result) {
}
