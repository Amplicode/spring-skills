# updateWithNull MapStruct method (Java)

## Insert Point
Abstract method in mapper interface body.

## Code

```defaults
skip by default (only generated when user explicitly requests full update method)
```

### When UPDATE_WITH_NULL_VALUES requested

```java
@org.mapstruct.InheritConfiguration(name = "{firstUpdateMethodName}")
{entityClassFqn} {methodName}({dtoClassFqn} {dtoParamName}, @org.mapstruct.MappingTarget {entityClassFqn} {entityParamName});
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
