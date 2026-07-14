---
name: run-tests
description: >-
  Run a Gradle project's tests by test type or module — unit, integration, or
  all. Use this skill whenever the user wants to run, re-run, or check the
  project's tests (e.g. "run the unit tests", "run all tests", "run the
  integration tests for the vet module", "did the tests pass?").
---

# Run tests

## Test types and modules

Test **types** (extend as a project needs):

| Type | Meaning | Typical Gradle tasks |
|------|---------|----------------------|
| `unit` | fast tests, no Spring/Docker | `test` |
| `integration` | Spring + Testcontainers | `testIntgr` or `integrationTest` |
| `all` | everything | `test` + `testIntgr` |

A **module** is a Gradle subproject; prefix its path onto the task
(`:web:test`). A module combines with a type on the fly — build that
configuration when first asked and remember it.

## Pick the path

Probe FIRST, here, before loading either reference — so you read only the one you
need. Call `steroid_list_projects` (via ToolSearch if the tool schema isn't
loaded):

- It returns the repo you're in → **IDE path**: note that entry's `project_name`
  and follow `references/ide.md` (runs tests inside the IDE, each in its own Run
  tab). Its steps start already knowing the `project_name` — don't re-probe.
- It fails, or the repo isn't listed → **console Gradle path**: follow
  `references/gradle.md` (runs Gradle in the console) and tell the user you used
  the console fallback because the IDE wasn't reachable.

Both paths end by collecting the result **in a Haiku subagent** that returns the
`references/report.md` report and nothing else — keeping the long test/build
output out of the main context. Act on that report in the main model.
