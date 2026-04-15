# GET_MANY method (Java)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No DTO:
```java
@org.springframework.web.bind.annotation.GetMapping("/by-ids")
public java.util.List<${EntityFqn}> getMany(@org.springframework.web.bind.annotation.RequestParam java.util.List<${IdType}> ids) {
    return ${repoFieldName}.findAllById(ids);
}
```

### with DTO
```java
@org.springframework.web.bind.annotation.GetMapping("/by-ids")
public java.util.List<${DtoFqn}> getMany(@org.springframework.web.bind.annotation.RequestParam java.util.List<${IdType}> ids) {
    java.util.List<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAllById(ids);
    return ${entityVarPlural}.stream()
        .map(${mapperFieldName}::${toDtoMethodName})
        .toList();
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
