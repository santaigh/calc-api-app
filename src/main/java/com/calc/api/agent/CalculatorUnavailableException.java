package com.calc.api.agent;

/** The MCP server could not be reached, so no sub-agent can calculate anything. */
public class CalculatorUnavailableException extends RuntimeException {

	public CalculatorUnavailableException(Throwable cause) {
		super("The calculator is unavailable right now. Please try again later.", cause);
	}
}
