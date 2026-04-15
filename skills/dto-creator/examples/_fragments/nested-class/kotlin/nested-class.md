# Nested data class (Kotlin)

## Insert Point
As a nested `data class` inside the parent DTO body. Indented 4 spaces.

## Code

```defaults
// Default: single-line nested data class with KDoc; multi-line only if children themselves wrap or carry validators
```

**Single-line form (preferred when only short scalars, no validators):**
```kotlin
    /**
     * DTO for [{entityFqn}]
     */
    data class {subDtoName}(val {field1}: {Type1}, val {field2}: {Type2}, val {field3}: {Type3})
```

**Multi-line form (when validators are present, or constructor would be too long):**
```kotlin
    /**
     * DTO for [{entityFqn}]
     */
    data class {subDtoName}(
        val {field1}: {Type1},
        @field:NotEmpty val {field2}: {Type2},
        val {field3}: {Type3}
    )
```

## Notes

- KDoc is **always** present, also using FQN form `[fully.qualified.EntityName]` (same rule as the top-level class — no short-name + import shortcut).
- The nested class is `data class`, not `class` — Kotlin always emits data classes for nested DTOs too.
- Auto-suffix on collision still applies: if a `PetDto` already exists somewhere accessible (top-level in the same package, or another nested), use `PetDto1`, `PetDto2`, etc.
- When an association becomes a nested class, auto-include all scalar children and leave sub-associations out by default.
- Back-reference filtering: follow the same rule as in `SKILL.md` (the global anti-hallucination checklist).

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{subDtoName}` | sub-DTO class name from attribute (auto-suffixed on collision) | -- |
| `{entityFqn}` | sub-entity FQN | -- |
| `{field*}` / `{Type*}` | child constructor parameters generated recursively via `_fragments/fields/kotlin.md` | -- |
