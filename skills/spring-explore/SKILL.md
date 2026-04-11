---
name: spring-explore
description: >
  Explores a Spring Boot application and builds primary context: tech stack,
  module structure, domain entities, REST endpoints.
  Use this skill AUTOMATICALLY at the start of any task if project context has not yet
  been collected in the current conversation — do not wait for an explicit request.
  Also triggers on explicit requests: "explore project", "describe project", "project overview",
  "what is this project", "project structure", "tech stack", "give me context about the project",
  or whenever you need to understand the project before starting any task.
---

# Explore Application

Collects primary project context in steps 0–6. Execute steps sequentially —
each one builds on the results of the previous.

**Important:** if context has already been collected in the current conversation, do not repeat
the exploration — use what is already known.

---

## Step 0 — Predict involvement from the user's request

Tell the user: `Step 0/6: Analyzing request...`

**Do NOT call any tools in this step.**

Read the user's request and reason about what the implementation likely involves.
Without calling any tools, make educated guesses based on naming conventions, domain language,
and typical Spring Boot patterns:

- **Entities** — what domain objects are likely involved? (e.g. "create order" → `Order`, `OrderItem`)
- **Repositories** — which repositories probably exist for those entities?
- **Services** — what service classes are likely needed?
- **Controllers** — what REST controllers probably handle this area?
- **Other beans/components** — mappers, validators, event listeners, configs, etc.
- **Files** — what non-Java files are likely relevant? (e.g. DB migration scripts, `application.properties`, Liquibase changelogs, HTML templates). Do not list Java classes here — they belong to the categories above.

Build a preliminary gap list containing only what is genuinely required:

```
### Predicted involvement:
- Entities: Order, OrderItem, Customer
- Repositories: OrderRepository, CustomerRepository
- Services: OrderService
- Controllers: OrderController
- Other: OrderMapper
- Files: src/main/resources/db/migration/V1__create_orders.sql, application.properties
```

This prediction drives steps 1–5 — skip anything irrelevant to the task.

---

## Step 1 — Define exploration goal and select paths

Tell the user: `Step 1/6: Selecting exploration paths...`

**Do NOT call any tools in this step.**

Read the current conversation context — the user's request, any prior exploration results, remaining gaps — and formulate the key exploration goal in one sentence. Show it to the user:

```
### Exploration goal:
Understand the Order aggregate structure and verify what repositories and mappers already exist.
```

Using this goal, go through each exploration path below and explicitly decide: **include** or **skip**, with a one-line reason. Do this for every path — do not skip the evaluation itself.

**Project structure**
- **Fetch project summary** — include if the tech stack, Spring Boot version, or module structure are needed. Skip if the task is narrowly scoped to specific classes.
- **List domain entities** — include ONLY if the domain area is completely unknown and you cannot predict which entities are involved. If entities are already named in the request or predictable from context — skip. Do NOT use to get entity structure or resolve FQNs.
- **List REST endpoints** — include only if you need to check what already exists to avoid duplication or understand conventions. Skip if the request fully defines all endpoints from scratch.

**Domain model**
- **Get entity description** — include if entity fields or annotations are needed. Apply only to predicted entities, not all entities. See [`references/entity-description.md`](references/entity-description.md).
- **Get deep model from entity** — include if relationships across multiple entities need to be traversed (e.g. nested resources, cascades). See [`references/deep-model-based-on-jpa.md`](references/deep-model-based-on-jpa.md).
- **Get DDD model from entity** — include if aggregate boundaries matter (e.g. URL design, cascade planning, DTO shaping). See [`references/ddd-model-based-on-jpa.md`](references/ddd-model-based-on-jpa.md).

**Persistence**
- **Get entity repositories** — include if repositories for predicted entities are unknown or need to be verified. See [`references/entity-repositories.md`](references/entity-repositories.md).
- **Get entity components** — include if you need a full picture of all components (repositories, services, controllers) for an entity. See [`references/entity-components.md`](references/entity-components.md).

