# Serializable (Kotlin)

## Insert Point
Modifies class declaration: adds `: java.io.Serializable` supertype.

## Code

```defaults
// Default: skip (depends on project config serializableType)
```

**SerializableType.Serializable:**
```kotlin
data class {className}() : java.io.Serializable
```

**SerializableType.SerializableWithVersionUID:**
```kotlin
data class {className}() : java.io.Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
```

Note: the serialVersionUID value is computed from the entity class. Use `1L` as default.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | DTO class name | -- |
