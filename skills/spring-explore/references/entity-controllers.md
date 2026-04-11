# Entity Controllers

Finds REST controllers associated with a given JPA entity by tracing the dependency chain:
entity → repositories → beans that inject those repositories → filter controllers.

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

Collect all injecting beans from the result. These may be services, facades, or controllers.

---

## Step 3 — Filter controllers

From the beans collected in step 2, keep only those that are REST controllers:
- Class is annotated with `@RestController` or `@Controller`
- Or class name ends with `Controller`

If injecting beans are services (not controllers), repeat step 2 for each service FQN
to find beans that inject those services — go one level deeper until controllers are found
or no more beans remain.

---

## Output format

```
Order → OrderRepository → OrderService → OrderController
Order → OrderRepository → OrderController (direct injection)
```

If no controllers are found, note it explicitly — the entity may not be exposed via REST.