**Services**
- **Get entity services** — include if services for predicted entities are unknown or need to be verified. See [`references/entity-services.md`](references/entity-services.md).

**Mappers**
- **Get entity mappers** — include if the task involves DTOs and you need to know what mappers exist. See [`references/entity-mappers.md`](references/entity-mappers.md).

**DTOs**
- **Get entity DTOs** — include if you need to know what DTO classes already exist. See [`references/entity-dtos.md`](references/entity-dtos.md).

**REST layer**
- **Get entity controllers** — include if you need to find which controllers are associated with an entity. See [`references/entity-controllers.md`](references/entity-controllers.md).

Write out the evaluation explicitly, then produce the final plan from included paths only:

**Example** — for a request "Add a paginated endpoint returning all orders for a customer with order items and product names":
```
### Path evaluation:
- Fetch project summary: INCLUDE — need Spring Boot version and module structure
- List domain entities: SKIP — Order, Customer, OrderItem, Product are predictable from the request
- List REST endpoints: INCLUDE — need to check if an orders endpoint already exists
- Get entity description: INCLUDE — need Order, OrderItem, Product fields for response DTO design
- Get deep model: SKIP — relationship structure is clear: Order → OrderItem → Product
- Get DDD model: SKIP — no cascade planning needed, just a read endpoint
- Get entity repositories: INCLUDE — need to verify OrderRepository exists and supports pagination
- Get entity components: SKIP — repositories are sufficient, no need for full component chain
- Get entity services: SKIP — no service layer changes expected
- Get entity mappers: INCLUDE — need to know if OrderMapper already exists before creating DTOs
- Get entity DTOs: INCLUDE — need to know if OrderDto already exists
- Get entity controllers: SKIP — will check via "List REST endpoints" instead

### Exploration plan:
1. Fetch project summary
2. List REST endpoints (filter to order-related controllers)
3. Get entity description for Order, OrderItem, Product
4. Get entity repositories for Order
5. Get entity mappers for Order
6. Get entity DTOs for Order
```

---

## Step 2 — Load references

Tell the user: `Step 2/6: Loading relevant references...`


**Do NOT call any tools in this step.**

Based on the exploration plan from step 1, load only the references needed for the selected paths **that have not already been loaded in this conversation**.
After loading, proceed directly to step 3 — **do NOT search files, glob, or explore the project structure manually. All project information must be obtained exclusively via MCP tools in steps 3–5.**

| Selected path | Reference to load |
|---|---|
| Get entity description | [`references/entity-description.md`](references/entity-description.md) |
| Get deep model from entity | [`references/deep-model-based-on-jpa.md`](references/deep-model-based-on-jpa.md) |
| Get DDD model from entity | [`references/ddd-model-based-on-jpa.md`](references/ddd-model-based-on-jpa.md) |
| Get entity repositories | [`references/entity-repositories.md`](references/entity-repositories.md) |
| Get entity components | [`references/entity-components.md`](references/entity-components.md) |
| Get entity services | [`references/entity-services.md`](references/entity-services.md) |
| Get entity mappers | [`references/entity-mappers.md`](references/entity-mappers.md) |
| Get entity DTOs | [`references/entity-dtos.md`](references/entity-dtos.md) |
| Get entity controllers | [`references/entity-controllers.md`](references/entity-controllers.md) |

If none of the paths require references — skip this step and proceed to step 3.

---

## Step 3 — Build unified exploration plan

Tell the user: `Step 3/6: Building exploration plan...`

**Do NOT call any tools in this step.**

Using the selected paths from step 1 and the processes described in the loaded references,
build a single unified numbered plan of MCP calls to execute in steps 4–5.
Each item must be a concrete MCP tool call, not a category name.

```
### Unified exploration plan:
1. get_project_summary
2. list_project_endpoints
3. list_all_domain_entities (regexPattern=Owner) — resolve FQN
4. get_entity_details (Owner FQN)
5. get_entity_details (Pet FQN)
6. get_entity_details (Visit FQN)
7. list_entity_repositories (Owner FQN)
8. list_entity_repositories (Pet FQN)
9. list_entity_repositories (Visit FQN)
```

