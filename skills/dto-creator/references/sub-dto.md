# Sub-DTO Creation Rules

## SubDtoType Options

When an entity has association attributes (e.g. `@ManyToOne`, `@OneToOne`,
`@OneToMany`, `@ManyToMany`), each association can be handled differently.

**Four options are available, identical for ToOne and ToMany:**

| Option | SubDtoType | Notes |
|---|---|---|
| New Class | `NEW_CLASS` | separate file, sub-entity scalars auto-selected |
| New Nested Class | `NEW_NESTED_CLASS` | inner class in parent DTO, sub-entity scalars auto-selected |
| Existing Class | `EXIST_CLASS` | reuse an existing DTO class for the sub-entity |
| Flat | `FLAT` | inline sub-entity attributes into the parent (see below) |

**There is NO "Only ID" option** — not for ToOne, not
for ToMany, not at any nesting level. The skill must NEVER offer "Only ID"
as a separate option. The "association id only" effect is achieved via
**Flat with only the `id` sub-attribute checked** (see "Flat — id-only" below).

The 4 options are the same for ToOne and ToMany; the **only** ToOne/ToMany
difference lies in what Flat produces (composite singular field for ToOne,
composite plural collection for ToMany — see below).

## Auto-selection of sub-entity attributes when expanding

When the user picks **New Class**, **New Nested Class** or **Flat** on an
association, **auto-select every scalar attribute** of the sub-entity (id,
name, …), leaving the sub-entity's own associations unselected. The skill
must apply this default. The user is then free to drop individual
sub-attributes; in particular, keeping only `id` is the way to get the
"association id only" effect (see Flat below).

## Bulk-selection shortcuts

Two bulk-selection modes are supported when including the entity's
attributes:

1. **Basic attributes** — include all scalar (non-association) fields of
   the root entity. Associations are left out.
2. **Basic and association id attributes** — include all scalar fields
   **and** for every ToOne association, switch it to **Flat** with only
   the sub-entity's `id` selected. ToMany associations are left out.

Treat "include association IDs" as syntactic sugar for "Flat + only id
sub-attribute" — the underlying mechanism is the same.

## NEW_CLASS (Separate File)

Creates a new DTO file via recursive generation. The sub-DTO inherits ALL
parent options:
- equalsHashCode, toString, allArgsConstructor, isMutable, fluentSetters
- isJavaRecord, isJsonIgnoreUnknownProperties, serializableType

The field in the parent DTO uses the sub-DTO short name as its type, and an
`import` line for the sub-DTO is added.

The new file's class-level Javadoc uses the **short name + import** form
(see `examples/_fragments/javadoc/java/javadoc.md`). This is the **same form
as every other Javadoc** generated — there is no asymmetry between
top-level and sub-DTO Javadocs.

## NEW_NESTED_CLASS (Inner Class)

**Java non-record:**
```java
public static class {SubDtoName} {
	// fields, constructors, getters/setters -- same rules as parent
}
```

**Java record:**
```java
public record {SubDtoName}({Type1} {field1}, {Type2} {field2}) {
}
```

**Kotlin:**
```kotlin
data class {SubDtoName}(
    val {field1}: {Type1},
    val {field2}: {Type2}
)
```

For Java, also apply the Javadoc fragment
(`examples/_fragments/javadoc/java/javadoc.md`, "Nested class" variant)
above the `public static class …` declaration. The nested-class form uses
the short name `{@link Pet}` plus an `import` for the sub-entity FQN — same
as the top-level form.

## EXIST_CLASS (Reuse Existing DTO)

Use this option when the user already has a DTO for the sub-entity that they
want to reuse instead of generating a new one.

### How to detect candidates

For each association attribute that the skill is about to handle, call
`list_entity_dtos(subEntityFqn)` lazily — the result is the list of
EXIST_CLASS candidates for that association.

The call is per-association and on-demand: do not pre-fetch in Step 1
or Step 2 of `SKILL.md`. The parent entity itself is **not** called via
`list_entity_dtos` — there is no downstream consumer for that.

### How to apply EXIST_CLASS

- Field type in the parent DTO = the chosen existing DTO **short name**.
- Add an `import {existingDtoFqn};` line (unless the existing DTO is in the
  same package as the parent DTO).
- Do NOT generate a new file or a new nested class for this attribute.
- For collection associations (`List<Pet>`), the field type becomes
  `List<PetDto>` — the collection wrapper stays, only the element type is
  replaced.

