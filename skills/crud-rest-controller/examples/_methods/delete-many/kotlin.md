# DELETE_MANY method (Kotlin)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
```kotlin
@org.springframework.web.bind.annotation.DeleteMapping
fun deleteMany(@org.springframework.web.bind.annotation.RequestParam ids: List<${IdType}>) {
    ${repoFieldName}.deleteAllById(ids)
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${IdType}` | entity ID type (boxed) | -- |
| `${repoFieldName}` | repository field name | -- |
