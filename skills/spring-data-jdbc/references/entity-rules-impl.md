# Entity Implementation Rules

Apply these rules when writing or modifying any Spring Data JDBC entity. All rules assume the conventions resolved in Step 1 of `entity-conventions.md` are already applied.

All annotation imports in this document come from Spring Data, **not** from `jakarta.persistence`:

- `org.springframework.data.relational.core.mapping.Table`
- `org.springframework.data.relational.core.mapping.Column`
- `org.springframework.data.relational.core.mapping.MappedCollection`
- `org.springframework.data.relational.core.mapping.Embedded`
- `org.springframework.data.relational.core.mapping.Embedded.Nullable` / `Embedded.Empty`
- `org.springframework.data.annotation.Id`
- `org.springframework.data.annotation.Version`
- `org.springframework.data.annotation.Transient`
- `org.springframework.data.annotation.PersistenceCreator`
- `org.springframework.data.jdbc.core.mapping.AggregateReference`

If you find yourself reaching for `@ManyToOne`, `@OneToMany`, `@JoinColumn`, `FetchType`, `@SequenceGenerator`, `@GeneratedValue`, or `HibernateProxy` — stop. Those belong to the JPA skill; Spring Data JDBC does not have them.

---

## Class — creating a new entity class

Branch on the **class shape** convention from Step 1.5.

### Mutable class (default constructor + setters)

- Annotate with `@Table` per the project's **@Table form** convention. Forms (all valid; `name` and `value` are `@AliasFor` siblings):
  - `@Table("orders")` — shorthand when only the table name is set. This is what the IDE's JDBC template emits.
  - `@Table(name = "orders", schema = "sales")` — named attributes when `schema` is also set.
  - `@Table` (no value) — only when naming strategy = implicit.
- Provide a public no-args constructor (explicit or default).
- Generate setters and getters for all fields (or use Lombok per resolved Lombok conventions).
- If Lombok = yes — add `@Getter`/`@Setter` etc. on the class according to the resolved Lombok features. Lombok `@Builder` requires both `@NoArgsConstructor` and `@AllArgsConstructor`.

```java
// CORRECT — mutable class, explicit naming, no schema
@Table("orders")
public class Order {
    @Id
    private Long id;
    @Column("placed_at")
    private Instant placedAt;
    // getters + setters
}
```

```java
// CORRECT — when project uses a non-default schema
@Table(name = "orders", schema = "sales")
public class Order { ... }
```

### Immutable class (`@PersistenceCreator`)

- Final fields, all-args constructor annotated with `@PersistenceCreator`.
- No setters; expose state via getters.
- Suitable when entities are conceptually values with identity.

```java
// CORRECT — immutable class
@Table("orders")
public final class Order {
    @Id private final Long id;
    @Column(value = "placed_at") private final Instant placedAt;

    @PersistenceCreator
    public Order(Long id, Instant placedAt) {
        this.id = id;
        this.placedAt = placedAt;
    }
    // getters only
}
```

### Java record

- Annotate the record type with `@Table(...)`.
- Place `@Id`, `@Column`, `@MappedCollection`, etc. on the record components.
- Records get `equals`/`hashCode`/`toString` for free — do not override them unless there is a strong reason.

```java
// CORRECT — record entity
@Table("orders")
public record Order(
    @Id Long id,
    @Column("placed_at") Instant placedAt
) {}
```

### WRONG — JPA leakage

```java
// WRONG — jakarta.persistence on a JDBC entity
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "orders")
public class Order { ... }
```

---

## Id

Every entity must have an `@Id` field that Spring Data can resolve. The id may be:

- **Declared on the entity itself** — the common case.
- **Inherited from a base class** — Spring Data JDBC reads `@Id` through the inheritance chain. The project's test fixtures use this pattern (e.g. `MyTable extends BaseTable` where `BaseTable` holds `@Id private String id;`). If the entity has a `extends ...` clause, open the parent source file (and walk further up if needed) before adding a new `@Id` — declaring a second `@Id` in a subclass is a bug.

