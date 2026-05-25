# Repository Implementation Rules

Apply these rules when writing or modifying any Spring Data JDBC repository. All rules assume the conventions resolved in Step 1 of `repository-conventions.md` are already applied.

Imports come from Spring Data, **not** from `jakarta.persistence` or `org.springframework.data.jpa.*`:

- `org.springframework.data.repository.ListCrudRepository`
- `org.springframework.data.repository.ListPagingAndSortingRepository`
- `org.springframework.data.jdbc.repository.query.Query`
- `org.springframework.data.jdbc.repository.query.Modifying`
- `org.springframework.data.repository.query.Param`

---

## Pre-checks before creating a repository

Before generating a new repository class, run two MCP checks:

1. `list_entity_repositories` — confirm a repository for this entity does not already exist.
2. `get_jdbc_entity_details` on the target entity — confirm the entity is an **aggregate root** (`aggregateRootFqn == null`).

If `aggregateRootFqn` is non-null, the entity is an owned child of another aggregate. Owned children must not have their own repository — the aggregate root's repository owns their lifecycle. Refuse to create the repository and explain: "<entity> is an owned child of aggregate <aggregateRootFqn>. Access it through the aggregate root's repository."

---

## Class — creating a new repository

- Declare as a `public interface` named `<Entity>Repository` (e.g. `OrderRepository`).
- Extend the base interface chosen in Step 1.5 (default `ListCrudRepository<T, ID>`).
- The `ID` generic must match the target entity's `@Id` type exactly. Fetch the id type via `get_jdbc_entity_details` (`idField.type`) — do not guess.

```java
// CORRECT — Long id
public interface OrderRepository extends ListCrudRepository<Order, Long> {
}
```

```java
// WRONG — ID generic widened to Number
public interface OrderRepository extends ListCrudRepository<Order, Number> { ... }
```

```java
// WRONG — extends a JPA interface on a JDBC entity
public interface OrderRepository extends JpaRepository<Order, Long> { ... }
```

---

## Derived query methods

Prefer derived query method names when the criterion is expressible in Spring Data's query DSL:

```java
// CORRECT — derived methods
List<Order> findByCustomer(AggregateReference<Customer, Long> customer);
Optional<Order> findByIdAndStatus(Long id, OrderStatus status);
long countByStatus(OrderStatus status);
```

When querying by an `AggregateReference` field, use the `AggregateReference` value (not the raw id) in the method signature — Spring Data unwraps the id internally.

---

## `@Query` — when a derived method is not enough

Use named parameters (`:name`) per the project's query style convention from Step 1.5:

```java
// CORRECT — named parameters
@Query("""
    SELECT o.* FROM orders o
    WHERE o.status = :status
      AND o.placed_at >= :since
""")
List<Order> findRecentByStatus(@Param("status") String status,
                               @Param("since") Instant since);
```

```java
// WRONG — positional parameters when project convention is named
@Query("SELECT o.* FROM orders o WHERE o.status = ?1")
List<Order> findByStatus(String status);
```

Notes:

- `@Query` for JDBC takes **native SQL**, not JPQL. Use real column names from the DDL, not Java field names.
- Always alias the table (`orders o`) and select `o.*` so Spring Data can map the row to the aggregate.
- When projecting to a DTO, write the SELECT list explicitly and declare the DTO either as a Java record or as an interface projection.

### DTO projection (record)

```java
public record OrderSummary(Long id, OrderStatus status, Instant placedAt) {}

@Query("""
    SELECT o.id AS id, o.status AS status, o.placed_at AS placed_at
    FROM orders o
    WHERE o.customer_id = :customerId
""")
List<OrderSummary> findSummariesByCustomer(@Param("customerId") Long customerId);
```

### Interface projection

```java
public interface OrderSummary {
    Long getId();
    OrderStatus getStatus();
    Instant getPlacedAt();
}
```

Pick the form (record vs interface) consistent with the **Result mapping** convention from Step 1.5.

---

## Modifying queries

Every `UPDATE` / `DELETE` `@Query` **must** be annotated with `@Modifying` from `org.springframework.data.jdbc.repository.query`. The return type follows the project's convention (default `int` row count):

```java
// CORRECT
@Modifying
@Query("UPDATE orders SET status = :newStatus WHERE status = :oldStatus")
int reassignStatus(@Param("oldStatus") String oldStatus,
                   @Param("newStatus") String newStatus);
```

```java
// WRONG — missing @Modifying
@Query("UPDATE orders SET status = :newStatus WHERE status = :oldStatus")
int reassignStatus(@Param("oldStatus") String oldStatus,
                   @Param("newStatus") String newStatus);
```

```java
// WRONG — JPA's @Modifying import
@org.springframework.data.jpa.repository.Modifying
@Query("...")
int reassignStatus(...);
```

---

## Pagination

When pagination is required and the project convention = `Pageable` + `Page<T>`:

```java
Page<Order> findByStatus(OrderStatus status, Pageable pageable);
```

`Slice<T>` is acceptable when you do not need a total count — it avoids the extra `COUNT(*)` query.

---

## Transactional boundaries

Do **not** annotate repository methods with `@Transactional`. Spring Data already wraps each repository call in its own transaction. Transactional boundaries live on the service layer so that a single business operation spans multiple repository calls.

```java
// CORRECT — boundary on service
@Service
public class OrderService {
    @Transactional
    public Order placeOrder(NewOrderCommand cmd) {
        Order order = orderRepository.save(/* ... */);
        auditRepository.save(/* ... */);
        return order;
    }
}
```

```java
// WRONG — @Transactional on repository method
public interface OrderRepository extends ListCrudRepository<Order, Long> {
    @Transactional
    @Modifying
    @Query("UPDATE orders SET status = :s WHERE id = :id")
    int updateStatus(@Param("id") Long id, @Param("s") String s);
}
```

(Exception: `@Transactional(readOnly = true)` on a heavy read-only finder is acceptable if the project convention permits it.)
