# @Mapper annotation (Kotlin)

## Insert Point
Annotation on the mapper abstract class created from skeleton.

## Code

```defaults
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING)
```

### SPRING component model (MappingConstants available)

```kotlin
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING)
```

### SPRING component model (MappingConstants NOT available)

```kotlin
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = "spring")
```

### CDI component model

```kotlin
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = "cdi")
```

### DEFAULT component model

```kotlin
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| componentModel | SPRING if any `spring-boot-starter*` / `spring-context` in `presentDeps`, else DEFAULT. CDI only when the user explicitly asked for it. | SPRING |
