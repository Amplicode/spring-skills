# GET_LIST method (Java)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No filter, PAGE pagination, no DTO, springdoc available:
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.domain.Page<${EntityFqn}> getAll(@org.springdoc.core.annotations.ParameterObject org.springframework.data.domain.Pageable pageable) {
    return ${repoFieldName}.findAll(pageable);
}
```

### no filter, PAGE pagination, with DTO
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.domain.Page<${DtoFqn}> getAll(@org.springdoc.core.annotations.ParameterObject org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.domain.Page<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAll(pageable);
    return ${entityVarPlural}.map(${mapperFieldName}::${toDtoMethodName});
}
```

### with filter, PAGE pagination, no DTO
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.domain.Page<${EntityFqn}> getAll(@org.springframework.web.bind.annotation.ModelAttribute @org.springdoc.core.annotations.ParameterObject ${FilterFqn} filter, @org.springdoc.core.annotations.ParameterObject org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.jpa.domain.Specification<${EntityFqn}> spec = filter.${toSpecificationMethodName}();
    return ${repoFieldName}.findAll(spec, pageable);
}
```

### no filter, no pagination, no DTO
```java
@org.springframework.web.bind.annotation.GetMapping
public java.util.List<${EntityFqn}> getAll() {
    return ${repoFieldName}.findAll();
}
```

### no filter, no pagination, with DTO
```java
@org.springframework.web.bind.annotation.GetMapping
public java.util.List<${DtoFqn}> getAll() {
    java.util.List<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAll();
    return ${entityVarPlural}.stream()
        .map(${mapperFieldName}::${toDtoMethodName})
        .toList();
}
```

### no filter, PAGE pagination, no DTO, no springdoc
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.domain.Page<${EntityFqn}> getAll(org.springframework.data.domain.Pageable pageable) {
    return ${repoFieldName}.findAll(pageable);
}
```

### no filter, PAGE pagination (PagedModel), no DTO, springdoc available
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.web.PagedModel<${EntityFqn}> getAll(@org.springdoc.core.annotations.ParameterObject org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.domain.Page<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAll(pageable);
    return new org.springframework.data.web.PagedModel<>(${entityVarPlural});
}
```

### no filter, PAGE pagination (PagedModel), no DTO, no springdoc
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.web.PagedModel<${EntityFqn}> getAll(org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.domain.Page<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAll(pageable);
    return new org.springframework.data.web.PagedModel<>(${entityVarPlural});
}
```

### no filter, PAGE pagination (PagedModel), with DTO, no springdoc
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.web.PagedModel<${DtoFqn}> getAll(org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.domain.Page<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAll(pageable);
    org.springframework.data.domain.Page<${DtoFqn}> dtoPage = ${entityVarPlural}.map(${mapperFieldName}::${toDtoMethodName});
    return new org.springframework.data.web.PagedModel<>(dtoPage);
}
```

### with filter, PAGE pagination (PagedModel), no DTO
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.web.PagedModel<${EntityFqn}> getAll(@org.springframework.web.bind.annotation.ModelAttribute ${FilterFqn} filter, org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.jpa.domain.Specification<${EntityFqn}> spec = filter.${toSpecificationMethodName}();
    org.springframework.data.domain.Page<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAll(spec, pageable);
    return new org.springframework.data.web.PagedModel<>(${entityVarPlural});
}
```

### with filter, PAGE pagination (PagedModel), with DTO
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.web.PagedModel<${DtoFqn}> getAll(@org.springframework.web.bind.annotation.ModelAttribute ${FilterFqn} filter, org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.jpa.domain.Specification<${EntityFqn}> spec = filter.${toSpecificationMethodName}();
    org.springframework.data.domain.Page<${EntityFqn}> ${entityVarPlural} = ${repoFieldName}.findAll(spec, pageable);
    org.springframework.data.domain.Page<${DtoFqn}> dtoPage = ${entityVarPlural}.map(${mapperFieldName}::${toDtoMethodName});
    return new org.springframework.data.web.PagedModel<>(dtoPage);
}
```

### with filter, WINDOW pagination, no DTO
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.domain.Window<${EntityFqn}> getAll(@org.springframework.web.bind.annotation.ModelAttribute ${FilterFqn} filter, org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.jpa.domain.Specification<${EntityFqn}> spec = filter.${toSpecificationMethodName}();
    return ${repoFieldName}.findBy(spec, fluentQuery -> fluentQuery
        .sortBy(pageable.getSort())
        .limit(pageable.getPageSize())
        .scroll(pageable.toScrollPosition()));
}
```

### with filter, WINDOW pagination, with DTO
```java
@org.springframework.web.bind.annotation.GetMapping
public org.springframework.data.domain.Window<${DtoFqn}> getAll(@org.springframework.web.bind.annotation.ModelAttribute ${FilterFqn} filter, org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.jpa.domain.Specification<${EntityFqn}> spec = filter.${toSpecificationMethodName}();
    org.springframework.data.domain.Window<${EntityFqn}> window = ${repoFieldName}.findBy(spec, fluentQuery -> fluentQuery
        .sortBy(pageable.getSort())
        .limit(pageable.getPageSize())
        .scroll(pageable.toScrollPosition()));
    return window.map(${mapperFieldName}::${toDtoMethodName});
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${EntityFqn}` | entity FQN | -- |
| `${DtoFqn}` | DTO FQN | -- |
| `${repoFieldName}` | repository field name | -- |
| `${mapperFieldName}` | mapper field name | -- |
| `${toDtoMethodName}` | mapper entity->DTO method | `toDto` |
| `${FilterFqn}` | filter class FQN | -- |
| `${toSpecificationMethodName}` | filter->Specification method | -- |
| `${entityVarPlural}` | pluralized entity var | -- |
