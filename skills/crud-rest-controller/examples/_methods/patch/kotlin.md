# PATCH method (Kotlin)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No DTO, ObjectPatcher available:
```kotlin
@org.springframework.web.bind.annotation.PatchMapping("/{id}")
fun patch(@org.springframework.web.bind.annotation.PathVariable id: ${IdType}, @org.springframework.web.bind.annotation.RequestBody patchNode: ${JsonNodeFqn}): ${EntityFqn} {
    var ${entityVar}: ${EntityFqn} = ${repoFieldName}.findById(id)
        .orElseThrow { org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `$id` not found") }
    ${entityVar} = objectPatcher.patchAndValidate(${entityVar}, patchNode)
    return ${repoFieldName}.save(${entityVar})
}
```

### with DTO + mapper, ObjectPatcher available
```kotlin
@org.springframework.web.bind.annotation.PatchMapping("/{id}")
fun patch(@org.springframework.web.bind.annotation.PathVariable id: ${IdType}, @org.springframework.web.bind.annotation.RequestBody patchNode: ${JsonNodeFqn}): ${DtoFqn} {
    var ${entityVar}: ${EntityFqn} = ${repoFieldName}.findById(id)
        .orElseThrow { org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `$id` not found") }
    var ${dtoVar} = ${mapperFieldName}.${toDtoMethodName}(${entityVar})
    ${dtoVar} = objectPatcher.patchAndValidate(${dtoVar}, patchNode)
    ${mapperFieldName}.updateWithNull(${dtoVar}, ${entityVar})
    val result${EntityName}: ${EntityFqn} = ${repoFieldName}.save(${entityVar})
    return ${mapperFieldName}.${toDtoMethodName}(result${EntityName})
}
```

### no DTO, ObjectPatcher available, NO validation lib
```kotlin
@org.springframework.web.bind.annotation.PatchMapping("/{id}")
fun patch(@org.springframework.web.bind.annotation.PathVariable id: ${IdType}, @org.springframework.web.bind.annotation.RequestBody patchNode: ${JsonNodeFqn}): ${EntityFqn} {
    var ${entityVar}: ${EntityFqn} = ${repoFieldName}.findById(id)
        .orElseThrow { org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `$id` not found") }
    ${entityVar} = objectPatcher.patch(${entityVar}, patchNode)
    return ${repoFieldName}.save(${entityVar})
}
```

### with DTO + mapper, ObjectMapper fallback
```kotlin
@org.springframework.web.bind.annotation.PatchMapping("/{id}")
@Throws(java.io.IOException::class)
fun patch(@org.springframework.web.bind.annotation.PathVariable id: ${IdType}, @org.springframework.web.bind.annotation.RequestBody patchNode: ${JsonNodeFqn}): ${DtoFqn} {
    val ${entityVar}: ${EntityFqn} = ${repoFieldName}.findById(id)
        .orElseThrow { org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `$id` not found") }

    var ${dtoVar} = ${mapperFieldName}.${toDtoMethodName}(${entityVar})
    objectMapper.readerForUpdating(${dtoVar}).readValue<${DtoFqn}>(patchNode)
    ${mapperFieldName}.updateWithNull(${dtoVar}, ${entityVar})

    val result${EntityName}: ${EntityFqn} = ${repoFieldName}.save(${entityVar})
    return ${mapperFieldName}.${toDtoMethodName}(result${EntityName})
}
```

### no DTO, ObjectMapper fallback
```kotlin
@org.springframework.web.bind.annotation.PatchMapping("/{id}")
@Throws(java.io.IOException::class)
fun patch(@org.springframework.web.bind.annotation.PathVariable id: ${IdType}, @org.springframework.web.bind.annotation.RequestBody patchNode: ${JsonNodeFqn}): ${EntityFqn} {
    val ${entityVar}: ${EntityFqn} = ${repoFieldName}.findById(id)
        .orElseThrow { org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `$id` not found") }
    objectMapper.readerForUpdating(${entityVar}).readValue<${EntityFqn}>(patchNode)
    return ${repoFieldName}.save(${entityVar})
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${EntityFqn}` | entity FQN | -- |
| `${EntityName}` | entity simple name | -- |
| `${DtoFqn}` | DTO FQN | -- |
| `${dtoVar}` | decapitalized DTO name | -- |
| `${IdType}` | entity ID type | -- |
| `${repoFieldName}` | repository field name | -- |
| `${entityVar}` | decapitalized entity name | -- |
| `${mapperFieldName}` | mapper field name | -- |
| `${toDtoMethodName}` | mapper entity->DTO method | `toDto` |
| `${JsonNodeFqn}` | Jackson JsonNode FQN | `com.fasterxml.jackson.databind.JsonNode` (Jackson 2.x) or `tools.jackson.databind.JsonNode` (Jackson 3.x) |
