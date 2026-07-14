# Measuring coverage via the IDE

## Steps

### 1. Project
You already have the `project_name` from the path-selection probe in SKILL.md
(`steroid_list_projects`). That unique key routes every steroid call below — reuse
it; don't re-probe.

### 2. Recall from memory
Check auto-memory for this skill's own `coverage-runner-configs` entry for THIS
project. It holds two things: the config per type/module, and the baseline
location. If both are known for what's being measured, skip to step 4.

### 3. Inspect and match (first use, or when memory has no match)
Send the bundled `scripts/coverage_in_ide.kts` as the `code` body of the steroid
`steroid_execute_code` MCP tool (load its schema via ToolSearch if not present),
with `ACTION = "inspect"`. It runs nothing and returns JSON with three things:
`configurations` (every Run Configuration with its `name`, `typeId`, `isGradle`,
`tasks`, `scriptParams`, `module`), `gradleTasks` (the build's coverage/test tasks
with `group` and `description`), and `baselineCandidates`.

**Tasks** — everything step 4 runs must come from `gradleTasks`; never assume a
name. Read the descriptions and sort them into three roles:

| role | how to spot it | example here |
|------|----------------|--------------|
| report | generates the coverage report | `jacocoTestReport` — "Generates code coverage report…" |
| test suite(s) | runs a suite; there may be several | `test` — "Runs the test suite.", `testIntgr` — "Runs the test intgr suite." |
| gate — **never run** | fails the build / verifies a threshold | `coverageRatchet` — "Fails the build if code coverage dropped…", `jacocoTestCoverageVerification` — "Verifies code coverage metrics…" |

Only the report task is conventional. Extra suites, merge steps and gate tasks are
things a build invents, so they exist only where someone wrote them. If no report
task is found, say so — do not guess one.

**Baseline** — `baselineCandidates` lists `.properties` entries whose key looks
like a coverage baseline, e.g.:

```json
{"path":"config/jacoco/coverage-baseline.properties","key":"coverage.instruction.minimum","value":"0.9740"}
```

