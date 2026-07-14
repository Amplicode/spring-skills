# Running tests via the IDE

Every step below sends the bundled script `scripts/run_tests_in_ide.kts` as the
`code` body of the steroid `steroid_execute_code` MCP tool (load its schema via
ToolSearch if not present).

## Steps

### 1. Project
You already have the `project_name` from the path-selection probe in SKILL.md
(`steroid_list_projects`). That unique key routes every steroid call below — reuse
it; don't re-probe.

### 2. Recall the mapping from memory
Check auto-memory for a `test-runner-configs` entry for THIS project (the mapping
step 3 writes). If it already names a configuration for the requested
type/module, skip to step 4 with that `CONFIG_NAME`.

### 3. Inspect and match (first use, or when memory has no match)
Send the script with `ACTION = "inspect"`. It runs nothing and returns JSON with
`configurations` (every Run Configuration with its `name`, `typeId`, `isGradle`,
`tasks`, `scriptParams`, `module`) and `gradleTasks` (the build's test-suite tasks
with `group` and `description`).

**Suites** — everything step 4 runs must come from `gradleTasks`; never assume a
name. Only `test` is a Gradle convention; extra suites are invented per project, so
read the descriptions and map each requested type to a real task:

```
unit        -> test        "Runs the test suite."
integration -> testIntgr   "Runs the test intgr suite."      (elsewhere: integrationTest, e2e, …)
all         -> every suite task found
```

The task name doubles as the source set — the JUnit XML lands in
`build/test-results/<task>` — which is what `RESULT_SOURCESET` needs in step 4.
Ignore anything that measures rather than runs (e.g. `jacoco*`, `pitest`); its
description gives it away. If a requested type has no matching task, say so — do
not guess one.

**Configuration** — judging by what each actually runs (the tasks matter more than
the name), build a short report mapping each requested test type to the
configuration(s) that fit it:

```
unit        -> Unit Tests
integration -> Integration Tests, IT (verify)
```

Then, per type:
- Exactly one fits → use it.
- None fits → decide a name for a new one (created in step 4); convention:
  `"<Module> <Type> Tests"` (module omitted when whole-project) — `Unit Tests`,
  `Integration Tests`, `All Tests`, `vet Integration Tests`.
- More than one → ask the user which to use with the `AskUserQuestion` tool
  (don't guess — the wrong config runs the wrong tests), offering that type's
  candidates as options, e.g.:

  ```
  AskUserQuestion(questions=[{
    "header": "Integration",
    "question": "Which configuration should run the integration tests?",
    "multiSelect": false,
    "options": [
      {"label": "Integration Tests", "description": "gradle: testIntgr"},
      {"label": "IT (verify)",        "description": "gradle: verify"}
    ]
  }])
  ```

Once the type→config is resolved — however it was resolved (one fit, the user
picked, or you named a new one) — **remember it** so step 2 can reuse it next
time. Keep one auto-memory file per project, `type: project`, named
`test-runner-configs` (link it from `MEMORY.md`); its body holds the mapping as
plain lines:

```
Project <name>: IDE test Run Configurations (run-tests skill).
- unit        -> "Unit Tests"        (gradle: test)
- integration -> "Integration Tests" (gradle: testIntgr)
- all         -> "All Tests"         (gradle: test testIntgr)
- module vet / integration -> "vet Integration Tests" (gradle: testIntgr --tests ru.openide.petclinic.vet.*)
```

Write only what's new — don't rewrite an unchanged mapping. If the user renames
or replaces a config, update the line rather than adding a duplicate. Since this
is memory, not repo state, an `inspect` that no longer shows a remembered config
means the user deleted it — drop it from the file.

### 4. Run and collect the result — in ONE Haiku subagent
Spawn a single subagent (Agent tool, `model: "haiku"`) that does BOTH the launch
and the result collection. Resolve the config parameters in the main model and
pass them in; the main model runs neither `run` nor `results` and never reads
their output — it only acts on the report the subagent returns. Running and
collecting in one subagent keeps the launch, the wait, and the long JUnit output
out of the main context.

Config parameters to resolve and pass to the subagent (the CONFIG block of the
script). Every task named below comes from step 3's `gradleTasks` — nothing is
assumed:

- `CONFIG_NAME` — reuse the config with this exact name if present, else create
  it. Convention for new names: `"<Module> <Type> Tests"` (module omitted when
  whole-project) — `Unit Tests`, `Integration Tests`, `All Tests`,
  `vet Integration Tests`, `web Unit Tests`.
- `GRADLE_TASKS` — the suite task(s) the requested type resolved to in step 3: one
  for a single type, every discovered suite for `all`. Prefix a subproject path for
  a module (`[":web:<suite>"]`). In this project that reads `["test"]` for unit,
  `["testIntgr"]` for integration, both for `all`.
- `TESTS_FILTER` — a package/class glob to narrow the run (→ `--tests`), e.g.
  `ru.openide.petclinic.vet.*`; empty for the whole task.
- `RERUN` (default true) — adds `--rerun-tasks` so tests actually execute even
  when Gradle would call them UP-TO-DATE; without it the tab shows "Test events
  were not received" and an empty tree. Costs the incremental cache.
- `NEW_TAB_PER_RUN` (default true) — each launch opens a NEW tab instead of
  reusing the config's tab (`isAllowRunningInParallel = true` +
  `settings.isSingleton = false`; single-instance is what makes the IDE reuse a
  tab).
- `PERSIST` (default true) — save into `.idea/runConfigurations` (survives IDE
  restart, shows in the ▶ list); false = transient, this session only.
- `RESULT_SOURCESET` — the source set to read results from. It is the suite task's
  own name (its XML lands in `build/test-results/<task>`), so use the task resolved
  above — `test` for unit here, `testIntgr` for integration. For `all`, run +
  collect once per suite.

Task for the subagent — call steroid `steroid_execute_code` with
`scripts/run_tests_in_ide.kts` TWICE, same CONFIG block:
1. `ACTION = "run"` — reuses or creates the config and launches it in its own tab
   (returns `configName`, `reusedExisting`, …). `run` only LAUNCHES.
2. `ACTION = "results"` — waits for the run to finish, parses the JUnit XML of
   `RESULT_SOURCESET`, and returns:

   ```json
   { "sourceSet": "test", "total": 25, "failed": 0, "passed": true, "failures": [] }
   ```

   On failure each `failures` entry carries the failed test and its error log:

   ```json
   { "test": "…VeterinarianServiceTest#createRejectsUnknownSpecialty",
     "message": "expected: <400> but was: <500>",
     "stacktrace": "java.lang.AssertionError: …\n\tat …" }
   ```

   (also `timedOut`, `xmlFiles`).

The subagent turns this into the report defined in `references/report.md` and
returns ONLY that. Act on the report in the main model.
