# partialUpdate MapStruct method (Java)

## Insert Point
Abstract method in mapper interface body.

## Code

```defaults
skip by default (only generated when user explicitly requests partial update method)
```

### When PARTIAL_UPDATE requested

Always **one** method, controlled by ONE `@BeanMapping` annotation. The
`nullValuePropertyMappingStrategy` enum value is selected by the user
(SET_TO_NULL / IGNORE / SET_TO_DEFAULT). Do **not** emit
`@InheritConfiguration` — the inherited
mappings are not needed for the canonical Pet/Owner-style DTOs.

```java
@org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.{strategyEnum})
{entityClassFqn} {methodName}({dtoClassFqn} {dtoParamName}, @org.mapstruct.MappingTarget {entityClassFqn} {entityParamName});
```

### Worked examples (one per strategy — only one is emitted)

```java
// SET_TO_NULL  (the most common — null in DTO clears the field)
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
Owner partialUpdate(OwnerDto ownerDto, @MappingTarget Owner owner);

// IGNORE  (null in DTO keeps the existing value)
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
Owner partialUpdate(OwnerDto ownerDto, @MappingTarget Owner owner);

// SET_TO_DEFAULT  (null in DTO resets to type default — 0 for int, "" for String, etc.)
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
Owner partialUpdate(OwnerDto ownerDto, @MappingTarget Owner owner);
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{methodName}` | naming pattern | `partialUpdate` |
| `{strategyEnum}` | user choice | `SET_TO_NULL` |
| `{dtoParamName}` | decapitalized DTO short name | e.g. `orderDto` |
| `{entityParamName}` | decapitalized entity short name | e.g. `order` |
| `{dtoClassFqn}` | DTO class FQN | — |
| `{entityClassFqn}` | entity class FQN | — |