The import for `@Id` is **always** `org.springframework.data.annotation.Id`.

Branch on the **Id type preset** and **Id generation** conventions from Step 1.5.

### Database IDENTITY — `Long` or `Integer` (recommended default)

The column is `IDENTITY` / `SERIAL` at the DB level. Spring Data JDBC returns the generated key after `save()`. No `@GeneratedValue`, no `@SequenceGenerator` — those do not exist in Spring Data JDBC.

```java
// CORRECT — database IDENTITY
@Id
@Column(value = "id")
private Long id;
```

```java
// WRONG — JPA annotation on a JDBC entity
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "id")
private Long id;
```

### Application-generated UUID

Generate the UUID in the `@PersistenceCreator` constructor (immutable / record) or in a `BeforeConvertCallback<T>` bean (mutable class) — Spring Data JDBC does not auto-generate UUIDs.

```java
// CORRECT — UUID assigned by application
@Table("orders")
public final class Order {
    @Id @Column("id") private final UUID id;
    // other fields

    @PersistenceCreator
    public Order(UUID id, /* ... */) {
        this.id = id != null ? id : UUID.randomUUID();
        // ...
    }
}
```

Alternative — a callback bean (use this when class shape = mutable):

```java
@Component
public class OrderIdAssigner implements BeforeConvertCallback<Order> {
    @Override
    public Order onBeforeConvert(Order entity) {
        if (entity.getId() == null) entity.setId(UUID.randomUUID());
        return entity;
    }
}
```

### Caller-supplied `String` natural key

```java
// CORRECT — caller supplies the id
@Id
@Column(value = "id")
private String id;
```

The service layer is responsible for assigning a unique value before `save()`. Spring Data JDBC distinguishes new vs. existing aggregates by id-nullness — for a non-nullable `String` id you must implement `Persistable<String>` so the framework knows when to INSERT vs UPDATE, or use a `BeforeConvertCallback` to track new aggregates.

---

## Version (optimistic locking)

Apply only when **`@Version` usage** = yes in Step 1.5.

```java
// CORRECT
@Version
private Long version;
```

Spring Data JDBC increments `version` on every update; a stale value causes an `OptimisticLockingFailureException`. Note the import: `org.springframework.data.annotation.Version`, not `jakarta.persistence.Version`.

---

## Field Annotation Rules

Every persistent field must have `@Column` with an explicit name when naming strategy = explicit (Step 1.5):

```java
// CORRECT
@Column(value = "birth_date")
private LocalDate birthDate;

// WRONG — missing explicit column name (relies on naming strategy)
private LocalDate birthDate;
```

For non-persistent fields, annotate with `@Transient` from `org.springframework.data.annotation`:

```java
// CORRECT
@Transient
private transient String cachedDisplayName;
```

```java
// WRONG — JPA import
@jakarta.persistence.Transient
private String cachedDisplayName;
```

**Validation** — use Jakarta Validation (`@NotNull`, `@Size`, etc.) for field constraints. Validation is orthogonal to persistence and is not stack-specific.

---

## Field Type Rules

### BigDecimal

Always declare `BigDecimal` columns with explicit DDL precision/scale in your migration. Spring Data JDBC itself does not carry precision/scale metadata on `@Column` (unlike JPA), so the DDL is the source of truth. In Java, the field is just:

```java
// CORRECT
@Column(value = "price")
private BigDecimal price;
```

Make sure the Flyway/Liquibase migration declares the column as e.g. `NUMERIC(10, 2)` — otherwise the database default rounds silently.

### Enum

Map enums by their `String` name unless the project explicitly stores ordinals. Register a `ReadingConverter` / `WritingConverter` if a custom mapping is needed.

```java
// CORRECT — String storage (DDL: status VARCHAR NOT NULL)
@Column(value = "status")
private OrderStatus status;
```

---

## Embedded

Apply when the entity has a value-object component that should be flattened into the same row.

Spring Data Relational's `@Embedded` annotation **requires** an `onEmpty` value — there is no default. The idiomatic forms are the meta-annotations `@Embedded.Empty` and `@Embedded.Nullable` (these are what the IDE's JDBC entity generator emits):

