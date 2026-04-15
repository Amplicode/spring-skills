# DELETE method (Kotlin)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No DTO:
```kotlin
@org.springframework.web.bind.annotation.DeleteMapping("/{id}")
fun delete(@org.springframework.web.bind.annotation.PathVariable id: ${IdType}): ${EntityFqn}? {
    val ${entityVar} = ${repoFieldName}.findById(id).orElse(null)
    if (${entityVar} != null) {
        ${repoFieldName}.delete(${entityVar})
    }
    return ${entityVar}
}
```

### with DTO
```kotlin
@org.springframework.web.bind.annotation.DeleteMapping("/{id}")
fun delete(@org.springframework.web.bind.annotation.PathVariable id: ${IdType}): ${DtoFqn}? {
    val ${entityVar} = ${repoFieldName}.findById(id).orElse(null)
    if (${entityVar} != null) {
        ${repoFieldName}.delete(${entityVar})
    }
    return if (${entityVar} != null) ${mapperFieldName}.${toDtoMethodName}(${entityVar}) else null
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
