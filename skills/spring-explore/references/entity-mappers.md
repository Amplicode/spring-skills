# Entity Mappers

Finds MapStruct mappers associated with a given JPA entity.

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

## Step 1 — Find mappers for the entity

- Call `list_entity_mappers` via MCP to get mappers linked to the entity.

```
list_entity_mappers projectPath=<PATH> entityFqn=com.example.Order
```

Collect from the result:
- Mapper class FQN
- Mapped DTO classes (if returned)

---

## Output format

```
**Order**
- OrderMapper — com.example.OrderMapper
  maps: Order ↔ OrderDto, Order ↔ OrderCreateDto
```

If no mappers are found, note it explicitly — the entity may use manual mapping or return raw entities.