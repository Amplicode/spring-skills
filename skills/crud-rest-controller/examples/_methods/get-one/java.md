# GET_ONE method (Java)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No DTO:
```java
@org.springframework.web.bind.annotation.GetMapping("/{id}")
public ${EntityFqn} getOne(@org.springframework.web.bind.annotation.PathVariable ${IdType} id) {
    java.util.Optional<${EntityFqn}> ${entityVar}Optional = ${repoFieldName}.findById(id);
    return ${entityVar}Optional.orElseThrow(() ->
        new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
}
```

### with DTO
```java
@org.springframework.web.bind.annotation.GetMapping("/{id}")
public ${DtoFqn} getOne(@org.springframework.web.bind.annotation.PathVariable ${IdType} id) {
    java.util.Optional<${EntityFqn}> ${entityVar}Optional = ${repoFieldName}.findById(id);
    return ${mapperFieldName}.${toDtoMethodName}(${entityVar}Optional.orElseThrow(() ->
        new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))));
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
