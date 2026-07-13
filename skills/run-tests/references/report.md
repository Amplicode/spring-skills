# Result report format

The report the result-collecting Haiku subagent returns to the main agent, after
running the tests (whichever path — IDE or console Gradle). It is the ONLY thing
the subagent returns: no build log, no console dump.

## Format

**Passed:**

```
<sourceSet>: PASSED (<total> tests)
```

**Failed:**

```
<sourceSet>: FAILED (<failed>/<total>)
- <test>
    <message>
    <key stacktrace line(s), verbatim>
```

For `all` (two source sets), give one block per source set.

## Rules

- Report the failure `message` and the key `stacktrace` line(s) **verbatim** —
  enough for the main agent to act on the real error, never a vague "some tests
  failed".
- Console noise is NOT a failure. Ignore Mockito self-attach warnings and
  "Invalid Java installation" auto-detect notes; they appear in red but don't
  affect the result. Judge only by actual test failures.
- If the run didn't complete (timed out), report it as NOT passed — not as a
  pass.

## Examples

Green:

```
test: PASSED (25 tests)
```

Red:

```
testIntgr: FAILED (1/14)
- ru.openide.petclinic.vet.VeterinarianApiIntegrationTest#createReturns201ThenGetReturnsIt
    expected: <201> but was: <500>
    java.lang.AssertionError: expected: <201> but was: <500>
        at ru.openide.petclinic.vet.VeterinarianApiIntegrationTest.createReturns201…(…:88)
```
