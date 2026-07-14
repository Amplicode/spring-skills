# Coverage report format

The report the result-collecting Haiku subagent returns to the main agent, after
measuring coverage (whichever path — IDE or console Gradle). It is the ONLY thing
the subagent returns: no build log, no console dump.

## Format — whole project

```
Coverage: <coveragePct> (<covered>/<total> instructions)
Baseline: <baselinePct>
Verdict:  <ABOVE | EQUAL | BELOW>
```

The verdict is computed from the numbers, NOT applied: the skill runs
`jacocoTestReport`, never `coverageRatchet`, so nothing was rewritten and nothing
failed. Say what *would* happen — phrase it in the conditional:

- `ABOVE` — coverage improved above the baseline. The baseline was **not** raised;
  running `./gradlew check` (or the ratchet task) rewrites the baseline file —
  commit it to lock the gain in. Name the actual file resolved for this project.
- `EQUAL` — coverage unchanged; the ratchet would pass.
- `BELOW` — coverage regressed; `check` **would fail**. Add tests to restore
  coverage; do NOT lower the baseline by hand.

## Format — a narrowed run (one type, and/or a module/group)

No baseline, no verdict: the baseline describes what ALL the tests cover together,
so a subset has nothing to be judged against. Name exactly what was measured, so
the number can't be mistaken for the project's.

```
Coverage by <unit|integration> tests<, of <glob>>: <coveragePct> (<covered>/<total> instructions)
```

## Format — not measured

The run never reached `jacocoTestReport` (`reportFound: false`). Gradle stops at
the first failing task, so this is almost always a failing test — name it, don't
just say the build broke. NEVER fall back to a previous number.

```
Coverage: NOT MEASURED — the run failed before producing the report.
Cause: <n> failing test(s) in <sourceSet>
- <test>
    <message>
    <key stacktrace line(s), verbatim>
```

Same rule as the `run-tests` skill: give the `message` AND the key `stacktrace`
lines **verbatim** — the file:line is what makes this actionable. `expected: true
but was: false` alone says what broke but not where, which sends the caller off to
re-run the tests just to learn what this run already knew.

## Both

Add the HTML report path so the user can open it:
```
HTML: build/reports/jacoco/test/html/index.html
```

## Rules

- Report the coverage % and the baseline % **verbatim** from the numbers read —
  never a vague "coverage looks fine".
- On `BELOW`, state the gap (current vs. baseline) so the main agent knows how far
  coverage fell.
- Never attach a verdict to a narrowed run (`partial: true` / `verdict:
  n/a-partial`), and never present its number as the project's coverage — it is a
  subset and is expected to be lower.
- If the run didn't complete (timed out, or `reportFound: false` — a failing test,
  or Docker unavailable so `testIntgr` couldn't run), report it as NOT measured and
  give the cause. Do NOT present a partial or unit-only number as the whole-project
  coverage, and do NOT report a number at all when the report is missing.
- Console noise is not a failure. Ignore Mockito self-attach warnings and "Invalid
  Java installation" auto-detect notes; judge only by the coverage number and the
  ratchet verdict.

## Examples

Improved:

```
Coverage: 97.75% (564/577 instructions)
Baseline: 97.40%
Verdict:  ABOVE
Coverage improved by 0.35% — the baseline is NOT raised yet. Run ./gradlew check
to ratchet it to 0.9775 and commit config/jacoco/coverage-baseline.properties.
HTML: build/reports/jacoco/test/html/index.html
```

Held:

```
Coverage: 97.40% (562/577 instructions)
Baseline: 97.40%
Verdict:  EQUAL
Coverage unchanged — the ratchet would pass.
HTML: build/reports/jacoco/test/html/index.html
```

Regressed (`check` would fail):

```
Coverage: 96.02% (554/577 instructions)
Baseline: 97.40%
Verdict:  BELOW
Coverage dropped 1.38% below the baseline — ./gradlew check would FAIL on
coverageRatchet. Add tests to restore coverage; do NOT lower the baseline.
HTML: build/reports/jacoco/test/html/index.html
```

Unit tests only:

```
Coverage by unit tests: 71.06% (410/577 instructions)
HTML: build/reports/jacoco/test/html/index.html
```

One module, unit tests only:

```
Coverage by unit tests, of ru.openide.petclinic.vet.*: 41.20% (238/577 instructions)
HTML: build/reports/jacoco/test/html/index.html
```

Not measured (a failing test aborted the run before the report):

```
Coverage: NOT MEASURED — the run failed before producing the report.
Cause: 1 failing test in test
- ru.openide.petclinic.vet.service.VeterinarianServiceTest#findByIdThrowsWhenMissing()
    org.opentest4j.AssertionFailedError: expected: true but was: false
    at ru.openide.petclinic.vet.service.VeterinarianServiceTest.findByIdThrowsWhenMissing(VeterinarianServiceTest.java:49)
```
