package com.calc.api.agent;

import com.calc.api.dto.ChatResponse;
import com.calc.api.dto.ParsedCalculation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * The only component that talks to the LLM -- twice. Sub-agents are deterministic and
 * never see a prompt. Call #1 turns free text into {operation, a, b}; call #2, made
 * only after the sub-agent/tool has returned a numeric result, turns that result into
 * the natural-language reply the chat UI shows.
 */
@Component
public class CalcAgent {

	private static final String PARSE_SYSTEM_PROMPT = """
			You extract a single arithmetic calculation from the user's message.

			Identify the operation and its two operands. The operation must be exactly one of:
			add, subtract, multiply, divide. Both operands are integers, however they are written
			in the message (digits or words). Respond with only that -- no explanation.
			""";

	private static final String RESPOND_SYSTEM_PROMPT = """
			You are a calculator chat assistant. You are given the operation, its two operands and
			the already-computed result. Reply with one short, natural sentence stating the answer.
			Do not show your reasoning, do not repeat raw data structures, and do not recompute
			anything -- just phrase the given result in words.
			""";

	private final ChatClient chatClient;
	private final AddAgent addAgent;
	private final SubtractAgent subtractAgent;
	private final MultiplyAgent multiplyAgent;
	private final DivisionAgent divisionAgent;

	public CalcAgent(ChatClient chatClient, AddAgent addAgent, SubtractAgent subtractAgent,
			MultiplyAgent multiplyAgent, DivisionAgent divisionAgent) {
		this.chatClient = chatClient;
		this.addAgent = addAgent;
		this.subtractAgent = subtractAgent;
		this.multiplyAgent = multiplyAgent;
		this.divisionAgent = divisionAgent;
	}

	public ChatResponse handle(String message) {
		ParsedCalculation parsed = chatClient.prompt()
				.system(PARSE_SYSTEM_PROMPT)
				.user(message)
				.call()
				.entity(ParsedCalculation.class);

		String operation = parsed.operation() == null ? "" : parsed.operation().trim().toLowerCase(Locale.ROOT);
		OperationAgent agent = switch (operation) {
			case "add" -> addAgent;
			case "subtract" -> subtractAgent;
			case "multiply" -> multiplyAgent;
			case "divide" -> divisionAgent;
			default -> throw new IllegalArgumentException("Unrecognized operation: " + parsed.operation());
		};

		double result = agent.execute(parsed.a(), parsed.b());

		String reply = chatClient.prompt()
				.system(RESPOND_SYSTEM_PROMPT)
				.user("""
						Question: %s
						Operation: %s
						a: %d
						b: %d
						Result: %s""".formatted(message, operation, parsed.a(), parsed.b(), result))
				.call()
				.content();

		return new ChatResponse(reply, operation, parsed.a(), parsed.b(), result);
	}
}
