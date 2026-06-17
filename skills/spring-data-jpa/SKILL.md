---
name: spring-data-jpa
description: Rules and guidelines for working with Spring Data JPA in the project. ALWAYS use this skill when adding, removing, or modifying JPA entities, repositories, or projections. Trigger on any request that involves changing entity structure, adding new entities, modifying field annotations, updating database mappings, creating or modifying Spring Data repositories, or defining query projections (interfaces, DTOs).
---

# Preflight: Spring MCP

This skill is part of the **Spring Agent Toolkit** and is designed to work with the **Spring MCP server** (provided by the Amplicode IntelliJ plugin). Before doing anything else, check your tool list for any Spring MCP tool — they are exposed under the `amplicode` MCP server (e.g. `get_project_summary`, `list_module_dependencies`, `get_entity_details`); harnesses that flatten MCP tools into the tool list use the `mcp__amplicode__` prefix on the same names.

- **If at least one Amplicode tool is available** — MCP is connected. Proceed with the skill below.
- **If none are available** — stop and invoke the **`amplicode-install`** skill (bundled with the Spring Agent Toolkit). It installs the Amplicode plugin and walks the user through the **«Настроить Spring Agent»** welcome-screen button + MCP-client restart. After it completes, the MCP tools become available — resume this skill.
- If `amplicode-install` is not registered in your skill list, tell the user (in their language): *"This skill needs the Amplicode IntelliJ plugin and its MCP server. Install it from https://amplicode.ru/marketplace into IntelliJ IDEA Ultimate/Community or GigaIDE, open any project, click «Настроить Spring Agent» on the Amplicode welcome screen, then restart your MCP client."*

---

# Working with JPA Entities

When the task involves creating or modifying a JPA entity:

1. If entity conventions have not been detected yet in this conversation — check memory for previously saved conventions first. If found in memory, reuse them. Otherwise read `references/entity-conventions.md` and follow all substeps there to detect project conventions.
2. Read `references/entity-rules-impl.md` and follow the rules there when writing or modifying the entity

# Reviewing JPA Patterns

When the user asks to review JPA patterns, conventions, or code quality in the project:

1. Detect current conventions by following `references/entity-conventions.md` (steps 1.1–1.5).
2. Compare the detected conventions against the best practices defined in `references/entity-rules-impl.md`. For each deviation, output a recommendation in the format:

```
### JPA Review

**[Convention or pattern name]**
- Current: <what the project does>
- Recommended: <what the best practice says>
- Reason: <why this matters>
```

If no deviations are found — state that the project follows best practices.

---

# Working with Transactions

When the task involves adding or modifying transactional behavior:

1. If transaction conventions have not been detected yet in this conversation — check memory for previously saved conventions first. If found in memory, reuse them. Otherwise read `references/transaction-conventions.md` and follow all substeps there to detect project conventions.
2. Read `references/transaction-rules-impl.md` and follow the rules there when writing or modifying transactional code.