This plan is what will be executed in the next step — do not deviate from it without reason.

---

## Step 4 — Execute exploration plan via subagent

Tell the user: `Step 4/6: Executing exploration plan...`

Spawn a subagent and pass it the following instructions:

```
Execute the exploration plan below by calling each MCP tool in order.
Use MCP tools directly (e.g. get_entity_details, list_entity_repositories).

Collect and return ALL results in full — do not summarize or truncate.

Plan:
<paste the numbered plan from step 3 here>
```

Wait for the subagent to complete and collect all results before proceeding.

---

## Step 5 — Build exploration report

Tell the user: `Step 5/6: Building exploration report...`

**Do NOT call any tools in this step — reason only from subagent results.**

Synthesize all findings collected across all exploration cycles into a single report. Include only what is genuinely valuable for the task — omit noise and obvious defaults.

Structure:

```
### Exploration Report

**Stack:** Java 21 · Spring Boot 3.x · JPA · Maven

**Domain model:**
- Order (id, status, totalAmount) → has many OrderItem → references Product
- Customer (id, name, email)

**Repositories:**
- OrderRepository — extends JpaRepository, supports pagination
- CustomerRepository — extends JpaRepository

**Services:**
- OrderService — handles order creation and status transitions

**Mappers:**
- OrderMapper (MapStruct) — maps Order ↔ OrderDto

**DTOs:**
- OrderDto, OrderItemDto — already exist

**REST API (relevant endpoints):**
- GET /orders — paginated list
- POST /orders — create order

**Notable findings:**
- SecurityConfig present — all endpoints require authentication
- No mapper for Customer — will need to create one
```

---

## Step 5.5 — Formulate implicit assumptions

Tell the user: `Step 5.5/6: Formulating implicit assumptions...`

**Do NOT call any tools in this step — reason only from subagent results and the user's request.**

Based on the exploration report and the user's request, identify everything the user did **not** explicitly say but likely expects from the implementation. These are implicit assumptions — unstated requirements, conventions, and design decisions the user probably takes for granted.

Focus on:
- **Behavioral expectations** — e.g. "user probably expects soft delete, not hard delete", "pagination assumed to be offset-based"
- **Security/access control** — e.g. "endpoint likely should require authentication like all others in this project"
- **Validation** — e.g. "fields like email and price are likely expected to be validated"
- **Error handling** — e.g. "returning 404 on missing entity is likely expected, not 500"
- **Conventions** — e.g. "response format likely expected to match existing endpoints (camelCase, wrapped in data field)"
- **Related side effects** — e.g. "creating an order probably expected to update inventory or send a notification"
- **DTO shape** — e.g. "response probably expected to include nested items, not just IDs"

Output all assumptions explicitly so they can be validated or corrected:

```
### Implicit assumptions:
1. The new endpoint should require authentication — all existing endpoints use SecurityConfig with auth required.
2. Response format should match existing endpoints — camelCase JSON, no wrapper object.
3. Pagination is expected to be offset-based (Pageable) — consistent with other list endpoints.
4. Missing entity should return 404, not 500 — standard REST convention followed elsewhere.
5. Price field is expected to be validated as positive — consistent with other monetary fields in the domain.
6. OrderItem list in response should include product name and quantity — implied by "order details" framing.
```

If no implicit assumptions can be identified — state that explicitly:
```
### Implicit assumptions: none identified — the request is fully specified.
```

---

## Step 6 — Decide on next cycle

Tell the user: `Step 6/6: Evaluating next cycle...`

**Do NOT call any tools in this step — reason only from subagent results.**

Predict the value of an additional cycle (0–100): how critical are the remaining gaps, and are they resolvable via MCP? Show score and reasoning:

```
### Additional cycle value: 87/100 → additional exploration cycle required.
```

**Score > 80** — go to **Step 1**. **Score ≤ 80** — stop.