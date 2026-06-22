---
name: spring-data-jdbc
description: Rules and guidelines for working with Spring Data JDBC in the project. ALWAYS use this skill when adding, removing, or modifying Spring Data JDBC entities (@Table from org.springframework.data.relational.core.mapping), aggregates, AggregateReference links, embedded objects, @MappedCollection associations, or Spring Data JDBC repositories (CrudRepository / ListCrudRepository). Trigger on any request that touches @Table, @Column, @MappedCollection, @Embedded annotations from spring-data-relational, AggregateReference fields, @PersistenceCreator constructors, or @Query methods on JDBC repositories. Do NOT use for JPA (jakarta.persistence) entities — use the spring-data-jpa skill instead.
---

# Detection guard

Before applying any rule from this skill, confirm the target file imports from `org.springframework.data.relational.core.mapping` / `org.springframework.data.annotation`. If you see `jakarta.persistence.*` imports, stop and switch to the `spring-data-jpa` skill — the two stacks are not interchangeable and patterns from JPA (`HibernateProxy`, `@ManyToOne`, `@OneToMany`, `@JoinColumn`, `FetchType`) do not apply here.

# Harness compatibility

This skill is designed to work across multiple agent runtimes (Claude Code, Codex, OpenCode). Two harness-specific primitives are referenced by name in this skill; treat them as preferred-but-optional and degrade gracefully:

- **`AskUserQuestion`** (Claude Code structured prompt with multiple-choice options). When the runtime supports it, use it for Step 1.4 of every conventions file — the JSON examples in those files map to the tool's expected payload. When the runtime does **not** support it (Codex, OpenCode, plain CLI), ask exactly the same questions inline in the conversation: render each question as a short paragraph followed by a numbered or bulleted list of options, mark the recommended option with `(Recommended)`, and accept either the option label or its number in the user's reply. The decision tree is identical; only the rendering changes.
- **"Memory"** — Claude Code's persistent file-based auto-memory. References like "check memory for previously saved conventions" mean: if you have access to Claude Code's auto-memory, look there first. In Codex/OpenCode (and any runtime without persistent memory), substitute "scan earlier turns of this conversation" — if conventions were resolved in the same session, reuse them; if the session is fresh, just run Step 1 from scratch.

Do not refuse a task because one of these primitives is missing. Substitute the inline equivalent and announce the substitution once at the start of the task ("AskUserQuestion not available in this runtime — asking inline" / "no persistent memory in this runtime — detecting conventions from scratch").

---

# Preflight: Spring MCP

This skill is part of the **Spring Agent Toolkit** and is designed to work with the **Spring MCP server** (provided by the Amplicode IntelliJ plugin). Before doing anything else, check your tool list for any Spring MCP tool — they are exposed under the `amplicode` MCP server (e.g. `get_jdbc_entity_details`, `list_all_domain_entities`, `list_entity_repositories`); harnesses that flatten MCP tools into the tool list use the `mcp__amplicode__` prefix on the same names.

- **If at least one Amplicode tool is available** — MCP is connected. Proceed with the skill below.
- **If none are available** — stop and invoke the **`amplicode-install`** skill (bundled with the Spring Agent Toolkit). It installs the Amplicode plugin and walks the user through the **«Настроить Spring Agent»** welcome-screen button + MCP-client restart. After it completes, the MCP tools become available — resume this skill.
- If `amplicode-install` is not registered in your skill list **or** the user declines to install (e.g. they are running in a harness without MCP support such as a CI sandbox), continue with the file-read fallbacks described in the next section so the task is not blocked.

---

# MCP availability and fallbacks

This skill prefers the Spring MCP tools (`get_jdbc_entity_details`, `list_all_domain_entities`, `list_entity_repositories`) because they return resolved, project-wide answers in one call. If the Spring MCP server is unreachable (connection error, tool not registered, harness without MCP support) and the user has chosen not to install the plugin via the **Preflight** above, do not refuse the task — fall back to direct file reads / grep:

This project is **Kotlin-first** (Kotlin 2.2.20 primary, Java for some modules) — every fallback grep must hit both `*.kt` and `*.java`. Do not pass `-t java` to `rg`; either omit the type filter or use `-t kotlin -t java`.

- Instead of `list_all_domain_entities` — grep the project for `org.springframework.data.relational.core.mapping.Table` imports (or `@Table` annotations whose import resolves there) to enumerate JDBC entities. Works the same in `.java` and `.kt`.
- Instead of `list_entity_repositories` — grep for the repository-declaration keyword (`extends` in Java, `:` in Kotlin; Kotlin may omit the space before the colon, so use `\s*`). Spell out every base interface explicitly rather than relying on optional prefixes — `(List)?(...|...)` parses ambiguously to a human reader:
  ```
  (extends|:)\s*(ListCrudRepository|CrudRepository|ListPagingAndSortingRepository|PagingAndSortingRepository)\b
  ```
