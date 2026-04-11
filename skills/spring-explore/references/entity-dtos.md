# Entity DTOs

Finds DTOs associated with a given JPA entity.

---

## Step 0 — Resolve entity FQN (if unknown)

If you only know the simple class name, resolve the FQN first:
- Call `list_all_domain_entities` via MCP with `regexPattern=<SimpleName>`.

```
list_all_domain_entities projectPath=<PATH> regexPattern=Order
```

Use the `qualifiedName` from the result in the next step.
Skip this step if the FQN is already known.

---

## Step 1 — Find DTOs for the entity

- Call `list_entity_dtos` via MCP to get DTOs linked to the entity.

```
list_entity_dtos projectPath=<PATH> entityFqn=com.example.Order
```

Collect from the result:
- DTO class FQN
- Fields (if returned)

---

## Output format

```
**Order**
- OrderDto — com.example.dto.OrderDto
- OrderCreateDto — com.example.dto.OrderCreateDto
```

If no DTOs are found, note it explicitly — the entity may be returned directly without a DTO layer.