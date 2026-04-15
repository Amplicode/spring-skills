# Serializable (Java)

## Insert Point
Modifies class declaration: adds `implements java.io.Serializable`.

## Code

```defaults
// Default: skip (depends on project config serializableType)
```

**SerializableType.Serializable:**
```java
public class {className} implements java.io.Serializable {
}
```

**SerializableType.SerializableWithVersionUID:**
```java
public class {className} implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
}
```

Note: the serialVersionUID value is computed from the entity class. Use `1L` as default.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | DTO class name | -- |
