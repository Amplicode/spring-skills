# Aggregate Implementation Rules

Apply these rules when reasoning about or modifying aggregate boundaries in Spring Data JDBC. All rules assume the conventions resolved in Step 1 of `aggregate-conventions.md` are already applied.

`AggregateReference` is imported from `org.springframework.data.jdbc.core.mapping.AggregateReference`.

---

## Identifying the role of an entity

Use `get_jdbc_entity_details` to read three cross-aggregate facts that are not obvious from a single file:

- `aggregateRootFqn` — null → this is itself a root; non-null → this is an owned child of the named root.
- `aggregates` — only populated for roots; lists owned children recursively, each with `cardinality` and `fieldName`.
- `referencedBy` — every other aggregate that links here via `AggregateReference`, with the `ownerEntityFqn` (the entity holding the field) and the root of the holder's aggregate.

`relationships` carries every outbound edge with a `relationType` of `ONE_TO_ONE`, `ONE_TO_MANY`, `AGGREGATE_REFERENCE`, or `EMBEDDED`. Only `AGGREGATE_REFERENCE` crosses an aggregate boundary; `ONE_TO_ONE`/`ONE_TO_MANY` are owned children inside the same aggregate; `EMBEDDED` is a value object flattened into the current row — **not** a member of the aggregate's owned-entity tree.

Note: the tool itself recommends reading the file directly when you are about to modify the entity. The tool is best used for the three cross-aggregate facts above — for in-file structure (field list, id type, current annotations) read the source.

---

## Without `get_jdbc_entity_details` (MCP fallback)

If the MCP tool is unreachable, derive the same four facts by reading source files. Announce up front that you are in fallback mode.

The project is **Kotlin-first** (Kotlin 2.2.20 primary; some Java). Every grep must scan both `*.kt` and `*.java` — do **not** restrict with `-t java`. Annotations also frequently sit on their own line above the field/constructor parameter they decorate, so a single-line regex over annotation-and-target will produce false negatives. Use a two-step pattern: (1) find files that contain the annotation; (2) open each file and read the surrounding declarations.

**`idField.type`** — find the field annotated with `@Id` (import `org.springframework.data.annotation.Id`) and read its declared type.

1. Open the target entity source file (both `.java` and `.kt` are valid).
2. Look for `@Id`. In Java/Kotlin classes it usually sits on a field; in records, on a record component; in Kotlin data classes, on a constructor parameter (often as `@field:Id` or `@property:Id`).
3. If `@Id` is not in the target file, follow the inheritance chain. In Java: `class MyTable extends BaseTable`. In Kotlin: `class MyTable : BaseTable()`. Open the parent source and repeat — the project's test fixtures use this pattern (`MyTable extends BaseTable` where `BaseTable` holds the `@Id`). Walk further up if the parent itself inherits.
4. The type declared next to `@Id` is the id type.

**`aggregateRootFqn`** — is the target a root or an owned child? Owned-child status comes **only** from `@MappedCollection` (or a plain entity-typed field that resolves to a `@Table` class) on a parent. `@Embedded` does NOT make the embedded type an aggregate member — it is a value object flattened into the parent's row.

Two-step search (works for Java and Kotlin):

1. List candidate parents — files containing `@MappedCollection` or a field whose declared type is `Target` (or a collection of `Target`):
   ```
   rg --files-with-matches "@MappedCollection|\\bTarget\\b"
   ```
2. Open each candidate file and inspect its field/property declarations. Read across line breaks — the annotation and the field/property are typically on separate lines:
   ```java
   @MappedCollection(idColumn = "order_id")
   private Set<OrderItem> items;   // Java
   ```
   ```kotlin
   @MappedCollection(idColumn = "order_id")
   var items: MutableSet<OrderItem> = mutableSetOf()   // Kotlin
   ```
   A field is an inbound link to `Target` if the declared type is `Target`, `Collection<Target>`, `Set<Target>`, `List<Target>`, or `Map<_, Target>` AND it carries `@MappedCollection` (or no annotation at all, with the type being itself `@Table`-annotated).

The first inbound match is the parent; recurse up the `@MappedCollection` chain until you find a `@Table` entity with no inbound `@MappedCollection` — that is the aggregate root. If no inbound exists, the target is itself the root.

Note: an `@Embedded.Empty` / `@Embedded.Nullable` / `@Embedded(...)` field pointing at the target does **not** make the target a child of the holder's aggregate. Embedded classes are value objects; they typically should not even be `@Table`-annotated — see the "Embedded objects do not have identity" rule below. If you find a `@Table` entity that is `@Embedded` from another entity, that is itself a code smell to flag, not a clue about aggregate membership.

