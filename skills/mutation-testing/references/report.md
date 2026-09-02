# Mutation testing report format

What the result-collecting Haiku subagent returns to the main agent. It is the
ONLY thing it returns: no PIT log, no per-mutator table, no timings.

## Format — a completed run

```
Mutation score: <killed>/<total> (<pct>)
Test strength:  <killed>/<covered> (<pct>)
Mutated:        <targetClasses>, driven by the <suite> suite
Threshold:      <n> — <PASSED | FAILED | not enforced (0)>

Survived (missing assertions), <n>:
- <Class>.<method>  <file>:<line>  <Mutator>
    <PIT's description of the mutant>

No coverage (not reached by the driving suite), <n>:
- <Class>.<method>  <file>:<line>  <Mutator>

HTML: build/reports/pitest/index.html
```

List every survivor when there are ten or fewer; above that, list the ten in the
classes with the most survivors and give the remaining count. A survivor without
`file:line` and a mutator name is not actionable — it sends the reader back to the
HTML report to learn what this run already knew.

Report **both** numbers, always. The mutation score alone conflates "not asserted
on" with "not reached", and those need different work.

## Format — nothing to mutate

Not a failure, and not a score of zero. Say what it means:

```
Mutation score: NO MUTANTS — PIT found nothing to mutate in <targetClasses>.
```

Then give the reason, because the two possible causes need opposite responses:
the code genuinely has no mutable logic yet (fine, nothing to do), or
`targetClasses`/`excludedClasses` no longer match the codebase (a config bug that
has been silently passing). Check which before reporting.

## Format — not measured

```
Mutation score: NOT MEASURED — the run produced no report.
Cause: <n> failing test(s) in <suite>
- <test>
    <message>
    <key stacktrace line(s), verbatim>
```

Give the message AND the key stacktrace lines verbatim, same rule as the
`run-tests` and `coverage` skills — the `file:line` is what makes it actionable.
`Mutation testing requires a green suite` on its own says the run stopped but not
what to fix.

Never fall back to an earlier number when this run produced none.

## Rules

- Numbers verbatim from `mutations.xml`, never a vague "mutation coverage looks
  reasonable".
- A `NO_COVERAGE` block is only a coverage gap if the driving suite is supposed to
  reach it. When it is covered by a suite outside `testSourceSets`, label it as
  outside the measurement instead of reporting it as untested code.
- When the score improved past a non-zero `mutationThreshold`, the threshold was
  NOT raised. Say so and leave the decision to the user.
- Console noise is not a failure: ignore "Invalid Java installation" auto-detect
  notes and the Arcmutate advertisement in PIT's build messages. Judge only by the
  numbers and the threshold verdict.

## Example

```
Mutation score: 34/41 (82.9%)
Test strength:  34/36 (94.4%)
Mutated:        com.example.* minus *Config/*Properties/accessors, driven by the test suite
Threshold:      75 — PASSED

Survived (missing assertions), 2:
- OwnerService.findByLastName  OwnerService.java:48  ConditionalsBoundary
    changed conditional boundary
- Pet.isYoung  Pet.java:31  NegateConditionals
    negated conditional

No coverage (not reached by the driving suite), 5:
- OwnerController.update  OwnerController.java:77  NegateConditionals
  (+4 more in OwnerController — covered by the testIntgr suite, which is outside
   testSourceSets, so this is not a coverage gap)

HTML: build/reports/pitest/index.html
```
