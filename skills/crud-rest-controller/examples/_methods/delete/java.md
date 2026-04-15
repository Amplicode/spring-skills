# DELETE method (Java)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No DTO:
```java
@org.springframework.web.bind.annotation.DeleteMapping("/{id}")
public ${EntityFqn} delete(@org.springframework.web.bind.annotation.PathVariable ${IdType} id) {
    ${EntityFqn} ${entityVar} = ${repoFieldName}.findById(id).orElse(null);
    if (${entityVar} != null) {
        ${repoFieldName}.delete(${entityVar});
    }
    return ${entityVar};
}
```

### with DTO
```java
@org.springframework.web.bind.annotation.DeleteMapping("/{id}")
public ${DtoFqn} delete(@org.springframework.web.bind.annotation.PathVariable ${IdType} id) {
    ${EntityFqn} ${entityVar} = ${repoFieldName}.findById(id).orElse(null);
    if (${entityVar} != null) {
        ${repoFieldName}.delete(${entityVar});
    }
    return ${mapperFieldName}.${toDtoMethodName}(${entityVar});
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
