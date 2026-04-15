# PATCH_MANY method (Java)

## Insert Point
As new method in controller class body, after last method.

## Note on @Valid
When `hasValidation = true`, add `@jakarta.validation.Valid` before the `ids` parameter in all variants below.
Example: `@RequestParam @jakarta.validation.Valid java.util.List<${IdType}> ids`

## Code

### defaults
No DTO, ObjectPatcher available:
```java
@org.springframework.web.bind.annotation.PatchMapping
public java.util.List<${IdType}> patchMany(@org.springframework.web.bind.annotation.RequestParam java.util.List<${IdType}> ids, @org.springframework.web.bind.annotation.RequestBody ${JsonNodeFqn} patchNode) {
    java.util.List<${EntityFqn}> ${entityVarPlural} = new java.util.ArrayList<>(${repoFieldName}.findAllById(ids));
    ${entityVarPlural}.replaceAll(${entityVar} -> objectPatcher.patchAndValidate(${entityVar}, patchNode));
    java.util.List<${EntityFqn}> result${EntityNamePlural} = ${repoFieldName}.saveAll(${entityVarPlural});
    return result${EntityNamePlural}.stream().map(${EntityFqn}::getId).toList();
}
```

### with DTO + mapper, ObjectPatcher available
```java
@org.springframework.web.bind.annotation.PatchMapping
public java.util.List<${IdType}> patchMany(@org.springframework.web.bind.annotation.RequestParam java.util.List<${IdType}> ids, @org.springframework.web.bind.annotation.RequestBody ${JsonNodeFqn} patchNode) {
    java.util.Collection<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAllById(ids);
    for (${EntityFqn} ${entityVar} : ${entityVarPlural}) {
        ${DtoFqn} ${dtoVar} = ${mapperFieldName}.${toDtoMethodName}(${entityVar});
        ${dtoVar} = objectPatcher.patchAndValidate(${dtoVar}, patchNode);
        ${mapperFieldName}.updateWithNull(${dtoVar}, ${entityVar});
    }
    java.util.List<${EntityFqn}> result${EntityNamePlural} = ${repoFieldName}.saveAll(${entityVarPlural});
    return result${EntityNamePlural}.stream().map(${EntityFqn}::getId).toList();
}
```

### with DTO + mapper, ObjectMapper fallback
```java
@org.springframework.web.bind.annotation.PatchMapping
public java.util.List<${IdType}> patchMany(@org.springframework.web.bind.annotation.RequestParam java.util.List<${IdType}> ids, @org.springframework.web.bind.annotation.RequestBody ${JsonNodeFqn} patchNode) throws java.io.IOException {
    java.util.Collection<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAllById(ids);

    for (${EntityFqn} ${entityVar} : ${entityVarPlural}) {
        ${DtoFqn} ${dtoVar} = ${mapperFieldName}.${toDtoMethodName}(${entityVar});
        objectMapper.readerForUpdating(${dtoVar}).readValue(patchNode);
        ${mapperFieldName}.updateWithNull(${dtoVar}, ${entityVar});
    }

    java.util.List<${EntityFqn}> result${EntityNamePlural} = ${repoFieldName}.saveAll(${entityVarPlural});
    return result${EntityNamePlural}.stream()
        .map(${EntityFqn}::getId)
        .toList();
}
```

### no DTO, ObjectMapper fallback
```java
@org.springframework.web.bind.annotation.PatchMapping
public java.util.List<${IdType}> patchMany(@org.springframework.web.bind.annotation.RequestParam java.util.List<${IdType}> ids, @org.springframework.web.bind.annotation.RequestBody ${JsonNodeFqn} patchNode) throws java.io.IOException {
    java.util.Collection<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAllById(ids);

    for (${EntityFqn} ${entityVar} : ${entityVarPlural}) {
        objectMapper.readerForUpdating(${entityVar}).readValue(patchNode);
    }

    java.util.List<${EntityFqn}> result${EntityNamePlural} = ${repoFieldName}.saveAll(${entityVarPlural});
    return result${EntityNamePlural}.stream()
        .map(${EntityFqn}::getId)
        .toList();
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${EntityFqn}` | entity FQN | -- |
| `${EntityNamePlural}` | pluralized entity name | -- |
| `${DtoFqn}` | DTO FQN | -- |
| `${dtoVar}` | decapitalized DTO name | -- |
| `${IdType}` | entity ID type (boxed) | -- |
| `${repoFieldName}` | repository field name | -- |
| `${entityVar}` | decapitalized entity name | -- |
| `${entityVarPlural}` | pluralized entity var | -- |
| `${mapperFieldName}` | mapper field name | -- |
| `${toDtoMethodName}` | mapper entity->DTO method | `toDto` |
| `${JsonNodeFqn}` | Jackson JsonNode FQN | `com.fasterxml.jackson.databind.JsonNode` (Jackson 2.x) or `tools.jackson.databind.JsonNode` (Jackson 3.x) |
