# Configuring PIT in a Gradle build

## 1. Read the build's facts first

Four facts decide the versions and the shape of the config. Gather them before
touching the build file.

**Gradle version** — two JDKs appear in this output; the daemon's is not the one
that matters.

```bash
./gradlew --version
```

**Java toolchain** — this is the JDK PIT will run on.

```bash
grep -n "JavaLanguageVersion\|sourceCompatibility" build.gradle
```

**JUnit Platform version** — Platform 6 (Spring Boot 4) is ahead of every released
`pitest-junit5-plugin`. It works, and it is the first thing to suspect if an
exotic test style fails later.

```bash
./gradlew -q dependencyInsight --configuration testRuntimeClasspath \
    --dependency junit-platform-commons
```

**Test-suite layout** — `jvm-test-suite` gives one source set per suite.

```bash
grep -n "testing {\|suites {\|sourceSets" build.gradle
```

## 2. Resolve the versions

Three versions, from two different repositories. Pin all three explicitly rather
than relying on the plugin's bundled defaults.

**Plugin (`info.solidsoft.pitest`)** — the Plugin Portal is authoritative; take
`<release>`. Maven Central's index lags behind it, and the version Central reports
as newest fails at apply time on Gradle 9 with `Could not get unknown property
'baseDir' for extension 'reporting'`. Do not read the `<versions>`
list instead: it is sorted lexicographically, so it ends at `1.9.11`, not at the
newest release.

```bash
curl -s "https://plugins.gradle.org/m2/info/solidsoft/gradle/pitest/gradle-pitest-plugin/maven-metadata.xml" \
  | grep -E "<latest>|<release>"
```

**PIT itself (`pitestVersion`)** — on Central; take `<release>`.

```bash
curl -s "https://repo1.maven.org/maven2/org/pitest/pitest/maven-metadata.xml" \
  | grep -E "<latest>|<release>"
```

**JUnit 5 bridge (`junit5PluginVersion`)** — on Central; take `<release>`, and
expect it to trail the platform version from step 1 by a major release.

```bash
curl -s "https://repo1.maven.org/maven2/org/pitest/pitest-junit5-plugin/maven-metadata.xml" \
  | grep -E "<latest>|<release>"
```

## 3. Write the config

Add the plugin to `plugins {}` and a `pitest {}` block. Worked example from a
Spring Boot 4 / Gradle 9 / Java 25 project with two `jvm-test-suite` suites
(`test` unit, `testIntgr` Spring + Testcontainers):

```groovy
plugins {
    id 'info.solidsoft.pitest' version '1.19.0'
}

pitest {
    pitestVersion = '1.30.0'
    junit5PluginVersion = '1.2.3'
    targetClasses = ['com.example.*']
    targetTests = ['com.example.*']
    testSourceSets = [sourceSets.test]
    mainSourceSets = [sourceSets.main]
    threads = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
    timestampedReports = false
    outputFormats = ['HTML', 'XML']
    failWhenNoMutations = false
    mutationThreshold = 0
}
```

Four of these are decisions rather than defaults:

- **`testSourceSets`** — start with the fast suite. PIT forks a fresh minion JVM
  per mutant group, so a suite that boots an application context or a container
  pays that cost again and again. Tell the user which layers this leaves outside
  the measurement.
- **`failWhenNoMutations = false`** — keeps a build with little logic green, at the
  price of passing silently once `targetClasses` stops matching. Flip it to `true`
  when there is logic worth mutating, and say so rather than leaving it quiet.
- **`mutationThreshold = 0`** — no gate until a real score exists; then set it just
  below the score achieved.
- **`outputFormats`** — keep `XML`: `scripts/summarize_mutations.py` reads it.

Do not wire `pitest` into `check`; offer it as a choice instead.

## 4. Add the exclusions

Follow `references/exclusions.md` now, while the config is being written: it puts
the set to the user as a questionnaire, and the answers go straight into the block
from step 3. Leaving it for later means the user's first report is dominated by
mutants in bean wiring and getters.

## 5. What the memory entry holds

The shape of this skill's OWN `pitest` file (one per project, `type: project`) —
not the `run-tests` or `coverage` skill's entries. It records what a later run
needs and what was expensive to find out, and nothing a glance at `build.gradle`
answers.

```
Pitest configuration (resolved <YYYY-MM-DD>):
- plugin              -> info.solidsoft.pitest <version>  (Plugin Portal ONLY; the version
                         Maven Central reports as latest fails to apply on Gradle 9)
- pitestVersion       -> <version>
- junit5PluginVersion -> <version>   (drives JUnit Platform <version> here)
- run                 -> ./gradlew pitest --rerun-tasks
- report              -> build/reports/pitest/index.html, build/reports/pitest/mutations.xml
- mutated             -> <targetClasses>, minus excludedClasses/excludedMethods in build.gradle
- killed by           -> the <name> suite only; <other suite> is NOT in testSourceSets,
                         so code covered only by it reports NO_COVERAGE
- gates               -> failWhenNoMutations=<bool>, mutationThreshold=<n>
```

Plus a **Why** line for whatever cost real time here (the Portal-vs-Central
lookup usually does), and a **How to apply** line naming the trap to avoid next
time. One file per project: an existing entry is updated, never joined by a
second.
