# Validation Annotations

## When Applied

Validation annotations are emitted on DTO fields when Hibernate Validator (or
Jakarta Validation) is on the classpath. There are **two sources** of
annotations:

1. **Inherited from the entity field** — every constraint already declared on
   the corresponding entity attribute. The skill must copy them by default.
2. **User-added overrides** — the user can ask the skill to add extra
   constraints (`@Email`, `@Size`, `@Length`, `@Pattern`, …), tweak parameters
   (`message`, `min`, `max`, `regexp`, …), or **remove** constraints inherited
   from the entity.

## Field rename

The skill must offer per-field rename functionality: accept an optional
`fieldNameOverride` per attribute and use it as the DTO field name (the
mapper still maps it from the original entity attribute).

## Java Format

Each annotation on a separate line before the field:
```java
@jakarta.validation.constraints.NotNull
@jakarta.validation.constraints.Size(min = 0, max = 255)
private final String name;
```

For records, inline before the component type:
```java
public record MyDto(@jakarta.validation.constraints.NotNull String name) {
}
```

## Kotlin Format

Use `@field:` target prefix:
```kotlin
@field:jakarta.validation.constraints.NotNull @field:jakarta.validation.constraints.Size(min = 0, max = 255) val name: String
```

## Available Annotations

Different constraint types are available per field type. The skill must respect the
same scoping when offering "extra" constraints — do not suggest `@Email` on a
`BigDecimal`.

**String:**
- `jakarta.validation.constraints.NotNull`
- `jakarta.validation.constraints.NotEmpty`
- `jakarta.validation.constraints.NotBlank`
- `jakarta.validation.constraints.Size` (min, max, message)
- `jakarta.validation.constraints.Pattern` (regexp, flags, message)
- `jakarta.validation.constraints.Email` (regexp, message)
- `jakarta.validation.constraints.Digits` (integer, fraction, message)
- `org.hibernate.validator.constraints.Length` (min, max, message)
- `org.hibernate.validator.constraints.CodePointLength`
- `org.hibernate.validator.constraints.URL`
- `org.hibernate.validator.constraints.CreditCardNumber`
- `org.hibernate.validator.constraints.LuhnCheck`

**Number / Integer / BigDecimal:**
- `jakarta.validation.constraints.NotNull`
- `jakarta.validation.constraints.Min` (value)
- `jakarta.validation.constraints.Max` (value)
- `jakarta.validation.constraints.Digits` (integer, fraction)
- `jakarta.validation.constraints.Positive`
- `jakarta.validation.constraints.PositiveOrZero`
- `jakarta.validation.constraints.Negative`
- `jakarta.validation.constraints.NegativeOrZero`
- `org.hibernate.validator.constraints.Range` (min, max)

**Date / Temporal:**
- `jakarta.validation.constraints.NotNull`
- `jakarta.validation.constraints.Past`
- `jakarta.validation.constraints.PastOrPresent`
- `jakarta.validation.constraints.Future`
- `jakarta.validation.constraints.FutureOrPresent`

**Collections / association references:**
- `jakarta.validation.constraints.NotNull`
- `jakarta.validation.constraints.Size` (min, max)

## Asking the user about per-field overrides

After Step 3 (attribute selection), if the user did NOT say "all defaults",
offer per-field customisation via `AskUserQuestion` (batched, one call per
attribute — or skip when the user passes, defaults inherit from the entity).

Use `AskUserQuestion` with up to 3 questions per attribute:
- "Rename field `{fieldName}` in DTO?" (options: No / Yes — enter name)
- "Add validations to `{fieldName}`?" (multiSelect, options by field type)
- "Remove inherited validations?" (multiSelect, options = inherited list)

Prefer `AskUserQuestion`; fall back to plain text only if the tool is unavailable.

Skip the question entirely if the entity field has no constraints AND the
user already said "use defaults".

## Rules

- Inherited annotations are kept by default; the user may remove them.
- User-added annotations are merged with inherited ones (no duplicates).
- Use FQN in fragment text — they will be shortened + imported by Step 6.4.
- Preserve parameter ordering exactly as in the source / user request
  (use `message, regexp` for `@Pattern`, not the alphabetical
  order from the entity).
- If neither entity nor user provides any constraint, generate no
  annotations on the field.
- **Empty parameter handling.** When the user adds a constraint and leaves
  every parameter blank (e.g. `@Size` with no `min`, `max`, or `message`),
  emit the bare annotation **without parentheses**: `@Size`, not
  `@Size()`. Same rule for `@Pattern`, `@Min`, `@Max`, `@Digits`, etc. —
  any constraint with all-blank params collapses to its bare form. As soon
  as at least one parameter has a value, switch to the
  `@Annotation(name = value, …)` form and include only the parameters
  that have values.
