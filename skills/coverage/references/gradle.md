# Measuring coverage via console Gradle (fallback)

Used when the IDE path isn't reachable (steroid absent, or the repo isn't among
the open projects). Runs Gradle directly in the console instead of an IDE Run
Configuration. Same shape as the IDE path — discover, resolve, remember, run in a
subagent, return the `references/report.md` report. There is no coverage panel to
show here; the report is the only output.

Tell the user up front you're using the console fallback because the IDE wasn't
reachable.

## Steps

### 1. Recall from memory
Check auto-memory for this skill's own `coverage-runner-configs` entry for THIS
project (the "Coverage tasks (console path)" section it writes). It holds the
command per type/module and the baseline location. If both are known, skip to
step 3.

### 2. Discover and resolve (first use, or when memory has no match)

**Tasks** — everything step 3 runs must come from the build's real task list; never
assume a name. List them with `./gradlew tasks --group=verification` (run in a
subagent — the output is long; use `--all` if the build files them elsewhere). Read
the descriptions and sort them into three roles:

| role | how to spot it | example here |
|------|----------------|--------------|
| report | generates the coverage report | `jacocoTestReport` — "Generates code coverage report…" |
| test suite(s) | runs a suite; there may be several | `test` — "Runs the test suite.", `testIntgr` — "Runs the test intgr suite." |
| gate — **never run** | fails the build / verifies a threshold | `coverageRatchet` — "Fails the build if code coverage dropped…", `jacocoTestCoverageVerification` — "Verifies code coverage metrics…" |

Only the report task is conventional. Extra suites, merge steps and gate tasks are
things a build invents, so they exist only where someone wrote them. If two tasks
plausibly fit a role, ask the user with `AskUserQuestion` rather than guessing. If
no report task is found, say so — do not guess one.

**Baseline** — if it is not remembered, find it ONCE: search the project's
`.properties` files (skipping `build/`, `.git/`, `.gradle/`, `.idea/`) for a key
that looks like a coverage baseline — contains `coverage` plus
`minimum`/`baseline`/`threshold`, with a numeric value. Match on the key, not the
file name; a baseline file is a project invention with no conventional path. Here:
`config/jacoco/coverage-baseline.properties` → `coverage.instruction.minimum`.
Several hits → ask the user. None → no baseline; report the number with no verdict.

**Command** — with the report task `R` and the discovered suites `S1…Sn`:

```
all (default) -> ./gradlew R --rerun-tasks
one type Si   -> rm -f build/jacoco/*.exec && ./gradlew R -x <every OTHER suite> --rerun-tasks
+ module/group -> rm -f build/jacoco/*.exec && ./gradlew Si --tests '<glob>' R -x <every OTHER suite> --rerun-tasks
```

Here that reads:

```
all           -> ./gradlew jacocoTestReport --rerun-tasks
unit          -> rm -f build/jacoco/*.exec && ./gradlew jacocoTestReport -x testIntgr --rerun-tasks
integration   -> rm -f build/jacoco/*.exec && ./gradlew jacocoTestReport -x test --rerun-tasks
module vet /unit -> rm -f build/jacoco/*.exec && ./gradlew test --tests 'ru.openide.petclinic.vet.*' jacocoTestReport -x testIntgr --rerun-tasks
```

Four rules behind those commands:

- **`--rerun-tasks` always.** Without it Gradle reports the tests UP-TO-DATE, skips
  them, and collects no coverage at all.
- **Running `R` alone covers `all`** — the report task pulls its suites in as
  dependencies. That is also why one type is isolated only by EXCLUDING the others
  (`-x`): naming a single suite still drags the rest in through that dependency.
- **Any narrowed run must clear `build/jacoco/*.exec` first.** The report is built
  from every `.exec` present, so the excluded suite's stale file would be counted
  anyway and the "unit only" number would quietly include the integration tests.
