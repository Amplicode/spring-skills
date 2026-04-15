# MAPPER factory field (Kotlin)

## Insert Point
Companion object inside the mapper abstract class body. Only when componentModel = DEFAULT.

## Code

```defaults
skip this fragment (componentModel is SPRING by default)
```

### When componentModel = DEFAULT

```kotlin
companion object {
    @kotlin.jvm.JvmStatic
    val MAPPER: {className} = org.mapstruct.factory.Mappers.getMapper({className}::class.java)
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | user choice | `{EntityName}Mapper` |
