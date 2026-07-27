# Formatting files through the IntelliJ IDE/OpenIDE

The primary path, used when the IDE is reachable. The IDE's own formatter
(`ReformatCodeProcessor`) applies the project's configured code style — the same
result a developer gets from **Code → Reformat Code**. Driving it through the IDE
(rather than an external CLI formatter) means the style always matches what the
team sees, and the IDE's VFS/PSI/index caches stay consistent after the edit.

## Prerequisites

You already have the `project_name` from the path-selection probe in `SKILL.md`
(`steroid_list_projects`) — that opaque key (e.g. `spring-petclinic-ipvczgii`,
**not** the human-readable `name`) routes every `steroid_execute_code` call below.
Reuse it; **don't re-probe**.

If the MCP Steroid tool schemas aren't loaded yet (deferred in Claude Code), load
them: `ToolSearch("steroid list projects execute code")`, then use
`steroid_execute_code`.

Run all formatting code with `modal: "smart_non_modal"` (the default). It commits
documents and refreshes the VFS around your script, which is exactly what
formatting needs.

## Choosing the recipe

| The user wants to format…               | Use                                                                        |
|-----------------------------------------|----------------------------------------------------------------------------|
| One file named by path or class name    | [Single file](#1-single-file-by-name)                                      |
| A specific block / line range in a file | [Code block](#2-code-block-line-range)                                     |
| Every uncommitted / changed file        | [All uncommitted files](#3-all-uncommitted-files) → run the bundled script |
| Several named files at once             | [A group of named files](#4-a-group-of-named-files)                        |

Always report back what actually changed — run `git diff --stat <paths>` after
formatting. It's normal and correct for a reformat to produce **no diff** when
the file already conforms to the code style; say so plainly rather than implying
work was done.

---

## 1. Single file by name

Script: [`../scripts/format-single-file.kts`](../scripts/format-single-file.kts).

**To run it:** read the script, edit the `name` / `pathSuffix` placeholders at
the top to point at the target file, then pass the body as the `code` argument
to `steroid_execute_code`. Drop the `OptimizeImportsProcessor` line for non-JVM
files.

## 2. Code block (line range)

Script: [`../scripts/format-code-block.kts`](../scripts/format-code-block.kts).

**To run it:** read the script, edit the `name` / `startLine` / `endLine`
placeholders, then pass the body as the `code` argument to
`steroid_execute_code`. If the user pasted a snippet rather than a line range,
build the `TextRange` from `indexOf(snippet)` instead — see the header comment
in the script.

Block / line-range formatting is **IDE-only** — there's no faithful CLI
equivalent. If the IDE is unreachable, say so rather than falling back to a
whole-file format.

## 3. All uncommitted files

This is a bundled, validated script:
[`../scripts/format-uncommitted.kts`](../scripts/format-uncommitted.kts).

**To run it:** read the script file and pass its body as the `code` argument to
`steroid_execute_code` (`project_name` = the routing key, `modal:
"smart_non_modal"`). It's self-contained — no arguments needed. Afterward, run
`git status --porcelain` / `git diff --stat` to show the user the result.

Adjust the `FORMATTABLE` extension set at the top of the script if the user
wants a narrower or wider net (e.g. only `java`).

## 4. A group of named files

Script: [`../scripts/format-file-group.kts`](../scripts/format-file-group.kts).

**To run it:** read the script, edit the `requested` list to the target files,
then pass the body as the `code` argument to `steroid_execute_code`.
