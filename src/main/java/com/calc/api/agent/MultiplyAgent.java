package com.calc.api.agent;

import org.springframework.stereotype.Component;

@Component
public class MultiplyAgent implements OperationAgent {

	private final CalcMcpClient calcMcpClient;

	public MultiplyAgent(CalcMcpClient calcMcpClient) {
		this.calcMcpClient = calcMcpClient;
	}

	@Override
	public double execute(int a, int b) {
		return calcMcpClient.call("multiply", a, b);
	}
}
