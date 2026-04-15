# mapFromAggregateReference helper (Java)

## Insert Point
Default method in mapper interface body.

## Code

```defaults
skip this fragment (only for Spring Data JDBC entities with AggregateReference attributes)
```

### When entity has AggregateReference attributes with ID sub-DTO type

```java
default <T, R> R mapFromAggregateReference(org.springframework.data.jdbc.core.mapping.AggregateReference<T, R> aggregateReference) {
    if (aggregateReference == null) {
        return null;
    }
    return aggregateReference.getId();
}
```

## Variables
None (generic helper method).