**`aggregates`** (only meaningful for roots) — open the root's source file and list every field/property annotated with `@MappedCollection` (or any plain entity-typed field whose declared type is itself `@Table`-annotated). Each such value type is an owned child entity. Recurse into each child to find nested owned children. Cardinality: a collection / `Map` / `List` / `Set` ⇒ `ONE_TO_MANY`; a scalar reference ⇒ `ONE_TO_ONE`. Skip `@Embedded` fields here — they are value objects, not members of the aggregate's entity tree.

**`referencedBy`** — find inbound `AggregateReference<Target, ...>` fields. A single-line regex over the whole generic is unreliable — Kotlin (and occasionally Java) wraps long generics across lines, e.g.:

```kotlin
var ref: AggregateReference<
    Target,
    Long
>? = null
```

Use a two-step search that does not assume same-line layout:

1. Find candidate files — those that contain **both** `AggregateReference` and the target type's simple name. Intersect two file lists:
   ```
   comm -12 \
     <(rg --files-with-matches "\bAggregateReference\b" | sort) \
     <(rg --files-with-matches "\bTarget\b" | sort)
   ```
   (or run the second `rg` only over the first list — equivalent and avoids the temp ordering.)
2. Open each candidate file and confirm by eye that the target appears in the **first** generic slot of an `AggregateReference<…, …>` field declaration (not as the `IdType` in slot two: `AggregateReference<Other, Target>` would be `Target` used as an id type — different relationship and a sign of a different bug, not an inbound link).

Each confirmed match is an inbound cross-aggregate link. For each match's owner class, walk back up the `@MappedCollection` chain (per the `aggregateRootFqn` procedure) to find the aggregate root that ultimately holds the reference.

The manual procedure is slower and less reliable than one MCP call — treat it as a last resort, not the default.

---

## Rule: Repositories only for aggregate roots

A Spring Data JDBC repository may exist **only** for an aggregate root.

When asked to create a repository, run `get_jdbc_entity_details` for the target. If `aggregateRootFqn != null` — refuse with:

> "<target> is an owned child of aggregate <aggregateRootFqn>. Owned children must be reached through the root's repository. To work with this entity in isolation, either (a) load the root and navigate to the child, or (b) split this child off into a separate aggregate root — but that requires replacing the parent's `@MappedCollection` with `AggregateReference<<target>, <id>>` and adding a `@Table` annotation+repository for the child."

Do not generate the repository anyway. Do not silently extend an inappropriate base interface.

---

## Rule: Owned children are loaded and saved with the root

To create / update / delete an owned child, mutate the parent and call `rootRepository.save(root)`. The framework re-inserts the child collection on every update.

```java
// CORRECT — mutate child via root
Order order = orderRepository.findById(orderId).orElseThrow();
order.getItems().add(new OrderItem(/* ... */));
orderRepository.save(order);
```

```java
// WRONG — direct repository call on an owned child
orderItemRepository.save(item); // owned child must not have a repository
```

To query owned children in isolation (e.g. "all items with status = PICKED across orders"), write a custom `@Query` on the root's repository that joins to the child table and returns a projection — do not introduce an OrderItemRepository.

---

## Rule: Cross-aggregate references use `AggregateReference<Target, IdType>`

When this aggregate must point at another aggregate root, declare the field as `AggregateReference<Target, IdType>`. The `IdType` must equal the target's `@Id` type — fetch it via `get_jdbc_entity_details` on the target (`idField.type`). Never widen it to `Number`/`Serializable`.

```java
// CORRECT
@Column(value = "customer_id")
private AggregateReference<Customer, Long> customer;
```

To resolve the target, the service layer dereferences via the target's repository:

```java
public CustomerView resolve(Order order) {
    Long customerId = order.getCustomer().getId();
    Customer c = customerRepository.findById(customerId).orElseThrow();
    return toView(c);
}
```

`AggregateReference.to(id)` is the only public factory. **It rejects null** — the contract is `id` must not be null, and calling `to(null)` throws `IllegalArgumentException` at runtime.

Nullable cross-aggregate links are expressed by storing `null` directly in the field, never by wrapping a null id:

```java
// CORRECT — assigning the field for a non-null id
order.setCustomer(AggregateReference.to(customer.getId()));
```

```java
// CORRECT — nullable link: store null in the field directly
order.setCustomer(null);
```

