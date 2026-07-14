---
name: coverage
description: >-
  Measure a project's code coverage and report the coverage number.
  Use whenever coverage needs to be measured — a user asking to
  run the coverage report or check the current level, and also when the task
  itself calls for it: recording the starting coverage before development,
  confirming coverage didn't drop below the committed baseline after a change.
  Measures the whole project (unit + integration merged) or just one test
  group — a package/class glob or a subproject.
---

# Coverage

## What to cover

Same two dimensions as the `run-tests` skill — test **type**, optionally narrowed
to a **module / group**:

| Coverage by | Typical Gradle task | Meaning |
|-------------|---------------------|---------|
| `all` (default) | `jacocoTestReport` | Both suites merged (`test` + `testIntgr`) — what the baseline is expressed in. Needs Docker. |
| `unit` | `jacocoTestReport -x testIntgr` | What the fast tests alone cover. |
| `integration` | `jacocoTestReport -x test` | What the Testcontainers tests alone cover. Needs Docker. |
| + a module / group | `--tests <glob>`, or a subproject path (`:web:test`) | Narrows any of the above to one package/class/subproject. |

## Pick the path

Probe FIRST, here, before loading either reference — so you read only the one you
need. Call `steroid_list_projects` (via ToolSearch if the tool schema isn't
loaded):

- It returns the repo you're in → **IDE path**: note that entry's `project_name`
  and follow `references/ide.md` (runs the coverage Gradle task inside the IDE in
  its own Run tab). Its steps start already knowing the `project_name` — don't
  re-probe.
- It fails, or the repo isn't listed → **console Gradle path**: follow
  `references/gradle.md` (runs Gradle in the console) and tell the user you used
  the console fallback because the IDE wasn't reachable.

Both paths end by collecting the result **in a Haiku subagent** that returns the
`references/report.md` report and nothing else — keeping the long build/coverage
output out of the main context. Act on that report in the main model.
