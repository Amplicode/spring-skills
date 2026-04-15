# GET_MANY method (Kotlin)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No DTO:
```kotlin
@org.springframework.web.bind.annotation.GetMapping("/by-ids")
fun getMany(@org.springframework.web.bind.annotation.RequestParam ids: List<${IdType}>): List<${EntityFqn}> =
    ${repoFieldName}.findAllById(ids)
```

### with DTO
```kotlin
@org.springframework.web.bind.annotation.GetMapping("/by-ids")
fun getMany(@org.springframework.web.bind.annotation.RequestParam ids: List<${IdType}>): List<${DtoFqn}> {
    val ${entityVarPlural} = ${repoFieldName}.findAllById(ids)
    return ${entityVarPlural}.map { ${mapperFieldName}.${toDtoMethodName}(it) }
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${EntityFqn}` | entity FQN | -- |
| `${DtoFqn}` | DTO FQN | -- |
| `${IdType}` | entity ID type (boxed) | -- |
| `${repoFieldName}` | repository field name | -- |
| `${entityVarPlural}` | pluralized entity var | -- |
| `${mapperFieldName}` | mapper field name | -- |
| `${toDtoMethodName}` | mapper entity->DTO method | `toDto` |
