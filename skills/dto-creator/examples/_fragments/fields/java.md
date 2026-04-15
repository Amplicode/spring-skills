# Java fields (non-record, non-Lombok)

## Insert Point
Inside the class body, as field declarations.

## Code

```defaults
// Default: immutable fields (private final).
// Indentation: Use the project's detected indent unit (see `SKILL.md` § Indentation).
// Each field line is indented with one unit. Validation annotations sit on
// their own lines, also indented with one unit. No blank lines between fields.
```

For each selected attribute, generate a field:

**Immutable (isMutable=false, default):**
```java
{validationAnnotations}private final {Type} {fieldName};
```

**Mutable (isMutable=true):**
```java
{validationAnnotations}private {Type} {fieldName};
```

**Mutable with entity initializer:**
```java
{validationAnnotations}private {Type} {fieldName} = {entityInitializer};
```

**Validation annotations** -- each on separate line before the field:
```java
@jakarta.validation.constraints.NotNull
@jakarta.validation.constraints.Size(min = 0, max = 255)
private final java.lang.String name;
```

For **collection** types, wrap: `java.util.List<{InnerType}>`, `java.util.Set<{InnerType}>`.

For **sub-DTO** attributes (subDtoType != FLAT), use the DTO FQN as the type instead of entity FQN.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{fieldName}` | entity attribute name | -- |
| `{Type}` | entity attribute type FQN (or DTO FQN for sub-DTOs), wrapped in collection if applicable | -- |
| `{validationAnnotations}` | from entity validation annotations, each `@fqn` on separate line | empty |
| `{entityInitializer}` | from entity field initializer (only when mutable) | none |
