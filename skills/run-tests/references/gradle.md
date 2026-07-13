# Running tests via console Gradle (fallback)

Used when the IDE path isn't reachable (steroid absent, or the repo isn't among
the open projects). Runs Gradle directly in the console instead of an IDE Run
Configuration. Same shape as the IDE path — match a test type/module to a Gradle
task, ask when unsure, remember, run in a subagent, return the
`references/report.md` report.

Tell the user up front you're using the console fallback because the IDE wasn't
reachable.

## Steps

### 1. Recall the mapping from memory
Check auto-memory for the `test-runner-configs` entry for THIS project (the
mapping step 2 writes, "Gradle tasks" section). If it already names the task(s)
for the requested type/module, skip to step 3 with those.

### 2. List tasks and match (first use, or when memory has no match)
List the project's Gradle tasks — `./gradlew tasks --all` (run it in a subagent;
the output is long). Judging by what each actually runs (not just the name), map
each requested test type to the task(s) that fit it:

```
unit        -> test
integration -> testIntgr
all         -> test testIntgr
```

Then, per type:
- Exactly one fits → use it.
- None fits → fall back to the conventional task from SKILL.md's type table
  (`test` / `testIntgr`), or ask the user for the task name.
- More than one plausible task (e.g. both `testIntgr` and `integrationTest`, or a
  broader `check`) → ask the user which to use with the `AskUserQuestion` tool
  (don't guess — the wrong task runs the wrong tests), e.g.:

  ```
  AskUserQuestion(questions=[{
    "header": "Integration",
    "question": "Which Gradle task should run the integration tests?",
    "multiSelect": false,
    "options": [
      {"label": "testIntgr",        "description": "the testIntgr source set"},
      {"label": "integrationTest",  "description": "the integrationTest source set"}
    ]
  }])
  ```

Once the type→task is resolved — however it was resolved (one fit, the user
picked, or the conventional default) — **remember it** so step 1 can reuse it
next time. Use the SAME `test-runner-configs` auto-memory file as the IDE path
(one file per project, `type: project`), under a separate "Gradle tasks" section:

```
Gradle tasks (console path):
- unit        -> ./gradlew test
- integration -> ./gradlew testIntgr
- all         -> ./gradlew test testIntgr
```

Write only what's new — don't rewrite an unchanged mapping. Keep it distinct from
the IDE "Run Configurations" section in the same file.

### 3. Run and collect the result — in ONE Haiku subagent
Spawn a single subagent (Agent tool, `model: "haiku"`) that runs the Gradle task
and reports the outcome — the main model runs no Gradle and reads no build log; it
only acts on the report the subagent returns. Gradle output is long and noisy, so
keeping the whole run in the subagent keeps it out of the main context (same
reason the project runs its `check` gate in a Haiku subagent).

Resolve the command in the main model and pass it to the subagent:
- Base command per type: `./gradlew test` (unit), `./gradlew testIntgr`
  (integration), `./gradlew test testIntgr` (all — or run once per task).
- Narrow to a package/class: append `--tests '<glob>'` (e.g.
  `--tests 'ru.openide.petclinic.vet.*'`).
- A module (Gradle subproject): prefix the task path (`:web:test`).

Task for the subagent — run the command, then read the JUnit XML under
`build/test-results/<sourceSet>/` (`test` for unit, `testIntgr` for integration;
for `all`, once per source set). For each failed `<testcase>`, take the
`<failure>`/`<error>` `message` and stacktrace. Turn this into the report defined
in `references/report.md` and return ONLY that. Act on the report in the main
model.