- **`--tests` goes immediately after its suite task** — Gradle binds a task option
  to the task that precedes it, and at the end it would land on the report task and
  abort with "Unknown command-line option". `-x` is a global option, so its position
  is free. For a Gradle subproject, prefix the path (`:web:test`).

**Never run a gate task.** It rewrites the committed baseline on an improvement and
throws on a regression — side effects a measurement must not have. Step 3 compares
the number to the baseline itself; enforcing it is `check`'s job.

Once resolved, **remember it** in this skill's OWN `coverage-runner-configs`
auto-memory file (one per project, `type: project`) — not in the `run-tests`
skill's `test-runner-configs` — under a "Coverage tasks (console path)" section,
distinct from the IDE section:

```
Coverage tasks (console path):
- all              -> ./gradlew jacocoTestReport --rerun-tasks
- unit             -> rm -f build/jacoco/*.exec && ./gradlew jacocoTestReport -x testIntgr --rerun-tasks
- integration      -> rm -f build/jacoco/*.exec && ./gradlew jacocoTestReport -x test --rerun-tasks
- module vet /unit -> rm -f build/jacoco/*.exec && ./gradlew test --tests 'ru.openide.petclinic.vet.*' jacocoTestReport -x testIntgr --rerun-tasks

Baseline (applies to `all` runs only):
- config/jacoco/coverage-baseline.properties : coverage.instruction.minimum
```

Write only what's new — don't rewrite an unchanged mapping.

### 3. Run and collect the result — in ONE Haiku subagent
Spawn a single subagent (Agent tool, `model: "haiku"`) that runs the command and
reports the outcome — the main model runs no Gradle and reads no build log; it
only acts on the report the subagent returns. Coverage-build output is long and
noisy, so keeping the whole run in the subagent keeps it out of the main context
(same reason the project runs its `check` gate in a Haiku subagent).

Resolve the command in the main model (step 2) and pass it to the subagent.

Task for the subagent:

1. **Stale-report guard — delete the XML report BEFORE running:**
   ```
   rm -f build/reports/jacoco/test/jacocoTestReport.xml
   ```
   Gradle stops at the first failing task, so a failing test aborts the run long
   before the report task. Without this, step 3 parses the PREVIOUS report and hands
   back a verdict for code that never ran. Deleting it first makes absent == "the run
   never got there".
2. Run the command from step 2 (it already clears `build/jacoco/*.exec` when the run
   is narrowed).
3. **If the XML is absent afterwards** — the run never reached the report. Find the
   cause: read `build/test-results/*/*.xml` and collect every `<testcase>` with a
   `<failure>`/`<error>`, taking the `message` and the stacktrace verbatim. Report
   NOT measured with those as the cause (`references/report.md`), and NEVER fall back
   to a previous number.
4. **Otherwise** read the coverage number from the report-level INSTRUCTION counter
   in `build/reports/jacoco/test/jacocoTestReport.xml` (the LAST report-level
   `<counter type="INSTRUCTION" covered=… missed=…/>` is the grand total;
   `coverage = covered / (covered + missed)`).
5. **`all` runs only** — read the baseline from the file/key resolved in step 1/2 and
   work out the verdict yourself (same rule as the ratchet: INSTRUCTION ratio vs.
   baseline, epsilon 0.0001): below → `check` WOULD fail; above → the ratchet WOULD
   raise the baseline (it has NOT — nothing was rewritten, and a run of `check` is
   what locks it in); equal → passes. Report what would happen, do not make it
   happen. For a **narrowed run** (one type, or a group) report the number with NO
   verdict — a subset is not comparable to the whole-suite baseline.

**Docker note:** an `all` run includes the integration suite, which needs Docker. If
Docker is genuinely unavailable that suite can't run and the number is incomplete —
the subagent must say so explicitly, not report a unit-only number as the
whole-project coverage.

Turn this into the report defined in `references/report.md` and return ONLY that.
Act on the report in the main model.
