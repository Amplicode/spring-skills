---
name: java-debug
description: >
    Safety rules, workflows, and tool reference for debugging applications via IntelliJ debugger:
    breakpoints, debug sessions, stepping, evaluating expressions, inspecting runtime state.
    TRIGGER when: user wants to debug, investigate a bug, set breakpoints, inspect runtime behavior,
    step through code, or understand why code behaves unexpectedly at runtime.
    Trigger phrases (EN): "debug", "breakpoint", "step through", "step into", "step over",
    "why does this crash", "why is this null", "launch in debug mode",
    "trace execution", "run with debugger", "evaluate expression".
    Trigger phrases (RU): "отладить", "дебаг", "брейкпоинт", "почему падает",
    "пошагово пройти", "зайти в метод", "посмотреть значение переменной",
    "запустить в режиме отладки", "почему null", "стектрейс".
    Also trigger when user wants to understand runtime behavior or investigate incorrect behavior.
---

# Preflight: IntelliJ Debug MCP

This skill is part of the **Spring Agent Toolkit** and is designed to work with the **IntelliJ Debug MCP server** (provided by the Amplicode IntelliJ plugin alongside the main Spring MCP). Before doing anything else, check your tool list for any debug MCP tool — they are exposed under the `intellij-debug` MCP server (e.g. `toggle_breakpoint`, `evaluate_expression`, `get_stack_trace`); harnesses that flatten MCP tools into the tool list use the `mcp__intellij-debug__` prefix on the same names.

- **If at least one debug tool is available** — MCP is connected. Proceed with the skill below.
- **If none are available** — stop and invoke the **`amplicode-install`** skill (bundled with the Spring Agent Toolkit). It installs the Amplicode plugin and walks the user through the **«Настроить Spring Agent»** welcome-screen button + MCP-client restart. After it completes, the debug MCP tools become available — resume this skill.
- If `amplicode-install` is not registered in your skill list, tell the user (in their language): *"This skill needs the Amplicode IntelliJ plugin and its debug MCP server. Install it from https://amplicode.ru/marketplace into IntelliJ IDEA Ultimate/Community or GigaIDE, open any project, click «Настроить Spring Agent» on the Amplicode welcome screen, then restart your MCP client."*

---

# Debugging with IntelliJ Debug MCP

This skill guides you through debugging applications via the IntelliJ Debug MCP server. The MCP server runs inside IntelliJ IDEA and gives you programmatic control over the debugger.

## SAFETY RULES — Read These First

These rules prevent you from hanging indefinitely or losing debugging context. They exist because the debugged application can be **suspended on a breakpoint** at any time, which means it stops responding to all requests.

### The Suspended-Process Trap

When the app hits a breakpoint, its threads freeze. Any HTTP request, curl call, or network interaction you make to that app **will hang forever** — the app can't respond until you resume it. This is the single most common mistake.

**Rule 1: Always use timeouts when talking to the debugged app.**
Use `--max-time 5` with curl, or set `run_in_background: true` on Bash tool calls. Do this even if you just checked that the app is running — it could hit a breakpoint between your check and your request.

**Rule 2: Check suspension status before network calls.**
Call `list_debug_sessions` and look at `isSuspended`. If the app is suspended, either `resume` it first or accept your request will block.

**Rule 3: Always verify position after stepping.**
After `step_over`, `step_into`, or `step_out`, call `get_current_position`. Never assume where execution landed — it might have jumped to an unexpected line or even a different file.

**Rule 4: Check status after resume.**
After calling `resume`, the app may immediately hit another breakpoint. Always call `list_debug_sessions` or `get_current_position` to confirm whether the session is still running or suspended again.

**Rule 5: Expression evaluation has side effects.**
`evaluate_expression` runs real code in the debugged JVM. Avoid expressions that modify state (like setters or `System.exit(0)`) unless that's specifically what you intend.

## Getting Started

Before using any debug tool, initialize the session:

1. Call `initialize` with `projectPath` set to the absolute path of the project root
2. If you get "project not found", the response lists all open projects — pick the right one and retry
3. All subsequent tool calls are scoped to this project

## Tool Reference

### Session
| Tool | Parameters |
|------|-----------|
| `initialize` | `projectPath` (string, required) — absolute path to project root |

### Run Configurations
| Tool | Parameters |
|------|-----------|
| `list_run_configurations` | _(none)_ |
| `debug_run_configuration` | `configurationName` (string, required) — exact name of the run config |
| `list_debug_sessions` | _(none)_ — returns session names and `isSuspended` status |
| `stop_debug_session` | `sessionName` (string, required) |

