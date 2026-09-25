# calc-api-app — working notes

Calculator REST API plus a natural-language chat ("flow A"). Spring Boot 4.1 · Java 17 ·
Gradle · Spring AI 2.0.1 · Claude Haiku 4.5. Port **8091**.

## How the user wants to work

**Ask before implementing. Every step.** Propose the step, state the decisions it needs,
then stop and wait for "go". Answering a design question is not approval to build.

## Run

```bash
gradlew.bat build      # compile + 32 tests
gradlew.bat bootRun    # http://localhost:8091  (index.html = manual calculator, chat.html = chat)
```

Git: https://github.com/santaigh/calc-api-app (public), branch `main`. Commit identity is set in this repo only (`santaigh`). Never commit `application-local.yml` -- it holds the API key.

## What it is

- `POST /api/calc/{add,subtract,multiply,divide}` — body `{"a": int, "b": int}`, returns a bare
  number. `CalculatorController` → `CalculatorService`. Divide by zero → **400** with the
  reason as plain-text body
- `POST /api/chat` — flow A: `CalcAgent` makes two LLM calls (parse to `{operation, a, b}`,
  then phrase the result); in between, a Java `switch` picks a sub-agent
- Sub-agents (`AddAgent`…`DivisionAgent`) are deterministic, no LLM. Each calls exactly one
  **MCP tool** through `CalcMcpClient`

## The one external dependency

The sub-agents reach the calculator through an **MCP server** (Streamable HTTP), not their
own REST calls. The address is a setting — override it per environment:

| Property | Default | Environment override |
|---|---|---|
| `spring.ai.mcp.client.streamable-http.connections.calc.url` | `http://localhost:8092` | `SPRING_AI_MCP_CLIENT_STREAMABLEHTTP_CONNECTIONS_CALC_URL` |

That server must expose tools `add`, `subtract`, `multiply`, `divide`, each taking integer
`a`, `b` and returning the result as text. Only `/api/chat` needs it; `/api/calc/*` never does.

## Decisions — do not relitigate

- **No tool layer.** The REST tool classes (`AddTool`…) were removed: once an MCP client
  exists, the MCP server *is* the tool layer, and wrappers around `callTool()` add nothing
- **Error mapping in `CalcMcpClient`:** a tool result with `isError` → `ArithmeticException`
  (so `ChatController` still answers 400, as before); cannot reach the server at all →
  `CalculatorUnavailableException` → **503**
- **`spring.ai.mcp.client.initialized=false`** — no handshake at startup, so the app starts
  and `/api/calc/*` works with the MCP server down. `CalcMcpClient` initialises on first use
- **`toolcallback.enabled=false`** — MCP tools are never handed to the LLM here; the model
  only parses and phrases. (Letting the model pick tools is a different design, not this app)
- API key in `application-local.yml` at the **project root** — gitignored, and not under
  `src/main/resources`, because anything there is packaged into the jar

## Traps found the hard way

- **A restarted MCP server forgets the session.** The first call after a restart failed even
  though the server was back — one question lost per restart. `CalcMcpClient` re-initialises
  and retries once
- **`addSuppressed` on the same exception throws.** When both attempts fail with the same
  object, `second.addSuppressed(first)` raises "Self-suppression not permitted" — guarded
- Tests use mocked `ChatClient` / `McpSyncClient` — no LLM and no MCP server needed.
  `CalcApiAppApplicationTests` proves the context starts with no MCP server running
