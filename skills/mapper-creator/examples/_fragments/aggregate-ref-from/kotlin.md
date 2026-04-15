# mapFromAggregateReference helper (Kotlin)

## Insert Point
Function in mapper abstract class body.

## Code

```defaults
skip this fragment (only for Spring Data JDBC entities with AggregateReference attributes)
```

### When entity has AggregateReference attributes with ID sub-DTO type

```kotlin
fun <T, R> mapFromAggregateReference(aggregateReference: org.springframework.data.jdbc.core.mapping.AggregateReference<T, R>): R {
    return aggregateReference?.id
}
```

## Variables
None (generic helper method).
