---
name: spring-data-jpa
description: Rules and guidelines for working with Spring Data JPA in the project. ALWAYS use this skill when adding, removing, or modifying JPA entities, repositories, or projections. Trigger on any request that involves changing entity structure, adding new entities, modifying field annotations, updating database mappings, creating or modifying Spring Data repositories, or defining query projections (interfaces, DTOs).
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
