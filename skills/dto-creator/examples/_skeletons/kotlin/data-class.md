# Kotlin data class skeleton

## Code

```defaults
// Default: Kotlin data class, KDoc with FQN reference, NO Serializable supertype
```

```kotlin
package {packageName}

{importsBlock}

/**
 * DTO for [{entityFqn}]
 */
data class {className}()
```

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{packageName}` | project context | -- |
| `{className}` | user choice | `{EntityName}Dto` (auto-suffixed `Dto1`, `Dto2`, … on collision) |
| `{entityFqn}` | source entity FQN | -- |
| `{importsBlock}` | union of all imports actually used by fields/validators/sub-DTOs, blank line after last import | -- |

## Notes

- KDoc is **always** present and uses the **fully-qualified** entity name in `[…]` form, even when the DTO sits in the same package as the entity. Kotlin does NOT shorten + import the entity reference (unlike Java's `{@link}` rule).
- The skeleton above is the *empty* shape; constructor parameters are added by `_fragments/fields/kotlin.md`.
- For nested data classes, the same skeleton applies recursively but is usually emitted in single-line form (see `_fragments/nested-class/kotlin/nested-class.md`).
- Do not emit `: java.io.Serializable` by default — leave it off unless the user explicitly opts in via serializableType.
