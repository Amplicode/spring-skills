# Context inner data class + helper methods (Kotlin)

## Insert Point
Inner data class in mapper abstract class body + helper methods.

## Code

```defaults
skip this fragment (only when entity has AggregateReference attributes with non-ID sub-DTO type)
```

### Context inner data class

```kotlin
data class {dtoName}LoadedContext(val {field1}: {type1}?, val {field2s}: java.util.Collection<{type2}>)
```

Context helper methods are generated per aggregate reference attribute. Their exact form depends on whether the attribute is a collection, child of collection, etc.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{dtoName}` | DTO short class name | e.g. `OrderDto` |
| `{fieldN}` | from attribute type class name, decapitalized | — |
| `{typeN}` | attribute type FQN | — |
