package com.calc.api.agent;

import org.springframework.stereotype.Component;

@Component
public class AddAgent implements OperationAgent {

	private final CalcMcpClient calcMcpClient;

	public AddAgent(CalcMcpClient calcMcpClient) {
		this.calcMcpClient = calcMcpClient;
	}

	@Override
	public double execute(int a, int b) {
		return calcMcpClient.call("add", a, b);
	}
}
