# Adding an exclusion

Adding or changing an exclusion on a build that already has PIT configured.

## 1. List what is already excluded

Two sources, and both count. An entry for something already excluded is dead
config: it cannot be verified by a later reader and it hides that the real filter
lives elsewhere.

**Configured in the build** — the source of truth for this project.

```bash
sed -n '/^pitest {/,/^}/p' build.gradle
```

**PIT's own filters** — ask the PIT version in use; the set grows between releases,
so a written-down list goes stale. Add `verbosity = 'VERBOSE'` to the `pitest {}`
block, run once, then take it back out.

```bash
./gradlew pitest --rerun-tasks 2>&1 | grep -E "PIT >> INFO : [+-]" | grep -i filter
```

`+` is on, `-` is off. PIT 1.30.0 ships 25 such filters, 23 of them on — records,
enums, Lombok output, logging calls, static initializers, equivalent returns,
try-with-resources, for-each and switch scaffolding, and more.

The same run prints the whole effective config at `FINE` level, which is the way
to confirm what PIT actually received rather than what the build file appears to
say:

```bash
./gradlew pitest --rerun-tasks 2>&1 \
  | grep -oE "(target|excluded)[A-Za-z]*=\[[^]]*\]"
```

Put the filter list into the `pitest` memory entry so the next exclusion request
does not need the verbose run again.

## 2. Reject the candidate if it should stay mutated

Stop and say so — do not add the entry — when the candidate:

- **has a branch, a computation or a decision.** That is what mutation testing is
  for. Untested logic must stay visible in the report; excluding it to tidy up the
  score is the one move this step exists to prevent. Entities with real behaviour,
  controllers, validators, filters and interceptors belong here — only their
  accessors are noise;
- is covered by a `+` filter from step 1 (a record, an enum, Lombok or generated
  scaffolding);
- is outside `targetClasses` already, so it was never mutated;
- is matched by an existing glob. Check the glob, not the class name: `*Config`
  matches only names ending in `Config`, so `ConfigLoader` is NOT covered by it.

## 3. Choose the narrowest form that works

Everything in this table removes the mutant from the report:

| Mechanism | Unit | Effect |
|-----------|------|--------|
| `targetClasses` | glob on the FQN | the inclusion boundary — anything outside it is never mutated. Only readable from the `pitest {}` block: there is no `-D` or `-P` override, so narrowing one run means editing it and putting it back |
| `excludedClasses` | glob on the FQN | subtracts from `targetClasses`: one class, or a whole package (`com.example.web.*`) |
| `excludedMethods` | glob on the method name | the same method across every class, which is how accessors are handled |
| `@DoNotMutate` and friends | class or method | see below |
| `mainSourceSets` | source set | which compiled output counts as mutable at all |
| `additionalMutableCodePaths` | path | the inverse: a path not listed is not mutated |
| `mutators` | mutation operator | drops a kind of mutant rather than a piece of code |
| `features` with `-` | built-in filter | turns a filter OFF, so it widens rather than narrows |
| `avoidCallsTo` | package | suppresses mutants on calls into it; already covers the logging frameworks |

Match the candidate to the narrowest one that fits:

| Candidate | Form |
|-----------|------|
| a whole class with no behaviour worth asserting | `excludedClasses` glob |
| accessors or `Object` boilerplate across many classes | `excludedMethods` glob |
| one method inside a class that must stay mutated | `@DoNotMutate` on that method |
| a whole layer the driving suite cannot reach | narrow `targetClasses` — and say which layers that leaves unmeasured |

Prefer a glob over listing classes one by one only when the name pattern is the
reason for exclusion (`*MapperImpl`). A glob chosen for brevity will silently
swallow the next class that happens to match.

### Annotations

PIT ships no annotation classes, so there is nothing to depend on — declare your
own, in any package. `+fann` matches on the name alone ("full package name not
required") and knows three by default: `Generated`, `DoNotMutate`,
`CoverageIgnore`.

```java
@Retention(RetentionPolicy.RUNTIME)   // CLASS works too; SOURCE never does
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DoNotMutate {
}
```

`SOURCE` retention is invisible to PIT, which rules out both ready-made
`Generated` annotations — `javax.annotation.processing.Generated`, the one
MapStruct puts on its impls, and `jakarta.annotation.Generated`. Generated code
needs a name glob instead.

A name of your own must be registered, and the parameter **replaces** the default
three instead of adding to them: registering `SkipMutation` by itself silently
stops `@DoNotMutate` from working. List every name that should stay in force.

```groovy
features = ['+fann(annotation[SkipMutation] annotation[DoNotMutate] annotation[Generated] annotation[CoverageIgnore])']
```

Simpler: call the annotation `DoNotMutate` and leave the feature parameter alone.

### The other axis: who may kill

`testSourceSets`, `targetTests`, `excludedTestClasses`, `includedGroups` /
`excludedGroups` and `includedTestMethods` restrict the TESTS, not the code. They
do not remove a mutant — they leave it as `NO_COVERAGE`. Reaching for one of them
to exclude a class makes the report worse: the class still counts against the
score, now labelled untested.

## 4. Apply, then measure the delta

Keep the per-class table from the run before the edit, apply the change, re-run,
and compare:

```bash
./gradlew pitest --rerun-tasks
python3 <skill-dir>/scripts/summarize_mutations.py
```

Two things to confirm, because a glob that over-matches looks exactly like a
successful exclusion:

- the target class or method is gone from the table;
- no OTHER class lost mutants.

If the total mutant count dropped by more than the target accounts for, the glob
is too wide — narrow it and measure again.

## 5. Update the memory entry

The `mutated` line of the `pitest` entry describes what gets mutated, so an
exclusion makes it wrong. Rewrite that line; leave the rest of the entry alone.