- Instead of `get_jdbc_entity_details` — see the "Without `get_jdbc_entity_details`" subsection in `references/aggregate-rules-impl.md`. The manual procedure there yields the same `idField.type` / `aggregateRootFqn` / `aggregates` / `referencedBy` information by reading source files (both Java and Kotlin).

State once at the start of the task that you are operating in fallback mode and why (e.g. "Spring MCP not reachable — using file-read fallback"). Do not silently switch.

# Working with JDBC Entities

When the task involves creating or modifying a Spring Data JDBC entity:

1. If entity conventions have not been detected yet in this conversation — check memory for previously saved conventions first (or earlier conversation turns in runtimes without persistent memory — see "Harness compatibility"). If found, reuse them. Otherwise read `references/entity-conventions.md` and follow all substeps there to detect project conventions.
2. Read `references/entity-rules-impl.md` and follow the rules there when writing or modifying the entity.
3. If the entities involve **any relationship between each other or to existing entities** — a collection field, a reference field, an FK column, or a relationship described in the user's request ("X belongs to Y", "Y has many X") — also read `references/aggregate-rules-impl.md` **before deciding the shape of any link**. The entity rules cover field syntax; the aggregate rules decide which relationship shapes are legal and in which direction the link may be held. Skipping this step is how illegal shapes (raw FK to a member of another aggregate, references to non-roots) get generated.

**Tool-vs-source policy** — when you are going to edit the entity, read the source file directly. `get_jdbc_entity_details` is documented as a read-only analysis tool and explicitly says "you plan to modify the entity class afterward — read the file directly instead." Use the MCP tool only for cross-aggregate context that is not visible from one file:

- The target's role in its aggregate (`aggregateRootFqn`).
- Other aggregates pointing here (`referencedBy`).
- The id type of a **different** aggregate you need to link to via `AggregateReference<Other, IdType>` (when reading `Other`'s source is overkill).

For everything in the file you are editing — id type, current fields, current `@MappedCollection` / `@Embedded` declarations — read the file.

# Reviewing JDBC Patterns

When the user asks to review JDBC patterns, conventions, or code quality in the project:

1. Detect current conventions by following `references/entity-conventions.md` (steps 1.1–1.5).
2. Compare the detected conventions against the best practices defined in `references/entity-rules-impl.md`. For each deviation, output a recommendation in the format:

```
### JDBC Review

**[Convention or pattern name]**
- Current: <what the project does>
- Recommended: <what the best practice says>
- Reason: <why this matters>
```

If no deviations are found — state that the project follows best practices.

---

# Working with JDBC Repositories

When the task involves adding or modifying a Spring Data JDBC repository:

1. If repository conventions have not been detected yet in this conversation — check memory (or earlier conversation turns — see "Harness compatibility") first. Otherwise read `references/repository-conventions.md` and follow all substeps there.
2. Read `references/repository-rules-impl.md` and follow the rules there.

Before creating a new repository, call `list_entity_repositories` with `entityFqn = <target>` to confirm no repository already exists for this entity, and call `get_jdbc_entity_details` for the target — a repository may only be created for an aggregate root (`aggregateRootFqn == null`). Note that `list_entity_repositories` has no JDBC filter; filter results manually if you called it without `entityFqn`.

---

# Working with Aggregates

When the task involves aggregate boundaries — adding `AggregateReference` fields, converting an owned `@MappedCollection` into a cross-aggregate link, splitting an aggregate, creating any relationship (including one-to-many, many-to-many) between entities, or answering "who references X?" / "what's inside aggregate Y?":

1. **MANDATORY:** Read `references/aggregate-rules-impl.md` **first** and scan it for rules that apply to your specific relationship type. Do not proceed with entity modifications until you have confirmed which rule applies.
2. If aggregate conventions have not been detected yet in this conversation — check memory (or earlier conversation turns — see "Harness compatibility") first. Otherwise read `references/aggregate-conventions.md` and follow all substeps there.
3. Only after reading both reference files — proceed with entity modifications. Follow the rules there exactly.

The MCP tool `get_jdbc_entity_details` is the source of truth for aggregate membership. Its response carries `aggregateRootFqn` (null if this entity is itself a root), `aggregates` (owned children, recursive — only populated for roots), and `referencedBy` (other aggregates linking here via `AggregateReference`). Read these before making any aggregate-boundary decision.

---

## Critical: One-to-many and many-to-many relationships

**Any one-to-many or many-to-many relationship between different aggregates requires a link entity.** This is non-negotiable. Before implementing such a relationship:

1. Verify from `get_jdbc_entity_details` (or by reading the entities) that both participants are aggregate roots or check their aggregate membership.
2. If they are in **different aggregates**, you **must** create an intermediate link entity — do not use direct `@MappedCollection` pointing at another root.
3. Read the specific rule in `references/aggregate-rules-impl.md` — section "Rule: One-to-many between two aggregates goes through a link entity" or "Rule: Many-to-many between two aggregates goes through a link entity".
4. Follow the link-entity pattern exactly as documented: holding side owns link collection via `@MappedCollection`; each link entity carries `AggregateReference<Target, IdType>` to the target root.