# @Mapper annotation (Java)

## Insert Point
Annotation on the mapper interface created from skeleton.

## Code

```defaults
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING)
```

### SPRING component model (MappingConstants available)

```java
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING)
```

### SPRING component model (MappingConstants NOT available, older MapStruct)

```java
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = "spring")
```

### CDI component model

```java
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, componentModel = "cdi")
```

### DEFAULT component model (no Spring, no CDI)

```java
@org.mapstruct.Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| componentModel | SPRING if any `spring-boot-starter*` / `spring-context` in `presentDeps`, else DEFAULT. CDI only when the user explicitly asked for it. | SPRING |

## Note — recommended default

Prefer the SPRING (modern) variant
(`MappingConstants.ComponentModel.SPRING`) whenever Spring is on the
classpath. The DEFAULT and CDI variants are listed for completeness
but are rarely needed in practice.
