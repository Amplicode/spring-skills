# Custom toEntity method (Kotlin)

## Insert Point
Extension function in the mapper file (not inside a class).

## Code

```defaults
Always generated for Custom mapper.
```

```kotlin
fun {dtoClassFqn}.{methodName}() = {entityClassFqn}().also {
    it.{field1} = this.{field1}
    it.{field2} = this.{field2}
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{methodName}` | naming pattern | `toEntity` |
| `{dtoClassFqn}` | DTO class FQN | — |
| `{entityClassFqn}` | entity class FQN | — |
| `{fieldN}` | attribute names | — |
