# partialUpdate MapStruct method (Kotlin)

## Insert Point
Abstract function in mapper abstract class body.

## Code

```defaults
skip by default (only generated when user explicitly requests partial update method)
```

### When PARTIAL_UPDATE requested

```kotlin
@org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
@org.mapstruct.InheritConfiguration(name = "{toEntityMethodName}")
abstract fun {methodName}({dtoParamName}: {dtoClassFqn}, @org.mapstruct.MappingTarget {entityParamName}: {entityClassFqn}): {entityClassFqn}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{methodName}` | naming pattern | `partialUpdate` |
| `{dtoParamName}` | decapitalized DTO short name | e.g. `orderDto` |
| `{entityParamName}` | decapitalized entity short name | e.g. `order` |
| `{dtoClassFqn}` | DTO class FQN | — |
| `{entityClassFqn}` | entity class FQN | — |
| `{toEntityMethodName}` | name of toEntity method | `toEntity` |
