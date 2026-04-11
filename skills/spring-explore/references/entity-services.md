# Entity Services

Finds Spring services associated with a given JPA entity by tracing the dependency chain:
entity → repositories → beans that inject those repositories → filter services.

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

## Step 1 — Find repositories for the entity

- Call `list_entity_repositories` via MCP to get repositories linked to the entity.

```
list_entity_repositories projectPath=<PATH> entityFqn=com.example.Order
```

Collect the FQN of each repository found.

---

## Step 2 — Find beans that inject those repositories

For each repository FQN from step 1:
- Call `get_bean_injection_info` via MCP to find all Spring beans that inject it.

```
get_bean_injection_info projectPath=<PATH> beanClassQualifiedName=com.example.OrderRepository
```

Collect all injecting beans from the result.

---

## Step 3 — Filter services

From the beans collected in step 2, keep only those that are services:
- Class is annotated with `@Service`
- Or class name ends with `Service`, `ServiceImpl`, or `Facade`

Exclude controllers (`@RestController`, `@Controller`) and other non-service beans.

---

## Output format

```
Order → OrderRepository → OrderService
Order → OrderRepository → OrderFacade
```

If no services are found, the entity is likely accessed directly from controllers — note this explicitly.