```java
// CORRECT — guard before wrapping (this is the pattern the project's
// MapStruct mapper generator emits)
order.setCustomer(customerId == null ? null : AggregateReference.to(customerId));
```

```java
// WRONG — AggregateReference.to(null) throws at runtime
order.setCustomer(AggregateReference.to(null));
```

```java
// WRONG — raw FK on an entity that belongs in a different aggregate
@Column(value = "customer_id")
private Long customerId;
```

```java
// WRONG — @MappedCollection used to cross an aggregate boundary
@MappedCollection(idColumn = "customer_id")
private Set<Customer> customers; // Customer is itself a root
```

---

## Rule: Converting an owned child into a separate aggregate

When the project decides that an owned child must become its own aggregate (independent lifecycle, its own repository), the migration has four mandatory steps:

1. Add `@Table("...")` (or `@Table(name = "...", schema = "...")`) to the child if missing, and ensure its `@Id` is in place — declared directly on the child, or inherited from a base class. The table likely already exists; the change is only on the Java side.
2. Replace the parent's `@MappedCollection` field. The right shape depends on cardinality:
   - **Parent had a single child** — `AggregateReference<Child, IdType>` field on the parent. Straightforward.
   - **Parent had a collection of children** — there is no single canonical "list of AggregateReferences" shape; pause and pick one of these explicitly:
     - Invert the direction: drop the field on the parent and add `AggregateReference<Parent, IdType>` to the (now-independent) child aggregate. The "list" becomes a query on the child's repository.
     - Introduce an explicit link entity (its own aggregate root with `AggregateReference<Parent, …>` and `AggregateReference<Child, …>` fields) when the relationship itself carries data.
     - Only fall back to `Set<AggregateReference<Child, IdType>>` modelled as a `@MappedCollection`-backed link table on the parent's side if the relationship is intentionally a bag of opaque references with no other attributes — and confirm this with the developer first; it is rarely the right shape.
3. Create a `ChildRepository` extending the conventional base interface (Step 1.5 of repository conventions).
4. Update every read/write path in the codebase: places that previously navigated `parent.getChildren()` now go through `childRepository`.

Do not stop after step 1 or 2 — partial migrations leave the codebase in an inconsistent state.

---

## Rule: Converting a separate aggregate into an owned child

The reverse migration is symmetrical:

1. Replace the parent's `AggregateReference<Child, IdType>` field with `@MappedCollection` (the child stays an entity with its own `@Id`). Do not convert to `@Embedded` — that would mean dropping the child's identity entirely and merging its columns into the parent row, which is a different and much bigger refactor.
2. Delete the `ChildRepository` interface.
3. Remove direct references to `childRepository` everywhere; navigate from the parent instead.

If `get_jdbc_entity_details` shows `referencedBy` is non-empty for the child, **do not** demote it to an owned child without first removing every external `AggregateReference` to it — those references would dangle.

---

## Rule: Embedded objects do not have identity

`@Embedded` value objects share the parent row, have no `@Id`, and cannot be referenced from elsewhere. Use `@Embedded` only for value semantics (`Address`, `Money`, `DateRange`). If a thing has its own lifecycle or is referenced from another aggregate, it is not embeddable.

```java
// CORRECT — Address is a value object
@Embedded.Empty(prefix = "ship_")
private Address shippingAddress;
```

```java
// WRONG — using @Embedded for an entity that other aggregates reference
@Embedded.Empty
private Customer customer; // Customer has identity and a repository
```

---

## Answering "what is in aggregate X?" / "who references X?"

These questions are answered directly from `get_jdbc_entity_details`:

- "What is inside aggregate Foo?" — call the tool for `Foo` and report `aggregates` (each entry has `entityFqn`, `fieldName`, `ownerEntityFqn`, `cardinality`).
- "Who references Foo?" — call the tool for `Foo` and report `referencedBy` (each entry has `ownerEntityFqn`, `fieldName`, `aggregateRootFqn`).
- "Is Foo a root?" — `aggregateRootFqn == null` ⇒ yes.

Do not crawl the codebase by hand for these answers when the tool returns the same information in one call.

---

## Rule: Aggregate size

Per the **Aggregate size** convention from Step 1.5 (default: ≤ 2 levels of nesting, ≤ 3 owned collections), flag oversized aggregates during reviews. A deeply nested aggregate is a sign that an inner collection should be split off into its own root and reached via `AggregateReference`.

This applies to **review** flows — do not unilaterally split aggregates the user has not asked you to change.
