# Running mutation testing and reading the score

## Run it and collect the result — in ONE Haiku subagent

Spawn a single subagent (Agent tool, `model: "haiku"`) that runs PIT and returns
the summary and nothing else. Hand that summary to the user as it came.

Task for the subagent:

1. Run, and keep its exit status:
   ```bash
   rm -f build/reports/pitest/mutations.xml && ./gradlew pitest
   ```
2. Summarise it:
   ```bash
   python3 <skill-dir>/scripts/summarize_mutations.py
   ```
3. **Script exits 0** — return its output verbatim; that is the report. If Gradle
   failed as well, the score breached `mutationThreshold`: add PIT's own line above
   the summary, e.g. `Mutation score of 33 is below threshold of 90`. A report plus
   a failed build means the gate fired, not that the run broke.
4. **Script exits non-zero** — 2 for no report on disk, 3 for an unparseable one —
   the run produced nothing. Find the cause in the Gradle output: a red test (name
   it, with its message and stacktrace verbatim), a `PitHelpError`, `No mutations
   found`, or a failure before PIT started at all. Report it in the NOT MEASURED
   form from `references/report.md`. Never fall back to an earlier number.
