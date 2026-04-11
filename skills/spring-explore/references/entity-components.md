# Entity Components

Finds all Spring components associated with a given JPA entity:
repositories, services, and controllers.

Use this reference when you need a complete picture of the component chain for an entity
before implementing or modifying functionality.

---

## Step 0 — Resolve entity FQN (if unknown)

If you only know the simple class name, resolve the FQN first:
- Call `list_all_domain_entities` via MCP with `regexPattern=<SimpleName>`.

```
list_all_domain_entities projectPath=<PATH> regexPattern=Order
```

Use the `qualifiedName` from the result in all subsequent steps.
Skip this step if the FQN is already known.

---

## Step 1 — Find repositories

Follow [`entity-repositories.md`](entity-repositories.md) to get all repositories for the entity.

---

## Step 2 — Find all injecting beans

For each repository FQN from step 1:
- Call `get_bean_injection_info` via MCP to find all beans that inject it.

```
get_bean_injection_info projectPath=<PATH> beanClassQualifiedName=com.example.OrderRepository
```

For each injecting bean that is a service, repeat `get_bean_injection_info` to find beans
that inject that service — go one level deeper until no new beans appear.

Collect all discovered beans across all levels.

---

## Step 3 — Classify components

Classify each discovered bean:

| Annotation / name pattern | Component type |
|---|---|
| `@Repository` or name ends with `Repository` | Repository |
| `@Service` or name ends with `Service`, `ServiceImpl`, `Facade` | Service |
| `@RestController`, `@Controller` or name ends with `Controller` | Controller |
| Other | Other bean |

---

## Output format

```
**Order**
- Repository: OrderRepository (com.example.OrderRepository)
- Service:    OrderService (com.example.OrderService)
- Controller: OrderController (com.example.OrderController)
```

If a component type is absent, note it explicitly — e.g. "No service layer — repository injected directly into controller."