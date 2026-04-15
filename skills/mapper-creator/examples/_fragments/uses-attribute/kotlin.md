# @Mapper(uses = ...) attribute (Kotlin)

## Insert Point
Modifies existing @Mapper annotation to add uses attribute.

## Code

```defaults
skip this fragment (no sub-mappers by default)
```

### When sub-mappers exist for association DTOs

```kotlin
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = ..., uses = [SubMapper1::class, SubMapper2::class])
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| sub-mapper FQNs | auto-detected from existing mappers for association entities | — |