```java
// CORRECT — meta-annotation, the form the IDE generates
@Embedded.Empty(prefix = "ship_")
private Address shippingAddress;
```

```java
// CORRECT — equivalent verbose form
@Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY, prefix = "ship_")
private Address shippingAddress;
```

```java
// CORRECT — nullable variant; field is set to null when all embedded columns are null
@Embedded.Nullable(prefix = "ship_")
private Address shippingAddress;
```

- `@Embedded.Empty` ≡ `@Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)` — all-null columns yield a non-null empty object.
- `@Embedded.Nullable` ≡ `@Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)` — all-null columns yield `null`.
- `prefix` follows the **Embedded prefix** convention. Without a prefix, two embedded fields of the same value type collide on column names.
- The embedded class itself is **not** annotated with `@Table` — it is a plain POJO.

Pick the form (meta-annotation vs. verbose) consistent with what the project already uses; **prefer the meta-annotation form** for new code — it is shorter and matches the IDE generator.

```java
// WRONG — @Embedded without onEmpty does not compile (onEmpty has no default)
@Embedded(prefix = "ship_")
private Address shippingAddress;
```

```java
// WRONG — JPA's @Embeddable / @AttributeOverrides
@Embedded
@AttributeOverrides({ @AttributeOverride(name = "city", column = @Column(name = "ship_city")) })
private Address shippingAddress;
```

---

## Associations — owned children (same aggregate)

Use `@MappedCollection` for children that belong to this aggregate and are loaded/saved with it. Default collection type is **Set** unless the project's convention says otherwise.

```java
// CORRECT — Set of owned children
@MappedCollection(idColumn = "order_id")
private Set<OrderItem> items = new LinkedHashSet<>();
```

For ordered collections, use **List** and supply `keyColumn`:

```java
// CORRECT — ordered List
@MappedCollection(idColumn = "order_id", keyColumn = "order_key")
private List<OrderItem> items = new ArrayList<>();
```

For keyed collections, use **Map** and supply `keyColumn`:

```java
// CORRECT — Map keyed by a column
@MappedCollection(idColumn = "order_id", keyColumn = "item_sku")
private Map<String, OrderItem> itemsBySku = new HashMap<>();
```

Column-name defaults (when the project does not override):

- `idColumn` = `<owner_table_singular>_id` (e.g. `order_id` for an `orders` table).
- `keyColumn` = `<owner_table_singular>_key`.

```java
// WRONG — owned child should not be reached via AggregateReference
@MappedCollection(idColumn = "order_id")
private Set<AggregateReference<OrderItem, Long>> items;
```

```java
// WRONG — JPA-style relationship on a JDBC entity
@OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
private Set<OrderItem> items;
```

Owned children are POJOs annotated with `@Table` (so the framework knows the table name) but are **not** aggregate roots — they have no repository and are not referenced from outside the aggregate. The aggregate root's repository handles their lifecycle.

---

## AggregateReference — cross-aggregate links

When the field points to an entity that belongs to **another** aggregate, model the link as `AggregateReference<Target, IdType>`. No annotation is required; the type itself carries the meaning. The `IdType` must equal the target entity's `@Id` type — read it via `get_jdbc_entity_details` (`idField.type`).

```java
// CORRECT — cross-aggregate link to a Customer (separate aggregate root)
@Column(value = "customer_id")
private AggregateReference<Customer, Long> customer;
```

To resolve the linked aggregate, the service layer calls the target repository: `customerRepository.findById(order.getCustomer().getId())`.

```java
// WRONG — raw foreign-key id field (loses type information; breaks refactors)
@Column(value = "customer_id")
private Long customerId;
```

```java
// WRONG — owned association used to cross an aggregate boundary
@MappedCollection(idColumn = "customer_id")
private Set<Customer> customers;
```

