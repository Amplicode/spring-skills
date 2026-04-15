# DELETE_MANY method (Java)

## Insert Point
As new method in controller class body, after last method.

## Code

### defaults
```java
@org.springframework.web.bind.annotation.DeleteMapping
public void deleteMany(@org.springframework.web.bind.annotation.RequestParam java.util.List<${IdType}> ids) {
    ${repoFieldName}.deleteAllById(ids);
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `${IdType}` | entity ID type (boxed) | -- |
| `${repoFieldName}` | repository field name | -- |
