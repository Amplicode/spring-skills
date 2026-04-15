# Kotlin constructor parameters

## Insert Point
As primary constructor parameters of the data class / class.

## Code

```defaults
// Default: immutable (val), non-nullable, 4-space indented multi-line for top-level data classes
```

For each selected attribute, generate a constructor parameter. The line is **indented 4 spaces** inside the constructor parameter list, and parameters are comma-separated with one per line for top-level data classes.

**Immutable, non-nullable (default):**
```kotlin
    val {fieldName}: {Type},
```

**Mutable** (when "Mutable fields" is selected — swaps every `val` to `var`, nothing else changes):
```kotlin
    var {fieldName}: {Type},
```

**Nullable:**
```kotlin
    val {fieldName}: {Type}?,
```

**With validation annotations** — short name + import (NOT FQN), `@field:` target prefix, on the **same line** as the field declaration:
```kotlin
    @field:NotEmpty val {fieldName}: {Type},
```

**Multiple validation annotations on one field** — space-separated, all on the same line as the field:
```kotlin
    @field:Digits(integer = 10, fraction = 0) @field:NotEmpty val {fieldName}: {Type},
```

The validator parameters are written in the annotation type's declaration order (e.g. `@Digits(integer, fraction)`), not the order they appeared in the source entity. Empty validator parameter set → bare annotation without parentheses (`@field:NotEmpty`, never `@field:NotEmpty()`).

For **collection** types: preserve the entity's collection type. If the entity field is `MutableSet<Pet>`, the DTO field is `MutableSet<PetDto>` (or `MutableSet<Int>` for Flat-id). Do NOT force `List`. `Set`, `MutableSet`, `List`, `MutableList` are all valid depending on the entity declaration.

For **sub-DTO** attributes:
- `subDtoType=NEW_NESTED_CLASS`: type is the nested class short name (e.g. `MutableSet<PetDto>`).
- `subDtoType=NEW_CLASS`: type is the separate-file DTO short name (with import).
- `subDtoType=EXIST_CLASS`: type is the existing DTO short name (with import unless same package).
- `subDtoType=FLAT` with only `id` selected: composite name `{singularAssociationName}Id` (singular) for ToOne, `{singularAssociationName}Ids` for ToMany. Type is `Int` / `MutableSet<Int>` / `MutableList<Int>` / `Set<Int>` / `List<Int>` matching the entity collection.
- `subDtoType=FLAT` with multiple sub-attributes selected: composite per-attribute names like `typeName: String`, `typeId: Int`.

**Java-to-Kotlin type conversion:** `java.lang.String` → `kotlin.String`, `java.lang.Long` → `kotlin.Long`, `java.lang.Integer` → `kotlin.Int`, `java.lang.Boolean` → `kotlin.Boolean`, `java.math.BigDecimal` stays `java.math.BigDecimal`, `java.time.LocalDate` stays `java.time.LocalDate`.

## Single-line vs multi-line form

- **Top-level data class with validations or > 3 short scalars:** multi-line, 4-space indent, one parameter per line, no trailing comma.
- **Nested data class with only short scalars and no validators:** single-line, all parameters on the same line as the `data class Foo(...)` declaration. See `_fragments/nested-class/kotlin/nested-class.md`.

## Variables
| Variable | Source | Default |
|----------|--------|---------|
| `{fieldName}` | entity attribute name (or composite Flat-id name) | -- |
| `{Type}` | entity attribute Kotlin type (preserving collection type and nullability) | -- |
| `{validationAnnotations}` | from entity validation annotations (or user overrides), each `@field:{ShortName}` inline on the same line as the field | empty |
