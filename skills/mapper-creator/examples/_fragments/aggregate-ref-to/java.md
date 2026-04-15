# mapToAggregateReference helper (Java)

## Insert Point
Default method in mapper interface body.

## Code

```defaults
skip this fragment (only for Spring Data JDBC entities with AggregateReference attributes)
```

### When entity has AggregateReference attributes with ID sub-DTO type

```java
default <T, R> org.springframework.data.jdbc.core.mapping.AggregateReference<T, R> mapToAggregateReference(R id) {
    if (id == null) {
        return null;
    }
    return org.springframework.data.jdbc.core.mapping.AggregateReference.to(id);
}
```

## Variables
None (generic helper method).
