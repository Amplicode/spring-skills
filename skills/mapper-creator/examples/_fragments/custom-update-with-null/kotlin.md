# Custom updateWithNull method (Kotlin)

## Insert Point
Extension function in the mapper file (not inside a class).

## Code

```defaults
skip by default (only generated when user explicitly requests full update method)
```

### When UPDATE_WITH_NULL_VALUES requested

```kotlin
fun {entityClassFqn}.{methodName}({dtoParamName}: {dtoClassFqn}) = apply {
    {field1} = {dtoParamName}.{field1}
    {field2} = {dtoParamName}.{field2}
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{methodName}` | naming pattern | `updateWithNull` |
| `{dtoParamName}` | decapitalized DTO short name | — |
| `{dtoClassFqn}` | DTO class FQN | — |
| `{entityClassFqn}` | entity class FQN | — |
| `{fieldN}` | attribute names | — |
