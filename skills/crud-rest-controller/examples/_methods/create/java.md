# CREATE method (Java)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No DTO, no validation:
```java
@org.springframework.web.bind.annotation.PostMapping
public ${EntityFqn} create(@org.springframework.web.bind.annotation.RequestBody ${EntityFqn} ${entityVar}) {
    return ${repoFieldName}.save(${entityVar});
}
```

### no DTO, with @Valid
```java
@org.springframework.web.bind.annotation.PostMapping
public ${EntityFqn} create(@org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid ${EntityFqn} ${entityVar}) {
    return ${repoFieldName}.save(${entityVar});
}
```

### with DTO
```java
@org.springframework.web.bind.annotation.PostMapping
public ${DtoFqn} create(@org.springframework.web.bind.annotation.RequestBody ${DtoFqn} dto) {
    ${EntityFqn} ${entityVar} = ${mapperFieldName}.${toEntityMethodName}(dto);
    ${EntityFqn} result${EntityName} = ${repoFieldName}.save(${entityVar});
    return ${mapperFieldName}.${toDtoMethodName}(result${EntityName});
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${EntityFqn}` | entity FQN | -- |
| `${EntityName}` | entity simple name | -- |
| `${DtoFqn}` | DTO FQN | -- |
| `${entityVar}` | decapitalized entity name | -- |
| `${repoFieldName}` | repository field name | -- |
| `${mapperFieldName}` | mapper field name | -- |
| `${toEntityMethodName}` | mapper DTO->entity method | `toEntity` |
| `${toDtoMethodName}` | mapper entity->DTO method | `toDto` |
