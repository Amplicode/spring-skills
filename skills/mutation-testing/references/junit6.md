# Making PIT run on JUnit Platform 6 (Spring Boot 4)

Follow this when step 1 of `configuration.md` shows `junit-platform-commons`
resolving to **6.x** (the platform Spring Boot 4 ships). The `pitest-junit5-plugin`
bridge itself is compatible with Platform 6 — its Launcher API works — so the fix
is never a different `junit5PluginVersion`. What breaks a run is the *environment*
around the minion, in three places that each fail **silently or misleadingly**.
Apply all three while writing the `pitest {}` block; do not wait for a failing run
to reveal them, because the most common failure looks like success.

Throughout, `6.0.3` stands for **the platform version step 1 reported** — pin
Platform, Jupiter, and the launcher to that one number (JUnit 6 unified them onto a
single version line, so there is just one to track).

## Fix 1 — Run the minion on the toolchain JDK, not the daemon's

**Symptom:** the run "passes," yet every class is `NO_COVERAGE` and the mutant
count is near zero — as if no test executed. None did.

**Cause:** PIT forks a "minion" JVM to run the tests and, by default, inherits the
Gradle **daemon's** JVM. That daemon often runs on an older JDK than the project's
toolchain, because a given Gradle version cannot always run on the newest JDK (e.g.
a Gradle 8.14 / 9.7 daemon on JDK 23 while the project targets Java 25). A minion
on the older JDK cannot load the newer class files, so it discovers **zero tests**
— and reports that as blanket `NO_COVERAGE`, not as an error.

**Fix:** point the minion at the toolchain's `java` executable, inside `pitest {}`:

```groovy
pitest {
    // ... versions, targetClasses, etc.
    jvmPath = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }
            .get().executablePath.asFile
}
```

Use the project's toolchain version in `.of(...)` (the one step 1 read from
`JavaLanguageVersion`). It must be `executablePath` — the `java` binary PIT execs
directly — not the toolchain home directory, which gives "Permission denied".

Skip this only when `./gradlew --version` shows the daemon already on the same JDK
as the toolchain. When unsure, set it anyway: it is a no-op if they already match.

## Fix 2 — Give the minion the JUnit 6 launcher + engine

**Cause:** PIT's minion launches tests through the JUnit Platform Launcher. If only
the bridge's own (older) transitive launcher is present, discovery misbehaves.

**Fix:** put the Platform 6 launcher and the Jupiter engine on PIT's **own** launch
classpath — the `pitest` configuration — so the minion starts with them:

```groovy
dependencies {
    pitest 'org.junit.platform:junit-platform-launcher:6.0.3'
    pitest 'org.junit.jupiter:junit-jupiter-api:6.0.3'
    pitest 'org.junit.jupiter:junit-jupiter-engine:6.0.3'
}
```

## Fix 3 — Force the launcher to align with the engine everywhere

**Symptom:** the minion exits abnormally and discovery aborts with
`OutputDirectoryCreator not available; probably due to unaligned versions of the
junit-platform-engine and junit-platform-launcher jars`.

**Cause:** Gradle's JUnit Platform test framework **strict-pins**
`junit-platform-launcher` to the launcher it bundles (a 1.x version). A `strictly`
constraint even downgrades an explicit 6.x request, and it beats
`resolutionStrategy.force` — so `force('...launcher:6.0.3')` is ignored. The
minion's classpath is its own `pitest`-config libs (launcher 6.x, from fix 2) *plus*
the project's test runtime classpath (launcher 1.x, from the strict pin). A 1.x
launcher next to the 6.x engine trips JUnit 6's version-alignment check.

**Fix:** rewrite the coordinate with `dependencySubstitution`, which acts *before*
conflict resolution and therefore wins where `force` loses:

```groovy
configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute module('org.junit.platform:junit-platform-launcher') using module('org.junit.platform:junit-platform-launcher:6.0.3')
    }
}
```

**Verify it took:**

```bash
./gradlew -q dependencyInsight --configuration testRuntimeClasspath \
    --dependency junit-platform-launcher
```

With the block the launcher resolves to `6.0.3`; without it, to the bundled 1.x
(you will see `-> 1.x` on the request). This substitution rewrites the launcher for
the ordinary suites too, so confirm they stay green: `./gradlew test`.

## After the three fixes

Run PIT (`usage.md`) and read the score. Two follow-ups specific to this path:

- **A first flood of `NO_COVERAGE`** that survives fix 1 usually means the code is
  covered only by a Spring/Testcontainers integration suite that is not in
  `testSourceSets` — a real gap, not a JUnit 6 problem. Widen `testSourceSets` or
  accept it, as in `configuration.md` step 3.
- **A few `TIMED_OUT` mutants on the first groups** can come from the newer JDK's
  cold start being slow. If it gets noisy, raise `timeoutConstInMillis` in
  `pitest {}`.

Upstream tracking: github.com/pitest/pitest-junit5-plugin#113. The released bridge
works once this environment is in place — you do not need an unreleased build.
