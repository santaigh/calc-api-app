package com.calc.api.dto;

/** Structured output target for LLM call #1 -- Calc-Agent asks the LLM to turn free text into this. */
public record ParsedCalculation(String operation, int a, int b) {
}
