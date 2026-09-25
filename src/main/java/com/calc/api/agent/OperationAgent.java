package com.calc.api.agent;

/** A deterministic sub-agent: calls exactly one MCP tool, no LLM access. */
public interface OperationAgent {

	double execute(int a, int b);
}
