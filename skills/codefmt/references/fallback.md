# Formatting via the project's own formatter (CLI fallback)

Used when the IDE path isn't reachable (steroid absent, or the repo isn't among
the open projects). Instead of the IDE's `ReformatCodeProcessor`, this path shells
out to the formatter the project already configures — **Spotless** first (it
applies the team's committed config, so the result matches the IDE closely), then
a standalone **google-java-format / palantir-java-format** JAR. Same shape as the
IDE path: detect the tool, run it in a subagent, report what changed.

Tell the user up front you're using the external-formatter fallback because the
IDE wasn't reachable.

Two of the four IDE recipes carry over here: **all uncommitted files** and
**named files / group**. **Block / line-range** formatting has no faithful CLI
equivalent — it stays IDE-only. If the user asked for a block and the IDE is
unreachable, say so rather than silently reformatting the whole file.

## A. Detect the formatter

Read the target repo's build files — don't guess. Detection is plain shell/grep
in the main model (cheap; small output). Take the first that matches, in this
order:

### 1. Recall from memory first
Check auto-memory for a `codefmt-formatter` entry for THIS project (the mapping
step D writes). If it already names the tool and its invocation, skip detection
and go straight to section B/C with it.

### 2. Spotless — preferred
Applies the project's committed config, so its output matches the IDE most
closely.

- **Gradle** — the plugin id `com.diffplug.spotless` appears in a build script,
  **or** a `spotlessApply` task exists:

  ```bash
  grep -rlE 'com\.diffplug\.spotless|spotless' \
    --include='build.gradle' --include='build.gradle.kts' \
    --include='settings.gradle' --include='settings.gradle.kts' \
    --include='libs.versions.toml' .
  ```

  Confirm the task is really wired: `./gradlew tasks --all | grep spotlessApply`
  (run in the subagent — the task list is long). Invocation: `./gradlew spotlessApply`.
- **Maven** — `spotless-maven-plugin` (group `com.diffplug.spotless`) in a `pom.xml`:

  ```bash
  grep -rl 'spotless-maven-plugin' --include='pom.xml' .
  ```

  Invocation: `mvn spotless:apply`.

### 3. Standalone google-java-format / palantir-java-format
Only when Spotless is **absent**. Java-only formatters. Look for a resolvable
JAR or launcher:

```bash
find . -maxdepth 4 -name 'google-java-format*.jar' -o -name 'palantir-java-format*.jar' 2>/dev/null
command -v google-java-format 2>/dev/null
```

Invocation: `java -jar <formatter>.jar --replace <files.java>` (or the launcher on
`PATH`). Note in the report that only `.java` files are covered.

### 4. None found
Do **not** silently no-op. Tell the user the IDE is unreachable **and** no
external formatter (Spotless / java-format) is configured, then ask with
`AskUserQuestion` whether to:
- (a) start the IDE / open the project so the IDE path can run, or
- (b) point at a formatter command to use.

Do not fabricate a format.

Once detected, record the tool + invocation in the `codefmt-formatter` memory
(section D) so the next fallback run skips detection.

## B. Invoke — all uncommitted files

Resolve the changed set exactly as the IDE script does — `git status --porcelain`
(staged + unstaged + untracked); keep only paths that still exist on disk.
Renames show `orig -> new` (take the new path); untracked lines start with `??`.

- **Spotless / Gradle**: `./gradlew spotlessApply`. To scope to just the changed
  files rather than the whole tree, pass the file filter
  `-PspotlessFiles='<regex>'` — Spotless matches the regex against **absolute**
  paths, so alternate the changed paths and anchor loosely
  (e.g. `-PspotlessFiles='.*/(Owner\.java|Pet\.java)'`). When the changed set is
  large or the regex is unwieldy, run the whole-project `spotlessApply` and **say
  in the report that it formatted the whole tree**, not just the changed files.
- **Spotless / Maven**: `mvn spotless:apply` (optionally `-DspotlessFiles='<regex>'`).
- **java-format JAR**: `java -jar <formatter>.jar --replace <changed .java files>`.
  Non-`.java` changed files are left untouched — **say so in the report**.

## C. Invoke — named files / group

Same tool, narrowed to the requested paths:

- **Spotless / Gradle**: `./gradlew spotlessApply -PspotlessFiles='<file-regex>'`.
- **Spotless / Maven**: `mvn spotless:apply -DspotlessFiles='<file-regex>'`.
- **java-format JAR**: `java -jar <formatter>.jar --replace <paths>`.

If a requested file's type isn't covered by the formatter — e.g. a `.xml` when
only Spotless's `java` block is configured, or any non-`.java` for a java-format
JAR — report it as **not formatted by the fallback** rather than claiming success.
The IDE would have formatted it; the CLI can't.

## Run the invocation in ONE Haiku subagent

Spawn a single subagent (Agent tool, `model: "haiku"`) that runs the resolved
command and reports the outcome — the main model runs no Gradle/Maven and reads no
build log; it only acts on the short report the subagent returns. Build output is
long and noisy, so keeping the whole run in the subagent keeps it out of the main
context (same reason `run-tests` runs its Gradle in a Haiku subagent). Detection
(section A) stays in the main model — it's cheap and its output is small.

Resolve the command in the main model and pass it to the subagent. Task for the
subagent: run the command, then run `git diff --stat` on the target paths and
return ONLY:
- which files changed (from `git diff --stat`), and
- any requested files the formatter did **not** cover (wrong type, or whole-tree
  fallback was used instead of a scoped run).

No build log, no console dump.

## D. Report + remember

After the subagent returns, run `git status --porcelain` / `git diff --stat
<paths>` to show the user what changed. **No diff is a valid outcome** — it means
the files already conform to the style; say so plainly rather than implying work
was done (same note as the IDE path).

Persist the detected formatter so section A can skip re-detection next time. Keep
one auto-memory file per project, `type: project`, named `codefmt-formatter` (link
it from `MEMORY.md`); its body holds the tool and its invocation as plain lines:

```
Project <name>: external formatter for the codefmt CLI fallback (IDE unreachable).
- tool        -> Spotless (Gradle)
- all changed -> ./gradlew spotlessApply [-PspotlessFiles='<regex>']
- named files -> ./gradlew spotlessApply -PspotlessFiles='<regex>'
- covers      -> java (Spotless `java` block only)
```

Write only what's new — don't rewrite an unchanged mapping. If the project switches
formatters (an `inspect`/grep no longer matches the remembered tool), update the
line rather than adding a duplicate.
