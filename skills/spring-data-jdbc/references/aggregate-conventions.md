# Detect Aggregate Conventions

Follow substeps 1.1 → 1.2 → 1.3 → 1.4 → 1.5 in order. Do not skip or reorder them.

Spring Data JDBC is built around the DDD aggregate concept:

- An **aggregate root** is the entrypoint for a cluster of related entities. It has a repository, a transactional lifecycle, and a database table.
- **Owned children** are inner entities reached via `@MappedCollection` (or a plain entity-typed field whose value type is itself `@Table`-annotated). They share the root's lifecycle, get their own table, and have no repository of their own.
- **Embedded value objects** (`@Embedded.Empty` / `@Embedded.Nullable` / `@Embedded(...)`) are *not* owned children — they have no identity, no separate table, and are flattened into the parent's row. They do not appear in the aggregate's owned-entity tree.
- Cross-aggregate links are always expressed as `AggregateReference<TargetRoot, IdType>` — never as direct object references.

The MCP tool `get_jdbc_entity_details` is the source of truth for the role of each entity:

- `aggregateRootFqn == null` → the entity **is** an aggregate root.
- `aggregateRootFqn != null` → the entity is an owned child of the named root.
- `aggregates` (only populated for roots) lists every owned child transitively, with `cardinality` and the field name on the owner.
- `referencedBy` lists other aggregates that link **to** this entity via `AggregateReference`.

---

## Step 1.1: Inventory aggregates

Call `list_all_domain_entities` and pick 3–5 representative Spring Data JDBC entities — those whose class is annotated with `@Table` from `org.springframework.data.relational.core.mapping` (the tool returns all domain entities including JPA; filter manually). Aim for a mix of roots and owned children. For each, call `get_jdbc_entity_details` and record:

- `aggregateRootFqn` (root vs. child).
- `aggregates` (the root's owned children — what is "inside" the aggregate).
- `referencedBy` (who points at this aggregate via `AggregateReference`).
- `relationships` containing entries with `relationType = AGGREGATE_REFERENCE` (this aggregate's outbound cross-links).

This map of roots, children, and cross-aggregate edges is the basis for every later decision.

---

## Step 1.2: Score each convention

- **Package layout** — are aggregate roots and their owned children co-located in one package, in nested packages (`com.example.order` for root, `com.example.order.item` for children), or scattered? Default: same package as the root.
- **Repository-per-root** — does the project consistently expose a repository **only** for aggregate roots (never for owned children)? Default: yes. If owned children have repositories, that is a violation to surface in Step 1.4.
- **Cross-aggregate link shape** — are cross-aggregate references always `AggregateReference<T, ID>`, or does the project also use raw foreign-key fields (e.g. `Long customerId`)? Default: always `AggregateReference`.
- **AggregateReference id type** — what id types are used for `AggregateReference<T, ID>` (must equal the target root's `@Id` type)? Recordable per-target rather than globally. Default: matches target's id type exactly.
- **Aggregate size** — are aggregates deliberately kept small (root + 1–2 child collections), or do they grow large with deep nesting? This shows up as `aggregates.size` in `get_jdbc_entity_details`. Default: small (≤ 2 levels of nesting; ≤ 3 owned collections per root). Large aggregates often signal a missing aggregate split.
- **Cycle handling** — are there bidirectional `AggregateReference` cycles (Order → Customer and Customer → Order)? Default: avoided; prefer querying via repository on one side.

---

## Step 1.3: Collect uncertain conventions

Score confidence only where the code contains relevant examples but the pattern is ambiguous (e.g. some aggregates use `AggregateReference`, others use raw `Long customerId`). If a convention is absent — confidence is high, use the default.

Collect all conventions where confidence < 80.

---

## Step 1.4: Ask developer

If there are any uncertain conventions, ask the developer. In Claude Code, combine all questions into a single `AskUserQuestion` call. In runtimes without that primitive (Codex / OpenCode / plain CLI), render the same questions inline — see the "Harness compatibility" section in `SKILL.md`. Example payload:

```json
{
  "questions": [
    {
      "header": "Cross-aggregate links",
      "question": "How should references to entities in other aggregates be modeled?",
      "multiSelect": false,
      "options": [
        { "label": "AggregateReference<T, ID> (Recommended)", "description": "Typed reference; documents intent; survives refactors" },
        { "label": "Raw foreign-key field", "description": "e.g. private Long customerId; — terser but loses type information" }
      ]
    },
    {
      "header": "Package layout",
      "question": "Where do owned child entities live relative to their aggregate root?",
      "multiSelect": false,
      "options": [
        { "label": "Same package as root (Recommended)", "description": "Order and OrderItem in the same package" },
        { "label": "Nested package", "description": "Order in com.example.order; OrderItem in com.example.order.item" }
      ]
    }
  ]
}
```

If Step 1.1 surfaced a repository for an owned child or a raw FK field used for cross-aggregate links, flag this as a deviation rather than silently asking — note it explicitly when answering the user.

---

## Step 1.5: Summarize resolved conventions

Output a consolidated list of every convention from Step 1.2 with its resolved value, plus the inventory of aggregates discovered in Step 1.1:

```
- Package layout: same package as root
- Repository-per-root: yes — only roots have repositories
- Cross-aggregate link shape: AggregateReference<T, ID>
- AggregateReference id type: matches target's @Id type
- Aggregate size: small (≤ 2 levels nesting, ≤ 3 owned collections)
- Cycle handling: avoided

Inventory:
- com.example.order.Order — root; aggregates: OrderItem (ONE_TO_MANY); referencedBy: Invoice
- com.example.order.OrderItem — child of Order
- com.example.customer.Customer — root; aggregates: ContactInfo (ONE_TO_ONE); referencedBy: Order
```

This list is your working contract for all aggregate-touching code written in this session.
