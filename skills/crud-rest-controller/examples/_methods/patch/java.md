# PATCH method (Java)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No DTO, ObjectPatcher available:
```java
@org.springframework.web.bind.annotation.PatchMapping("/{id}")
public ${EntityFqn} patch(@org.springframework.web.bind.annotation.PathVariable ${IdType} id, @org.springframework.web.bind.annotation.RequestBody ${JsonNodeFqn} patchNode) {
    ${EntityFqn} ${entityVar} = ${repoFieldName}.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    ${entityVar} = objectPatcher.patchAndValidate(${entityVar}, patchNode);
    return ${repoFieldName}.save(${entityVar});
}
```

### with DTO + mapper, ObjectPatcher available
```java
@org.springframework.web.bind.annotation.PatchMapping("/{id}")
public ${DtoFqn} patch(@org.springframework.web.bind.annotation.PathVariable ${IdType} id, @org.springframework.web.bind.annotation.RequestBody ${JsonNodeFqn} patchNode) {
    ${EntityFqn} ${entityVar} = ${repoFieldName}.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    ${DtoFqn} ${dtoVar} = ${mapperFieldName}.${toDtoMethodName}(${entityVar});
    ${dtoVar} = objectPatcher.patchAndValidate(${dtoVar}, patchNode);
    ${mapperFieldName}.updateWithNull(${dtoVar}, ${entityVar});
    ${EntityFqn} result${EntityName} = ${repoFieldName}.save(${entityVar});
    return ${mapperFieldName}.${toDtoMethodName}(result${EntityName});
}
```

### no DTO, ObjectPatcher available, NO validation lib
```java
@org.springframework.web.bind.annotation.PatchMapping("/{id}")
public ${EntityFqn} patch(@org.springframework.web.bind.annotation.PathVariable ${IdType} id, @org.springframework.web.bind.annotation.RequestBody ${JsonNodeFqn} patchNode) {
    ${EntityFqn} ${entityVar} = ${repoFieldName}.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    ${entityVar} = objectPatcher.patch(${entityVar}, patchNode);
    return ${repoFieldName}.save(${entityVar});
}
```

### no DTO, ObjectMapper fallback
```java
@org.springframework.web.bind.annotation.PatchMapping("/{id}")
public ${EntityFqn} patch(@org.springframework.web.bind.annotation.PathVariable ${IdType} id, @org.springframework.web.bind.annotation.RequestBody ${JsonNodeFqn} patchNode) throws java.io.IOException {
    ${EntityFqn} ${entityVar} = ${repoFieldName}.findById(id).orElseThrow(() ->
        new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

    objectMapper.readerForUpdating(${entityVar}).readValue(patchNode);

    return ${repoFieldName}.save(${entityVar});
}
```

### with DTO + mapper, ObjectMapper fallback
```java
@org.springframework.web.bind.annotation.PatchMapping("/{id}")
public ${DtoFqn} patch(@org.springframework.web.bind.annotation.PathVariable ${IdType} id, @org.springframework.web.bind.annotation.RequestBody ${JsonNodeFqn} patchNode) throws java.io.IOException {
    ${EntityFqn} ${entityVar} = ${repoFieldName}.findById(id).orElseThrow(() ->
        new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

    ${DtoFqn} ${dtoVar} = ${mapperFieldName}.${toDtoMethodName}(${entityVar});
    objectMapper.readerForUpdating(${dtoVar}).readValue(patchNode);
    ${mapperFieldName}.updateWithNull(${dtoVar}, ${entityVar});

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
| `${dtoVar}` | decapitalized DTO name | -- |
| `${IdType}` | entity ID type | -- |
| `${repoFieldName}` | repository field name | -- |
| `${entityVar}` | decapitalized entity name | -- |
| `${mapperFieldName}` | mapper field name | -- |
| `${toDtoMethodName}` | mapper entity->DTO method | `toDto` |
| `${JsonNodeFqn}` | Jackson JsonNode FQN | `com.fasterxml.jackson.databind.JsonNode` (Jackson 2.x) or `tools.jackson.databind.JsonNode` (Jackson 3.x) |
