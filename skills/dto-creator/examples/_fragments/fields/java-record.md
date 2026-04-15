# Java record components

## Insert Point
Replace the empty record header `()` in the record declaration.

## Code

```defaults
// Default: record components from all selected attributes
```

Generate record components joined by `, `:

```java
public record {className}({validationAnnotations}{Type1} {field1}, {validationAnnotations}{Type2} {field2}) {
}
```

**With validation annotations** (inline before type):
```java
public record {className}(@jakarta.validation.constraints.NotNull java.lang.String name, java.lang.Long id) {
}
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | user choice | -- |
| `{fieldN}` | entity attribute name | -- |
| `{TypeN}` | entity attribute type FQN | -- |
| `{validationAnnotations}` | from entity validation annotations, inline before type | empty |