Exactly one → that is `BASELINE_FILE` / `BASELINE_KEY`. Several → ask the user
which. None → the project has no baseline; measure and report the number with no
verdict (`no-baseline`). Remember the answer (step 3's memory block) — it is only
searched for once. A baseline is a project invention, so never assume a path.

**Configuration** — match against the roles resolved above. With the report task
`R` and the discovered suites `S1, S2, …`:

```
all (default) -> a config running R, excluding nothing
one type      -> a config running R, with -x on every OTHER suite
+ module/group -> the same plus --tests <glob> (or a :module:test path)
```

Here that reads: `all` → `jacocoTestReport`; `unit` → `jacocoTestReport -x
testIntgr`; `integration` → `jacocoTestReport -x test`.

A config running a gate task is not a match — see SKILL.md. If it is the only
candidate, make a new one.

Then:
- Exactly one fits → use it.
- None fits → decide a name for a new one (created in step 4). Convention: the
  `run-tests` config's name with ` Coverage` appended — `"All Tests Coverage"`,
  `"Unit Tests Coverage"`, `"Integration Tests Coverage"`, and per module
  `"vet Unit Tests Coverage"`, `"web Unit Tests Coverage"`.
- More than one plausible → ask the user which to use with the `AskUserQuestion`
  tool (don't guess — the wrong config measures the wrong thing), e.g.:

  ```
  AskUserQuestion(questions=[{
    "header": "Coverage",
    "question": "Which configuration should measure coverage?",
    "multiSelect": false,
    "options": [
      {"label": "All Tests Coverage",  "description": "gradle: jacocoTestReport (all tests)"},
      {"label": "Unit Tests Coverage", "description": "gradle: jacocoTestReport -x testIntgr (unit only)"}
    ]
  }])
  ```

Once resolved — however it was resolved — **remember it** so step 2 can reuse it
next time. Keep this skill's OWN auto-memory file, `coverage-runner-configs` (one
per project, `type: project`) — do not write into the `run-tests` skill's
`test-runner-configs`; each skill owns its memory:

```
Coverage Run Configurations (coverage skill):
- all              -> "All Tests Coverage"         (gradle: jacocoTestReport)
- unit             -> "Unit Tests Coverage"        (gradle: jacocoTestReport -x testIntgr)
- integration      -> "Integration Tests Coverage" (gradle: jacocoTestReport -x test)
- module vet /unit -> "vet Unit Tests Coverage"    (gradle: jacocoTestReport -x testIntgr --tests ru.openide.petclinic.vet.*)

Baseline (applies to `all` runs only):
- config/jacoco/coverage-baseline.properties : coverage.instruction.minimum
```

Write only what's new — don't rewrite an unchanged mapping. If the user renames or
replaces a config, update the line rather than adding a duplicate. Since this is
memory, not repo state, an `inspect` that no longer shows a remembered config
means the user deleted it — drop it from the file.

### 4. Run and collect the result — in ONE Haiku subagent
Spawn a single subagent (Agent tool, `model: "haiku"`) that does BOTH the launch
and the result collection. Resolve the config parameters in the main model and
pass them in; the main model runs neither `run` nor `results` and never reads
their output — it only acts on the report the subagent returns.

Config parameters to resolve and pass to the subagent (the CONFIG block of the
script):

- `CONFIG_NAME` — reuse the config with this exact name if present, else create it
  (naming convention in step 3).
- `BASELINE_FILE` / `BASELINE_KEY` — from memory or step 3. Pass them ONLY for an
  `all` run: the baseline describes what every test covers together, so on a
  narrowed run the script neither reads nor reports it. Empty → `no-baseline`.
- `GRADLE_TASKS` — the REPORT task from step 3, and nothing else for `all`:
  running it pulls its suites in as dependencies. To narrow to a group, put the
  relevant suite task first: `[<suite>, <report>]`. For a Gradle subproject prefix
  the path (`[":web:test", <report>]`).
- `EXCLUDE_TASKS` — `[]` for `all`. To isolate ONE type, list every OTHER suite
  task step 3 found. Excluding is the only way: the report task depends on all of
  them, so naming one suite still drags the rest in.
- `TESTS_FILTER` — the group's package/class glob (`ru.openide.petclinic.vet.*`),
  space-separated for several; empty for every test of that type.
- `CLEAN_EXEC` — TRUE whenever the run is narrowed at all (`EXCLUDE_TASKS` or
  `TESTS_FILTER` set), false for `all`. Otherwise the excluded suite's stale
  `.exec` is picked up anyway.

Never put a gate task in `GRADLE_TASKS` — measuring must not mutate the repo or go
red (SKILL.md).
- `RERUN` (default true) — adds `--rerun-tasks`; without it Gradle may report
  UP-TO-DATE and collect no coverage.
- `NEW_TAB_PER_RUN` (default true) — each launch opens a NEW tab.
- `PERSIST` (default true) — save into `.idea/runConfigurations`.
Task for the subagent — THREE steroid `steroid_execute_code` calls:

1. `scripts/coverage_in_ide.kts` with `ACTION = "run"` — deletes the previous XML
   report, then reuses or creates the config and launches it in its own tab.
   Returns `configName`, `reusedExisting`, `tasks`, `staleReportDeleted`, …
2. Same script, same CONFIG, `ACTION = "results"` — waits for the run to finish,
   reads the number, computes the verdict, then returns:

   ```json
   { "coveragePct": "97.40%", "baselinePct": "97.40%", "verdict": "equal",
     "covered": 562, "total": 577, "reportFound": true, "timedOut": false,
     "failures": [],
     "htmlReport": "…/build/reports/jacoco/test/html/index.html" }
   ```

3. **Show the coverage in the IDE — once the run configuration has finished, and
   before returning the report.** Send the OTHER bundled script,
   `scripts/load_coverage_into_ide.kts`, with `SHOW_PANEL = true`. It registers the
   `.exec` as a coverage suite and activates it (that paints the editor gutters),
   then opens the Coverage tool window, printing `suite registered = …`,
   `active bundle = …`, `DONE — …`.

   Leave `EXEC_REL_PATH = ""` and pass `NEWER_THAN_MS = <startedAtMs from step 1>`.
   That shows the `.exec` THIS run produced. Do not name a file: which `.exec` a
   build writes is project-specific. The newest file this run wrote is the one the
   report was built from either way.

   `NEWER_THAN_MS` is what makes "this run" true: an aborted earlier run can leave a
   per-suite `.exec` newer than the good one, and without the filter the panel would
   show that instead.

   Skip this step only when `reportFound: false` — a run that died before the
   report produced no fresh `.exec` worth showing.

   `verdict` ∈ `above` | `equal` | `below` | `n/a-partial` (a narrowed run — one
   type or group, not comparable to the whole-suite baseline) | `no-baseline` |
   `unknown`. It is computed, never applied — nothing was rewritten, nothing
   failed.

Rules for reading that JSON:

- `reportFound: false` → the run never reached the report. Report NOT measured and
  give `failures` as the cause. Never fall back to a previous number.
- Each `failures` entry carries `test`, `suite`, `message` and `stacktrace`
  verbatim — pass the message and the key stacktrace lines through to the report.
- A whole-project run needs Docker (the `testIntgr` suite). If it timed out or the
  report is missing because Docker is unavailable, say so — do NOT present a
  unit-only number as the whole-project coverage.
- Gutters are painted from the IDE's class files, not Gradle's. If the IDE warns
  "Project class files are out of date", the highlighting may be wrong — the number
  and verdict are unaffected. Tell the user to Recompile if they care about the
  gutters.

The subagent turns this into the report defined in `references/report.md` and
returns ONLY that. Act on the report in the main model.
