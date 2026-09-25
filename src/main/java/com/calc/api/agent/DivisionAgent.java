package com.calc.api.agent;

import org.springframework.stereotype.Component;

@Component
public class DivisionAgent implements OperationAgent {

	private final CalcMcpClient calcMcpClient;

	public DivisionAgent(CalcMcpClient calcMcpClient) {
		this.calcMcpClient = calcMcpClient;
	}

	@Override
	public double execute(int a, int b) {
		return calcMcpClient.call("divide", a, b);
	}
}