### Breakpoints
| Tool | Parameters |
|------|-----------|
| `list_breakpoints` | _(none)_ |
| `add_breakpoint` | `filePath` (string, required), `line` (int, required, 1-based), `condition` (string, optional) |
| `remove_breakpoint` | `filePath` (string, required), `line` (int, required, 1-based) |
| `toggle_breakpoint` | `filePath` (string, required), `line` (int, required, 1-based), `enabled` (bool, optional — toggles if omitted) |
| `set_breakpoint_condition` | `filePath` (string, required), `line` (int, required, 1-based), `condition` (string, required — empty string removes) |

### Execution Control
| Tool | Parameters |
|------|-----------|
| `resume` | `sessionName` (string, optional — defaults to first active session) |
| `pause` | `sessionName` (string, optional) |
| `step_over` | `sessionName` (string, optional) |
| `step_into` | `sessionName` (string, optional) |
| `step_out` | `sessionName` (string, optional) |

### Inspection
| Tool | Parameters |
|------|-----------|
| `get_current_position` | `sessionName` (string, optional) — returns file, line, isSuspended |
| `get_stack_trace` | `sessionName` (string, optional) — returns frames with file, line, description |
| `list_threads` | `sessionName` (string, optional) — returns thread names and top frame info |
| `evaluate_expression` | `expression` (string, required), `sessionName` (string, optional), `frameIndex` (int, optional, 0-based, default 0) |

**Note:** All line numbers in breakpoint tools are **1-based** (matching what you see in source files). Use absolute file paths or paths relative to the project root.

## Workflow: Investigate a Bug at a Known Location

Use this when you know roughly where the bug is (a specific file/method/line).

1. Read the source file to understand context around the suspicious area
2. `add_breakpoint` at the line you want to inspect (use `condition` if you only care about specific cases)
3. `debug_run_configuration` to start the app in debug mode
4. Trigger the bug — if via HTTP, remember: **use `--max-time 5` with curl** or `run_in_background: true`
5. `get_current_position` — confirm the breakpoint was hit and you're at the expected line
6. `evaluate_expression` — inspect variables, call methods, check state. Start simple (variable names), then build up to more complex expressions
7. `get_stack_trace` — understand how execution reached this point
8. Decide: `step_over`/`step_into` to trace further, `add_breakpoint` elsewhere, or conclude
9. After each step, call `get_current_position` to confirm where you are
10. When done: `resume` or `stop_debug_session`, then `remove_breakpoint` to clean up

## Workflow: Explore Runtime Behavior of Unknown Code

Use this when you need to understand how unfamiliar code actually executes.

1. `list_run_configurations` — find the right way to start the app
2. Set breakpoints at entry points (controller methods, main method, event handlers)
3. `debug_run_configuration` to start debugging
4. Trigger execution (HTTP request with timeout, CLI command, UI action described to user)
5. When suspended: `get_stack_trace` to see the full call chain
6. Use `step_into` to follow execution into methods, `step_over` to skip known code, `step_out` to return to callers
7. Use `evaluate_expression` liberally — inspect method arguments, return values, object fields
8. After each step: `get_current_position` to stay oriented

## Expression Evaluation Tips

- Evaluation only works when the session is **suspended** on a breakpoint or after a step
- `frameIndex` lets you evaluate in different stack frames: 0 = current (top) frame, 1 = caller, etc.
- Start with simple variable names, then progress to method calls and complex expressions
- Be cautious with side-effect expressions — they run real code in the target JVM

## Error Recovery

| Error | What it means | What to do |
|-------|--------------|------------|
| "Session not found" | No session with that name, or no sessions running | `list_debug_sessions` to see what's available. May need to start a new one. |
| "Not suspended" | Session is running, not paused on a breakpoint | The breakpoint wasn't hit yet, or was already resumed. Wait for the trigger, or `pause` the session. |
| "Evaluator not available" | The current frame doesn't support evaluation | Try a different `frameIndex`. Native or framework frames often can't evaluate. |
| Session crash/disconnect | The debugged app crashed or was killed | Check IDE logs. `list_debug_sessions` to confirm. Start a new debug session. |

After debugging is complete, clean up: `remove_breakpoint` for any breakpoints you added. Leaving breakpoints behind clutters future debug sessions.

## Multiple Debug Sessions

When more than one debug session is active, always specify `sessionName` in your tool calls — otherwise the tools default to the first active session, which may not be the one you intend.

Call `list_debug_sessions` to discover all active sessions and their states before operating.

## Known Limitations

- **No console output access**: The MCP server doesn't expose stdout/stderr from the debugged app. Workarounds: read log files directly, use `evaluate_expression` to check state, or ask the user what they see in the IDE console.
- **No breakpoint-hit notifications**: The server doesn't push events when a breakpoint is hit. After any action that might trigger a breakpoint (like `resume`, an HTTP request, or `debug_run_configuration`), check status with `list_debug_sessions` or `get_current_position`. Don't use timed polling — just check after each relevant action.
