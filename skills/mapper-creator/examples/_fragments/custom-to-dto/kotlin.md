# Custom toDto method (Kotlin)

## Insert Point
Extension function in the mapper file (not inside a class).

## Code

```defaults
Always generated for Custom mapper.
```

```kotlin
fun {entityClassFqn}.{methodName}() = {dtoClassFqn}(
    {field1} = this.{field1},
    {field2} = this.{field2}
)
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{methodName}` | naming pattern `to${DTO_NAME}` | e.g. `toOrderDto` |
| `{entityClassFqn}` | entity class FQN | — |
| `{dtoClassFqn}` | DTO class FQN | — |
| `{fieldN}` | DTO attribute names | — |
