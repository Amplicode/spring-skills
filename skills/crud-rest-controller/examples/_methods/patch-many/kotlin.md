# PATCH_MANY method (Kotlin)

## Insert Point
As new method in controller class body, after last method.

## Note on @Valid
When `hasValidation = true`, add `@jakarta.validation.Valid` before the `ids` parameter in all variants below.
Example: `@org.springframework.web.bind.annotation.RequestParam @jakarta.validation.Valid ids: List<${IdType}>`

## Code

### defaults
No DTO, ObjectPatcher available:
```kotlin
@org.springframework.web.bind.annotation.PatchMapping
fun patchMany(@org.springframework.web.bind.annotation.RequestParam ids: List<${IdType}>, @org.springframework.web.bind.annotation.RequestBody patchNode: ${JsonNodeFqn}): List<${IdType}> {
    val ${entityVarPlural} = kotlin.collections.ArrayList(${repoFieldName}.findAllById(ids))
    ${entityVarPlural}.replaceAll { ${entityVar} -> objectPatcher.patchAndValidate(${entityVar}, patchNode) }
    val result${EntityNamePlural} = ${repoFieldName}.saveAll(${entityVarPlural})
    return result${EntityNamePlural}.mapNotNull { it.id }
}
```

### with DTO + mapper, ObjectPatcher available
```kotlin
@org.springframework.web.bind.annotation.PatchMapping
fun patchMany(@org.springframework.web.bind.annotation.RequestParam ids: List<${IdType}>, @org.springframework.web.bind.annotation.RequestBody patchNode: ${JsonNodeFqn}): List<${IdType}> {
    val ${entityVarPlural} = ${repoFieldName}.findAllById(ids)
    for (${entityVar} in ${entityVarPlural}) {
        var ${dtoVar} = ${mapperFieldName}.${toDtoMethodName}(${entityVar})
        ${dtoVar} = objectPatcher.patchAndValidate(${dtoVar}, patchNode)
        ${mapperFieldName}.updateWithNull(${dtoVar}, ${entityVar})
    }
    val result${EntityNamePlural} = ${repoFieldName}.saveAll(${entityVarPlural})
    return result${EntityNamePlural}.mapNotNull { it.id }
}
```

### with DTO + mapper, ObjectMapper fallback
```kotlin
@org.springframework.web.bind.annotation.PatchMapping
@Throws(java.io.IOException::class)
fun patchMany(@org.springframework.web.bind.annotation.RequestParam ids: List<${IdType}>, @org.springframework.web.bind.annotation.RequestBody patchNode: ${JsonNodeFqn}): List<${IdType}> {
    val ${entityVarPlural} = ${repoFieldName}.findAllById(ids)

    for (${entityVar} in ${entityVarPlural}) {
        var ${dtoVar} = ${mapperFieldName}.${toDtoMethodName}(${entityVar})
        objectMapper.readerForUpdating(${dtoVar}).readValue<${DtoFqn}>(patchNode)
        ${mapperFieldName}.updateWithNull(${dtoVar}, ${entityVar})
    }

    val result${EntityNamePlural} = ${repoFieldName}.saveAll(${entityVarPlural})
    return result${EntityNamePlural}.mapNotNull { it.id }
}
```

### no DTO, ObjectMapper fallback
```kotlin
@org.springframework.web.bind.annotation.PatchMapping
@Throws(java.io.IOException::class)
fun patchMany(@org.springframework.web.bind.annotation.RequestParam ids: List<${IdType}>, @org.springframework.web.bind.annotation.RequestBody patchNode: ${JsonNodeFqn}): List<${IdType}> {
    val ${entityVarPlural} = ${repoFieldName}.findAllById(ids)

    for (${entityVar} in ${entityVarPlural}) {
        objectMapper.readerForUpdating(${entityVar}).readValue<${EntityFqn}>(patchNode)
    }

    val result${EntityNamePlural} = ${repoFieldName}.saveAll(${entityVarPlural})
    return result${EntityNamePlural}.mapNotNull { it.id }
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${EntityFqn}` | entity FQN | -- |
| `${EntityNamePlural}` | pluralized entity name | -- |
| `${DtoFqn}` | DTO FQN | -- |
| `${dtoVar}` | decapitalized DTO name | -- |
| `${IdType}` | entity ID type (boxed) | -- |
| `${repoFieldName}` | repository field name | -- |
| `${entityVar}` | decapitalized entity name | -- |
| `${entityVarPlural}` | pluralized entity var | -- |
| `${mapperFieldName}` | mapper field name | -- |
| `${toDtoMethodName}` | mapper entity->DTO method | `toDto` |
| `${JsonNodeFqn}` | Jackson JsonNode FQN | `com.fasterxml.jackson.databind.JsonNode` (Jackson 2.x) or `tools.jackson.databind.JsonNode` (Jackson 3.x) |
