# updateWithNull MapStruct method (Kotlin)

## Insert Point
Abstract function in mapper abstract class body.

## Code

```defaults
skip by default (only generated when user explicitly requests full update method)
```

### When UPDATE_WITH_NULL_VALUES requested

```kotlin
@org.mapstruct.InheritConfiguration(name = "{firstUpdateMethodName}")
abstract fun {methodName}({dtoParamName}: {dtoClassFqn}, @org.mapstruct.MappingTarget {entityParamName}: {entityClassFqn}): {entityClassFqn}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{methodName}` | naming pattern | `updateWithNull` |
| `{firstUpdateMethodName}` | name of first update/toEntity method | `toEntity` |
| `{dtoParamName}` | decapitalized DTO short name | — |
| `{entityParamName}` | decapitalized entity short name | — |
| `{dtoClassFqn}` | DTO class FQN | — |
| `{entityClassFqn}` | entity class FQN | — |
