package com.calc.api;

import com.calc.api.agent.CalcMcpClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The context starts with no MCP server running -- the point of
 * spring.ai.mcp.client.initialized=false. With the default, a refused connection would
 * stop the whole app, /api/calc/* included.
 */
@SpringBootTest
class CalcApiAppApplicationTests {

	@Autowired
	private CalcMcpClient calcMcpClient;

	@Test
	void startsWithoutAnMcpServer() {
		assertThat(calcMcpClient).isNotNull();
	}
}
