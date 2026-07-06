---
name: codefmt
description: >-
  Reformat source code in this project using its own code-style settings. Use
  this whenever the user asks to format, reformat, tidy, "prettify", fix
  indentation, or optimize imports on Java/Kotlin/XML/etc. files — whether it's
  a single named file, a selected block or line range, a group of named files,
  or all uncommitted (git-changed) files. This skill is also normally invoked
  after any editing of files — apply it to the files you just created or changed
  once the edits are done, so the result stays consistent with the project's
  code style.
---

# Formatting files through the IntelliJ IDE/OpenIDE

This project runs inside a live IntelliJ IDEA instance reachable through the
**MCP Steroid** tools. The IDE's own formatter (`ReformatCodeProcessor`) applies
the project's configured code style — the same result a developer gets from
**Code → Reformat Code**. Driving it through the IDE (rather than an external
CLI formatter) means the style always matches what the team sees, and the IDE's
VFS/PSI/index caches stay consistent after the edit.

## Prerequisites

1. Load the MCP Steroid tools if they aren't already available — their schemas
   are deferred in Claude Code:
   `ToolSearch("steroid list projects execute code")` then use
   `steroid_list_projects` and `steroid_execute_code`.
2. Get the routing key: call `steroid_list_projects` and take the
   `project_name` (the opaque key, e.g. `spring-petclinic-ipvczgii`), **not** the
   human-readable `name`. Every `steroid_execute_code` call needs this key.
3. Run all formatting code with `modal: "smart_non_modal"` (the default). It
   commits documents and refreshes the VFS around your script, which is exactly
   what formatting needs.

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

Script: [`scripts/format-single-file.kts`](scripts/format-single-file.kts).

**To run it:** read the script, edit the `name` / `pathSuffix` placeholders at
the top to point at the target file, then pass the body as the `code` argument
to `steroid_execute_code`. Drop the `OptimizeImportsProcessor` line for non-JVM
files.

## 2. Code block (line range)

Script: [`scripts/format-code-block.kts`](scripts/format-code-block.kts).

**To run it:** read the script, edit the `name` / `startLine` / `endLine`
placeholders, then pass the body as the `code` argument to
`steroid_execute_code`. If the user pasted a snippet rather than a line range,
build the `TextRange` from `indexOf(snippet)` instead — see the header comment
in the script.

## 3. All uncommitted files

This is a bundled, validated script:
[`scripts/format-uncommitted.kts`](scripts/format-uncommitted.kts).

**To run it:** read the script file and pass its body as the `code` argument to
`steroid_execute_code` (`project_name` = the routing key, `modal:
"smart_non_modal"`). It's self-contained — no arguments needed. Afterward, run
`git status --porcelain` / `git diff --stat` to show the user the result.

Adjust the `FORMATTABLE` extension set at the top of the script if the user
wants a narrower or wider net (e.g. only `java`).

## 4. A group of named files

Script: [`scripts/format-file-group.kts`](scripts/format-file-group.kts).

**To run it:** read the script, edit the `requested` list to the target files,
then pass the body as the `code` argument to `steroid_execute_code`.
