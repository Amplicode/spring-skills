# GET_LIST method (Kotlin)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
No filter, PAGE pagination, no DTO, springdoc available:
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(@org.springdoc.core.annotations.ParameterObject pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<${EntityFqn}> =
    ${repoFieldName}.findAll(pageable)
```

### no filter, PAGE pagination, with DTO
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(@org.springdoc.core.annotations.ParameterObject pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<${DtoFqn}> {
    val ${entityVarPlural}: org.springframework.data.domain.Page<${EntityFqn}> = ${repoFieldName}.findAll(pageable)
    return ${entityVarPlural}.map(${mapperFieldName}::${toDtoMethodName})
}
```

### no filter, no pagination, no DTO
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(): List<${EntityFqn}> =
    ${repoFieldName}.findAll()
```

### no filter, PAGE pagination, no DTO, no springdoc
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<${EntityFqn}> =
    ${repoFieldName}.findAll(pageable)
```

### no filter, no pagination, with DTO
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(): List<${DtoFqn}> {
    val ${entityVarPlural} = ${repoFieldName}.findAll()
    return ${entityVarPlural}.map(${mapperFieldName}::${toDtoMethodName})
}
```

### no filter, PAGE pagination (PagedModel), no DTO, no springdoc
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(pageable: org.springframework.data.domain.Pageable): org.springframework.data.web.PagedModel<${EntityFqn}> {
    val ${entityVarPlural} = ${repoFieldName}.findAll(pageable)
    return org.springframework.data.web.PagedModel(${entityVarPlural})
}
```

### no filter, PAGE pagination (PagedModel), with DTO, no springdoc
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(pageable: org.springframework.data.domain.Pageable): org.springframework.data.web.PagedModel<${DtoFqn}> {
    val ${entityVarPlural} = ${repoFieldName}.findAll(pageable)
    val dtoPage = ${entityVarPlural}.map(${mapperFieldName}::${toDtoMethodName})
    return org.springframework.data.web.PagedModel(dtoPage)
}
```

### with filter, PAGE pagination, no DTO
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(@org.springframework.web.bind.annotation.ModelAttribute filter: ${FilterFqn}, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<${EntityFqn}> {
    val spec = filter.${toSpecificationMethodName}()
    return ${repoFieldName}.findAll(spec, pageable)
}
```

### with filter, PAGE pagination (PagedModel), no DTO
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(@org.springframework.web.bind.annotation.ModelAttribute filter: ${FilterFqn}, pageable: org.springframework.data.domain.Pageable): org.springframework.data.web.PagedModel<${EntityFqn}> {
    val spec = filter.${toSpecificationMethodName}()
    val ${entityVarPlural} = ${repoFieldName}.findAll(spec, pageable)
    return org.springframework.data.web.PagedModel(${entityVarPlural})
}
```

### with filter, PAGE pagination (PagedModel), with DTO
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(@org.springframework.web.bind.annotation.ModelAttribute filter: ${FilterFqn}, pageable: org.springframework.data.domain.Pageable): org.springframework.data.web.PagedModel<${DtoFqn}> {
    val spec = filter.${toSpecificationMethodName}()
    val ${entityVarPlural} = ${repoFieldName}.findAll(spec, pageable)
    val dtoPage = ${entityVarPlural}.map(${mapperFieldName}::${toDtoMethodName})
    return org.springframework.data.web.PagedModel(dtoPage)
}
```

### with filter, WINDOW pagination, no DTO
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(@org.springframework.web.bind.annotation.ModelAttribute filter: ${FilterFqn}, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Window<${EntityFqn}> {
    val spec = filter.${toSpecificationMethodName}()
    return ${repoFieldName}.findBy(spec) { fluentQuery ->
        fluentQuery
            .sortBy(pageable.sort)
            .limit(pageable.pageSize)
            .scroll(pageable.toScrollPosition())
    }
}
```

### with filter, WINDOW pagination, with DTO
```kotlin
@org.springframework.web.bind.annotation.GetMapping
fun getAll(@org.springframework.web.bind.annotation.ModelAttribute filter: ${FilterFqn}, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Window<${DtoFqn}> {
    val spec = filter.${toSpecificationMethodName}()
    val window = ${repoFieldName}.findBy(spec) { fluentQuery ->
        fluentQuery
            .sortBy(pageable.sort)
            .limit(pageable.pageSize)
            .scroll(pageable.toScrollPosition())
    }
    return window.map(${mapperFieldName}::${toDtoMethodName})
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