**Rule of thumb** — if the target type has its own repository, it is an aggregate root and must be reached via `AggregateReference`. When in doubt, call `get_jdbc_entity_details` on the target type: a non-null `aggregateRootFqn` means the target is an owned child; a null `aggregateRootFqn` means it is itself a root and must be linked via `AggregateReference`.

**If the target is an owned child of another aggregate** (non-null `aggregateRootFqn`) — stop. This does **not** mean "fall back to a raw FK column"; referencing a member of another aggregate is not allowed in any shape (raw FK included — it is the same WRONG raw-FK example above, and it is invisible to `referencedBy` tooling). Read the rule "External references may only target aggregate roots" in `references/aggregate-rules-impl.md` and pick one of its three resolutions: re-frame the direction (the member side holds a link collection pointing at your root), promote the child to its own aggregate root, or reference the owning root instead.

---

## Records — extra notes

When class shape = record (from Step 1.5):

```java
// CORRECT
@Table("orders")
public record Order(
    @Id Long id,
    @Column("placed_at") Instant placedAt,
    @MappedCollection(idColumn = "order_id") Set<OrderItem> items,
    @Column("customer_id") AggregateReference<Customer, Long> customer
) {}
```

- Do not write a manual `equals`/`hashCode`/`toString` for records.
- Do not use Lombok on records.
- Mutating fields is not possible — re-create the record (`new Order(...)`) to update state.

---

## equals & hashCode

Branch on the **equals/hashCode style** convention from Step 1.5.

> Spring Data JDBC does **not** create proxies. Do NOT copy the JPA `HibernateProxy` pattern here — it is dead code in this stack.

### Manual on `id` (mutable / immutable classes)

```java
// CORRECT — plain id-based equality
@Override
public final boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Order other)) return false;
    return id != null && id.equals(other.id);
}

@Override
public final int hashCode() {
    return getClass().hashCode();
}
```

Notes:
- `equals` returns `false` when `id == null` so transient (unsaved) entities are not considered equal — this matches the JPA convention.
- `hashCode` is stable across the lifecycle by hashing on the class, not the (mutable) id.

### Lombok

```java
// CORRECT
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {
    @Id @EqualsAndHashCode.Include
    private Long id;
    // ...
}
```

Only annotate `id` (or the fields resolved in Step 1.5) with `@EqualsAndHashCode.Include`. Never include `AggregateReference` fields or `@MappedCollection` fields.

### Record

The record auto-generated `equals`/`hashCode` compares every component, including collections — which is usually fine for value-style aggregates but expensive for large owned collections. If that is a problem, switch the class shape to immutable class with a manual `id`-based `equals`/`hashCode`.

### WRONG — copy from JPA

```java
// WRONG — Spring Data JDBC has no proxies; this branch is dead
@Override
public final boolean equals(Object o) {
    if (o instanceof HibernateProxy proxy) { /* never true */ }
    // ...
}
```

---

## toString

Branch on the **toString style** convention from Step 1.5.

Spring Data JDBC has no lazy loading — all local scalar fields, embedded objects, and `AggregateReference` fields (which only hold an id) are safe to include in `toString()`. Owned `@MappedCollection` collections are also loaded eagerly with the aggregate, so they are technically safe but typically excluded to keep output short.

### Manual

```java
// CORRECT — all local non-collection fields + AggregateReference fields
@Override
public String toString() {
    return "Order{id=" + id
        + ", placedAt=" + placedAt
        + ", customer=" + customer  // AggregateReference is safe; it holds an id
        + "}";
}
```

### Lombok

```java
// CORRECT
@ToString(onlyExplicitlyIncluded = true)
public class Order {
    @ToString.Include private Long id;
    @ToString.Include private Instant placedAt;
    @ToString.Include private AggregateReference<Customer, Long> customer;
    @MappedCollection(idColumn = "order_id") private Set<OrderItem> items; // excluded
}
```

Or — without `onlyExplicitlyIncluded`:

```java
@ToString
public class Order {
    private Long id;
    private Instant placedAt;
    @ToString.Exclude private Set<OrderItem> items;
}
```

### Record

Use the record's auto-generated `toString()`. Override only when output volume is a concern.