### Sanity check

Before substituting, check that the candidate DTO actually targets the
sub-entity (its class-level Javadoc `{@link …}` should reference the
sub-entity). If `list_entity_dtos` returned a class that does NOT match,
fall back to NEW_NESTED_CLASS and warn the user.

## FLAT (Flatten) — works for BOTH ToOne and ToMany

Flat **inlines** the chosen sub-entity attributes into the parent DTO. The
exact result depends on cardinality and on which sub-attributes the user
left checked:

### Flat on ToOne (`@ManyToOne`, `@OneToOne`)

For each checked scalar sub-attribute, a field is added to the parent with
**composite singular naming**:

```
{associationName} + {SubAttributeName capitalised}
```

Examples (Pet → PetType):
- Sub-attributes checked: `id` only → parent gets `Integer typeId`
- Sub-attributes checked: `id`, `name` → parent gets `Integer typeId`, `String typeName`

If the user leaves only `id` checked under Flat, the result is the classic
"association id only" form. There is no separate option for this — Flat with
id-only IS the mechanism, and the "Basic + association id" bulk-selection
shortcut produces exactly this configuration automatically.

### Flat on ToMany (`@OneToMany`, `@ManyToMany`, `List<X>`, `Set<X>`)

For each checked scalar sub-attribute, a **collection** field is added to
the parent with **composite plural naming**:

```
singular({associationName}) + {SubAttributeName capitalised} + s   →   wrapped in the original collection type
```

Examples (Vet → Specialty, where the field is `specialties: Set<Specialty>`):
- Sub-attributes checked: `id` only → parent gets `Set<Integer> specialtyIds`
- Sub-attributes checked: `id`, `name` → parent gets `Set<Integer> specialtyIds` AND `Set<String> specialtyNames`

Examples (Owner → Pet, where the field is `pets: List<Pet>`):
- Sub-attributes checked: `id` only → parent gets `List<Integer> petIds`

The collection wrapper (`List`/`Set`) is preserved; only the element type
is replaced. The base name is the **singular** form of the association name
(`specialties → specialty`, `pets → pet`).

**Important:** Flat works correctly for collections, but only **after** the
user explicitly checks at least one sub-attribute. If the user picks Flat
without checking anything, no fields are generated for that association. The
skill must auto-check the sub-entity scalars when switching to Flat (same
behaviour as for NEW_NESTED_CLASS), so this corner case never occurs.

## Filtered fields — back-references

**Do NOT offer** back-reference `@ManyToOne` fields in the attribute list.
Concretely: `Pet` has an `Owner owner` field, but when creating `Pet` DTO,
only offer `id, name, birthDate, type, visits` — `owner` is excluded because
it points back to a parent entity that owns `Pet`.

The skill must apply this rule: when listing the entity's attributes via
`get_entity_details`, **drop any `@ManyToOne` field whose target entity also
has a `@OneToMany` collection of the current entity** (this is the parent side).
Do not offer such fields to the user.

## Asking the User

For each association attribute found via `get_entity_details` (after
filtering back-references), use `AskUserQuestion` with `preview` fields
showing the concrete code shape for each option.

### ToOne (`@ManyToOne`, `@OneToOne`)

Use `AskUserQuestion` with these options (show only Existing Class if
`list_entity_dtos` returned candidates):

| Option | Label | Description |
|--------|-------|-------------|
| Flat (Recommended) | `Flat — only {type}Id` | Most compact form: a single `{type}Id` field |
| New Nested Class | `Nested record/class` | Nested class with sub-entity scalars |
| New Class | `Separate file` | Separate DTO file for the sub-entity |
| Existing Class | `Existing class` | Reuse an existing DTO |

Default: **Flat with only `id` checked** — the most compact and common combination.

### ToMany (`@OneToMany`, `@ManyToMany`, `List<X>`, `Set<X>`)

Use `AskUserQuestion` with these options:

| Option | Label | Description |
|--------|-------|-------------|
| New Nested Class (Recommended) | `Nested record/class` | Nested class with sub-entity scalars |
| New Class | `Separate file` | Separate DTO file for the sub-entity |
| Existing Class | `Existing class` | Reuse an existing DTO |
| Flat | `Flat — scalar collection` | `{Collection}<{IdType}> {singular}Ids` etc. |

Default: **New Nested Class**.

In both cases, **never** offer "Only ID" as a separate option — that
option does not exist. It is achieved via Flat-with-id-only.
