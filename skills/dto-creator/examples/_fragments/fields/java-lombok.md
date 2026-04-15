# Java fields (Lombok mode)

## Insert Point
Inside the class body, as field declarations.

## Code

```defaults
// Default: no access/final modifiers (Lombok handles them via @Value/@Data/@Getter)
```

For each selected attribute, generate a field:

**Default (Lombok manages access and mutability):**
```java
{validationAnnotations}{Type} {fieldName};
```

**Validation annotations** -- each on separate line before the field:
```java
@jakarta.validation.constraints.NotNull
@jakarta.validation.constraints.Size(min = 0, max = 255)
java.lang.String name;
```

For **collection** types, wrap: `java.util.List<{InnerType}>`, `java.util.Set<{InnerType}>`.

For **sub-DTO** attributes (subDtoType != FLAT), use the DTO FQN as the type instead of entity FQN.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{fieldName}` | entity attribute name | -- |
| `{Type}` | entity attribute type FQN (or DTO FQN for sub-DTOs), wrapped in collection if applicable | -- |
| `{validationAnnotations}` | from entity validation annotations, each `@fqn` on separate line | empty |
