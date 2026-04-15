# CREATE method (Kotlin)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No DTO:
```kotlin
@org.springframework.web.bind.annotation.PostMapping
fun create(@org.springframework.web.bind.annotation.RequestBody ${entityVar}: ${EntityFqn}): ${EntityFqn} =
    ${repoFieldName}.save(${entityVar})
```

### with @Valid (no DTO)
```kotlin
@org.springframework.web.bind.annotation.PostMapping
fun create(@org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid ${entityVar}: ${EntityFqn}): ${EntityFqn} =
    ${repoFieldName}.save(${entityVar})
```

### with DTO
```kotlin
@org.springframework.web.bind.annotation.PostMapping
fun create(@org.springframework.web.bind.annotation.RequestBody dto: ${DtoFqn}): ${DtoFqn} {
    var ${entityVar} = ${mapperFieldName}.${toEntityMethodName}(dto)
    ${entityVar} = ${repoFieldName}.save(${entityVar})
    return ${mapperFieldName}.${toDtoMethodName}(${entityVar})
}
```

### with @Valid and DTO
```kotlin
@org.springframework.web.bind.annotation.PostMapping
fun create(@org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid dto: ${DtoFqn}): ${DtoFqn} {
    var ${entityVar} = ${mapperFieldName}.${toEntityMethodName}(dto)
    ${entityVar} = ${repoFieldName}.save(${entityVar})
    return ${mapperFieldName}.${toDtoMethodName}(${entityVar})
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${EntityFqn}` | entity FQN | -- |
| `${DtoFqn}` | DTO FQN | -- |
| `${entityVar}` | decapitalized entity name | -- |
| `${repoFieldName}` | repository field name | -- |
| `${mapperFieldName}` | mapper field name | -- |
| `${toEntityMethodName}` | mapper DTO->entity method | `toEntity` |
| `${toDtoMethodName}` | mapper entity->DTO method | `toDto` |
