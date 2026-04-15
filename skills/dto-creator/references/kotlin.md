# Kotlin DTO

## Conditions
- language = KOTLIN

## Generation Order

1. **Skeleton:** Read `examples/_skeletons/kotlin/data-class.md` (default, isKotlinDataClass=true) or `examples/_skeletons/kotlin/class.md` (isKotlinDataClass=false — rare)
2. **KDoc:** Always emit a 3-line block KDoc above the class declaration referencing the entity by **fully-qualified name** in `[…]` form. Same package does NOT shorten — Kotlin uses FQN, not short-name+import (see `references/javadoc.md` § Kotlin).
3. **Constructor parameters:** Read `examples/_fragments/fields/kotlin.md` — add as primary constructor parameters
4. **Nested classes** (for each attribute with subDtoType=NEW_NESTED_CLASS): Read `examples/_fragments/nested-class/kotlin/nested-class.md`
5. **Serializable** (only when explicitly requested via serializableType): Read `examples/_fragments/serializable/kotlin/serializable.md`. Default for Kotlin is **no** Serializable supertype.

## Variant-Specific Questions

Prefer `AskUserQuestion`; fall back to plain text only if the tool is unavailable.

| Question | Header | Options (first = default) |
|----------|--------|--------------------------|
| Mutable DTO (var instead of val)? | Mutable | No (Recommended) / Yes |

The mutable variant only swaps `val` → `var` on every primary-constructor parameter — nothing else changes (no no-args constructor, no fluent setters, no separate getters/setters; data-class semantics still apply).

## Notes

- Kotlin data classes generate `equals()`, `hashCode()`, `toString()`, `copy()`, `componentN()` automatically. The skill should NOT ask about them and should NOT emit hand-written equals/hashCode/toString.
- `isKotlinDataClass=true` is the default — every Kotlin DTO is a `data class`. Only emit a plain `class` if the user explicitly asks.
- Validation annotations always use `@field:` target prefix in Kotlin, **short-name + import**, NOT FQN: `@field:NotEmpty`, not `@field:jakarta.validation.constraints.NotEmpty`.
- **Multiple validators on one field stay on one line** with the field, space-separated: `@field:Digits(integer = 10, fraction = 0) @field:NotEmpty val telephone: String`. Do NOT line-break per validator.
- **Validator parameter order** is canonical (declaration order from the annotation type), not the order they appeared on the entity. For `@Digits` it is `integer`, then `fraction`.
- **Empty validator params → bare annotation** (no parentheses): `@field:NotEmpty`, not `@field:NotEmpty()`. Same rule as Java; see `references/validation.md`.
- Java types are converted to Kotlin types: `java.lang.String` → `String`, `java.lang.Long` → `Long`, `java.lang.Integer` → `Int`, etc.
- **Collection types preserve the entity declaration**: if the entity field is `MutableSet<Pet>`, the DTO field is `MutableSet<PetDto>` (or `MutableSet<Int>` for Flat-id). Do NOT force `List` — read the entity collection type and use it as-is.
- Nullable types get `?` suffix: `String?`. Default is non-null.
- **Composite naming for Flat-id sub-DTOs** (singular sub-attribute name + `Id`/`Ids` suffix): `type: PetType` (Flat, only `id` selected) → `val typeId: Int`; `pets: MutableSet<Pet>` → `val petIds: MutableSet<Int>`; `specialties: MutableSet<Specialty>` → `val specialtyIds: MutableSet<Int>`. Singular comes from the association name, not the target entity.
- **Auto-suffix on collision**: if `OwnerDto` already exists in the target package, the next generation uses `OwnerDto1`, then `OwnerDto2`, etc. Same rule as Java (`references/java-record.md` § Auto-suffix).
- **Nested data classes are usually single-line** when the constructor only contains short scalars: `data class PetDto(val id: Int, val name: String, val birthDate: LocalDate)`. Multi-line form is used when the parent class has validation, long parameter lists, or sub-attributes that themselves wrap.
- **Indentation: 4 spaces** inside the constructor parameter list. No leading column.
