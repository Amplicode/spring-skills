# Parent interface supertypes clause (Kotlin)

## Insert Point
Modifies the abstract class declaration to add supertype.

## Code

```defaults
skip this fragment (no parent interface by default)
```

### When parent interface selected

```kotlin
abstract class {className} : {parentFqn}<{dtoClassFqn}, {entityClassFqn}>() {
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{className}` | user choice | `{EntityName}Mapper` |
| `{parentFqn}` | user choice | — |
| `{dtoClassFqn}` | DTO class FQN | — |
| `{entityClassFqn}` | entity class FQN | — |
