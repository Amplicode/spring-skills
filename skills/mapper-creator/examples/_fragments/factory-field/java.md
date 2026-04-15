# MAPPER factory field (Java)

## Insert Point
Field inside the mapper interface body. Only when componentModel = DEFAULT.

## Code

```defaults
skip this fragment (componentModel is SPRING by default)
```

### When componentModel = DEFAULT

```java
{className} MAPPER = org.mapstruct.factory.Mappers.getMapper({className}.class);
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | user choice | `{EntityName}Mapper` |
