---
name: codefmt
description: >-
  Reformat source code in this project using its own code-style settings —
  through the IntelliJ IDE when it's running, otherwise via the project's own
  formatter (Spotless / google-java-format / palantir-java-format) as a fallback.
  Use this whenever the user asks to format, reformat, tidy, "prettify", fix
  indentation, or optimize imports on Java/Kotlin/XML/etc. files — whether it's
  a single named file, a selected block or line range, a group of named files,
  or all uncommitted (git-changed) files. This skill is also normally invoked
  after any editing of files — apply it to the files you just created or changed
  once the edits are done, so the result stays consistent with the project's
  code style.
---

# Format source files with the project's code style

Two paths reach the same code style: the **IDE** applies it through IntelliJ's own
formatter (highest fidelity, keeps the IDE's VFS/PSI caches consistent); the
**CLI fallback** applies it through the project's own external formatter (Spotless
/ google-java-format / palantir-java-format) when the IDE isn't reachable.

## Pick the path

Probe FIRST, here, before loading either reference — so you read only the one you
need. Call `steroid_list_projects` (via `ToolSearch("steroid list projects
execute code")` if the tool schema isn't loaded):

- It returns the repo you're in → **IDE path**: note that entry's `project_name`
  (the opaque routing key, **not** the human-readable `name`) and follow
  [`references/ide.md`](references/ide.md). Its recipes start already knowing the
  `project_name` — don't re-probe.
- It fails, or the repo isn't listed → **CLI fallback path**: follow
  [`references/fallback.md`](references/fallback.md) (detects Spotless /
  java-format and runs it in the console) and tell the user up front you're using
  the external-formatter fallback because the IDE wasn't reachable.

Block / line-range formatting (IDE recipe 2) is **IDE-only** — there's no faithful
CLI equivalent. If the user asked for a block and the IDE is unreachable, say so
rather than silently reformatting the whole file.

Both paths end the same way: run `git diff --stat <paths>` and report what
actually changed. A reformat that produces **no diff** is a valid outcome — the
files already conform to the style; say so plainly rather than implying work was
done.
