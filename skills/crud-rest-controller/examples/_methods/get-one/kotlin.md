# GET_ONE method (Kotlin)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No DTO:
```kotlin
@org.springframework.web.bind.annotation.GetMapping("/{id}")
fun getOne(@org.springframework.web.bind.annotation.PathVariable id: ${IdType}): ${EntityFqn} =
    ${repoFieldName}.findById(id)
        .orElseThrow { org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `$id` not found") }
```

### with DTO
```kotlin
@org.springframework.web.bind.annotation.GetMapping("/{id}")
fun getOne(@org.springframework.web.bind.annotation.PathVariable id: ${IdType}): ${DtoFqn} {
    val ${entityVar} = ${repoFieldName}.findById(id)
        .orElseThrow { org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `$id` not found") }
    return ${mapperFieldName}.${toDtoMethodName}(${entityVar})
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${EntityFqn}` | entity FQN | -- |
| `${DtoFqn}` | DTO FQN | -- |
| `${IdType}` | entity ID type | -- |
| `${repoFieldName}` | repository field name | -- |
| `${entityVar}` | decapitalized entity name | -- |
| `${mapperFieldName}` | mapper field name | -- |
| `${toDtoMethodName}` | mapper entity->DTO method | `toDto` |
