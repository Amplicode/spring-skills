---
name: mutation-testing
description: >-
  Set up and run mutation testing with PIT (pitest) on a Gradle project, and
  read the mutation score. Use this skill whenever the user wants mutation
  testing added to a build, configured, run or re-run, wants a mutation score
  or a mutation report interpreted, asks why a particular mutant survived, or
  asks what should be excluded from mutation analysis, or wants something added
  to those exclusions — in any language, and whether or not the tool is named.
  Also use it when the task calls for it: checking test quality rather than
  coverage — a high JaCoCo number can hide tests that assert nothing.
---

# Mutation testing

## Steps

Start by reading this skill's own `pitest` entry for THIS project
(`type: project`) — the checks below are built on it. A missing entry is not an
error; it means nothing has been recorded yet.

Then work through the steps in order. Each carries a check: when the check fires
the step is already done — skip it and move on. Load a step's reference only if
you actually run that step, so there is context left for the PIT output.

### 1. Configure PIT in the build

**Skip if** PIT is already configured. Two signals say so — stop at the first hit:

- **The entry** describes a configuration for this project. That is enough: skip
  the step, and do not spend a command confirming what the entry just said.
- **No entry** — then ask the build file:

  ```bash
  grep -n "info.solidsoft.pitest" build.gradle build.gradle.kts 2>/dev/null
  ```

Otherwise follow `references/configuration.md`, which applies the exclusions as
part of the setup.

If step 3 then fails with `Task 'pitest' not found`, the entry was stale: say so,
run this step after all, and continue.

### 2. Adjust what gets mutated

**Skip if** the request has nothing to do with the exclusions or `targetClasses`.

Otherwise follow `references/add-exclusion.md`. It answers a question as well as
it applies a change — when the request is only a question, stop after judging the
candidate and leave the build alone.

### 3. Run PIT and report the score

**Skip if** the user asked only to wire PIT into the build, or only for advice on
what to exclude — then say a run is available and stop there.

Otherwise follow `references/usage.md`.

### 4. Record what was resolved

**Skip if** the entry already describes what the run actually used.

Otherwise write or update `pitest` in the shape `references/configuration.md`
step 5 defines. This is what lets step 1 be skipped next time, so it is not
optional bookkeeping — dropping it makes the next run pay for the version probing
and compatibility checks all over again.
